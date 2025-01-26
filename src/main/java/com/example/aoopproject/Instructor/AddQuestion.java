package com.example.aoopproject.Instructor;

import com.example.aoopproject.database.DatabaseConnection;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;

public class AddQuestion extends Application {

    private Stage window;
    private Scene subjectSelectionScene, questionFormScene, successScene;

    @Override
    public void start(Stage primaryStage) {
        window = primaryStage;

        // Subject Selection Scene
        GridPane subjectGrid = new GridPane();
        subjectGrid.setPadding(new Insets(10, 10, 10, 10));
        subjectGrid.setVgap(8);
        subjectGrid.setHgap(10);

        Label subjectLabel = new Label("Select Subject:");
        GridPane.setConstraints(subjectLabel, 0, 0);
        ComboBox<String> subjectComboBox = new ComboBox<>();
        GridPane.setConstraints(subjectComboBox, 1, 0);
        ObservableList<String> subjects = getSubjectsFromDatabase();
        subjectComboBox.setItems(subjects);

        Button nextButton = new Button("Next");
        GridPane.setConstraints(nextButton, 1, 1);
        subjectGrid.getChildren().addAll(subjectLabel, subjectComboBox, nextButton);

        subjectSelectionScene = new Scene(subjectGrid, 300, 200);

        // Question Form Scene
        GridPane questionGrid = new GridPane();
        questionGrid.setPadding(new Insets(10, 10, 10, 10));
        questionGrid.setVgap(8);
        questionGrid.setHgap(10);

        Label questionLabel = new Label("Question:");
        GridPane.setConstraints(questionLabel, 0, 0);
        TextField questionInput = new TextField();
        GridPane.setConstraints(questionInput, 1, 0);

        Label option1Label = new Label("Option 1:");
        GridPane.setConstraints(option1Label, 0, 1);
        TextField option1Input = new TextField();
        GridPane.setConstraints(option1Input, 1, 1);

        Label option2Label = new Label("Option 2:");
        GridPane.setConstraints(option2Label, 0, 2);
        TextField option2Input = new TextField();
        GridPane.setConstraints(option2Input, 1, 2);

        Label option3Label = new Label("Option 3:");
        GridPane.setConstraints(option3Label, 0, 3);
        TextField option3Input = new TextField();
        GridPane.setConstraints(option3Input, 1, 3);

        Label option4Label = new Label("Option 4:");
        GridPane.setConstraints(option4Label, 0, 4);
        TextField option4Input = new TextField();
        GridPane.setConstraints(option4Input, 1, 4);

        Label correctOptionLabel = new Label("Correct Option (1-4):");
        GridPane.setConstraints(correctOptionLabel, 0, 5);
        TextField correctOptionInput = new TextField();
        GridPane.setConstraints(correctOptionInput, 1, 5);

        Button addButton = new Button("Add Question");
        GridPane.setConstraints(addButton, 1, 6);

        questionGrid.getChildren().addAll(questionLabel, questionInput, option1Label, option1Input, option2Label, option2Input,
                option3Label, option3Input, option4Label, option4Input, correctOptionLabel, correctOptionInput, addButton);

        questionFormScene = new Scene(questionGrid, 400, 400);

        // Success Scene
        VBox successLayout = new VBox(10);
        successLayout.setPadding(new Insets(20, 20, 20, 20));
        Label successLabel = new Label("Question added successfully!");
        Button addAnotherButton = new Button("Add Another Question");
        successLayout.getChildren().addAll(successLabel, addAnotherButton);

        successScene = new Scene(successLayout, 300, 200);

        nextButton.setOnAction(e -> {
            String selectedSubject = subjectComboBox.getValue();
            if (selectedSubject != null) {
                window.setScene(questionFormScene);
                addButton.setOnAction(event -> {
                    String question = questionInput.getText();
                    String option1 = option1Input.getText();
                    String option2 = option2Input.getText();
                    String option3 = option3Input.getText();
                    String option4 = option4Input.getText();
                    int correctOption = Integer.parseInt(correctOptionInput.getText());
                    int subjectID = getSubjectID(selectedSubject);
                    addQuestionToDatabase(question, option1, option2, option3, option4, correctOption, subjectID);

                    // Show success message
                    window.setScene(successScene);
                });

                addAnotherButton.setOnAction(event -> {
                    questionInput.clear();
                    option1Input.clear();
                    option2Input.clear();
                    option3Input.clear();
                    option4Input.clear();
                    correctOptionInput.clear();
                    window.setScene(questionFormScene);
                });
            }
        });

        window.setScene(subjectSelectionScene);
        window.setTitle("Online Exam System");
        window.show();
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

    private void addQuestionToDatabase(String question, String option1, String option2, String option3, String option4, int correctOption, int subjectID) {
        String insertSQL = "INSERT INTO examquestions (questionText, option1, option2, option3, option4, correctOption, subjectID) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {

            preparedStatement.setString(1, question);
            preparedStatement.setString(2, option1);
            preparedStatement.setString(3, option2);
            preparedStatement.setString(4, option3);
            preparedStatement.setString(5, option4);
            preparedStatement.setInt(6, correctOption);
            preparedStatement.setInt(7, subjectID);

            preparedStatement.executeUpdate();
            System.out.println("Question added successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
