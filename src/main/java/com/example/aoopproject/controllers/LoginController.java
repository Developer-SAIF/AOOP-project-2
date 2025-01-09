package com.example.aoopproject.controllers;

import com.example.aoopproject.database.DatabaseConnection;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import com.example.aoopproject.views.ViewFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {


    @FXML
    public AnchorPane mainPane;

    @FXML
    public AnchorPane subPane;

    @FXML
    public MFXPasswordField passwordField;

    @FXML
    private TextField userIdField;

    @FXML
    private Button loginButton;

    @FXML
    private Button registrationButton;

    @FXML
    private Button deleteUserButton;

    @FXML
    private Label statusLabel;

    @FXML
    void handleDeleteUserButtonAction(ActionEvent ignoredEvent) {
        Stage stage = (Stage) deleteUserButton.getScene().getWindow();
        ViewFactory.getInstance().showDeleteUserScreen(stage);
    }

    @FXML
    void handleLoginButtonAction(ActionEvent ignoredEvent) {
        String id = userIdField.getText();
        String password = passwordField.getText();

        // Validate input (e.g., non-empty, password length, etc.)
        if (id.isBlank() || password.isBlank()) {
            statusLabel.setText("Please fill all fields.");
            statusLabel.setOpacity(1.0);
            statusLabel.setVisible(true);
            return;
        }

        String userType = authenticate(id, password);
        if (userType != null) {
            // Navigate to the appropriate dashboard
            Stage stage = (Stage) loginButton.getScene().getWindow();
            if ("student".equalsIgnoreCase(userType)) {
                ViewFactory.getInstance().showStudentDashboard(stage);
            } else if ("admin".equalsIgnoreCase(userType)) {
                ViewFactory.getInstance().showAdminDashboard(stage);
            }
        } else {
            statusLabel.setText("Login Failed - Try Again");
            statusLabel.setOpacity(1.0);
            statusLabel.setVisible(true);
        }
    }

    @FXML
    void handleRegisterButtonAction(ActionEvent ignoredEvent) {
        Stage stage = (Stage) registrationButton.getScene().getWindow();
        ViewFactory.getInstance().showRegistrationForm(stage);
    }

    private String authenticate(String id, String password) {
        String table = "Users";
        String query = "SELECT Type FROM " + table + " WHERE ID = ? AND Password = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, id);
            statement.setString(2, password);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("Type"); // return the Type if a record is found
            }

        } catch (SQLException e) {
            // Log the exception (consider using a logging framework)
            System.err.println("Database error: " + e.getMessage());
            // Optionally, update the status label to inform the user
            statusLabel.setText("An error occurred. Please try again later.");
            statusLabel.setOpacity(1.0);
            statusLabel.setVisible(true);
        }

        return null; // return null if authentication fails
    }

    @FXML
    public void initialize() {
        statusLabel.setVisible(false);
    }
}