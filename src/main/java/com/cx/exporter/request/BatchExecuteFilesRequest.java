package com.cx.exporter.request;

import lombok.Getter;

import java.util.List;

@Getter
public class BatchExecuteFilesRequest {
    private String jdbcUrl;
    private String userName;
    private String password;
    private List<String> filePaths;
}
