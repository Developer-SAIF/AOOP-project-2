//package com.example.OnlineExamSystem.src.main.java.com.example.onlineexamsystem.Student;
//
//import com.example.onlineexamsystem.Instructor.Database.DatabaseConnection;
//import javafx.animation.KeyFrame;
//import javafx.animation.Timeline;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.geometry.Insets;
//import javafx.scene.chart.LineChart;
//import javafx.scene.chart.NumberAxis;
//import javafx.scene.chart.XYChart;
//import javafx.scene.control.*;
//import javafx.scene.layout.*;
//import javafx.util.Duration;
//import java.sql.*;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//
//public class StudentDashboard extends VBox {
//    private final int studentId;
//    private Label nextExamLabel;
//    private LineChart<Number, Number> performanceChart;
//    private ListView<String> upcomingExamsList;
//    private ListView<String> recentScoresList;
//
//    public StudentDashboard(int studentId) {
//        this.studentId = studentId;
//        setupDashboard();
//        startAutoRefresh();
//    }
//
//    private void setupDashboard() {
//        setPadding(new Insets(20));
//        setSpacing(20);
//
//        // Welcome Section
//        setupWelcomeSection();
//
//        // Next Exam Section
//        setupNextExamSection();
//
//        // Performance Chart
//        setupPerformanceChart();
//
//        // Upcoming Exams
//        setupUpcomingExamsSection();
//
//        // Recent Scores
//        setupRecentScoresSection();
//
//        // Initial data load
//        refreshDashboard();
//    }
//
//    private void setupWelcomeSection() {
//        String studentName = getStudentName();
//        Label welcomeLabel = new Label("Welcome, " + studentName);
//        welcomeLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
//        getChildren().add(welcomeLabel);
//    }
//
//    private void setupNextExamSection() {
//        nextExamLabel = new Label();
//        nextExamLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #2c3e50;");
//        getChildren().add(nextExamLabel);
//    }
//
//    private void setupPerformanceChart() {
//        NumberAxis xAxis = new NumberAxis();
//        NumberAxis yAxis = new NumberAxis(0, 100, 10);
//        performanceChart = new LineChart<>(xAxis, yAxis);
//        performanceChart.setTitle("Performance History");
//        performanceChart.setPrefHeight(300);
//        getChildren().add(performanceChart);
//    }
//
//    private void setupUpcomingExamsSection() {
//        Label upcomingTitle = new Label("Upcoming Exams");
//        upcomingTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
//
//        upcomingExamsList = new ListView<>();
//        upcomingExamsList.setPrefHeight(150);
//
//        getChildren().addAll(upcomingTitle, upcomingExamsList);
//    }
//
//    private void setupRecentScoresSection() {
//        Label scoresTitle = new Label("Recent Scores");
//        scoresTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
//
//        recentScoresList = new ListView<>();
//        recentScoresList.setPrefHeight(150);
//
//        getChildren().addAll(scoresTitle, recentScoresList);
//    }
//
//    private void startAutoRefresh() {
//        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(30), e -> refreshDashboard()));
//        timeline.setCycleCount(Timeline.INDEFINITE);
//        timeline.play();
//    }
//
//    private void refreshDashboard() {
//        updateNextExam();
//        updatePerformanceChart();
//        updateUpcomingExams();
//        updateRecentScores();
//    }
//
//    private String getStudentName() {
//        String query = "SELECT username FROM users WHERE userID = ? AND role = 'student'";
//        try (Connection conn = DatabaseConnection.getConnection();
//             PreparedStatement stmt = conn.prepareStatement(query)) {
//
//            stmt.setInt(1, studentId);
//            ResultSet rs = stmt.executeQuery();
//
//            if (rs.next()) {
//                return rs.getString("username");
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return "Student";
//    }
//
//    private void updateNextExam() {
//        String query = """
//            SELECT e.examName, e.examDate, s.subjectName
//            FROM exams e
//            JOIN subjects s ON e.subjectID = s.subjectID
//            WHERE e.examDate > NOW()
//            ORDER BY e.examDate ASC
//            LIMIT 1
//        """;
//
//        try (Connection conn = DatabaseConnection.getConnection();
//             PreparedStatement stmt = conn.prepareStatement(query)) {
//
//            ResultSet rs = stmt.executeQuery();
//            if (rs.next()) {
//                String examName = rs.getString("examName");
//                LocalDateTime examDate = rs.getTimestamp("examDate").toLocalDateTime();
//                String subject = rs.getString("subjectName");
//
//                String formattedDate = examDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
//                nextExamLabel.setText("Next Exam: " + examName + " (" + subject + ") on " + formattedDate);
//            } else {
//                nextExamLabel.setText("No upcoming exams scheduled");
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void updatePerformanceChart() {
//        String query = """
//            SELECT e.examName, r.score
//            FROM responses r
//            JOIN exams e ON r.examID = e.examID
//            WHERE r.userID = ?
//            ORDER BY e.examDate DESC
//            LIMIT 10
//        """;
//
//        XYChart.Series<Number, Number> series = new XYChart.Series<>();
//        series.setName("Exam Scores");
//
//        try (Connection conn = DatabaseConnection.getConnection();
//             PreparedStatement stmt = conn.prepareStatement(query)) {
//
//            stmt.setInt(1, studentId);
//            ResultSet rs = stmt.executeQuery();
//
//            int index = 0;
//            while (rs.next()) {
//                series.getData().add(new XYChart.Data<>(index++, rs.getInt("score")));
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//        performanceChart.getData().clear();
//        performanceChart.getData().add(series);
//    }
//
//    private void updateUpcomingExams() {
//        String query = """
//            SELECT e.examName, e.examDate, s.subjectName
//            FROM exams e
//            JOIN subjects s ON e.subjectID = s.subjectID
//            WHERE e.examDate > NOW()
//            ORDER BY e.examDate ASC
//            LIMIT 5
//        """;
//
//        ObservableList<String> upcomingExams = FXCollections.observableArrayList();
//
//        try (Connection conn = DatabaseConnection.getConnection();
//             PreparedStatement stmt = conn.prepareStatement(query)) {
//
//            ResultSet rs = stmt.executeQuery();
//            while (rs.next()) {
//                String examInfo = String.format("%s - %s (%s)",
//                        rs.getString("examName"),
//                        rs.getString("subjectName"),
//                        rs.getTimestamp("examDate").toLocalDateTime().format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"))
//                );
//                upcomingExams.add(examInfo);
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//        upcomingExamsList.setItems(upcomingExams);
//    }
//
//    private void updateRecentScores() {
//        String query = """
//            SELECT e.examName, r.score, s.subjectName
//            FROM responses r
//            JOIN exams e ON r.examID = e.examID
//            JOIN subjects s ON e.subjectID = s.subjectID
//            WHERE r.userID = ?
//            ORDER BY e.examDate DESC
//            LIMIT 5
//        """;
//
//        ObservableList<String> recentScores = FXCollections.observableArrayList();
//
//        try (Connection conn = DatabaseConnection.getConnection();
//             PreparedStatement stmt = conn.prepareStatement(query)) {
//
//            stmt.setInt(1, studentId);
//            ResultSet rs = stmt.executeQuery();
//
//            while (rs.next()) {
//                String scoreInfo = String.format("%s - %s: %d%%",
//                        rs.getString("examName"),
//                        rs.getString("subjectName"),
//                        rs.getInt("score")
//                );
//                recentScores.add(scoreInfo);
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//        recentScoresList.setItems(recentScores);
//    }
//}