package com.cx.exporter;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseCenter {

    DataSource dataSource;

    public DatabaseCenter(String jdbcUrl, String userName, String password) {
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
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            List<String> createTableSqlList = new ArrayList<>();
            for (String tableName : tableNames) {
                String sql = "show create table " + tableName;
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
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            Map<String, List<String>> tableFieldsMap = new HashMap<>();
            for (String tableName : tableNames) {
                String showColumnSql = "show columns from " + tableName;
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
}
