package com.example.aoopproject.Instructor;

import com.example.aoopproject.database.LocalDatabaseConnection;
import com.example.onlineexamsystem.Instructor.Model.Question;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ViewQuestions extends Application {

    private TableView<Question> table;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("View Questions");

        TableColumn<Question, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setMinWidth(50);
        idColumn.setCellValueFactory(new PropertyValueFactory<>("questionID"));

        TableColumn<Question, String> questionColumn = new TableColumn<>("Question");
        questionColumn.setMinWidth(200);
        questionColumn.setCellValueFactory(new PropertyValueFactory<>("questionText"));

        TableColumn<Question, String> subjectColumn = new TableColumn<>("Subject");
        subjectColumn.setMinWidth(100);
        subjectColumn.setCellValueFactory(new PropertyValueFactory<>("subjectName"));

        TableColumn<Question, String> option1Column = new TableColumn<>("Option 1");
        option1Column.setMinWidth(100);
        option1Column.setCellValueFactory(new PropertyValueFactory<>("option1"));

        TableColumn<Question, String> option2Column = new TableColumn<>("Option 2");
        option2Column.setMinWidth(100);
        option2Column.setCellValueFactory(new PropertyValueFactory<>("option2"));

        TableColumn<Question, String> option3Column = new TableColumn<>("Option 3");
        option3Column.setMinWidth(100);
        option3Column.setCellValueFactory(new PropertyValueFactory<>("option3"));

        TableColumn<Question, String> option4Column = new TableColumn<>("Option 4");
        option4Column.setMinWidth(100);
        option4Column.setCellValueFactory(new PropertyValueFactory<>("option4"));

        TableColumn<Question, Integer> correctOptionColumn = new TableColumn<>("Correct Option");
        correctOptionColumn.setMinWidth(100);
        correctOptionColumn.setCellValueFactory(new PropertyValueFactory<>("correctOption"));

        table = new TableView<>();
        table.setItems(getQuestions());
        table.getColumns().addAll(idColumn, questionColumn, subjectColumn, option1Column, option2Column, option3Column, option4Column, correctOptionColumn);

        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> deleteQuestion());

        Button editButton = new Button("Edit");
        editButton.setOnAction(e -> showEditWindow());

        VBox vBox = new VBox();
        vBox.setPadding(new Insets(10));
        vBox.getChildren().addAll(table, deleteButton, editButton);

        Scene scene = new Scene(vBox);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public ObservableList<Question> getQuestions() {
        ObservableList<Question> questions = FXCollections.observableArrayList();
        String query = "SELECT Q.questionID, Q.questionText, Q.option1, Q.option2, Q.option3, Q.option4, Q.correctOption, S.subjectName FROM examquestions Q JOIN subjects S ON Q.subjectID = S.subjectID WHERE Q.createdBy = ?";

        try (Connection connection = LocalDatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, 0); // Use the instructor ID 0
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                questions.add(new Question(resultSet.getInt("questionID"),
                        resultSet.getString("questionText"),
                        resultSet.getString("option1"),
                        resultSet.getString("option2"),
                        resultSet.getString("option3"),
                        resultSet.getString("option4"),
                        resultSet.getInt("correctOption"),
                        resultSet.getString("subjectName")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return questions;
    }

    private void deleteQuestion() {
        Question selectedQuestion = table.getSelectionModel().getSelectedItem();
        if (selectedQuestion != null) {
            String query = "DELETE FROM examquestions WHERE questionID = ?";
            try (Connection connection = LocalDatabaseConnection.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {

                preparedStatement.setInt(1, selectedQuestion.getQuestionID());
                preparedStatement.executeUpdate();

                table.getItems().remove(selectedQuestion);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void showEditWindow() {
        Question selectedQuestion = table.getSelectionModel().getSelectedItem();
        if (selectedQuestion != null) {
            Stage editWindow = new Stage();
            editWindow.setTitle("Edit Question");

            GridPane editGrid = new GridPane();
            editGrid.setPadding(new Insets(10, 10, 10, 10));
            editGrid.setVgap(8);
            editGrid.setHgap(10);

            Label questionLabel = new Label("Question:");
            GridPane.setConstraints(questionLabel, 0, 0);
            TextField editQuestionInput = new TextField(selectedQuestion.getQuestionText());
            GridPane.setConstraints(editQuestionInput, 1, 0);

            Label option1Label = new Label("Option 1:");
            GridPane.setConstraints(option1Label, 0, 1);
            TextField editOption1Input = new TextField(selectedQuestion.getOption1());
            GridPane.setConstraints(editOption1Input, 1, 1);

            Label option2Label = new Label("Option 2:");
            GridPane.setConstraints(option2Label, 0, 2);
            TextField editOption2Input = new TextField(selectedQuestion.getOption2());
            GridPane.setConstraints(editOption2Input, 1, 2);

            Label option3Label = new Label("Option 3:");
            GridPane.setConstraints(option3Label, 0, 3);
            TextField editOption3Input = new TextField(selectedQuestion.getOption3());
            GridPane.setConstraints(editOption3Input, 1, 3);

            Label option4Label = new Label("Option 4:");
            GridPane.setConstraints(option4Label, 0, 4);
            TextField editOption4Input = new TextField(selectedQuestion.getOption4());
            GridPane.setConstraints(editOption4Input, 1, 4);

            Label correctOptionLabel = new Label("Correct Option (1-4):");
            GridPane.setConstraints(correctOptionLabel, 0, 5);
            TextField editCorrectOptionInput = new TextField(String.valueOf(selectedQuestion.getCorrectOption()));
            GridPane.setConstraints(editCorrectOptionInput, 1, 5);

            Button saveButton = new Button("Save Changes");
            GridPane.setConstraints(saveButton, 1, 6);

            editGrid.getChildren().addAll(questionLabel, editQuestionInput, option1Label, editOption1Input,
                    option2Label, editOption2Input, option3Label, editOption3Input, option4Label, editOption4Input,
                    correctOptionLabel, editCorrectOptionInput, saveButton);

            saveButton.setOnAction(e -> updateQuestion(selectedQuestion, editQuestionInput, editOption1Input, editOption2Input, editOption3Input, editOption4Input, editCorrectOptionInput, editWindow));

            Scene editScene = new Scene(editGrid, 400, 300);
            editWindow.setScene(editScene);
            editWindow.show();
        }
    }

    private void updateQuestion(Question selectedQuestion, TextField editQuestionInput, TextField editOption1Input, TextField editOption2Input, TextField editOption3Input, TextField editOption4Input, TextField editCorrectOptionInput, Stage editWindow) {
        String query = "UPDATE examquestions SET questionText = ?, option1 = ?, option2 = ?, option3 = ?, option4 = ?, correctOption = ? WHERE questionID = ?";
        try (Connection connection = LocalDatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, editQuestionInput.getText());
            preparedStatement.setString(2, editOption1Input.getText());
            preparedStatement.setString(3, editOption2Input.getText());
            preparedStatement.setString(4, editOption3Input.getText());
            preparedStatement.setString(5, editOption4Input.getText());
            preparedStatement.setInt(6, Integer.parseInt(editCorrectOptionInput.getText()));
            preparedStatement.setInt(7, selectedQuestion.getQuestionID());

            preparedStatement.executeUpdate();

            selectedQuestion.setQuestionText(editQuestionInput.getText());
            selectedQuestion.setOption1(editOption1Input.getText());
            selectedQuestion.setOption2(editOption2Input.getText());
            selectedQuestion.setOption3(editOption3Input.getText());
            selectedQuestion.setOption4(editOption4Input.getText());
            selectedQuestion.setCorrectOption(Integer.parseInt(editCorrectOptionInput.getText()));
            table.refresh();

            editWindow.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private int getCurrentInstructorID() {
        // Replace this method with the actual logic to get the current instructor's ID
        return 1; // Example instructor ID
    }

    public static void main(String[] args) {
        launch(args);
    }
}
