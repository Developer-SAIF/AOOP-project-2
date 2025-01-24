package com.example.aoopproject.models;

import com.example.aoopproject.database.DatabaseConnection;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.sql.*;
import java.util.*;
import java.time.LocalDateTime;
import java.awt.Desktop;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

public class SharedFile {
    private String fileId;
    private String fileName;
    private String fileUrl;
    private String uploaderId;
    private LocalDateTime uploadDateTime;

    public SharedFile(String fileId, String fileName, String fileUrl,
                      String uploaderId, LocalDateTime uploadDateTime) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.uploaderId = uploaderId;
        this.uploadDateTime = uploadDateTime;
    }

    // Getters
    public String getFileId() { return fileId; }
    public String getFileName() { return fileName; }
    public String getFileUrl() { return fileUrl; }
    public String getUploaderId() { return uploaderId; }
    public LocalDateTime getUploadDateTime() { return uploadDateTime; }

    // Open URL in default browser
    public void openInBrowser() {
        CompletableFuture.runAsync(() -> {
            try {
                Desktop.getDesktop().browse(new URI(fileUrl));
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Could not open the link");
                    alert.setContentText("Failed to open the URL in browser. Please try again.");
                    alert.showAndWait();
                });
            }
        });
    }

    // Database operations
    public static List<SharedFile> getAllFiles() {
        List<SharedFile> files = new ArrayList<>();
        String sql = "SELECT * FROM SharedFiles";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                SharedFile file = new SharedFile(
                        rs.getString("FileID"),
                        rs.getString("FileName"),
                        rs.getString("FileURL"),
                        rs.getString("UploaderID"),
                        rs.getTimestamp("UploadDateTime").toLocalDateTime()
                );
                files.add(file);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return files;
    }

    public boolean uploadFile() {
        String sql = "INSERT INTO SharedFiles (FileID, FileName, FileURL, UploaderID, UploadDateTime) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, fileId);
            pstmt.setString(2, fileName);
            pstmt.setString(3, fileUrl);
            pstmt.setString(4, uploaderId);
            pstmt.setTimestamp(5, Timestamp.valueOf(uploadDateTime));

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteFile(String userId) {
        // Only allow uploader to delete their own links
        if (!uploaderId.equals(userId)) {
            return false;
        }

        String sql = "DELETE FROM SharedFiles WHERE FileID = ? AND UploaderID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, fileId);
            pstmt.setString(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}