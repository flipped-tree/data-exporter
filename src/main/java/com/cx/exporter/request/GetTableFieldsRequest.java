package com.cx.exporter.request;

import java.util.List;

public class GetTableFieldsRequest {
    private String jdbcUrl;
    private String userName;
    private String password;
    private List<String> tableNames;

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public List<String> getTableNames() {
        return tableNames;
    }
}
