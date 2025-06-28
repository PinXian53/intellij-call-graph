package com.pino.intellijcallgraph.utils;

import com.intellij.openapi.vfs.VirtualFile;

import java.util.ArrayList;
import java.util.List;

public class JavaUtils {

    private JavaUtils() {
    }

    public static List<VirtualFile> collectJavaFiles(VirtualFile[] files) {
        var javaFiles = new ArrayList<VirtualFile>();
        for (VirtualFile file : files) {
            javaFiles.addAll(collectJavaFiles(file));
        }
        return javaFiles;
    }

    public static List<VirtualFile> collectJavaFiles(VirtualFile file) {
        var javaFiles = new ArrayList<VirtualFile>();
        if (file.isDirectory()) {
            for (var child : file.getChildren()) {
                javaFiles.addAll(collectJavaFiles(child));
            }
        } else if (isJavaFile(file)) {
            javaFiles.add(file);
        }
        return javaFiles;
    }

    public static boolean isJavaFile(VirtualFile file) {
        return "java".equalsIgnoreCase(file.getExtension());
    }
}
