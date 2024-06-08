package com.cx.exporter.request;

import lombok.Getter;

import java.util.List;

@Getter
public class ShowTableInfoRequest {
    private String jdbcUrl;
    private String userName;
    private String password;
    private List<String> tableNames;
}
