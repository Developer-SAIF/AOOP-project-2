package com.example.aoopproject.Instructor;

import com.example.aoopproject.database.DatabaseConnection;
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

public class ScheduleExam extends Application {

    private Stage window;
    private Scene scheduleFormScene, successScene;

    @Override
    public void start(Stage primaryStage) {
        window = primaryStage;

        window.setScene(new Scene(getView(), 400, 300));
        window.setTitle("Schedule Exam");
        window.show();
    }

    public VBox getView() {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(10, 10, 10, 10));

        GridPane scheduleGrid = new GridPane();
        scheduleGrid.setPadding(new Insets(10, 10, 10, 10));
        scheduleGrid.setVgap(8);
        scheduleGrid.setHgap(10);

        Label subjectLabel = new Label("Select Subject:");
        GridPane.setConstraints(subjectLabel, 0, 0);
        ComboBox<String> subjectComboBox = new ComboBox<>();
        GridPane.setConstraints(subjectComboBox, 1, 0);
        ObservableList<String> subjects = getSubjectsFromDatabase();
        subjectComboBox.setItems(subjects);

        Label dateLabel = new Label("Exam Date:");
        GridPane.setConstraints(dateLabel, 0, 1);
        DatePicker datePicker = new DatePicker();
        GridPane.setConstraints(datePicker, 1, 1);

        Label timeLabel = new Label("Exam Time:");
        GridPane.setConstraints(timeLabel, 0, 2);
        ComboBox<Integer> hourComboBox = new ComboBox<>();
        ObservableList<Integer> hours = FXCollections.observableArrayList();
        for (int i = 0; i < 24; i++) {
            hours.add(i);
        }
        hourComboBox.setItems(hours);
        GridPane.setConstraints(hourComboBox, 1, 2);

        ComboBox<Integer> minuteComboBox = new ComboBox<>();
        ObservableList<Integer> minutes = FXCollections.observableArrayList();
        for (int i = 0; i < 60; i += 5) {
            minutes.add(i);
        }
        minuteComboBox.setItems(minutes);
        GridPane.setConstraints(minuteComboBox, 2, 2);

        Button scheduleButton = new Button("Schedule Exam");
        GridPane.setConstraints(scheduleButton, 1, 3);

        scheduleButton.setOnAction(e -> {
            String selectedSubject = subjectComboBox.getValue();
            LocalDate selectedDate = datePicker.getValue();
            Integer selectedHour = hourComboBox.getValue();
            Integer selectedMinute = minuteComboBox.getValue();
            if (selectedSubject != null && selectedDate != null && selectedHour != null && selectedMinute != null) {
                LocalTime time = LocalTime.of(selectedHour, selectedMinute);
                LocalDateTime examDateTime = LocalDateTime.of(selectedDate, time);
                int subjectID = getSubjectID(selectedSubject);
                scheduleExamInDatabase(subjectID, examDateTime);

                // Clear the input fields
                datePicker.setValue(null);
                hourComboBox.setValue(null);
                minuteComboBox.setValue(null);
                subjectComboBox.setValue(null);

                // Show success message
                new Alert(Alert.AlertType.INFORMATION, "Exam scheduled successfully!").showAndWait();
            }
        });

        scheduleGrid.getChildren().addAll(subjectLabel, subjectComboBox, dateLabel, datePicker, timeLabel, hourComboBox, minuteComboBox, scheduleButton);
        layout.getChildren().add(scheduleGrid);

        return layout;
    }

    private ObservableList<String> getSubjectsFromDatabase() {
        ObservableList<String> subjects = FXCollections.observableArrayList();
        String query = "SELECT subjectName FROM subjects";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                subjects.add(resultSet.getString("subjectName"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return subjects;
    }

    private int getSubjectID(String subjectName) {
        String query = "SELECT subjectID FROM subjects WHERE LOWER(subjectName) = LOWER(?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, subjectName);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("subjectID");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Subject not found
    }

    private void scheduleExamInDatabase(int subjectID, LocalDateTime examDate) {
        String insertSQL = "INSERT INTO examschedules (subjectID, examDate) VALUES (?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {

            preparedStatement.setInt(1, subjectID);
            preparedStatement.setTimestamp(2, Timestamp.valueOf(examDate));
            //preparedStatement.setInt(3,getCurrentInstructorID()); // Use the current instructor ID

            preparedStatement.executeUpdate();
            System.out.println("Exam scheduled successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

//    private int getCurrentInstructorID() {
//        // Simulate fetching the current instructor's ID after login
//        // In a real application, this would be replaced by actual authentication logic
//        return 0; // Example instructor ID
//    }

    public static void main(String[] args) {
        launch(args);
    }
}
