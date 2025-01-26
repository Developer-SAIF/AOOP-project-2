package com.example.aoopproject.FileUpload;


import java.time.LocalDateTime;

public class FileDetails {
    private final String name;
    private final String path; // Path to the file
    private final String size;
    private final LocalDateTime uploadTime;

    public FileDetails(String name, String path, String size, LocalDateTime uploadTime) {
        this.name = name;
        this.path = path;
        this.size = size;
        this.uploadTime = uploadTime;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getSize() {
        return size;
    }

    public LocalDateTime getUploadTime() {
        return uploadTime;
    }
}

