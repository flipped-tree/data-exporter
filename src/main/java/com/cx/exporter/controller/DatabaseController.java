package com.cx.exporter.controller;

import com.cx.exporter.DataExporter;
import com.cx.exporter.DatabaseCenter;
import com.cx.exporter.request.ShowTableInfoRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/database")
public class DatabaseController {

    @PostMapping("/exportCreateTable")
    public ResponseEntity<String> exportCreateTable(@Validated @RequestBody ShowTableInfoRequest request) throws SQLException, IOException {
        List<String> dataList = new DatabaseCenter(request.getJdbcUrl(), request.getUserName(), request.getPassword()).getCreateTableList(request.getTableNames());
        String filePath = DataExporter.export2File(dataList);
        return ResponseEntity.ok(filePath);
    }
}
