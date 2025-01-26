package com.example.aoopproject.FileUpload;


import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler {
    private static final String DATABASE_URL = "jdbc:sqlite:noteDB.db";

    static {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             Statement stmt = conn.createStatement()) {

            String createTableSQL = """
                CREATE TABLE IF NOT EXISTS files (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    path TEXT NOT NULL,
                    size TEXT NOT NULL,
                    upload_time TEXT NOT NULL
                )
            """;

            stmt.execute(createTableSQL);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<FileDetails> getFiles() {
        List<FileDetails> files = new ArrayList<>();
        String query = "SELECT * FROM files";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String name = rs.getString("name");
                String path = rs.getString("path");
                String size = rs.getString("size");
                LocalDateTime uploadTime = LocalDateTime.parse(rs.getString("upload_time"));

                files.add(new FileDetails(name, path, size, uploadTime));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return files;
    }

    public static void addFile(FileDetails file) {
        String insertSQL = """
            INSERT INTO files (name, path, size, upload_time)
            VALUES (?, ?, ?, ?)
        """;

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            pstmt.setString(1, file.getName());
            pstmt.setString(2, file.getPath());
            pstmt.setString(3, file.getSize());
            pstmt.setString(4, file.getUploadTime().toString());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

