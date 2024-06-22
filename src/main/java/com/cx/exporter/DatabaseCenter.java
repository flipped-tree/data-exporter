package com.cx.exporter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import com.cx.exporter.constant.SqlConstants;
import com.cx.exporter.utils.FileUtils;
import com.cx.exporter.utils.SqlUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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
            List<String> lines = FileUtils.readLines(filePath);
            if (CollUtil.isEmpty(lines)) {
                return false;
            }
            String sql = SqlUtils.generateMultiInsertSql(lines);
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement()) {
                return statement.execute(sql);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }


}
