package com.cx.exporter;

import com.cx.exporter.constant.SqlConstants;
import com.cx.exporter.exception.ExecuteException;
import com.cx.exporter.utils.BooleanUtils;
import com.cx.exporter.utils.CollUtils;
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
import java.util.function.Function;

@Slf4j
public class DatabaseCenter {

    private final DataSource dataSource;

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
        if (CollUtils.isEmpty(filePaths)) {
            return false;
        }

        List<CompletableFuture<Boolean>> futures = executeBatchAsync(filePaths, this::executeBatchAsync);

        return futures.stream().allMatch(future -> {
            try {
                return BooleanUtils.isTrue(future.get());
            } catch (InterruptedException | ExecutionException e) {
                log.error(e.getMessage(), e);
                Thread.currentThread().interrupt();
            }
            return false;
        });
    }

    private <I, O> List<CompletableFuture<O>> executeBatchAsync(List<I> inputList,
                                                                Function<I, CompletableFuture<O>> function) {
        List<CompletableFuture<O>> futures = inputList.stream().map(function).toList();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return futures;
    }

    private CompletableFuture<Boolean> executeBatchAsync(String filePath) {
        return CompletableFuture.supplyAsync(() -> {
            List<String> lines = FileUtils.readLines(filePath);
            if (CollUtils.isEmpty(lines)) {
                return false;
            }
            String sql = SqlUtils.generateMultiInsertSql(lines);
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement()) {
                return statement.execute(sql);
            } catch (SQLException e) {
                throw new ExecuteException(e);
            }
        });
    }


}
