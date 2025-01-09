package com.example.aoopproject.controllers;

import com.example.aoopproject.database.DatabaseConnection;
import com.example.aoopproject.views.ViewFactory;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RegistrationController {

    @FXML
    public AnchorPane mainPane;

    @FXML
    public MFXPasswordField passwordField;

    @FXML
    private TextField userIdField;

    @FXML
    public Button registerButton;

    @FXML
    private Label statusLabel;

    @FXML
    private void handleRegisterButtonAction() {
        String userId = userIdField.getText();
        String password = passwordField.getText();

        // Validate input (e.g., non-empty, password length, etc.)
        if (userId.isBlank() || password.isBlank()) {
            statusLabel.setText("Please fill all fields.");
            statusLabel.setOpacity(1.0);
            statusLabel.setVisible(true);
            return;
        }

        // Insert the new user record into the remote database
        try (Connection connection = DatabaseConnection.getConnection()) {
            String insertQuery = "INSERT INTO Users (ID, Password, Type) VALUES (?, ?, 'student')";
            PreparedStatement statement = connection.prepareStatement(insertQuery);
            statement.setString(1, userId);
            statement.setString(2, password);

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                statusLabel.setText("Registration successful!");
            } else {
                statusLabel.setText("Registration failed. Try again.");
            }
            statusLabel.setOpacity(1.0);
            statusLabel.setVisible(true);
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            statusLabel.setText("An error occurred while registering.");
            statusLabel.setOpacity(1.0);
            statusLabel.setVisible(true);
        }
    }

    @FXML
    private Button backButton;

    @FXML
    private void handleBackButtonAction() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        ViewFactory.getInstance().showLoginScreen(stage);
    }

    @FXML
    public void initialize() {
        statusLabel.setVisible(false);
    }
}

