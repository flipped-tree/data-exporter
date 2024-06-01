package com.cx.exporter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class DataExporter {
    private DataExporter() {
    }

    public static String export2File(List<String> dataList) throws IOException {
        if (CollUtil.isEmpty(dataList)) {
            return CharSequenceUtil.EMPTY;
        }
        String projectRootDir = System.getProperty("user.dir");
        String tempDirPath = projectRootDir + "/temp";
        String fileName = "output.sql";
        Path filePath = Paths.get(tempDirPath, fileName);
        Files.deleteIfExists(filePath);
        Files.createDirectories(filePath.getParent());
        Files.createFile(filePath);
        Files.write(filePath, dataList, StandardCharsets.UTF_8);
        return filePath.toString();
    }
}
