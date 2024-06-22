package com.cx.exporter.utils;

import java.io.IOException;
import java.nio.file.Files;
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
            throw new RuntimeException(e);
        }
    }
}
