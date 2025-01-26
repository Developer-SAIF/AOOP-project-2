package com.example.aoopproject.FileUpload;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FilePathHandler {
    private static final String SERVER_UPLOAD_DIR = "server_files";

    public static void ensureServerDirectoryExists() {
        try {
            Path serverDir = Paths.get(SERVER_UPLOAD_DIR);
            if (!Files.exists(serverDir)) {
                Files.createDirectories(serverDir);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Path resolveServerPath(String fileName) {
        return Paths.get(SERVER_UPLOAD_DIR, fileName).toAbsolutePath();
    }

    public static boolean verifyFileExists(String fileName) {
        Path filePath = resolveServerPath(fileName);
        return Files.exists(filePath);
    }

    public static void debugPrintFilePaths() {
        File serverDir = new File(SERVER_UPLOAD_DIR);
        System.out.println("Server Directory: " + serverDir.getAbsolutePath());

        if (serverDir.exists()) {
            System.out.println("Files in server directory:");
            File[] files = serverDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    System.out.println(" - " + file.getName() + " (Size: " + file.length() + " bytes)");
                }
            }
        } else {
            System.out.println("Server directory does not exist!");
        }
    }
}