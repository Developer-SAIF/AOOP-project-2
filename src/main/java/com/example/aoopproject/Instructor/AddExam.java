package com.example.aoopproject.Instructor;

import com.example.aoopproject.database.DatabaseConnection;
import com.example.aoopproject.models.ExamManager;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class AddExam extends Application {
    private Stage window;
    private Scene examFormScene, successScene;
    private TextField examNameField;
    private ComboBox<String> subjectComboBox;
    private DatePicker examDatePicker;
    private TextField durationField;

    @Override
    public void start(Stage primaryStage) {
        window = primaryStage;

        // Exam Form Scene
        GridPane examGrid = new GridPane();
        examGrid.setPadding(new Insets(10, 10, 10, 10));
        examGrid.setVgap(8);
        examGrid.setHgap(10);

        // Subject Selection
        Label subjectLabel = new Label("Select Subject:");
        GridPane.setConstraints(subjectLabel, 0, 0);
        subjectComboBox = new ComboBox<>();
        GridPane.setConstraints(subjectComboBox, 1, 0);
        ObservableList<String> subjects = getSubjectsFromDatabase();
        subjectComboBox.setItems(subjects);

        // Exam Name
        Label examNameLabel = new Label("Exam Name:");
        GridPane.setConstraints(examNameLabel, 0, 1);
        examNameField = new TextField();
        GridPane.setConstraints(examNameField, 1, 1);

        // Exam Date
        Label dateLabel = new Label("Exam Date:");
        GridPane.setConstraints(dateLabel, 0, 2);
        examDatePicker = new DatePicker(LocalDate.now());
        GridPane.setConstraints(examDatePicker, 1, 2);

        // Duration
        Label durationLabel = new Label("Duration (minutes):");
        GridPane.setConstraints(durationLabel, 0, 3);
        durationField = new TextField();
        GridPane.setConstraints(durationField, 1, 3);

        // Add Button
        Button addButton = new Button("Add Exam");
        styleButton(addButton);
        GridPane.setConstraints(addButton, 1, 4);

        examGrid.getChildren().addAll(
                subjectLabel, subjectComboBox,
                examNameLabel, examNameField,
                dateLabel, examDatePicker,
                durationLabel, durationField,
                addButton
        );

        examFormScene = new Scene(examGrid, 400, 300);

        // Success Scene
        VBox successLayout = new VBox(10);
        successLayout.setPadding(new Insets(20));
        Label successLabel = new Label("Exam added successfully!");
        Button addAnotherButton = new Button("Add Another Exam");
        styleButton(addAnotherButton);
        successLayout.getChildren().addAll(successLabel, addAnotherButton);

        successScene = new Scene(successLayout, 300, 200);

        // Button Actions
        addButton.setOnAction(e -> handleAddExam());
        addAnotherButton.setOnAction(e -> {
            clearForm();
            window.setScene(examFormScene);
        });

        window.setScene(examFormScene);
        window.setTitle("Add New Exam");
        window.show();
    }

    private void handleAddExam() {
        if (!validateInputs()) return;

        try {
            String examName = examNameField.getText().trim();
            String selectedSubject = subjectComboBox.getValue();
            LocalDate examDate = examDatePicker.getValue();
            int duration = Integer.parseInt(durationField.getText().trim());
            int subjectId = getSubjectID(selectedSubject);

            if (addExamToDatabase(examName, subjectId, examDate, duration)) {
                window.setScene(successScene);
            } else {
                showError("Failed to add exam");
            }
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
        }
    }

    private boolean addExamToDatabase(String examName, int subjectId, LocalDate examDate, int duration) {
        String sql = "INSERT INTO exams (examName, subjectID, examDate, duration, instructorID) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, examName);
            pstmt.setInt(2, subjectId);
            pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.of(examDate, LocalTime.of(9, 0))));
            pstmt.setInt(4, duration);
            pstmt.setInt(5, 1); // Default instructorID, modify as needed

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private ObservableList<String> getSubjectsFromDatabase() {
        ObservableList<String> subjects = FXCollections.observableArrayList();
        String query = "SELECT subjectName FROM subjects";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                subjects.add(rs.getString("subjectName"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return subjects;
    }

    private int getSubjectID(String subjectName) {
        String query = "SELECT subjectID FROM subjects WHERE LOWER(subjectName) = LOWER(?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, subjectName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("subjectID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private boolean validateInputs() {
        if (examNameField.getText().trim().isEmpty()) {
            showError("Please enter exam name");
            return false;
        }
        if (subjectComboBox.getValue() == null) {
            showError("Please select a subject");
            return false;
        }
        if (examDatePicker.getValue() == null) {
            showError("Please select exam date");
            return false;
        }
        try {
            int duration = Integer.parseInt(durationField.getText().trim());
            if (duration <= 0) {
                showError("Duration must be positive");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Please enter valid duration");
            return false;
        }
        return true;
    }

    private void styleButton(Button button) {
        button.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        button.setOnMouseEntered(e ->
                button.setStyle("-fx-background-color: #45a049; -fx-text-fill: white;"));
        button.setOnMouseExited(e ->
                button.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;"));
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearForm() {
        examNameField.clear();
        subjectComboBox.setValue(null);
        examDatePicker.setValue(LocalDate.now());
        durationField.clear();
    }

    public static void main(String[] args) {
        launch(args);
    }
}