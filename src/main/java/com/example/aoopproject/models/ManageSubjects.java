package com.example.aoopproject.models;

import com.example.aoopproject.database.DatabaseConnection;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ManageSubjects extends Application {

    private TableView<String> table;
    private TextField subjectInput;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Manage Subjects");

        TableColumn<String, String> subjectColumn = new TableColumn<>("Subject Name");
        subjectColumn.setMinWidth(200);
        subjectColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue()));

        table = new TableView<>();
        table.setItems(getSubjects());
        table.getColumns().add(subjectColumn);

        subjectInput = new TextField();
        subjectInput.setPromptText("Subject Name");
        subjectInput.setStyle("-fx-padding: 10; -fx-border-color: #3498db; -fx-border-radius: 5;");
        Button addButton = new Button("Add");
        styleButton(addButton);
        addButton.setOnAction(e -> addSubject());
        Button deleteButton = new Button("Delete");
        styleButton(deleteButton);
        deleteButton.setOnAction(e -> deleteSubject());

        GridPane layout = new GridPane();
        layout.setPadding(new Insets(20));
        layout.setVgap(10);
        layout.setHgap(10);
        layout.setStyle("-fx-background-color: #ffffff; -fx-border-color: #ccc; -fx-border-width: 2; -fx-border-radius: 5;");
        GridPane.setConstraints(subjectInput, 0, 0);
        GridPane.setConstraints(addButton, 1, 0);
        GridPane.setConstraints(deleteButton, 1, 1);
        GridPane.setConstraints(table, 0, 1, 2, 1);

        layout.getChildren().addAll(subjectInput, addButton, deleteButton, table);

        Scene scene = new Scene(layout, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private ObservableList<String> getSubjects() {
        ObservableList<String> subjects = FXCollections.observableArrayList();
        String query = "SELECT subjectName FROM subjects";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                subjects.add(resultSet.getString("subjectName"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return subjects;
    }

    private void addSubject() {
        String subject = subjectInput.getText();
        if (subject.isEmpty()) {
            showAlert("Subject name cannot be empty!");
            return;
        }

        String query = "INSERT INTO subjects (subjectName) VALUES (?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, subject);
            preparedStatement.executeUpdate();
            table.getItems().add(subject);
            subjectInput.clear();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteSubject() {
        String selectedSubject = table.getSelectionModel().getSelectedItem();
        if (selectedSubject == null) {
            showAlert("No subject selected!");
            return;
        }

        String query = "DELETE FROM subjects WHERE subjectName = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, selectedSubject);
            preparedStatement.executeUpdate();
            table.getItems().remove(selectedSubject);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void styleButton(Button button) {
        button.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;"));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
