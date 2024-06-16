package com.cx.exporter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import com.cx.exporter.constant.SqlConstants;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

@Slf4j
public class DatabaseCenter {


    DataSource dataSource;

    public DatabaseCenter(String jdbcUrl,
                          String userName,
                          String password) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(userName);
        hikariConfig.setPassword(password);
        hikariConfig.addDataSourceProperty("useInformationSchema", Boolean.TRUE.toString());
        hikariConfig.setMinimumIdle(2);
        hikariConfig.setMaximumPoolSize(5);
        this.dataSource = new HikariDataSource(hikariConfig);
    }

    public List<String> getCreateTableList(List<String> tableNames) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            List<String> createTableSqlList = new ArrayList<>();
            for (String tableName : tableNames) {
                String sql = SqlConstants.SHOW_CREATE_TABLE + tableName;
                try (ResultSet resultSet = statement.executeQuery(sql)) {
                    if (resultSet.next()) {
                        String createTableSql = resultSet.getString(2) + ";\n";
                        createTableSqlList.add(createTableSql);
                    }
                }
            }
            return createTableSqlList;
        }
    }

    public Map<String, List<String>> getTableFields(List<String> tableNames) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            Map<String, List<String>> tableFieldsMap = new HashMap<>();
            for (String tableName : tableNames) {
                String showColumnSql = SqlConstants.SHOW_COLUMNS_FROM + tableName;
                try (ResultSet resultSet = statement.executeQuery(showColumnSql)) {
                    List<String> fieldList = new ArrayList<>();
                    while (resultSet.next()) {
                        fieldList.add(resultSet.getString("Field"));
                    }
                    tableFieldsMap.put(tableName, fieldList);
                }
            }
            return tableFieldsMap;
        }
    }

    public boolean batchExecutePathFiles(List<String> filePaths) {
        if (CollUtil.isEmpty(filePaths)) {
            return false;
        }

        List<CompletableFuture<Boolean>> futures = new ArrayList<>();

        // 为每个文件创建一个CompletableFuture
        for (String filePath : filePaths) {
            CompletableFuture<Boolean> future = executeSqlFileAsync(filePath);
            futures.add(future);
        }
        // 等待所有任务完成，但请注意allOf不提供结果
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        return futures.stream().allMatch(future -> {
            try {
                return BooleanUtil.isTrue(future.get());
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private CompletableFuture<Boolean> executeSqlFileAsync(String filePath) {
        return CompletableFuture.supplyAsync(() -> {
            List<String> lines = readLines(filePath);
            if (CollUtil.isEmpty(lines)) {
                return false;
            }
            String sql = generateMultiInsertSql(lines);
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement()) {
                return statement.execute(sql);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private String generateMultiInsertSql(List<String> lines) {
        boolean isFirstLine = true;
        StringBuilder mergedSql = new StringBuilder();
        for (String line : lines) {
            // 假设每行都是一个完整的insert语句（不包括分号结尾）
            // 如果需要处理包含分号的行，可以添加逻辑来忽略它或进行相应处理
            if (line.trim().startsWith("insert into user")) {
                // 移除分号（如果存在的话）
                if (line.endsWith(";")) {
                    line = line.substring(0, line.length() - 1);
                }
                // 如果是第一行，则直接添加，否则添加逗号
                if (isFirstLine) {
                    isFirstLine = false;
                    mergedSql.append(line);
                } else {
                    // 添加括号并逗号分隔
                    mergedSql.append(",(").append(
                            line.substring(line.indexOf("values (") + "values (".length())).append(")");
                }
            }
        }
        // 添加初始的 INSERT 语句部分（如果需要的话）
        if (!mergedSql.toString().startsWith("insert into user")) {
            // 这里假设第一行已经包含了完整的INSERT语句（包括列名和第一个值对）
            // 因此我们只需要从第二行开始合并
            mergedSql.insert(0, "insert into user (id,name) values ");
        }
        // 添加分号（如果需要的话）
        if (!mergedSql.toString().endsWith(";")) {
            mergedSql.append(";");
        }
        return mergedSql.toString();
    }

    private List<String> readLines(String filePath) {
        try (Stream<String> lineStream = Files.lines(Paths.get(filePath))) {
            return lineStream.toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
