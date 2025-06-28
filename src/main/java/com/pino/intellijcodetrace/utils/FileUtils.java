package com.pino.intellijcodetrace.utils;

import lombok.SneakyThrows;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class FileUtils {

    private FileUtils() {
    }

    @SneakyThrows
    public static void write(Path outputFilePath, String content) {
        if (content.isBlank()) {
            return;
        }
        try (var osw = new OutputStreamWriter(new FileOutputStream(outputFilePath.toFile(), true), StandardCharsets.UTF_8);
             var bw = new BufferedWriter(osw);
             var pw = new PrintWriter(bw)) {
            pw.print(content);
        }
    }
}
