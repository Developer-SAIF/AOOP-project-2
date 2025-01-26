package com.example.aoopproject.models;

import com.example.aoopproject.database.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDateTime;

public class ExamManager {
    private static ExamManager instance;

    public static class ExamInfo {
        private final int examId;
        private final String examName;
        private final String subjectName;
        private final LocalDateTime examDate;
        private final int duration;

        public ExamInfo(int examId, String examName, String subjectName,
                        LocalDateTime examDate, int duration) {
            this.examId = examId;
            this.examName = examName;
            this.subjectName = subjectName;
            this.examDate = examDate;
            this.duration = duration;
        }

        // Getters
        public int getExamId() { return examId; }
        public String getExamName() { return examName; }
        public String getSubjectName() { return subjectName; }
        public LocalDateTime getExamDate() { return examDate; }
        public int getDuration() { return duration; }
    }

    private ExamManager() {}

    public static ExamManager getInstance() {
        if (instance == null) {
            instance = new ExamManager();
        }
        return instance;
    }

    public ObservableList<ExamInfo> getUpcomingExams() {
        ObservableList<ExamInfo> exams = FXCollections.observableArrayList();
        String query = """
            SELECT e.examID, e.examName, s.subjectName, e.examDate, e.duration
            FROM exams e
            JOIN subjects s ON e.subjectID = s.subjectID
            WHERE e.examDate > NOW()
            ORDER BY e.examDate ASC
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ExamInfo exam = new ExamInfo(
                        rs.getInt("examID"),
                        rs.getString("examName"),
                        rs.getString("subjectName"),
                        rs.getTimestamp("examDate").toLocalDateTime(),
                        rs.getInt("duration")
                );
                exams.add(exam);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return exams;
    }

    public boolean isExamAvailable(int examId) {
        String query = """
            SELECT 1 FROM exams 
            WHERE examID = ? 
            AND examDate <= NOW() 
            AND DATE_ADD(examDate, INTERVAL duration MINUTE) >= NOW()
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, examId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}