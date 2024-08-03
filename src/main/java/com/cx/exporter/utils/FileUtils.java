package com.cx.exporter.utils;

import com.cx.exporter.exception.ExecuteException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class FileUtils {
    private FileUtils() {
    }

    public static List<String> readLines(String filePath) {
        try (Stream<String> lineStream = Files.lines(Paths.get(filePath))) {
            return lineStream.toList();
        } catch (IOException e) {
            throw new ExecuteException(e);
        }
    }

    public static String export2File(List<String> dataList) throws IOException {
        if (CollUtils.isEmpty(dataList)) {
            return "";
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
