package com.cx.exporter;

import cn.hutool.json.JSONUtil;


import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Slf4j
public class Executor {
    public static void main(String[] args) throws SQLException {
        DatabaseCenter dataCenter = new DatabaseCenter("", "", "");
        Map<String, List<String>> tableFields = dataCenter.getTableFields(List.of());
        System.out.println(JSONUtil.toJsonStr(tableFields));
    }
}
