package com.cx.exporter.utils;

import java.util.List;

public class SqlUtils {
    private SqlUtils(){}

    public static String generateMultiInsertSql(List<String> lines) {
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
}
