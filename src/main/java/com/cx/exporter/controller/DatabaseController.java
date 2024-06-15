package com.cx.exporter.controller;

import com.cx.exporter.DataExporter;
import com.cx.exporter.DatabaseCenter;
import com.cx.exporter.annotation.EnableLog;
import com.cx.exporter.request.BatchExecuteFilesRequest;
import com.cx.exporter.request.GetTableFieldsRequest;
import com.cx.exporter.request.ShowTableInfoRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/database")
public class DatabaseController {

    @EnableLog
    @PostMapping("/exportCreateTable")
    public ResponseEntity<String> exportCreateTable(@Validated @RequestBody ShowTableInfoRequest request) throws SQLException, IOException {
        List<String> dataList = new DatabaseCenter(request.getJdbcUrl(), request.getUserName(),
                request.getPassword()).getCreateTableList(request.getTableNames());
        String filePath = DataExporter.export2File(dataList);
        return ResponseEntity.ok(filePath);
    }

    @EnableLog
    @PostMapping("/getTableFields")
    public ResponseEntity<Map<String, List<String>>> getTableFields(@Validated @RequestBody GetTableFieldsRequest request) throws SQLException {
        Map<String, List<String>> tableFields = new DatabaseCenter(request.getJdbcUrl(), request.getUserName(),
                request.getPassword()).getTableFields(request.getTableNames());
        return ResponseEntity.ok(tableFields);
    }

    @EnableLog
    @PostMapping("/batchExecuteFiles")
    public ResponseEntity<Boolean> batchExecuteFiles(@Validated @RequestBody BatchExecuteFilesRequest request) {
         new DatabaseCenter(request.getJdbcUrl(), request.getUserName(),
                request.getPassword()).batchExecuteFiles(request.getFilePaths());
        return ResponseEntity.ok(true);
    }
}
