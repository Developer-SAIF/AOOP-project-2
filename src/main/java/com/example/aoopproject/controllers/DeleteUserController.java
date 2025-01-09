package com.example.aoopproject.controllers;

import com.example.aoopproject.database.DatabaseConnection;
import com.example.aoopproject.views.ViewFactory;
import io.github.palexdev.materialfx.controls.MFXButton;
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

public class DeleteUserController {

    @FXML
    public MFXPasswordField passwordField;

    @FXML
    public MFXButton deleteUserButton;

    @FXML
    public AnchorPane mainPane;

    @FXML
    private TextField userIdField;

    @FXML
    private Label statusLabel;

    @FXML
    private Button backButton;

    @FXML
    void handleDeleteButtonAction() {
        String userId = userIdField.getText();
        String password = passwordField.getText();

        // Basic validation
        if (userId.isBlank() || password.isBlank()) {
            statusLabel.setText("Please fill all fields.");
            statusLabel.setOpacity(1.0);
            statusLabel.setVisible(true);
            return;
        }

        // Delete the user record from the DB
        try (Connection connection = DatabaseConnection.getConnection()) {
            String deleteQuery = "DELETE FROM Users WHERE ID = ? AND Password = ?";
            PreparedStatement statement = connection.prepareStatement(deleteQuery);
            statement.setString(1, userId);
            statement.setString(2, password);

            int rowsDeleted = statement.executeUpdate();
            if (rowsDeleted > 0) {
                statusLabel.setText("Account deleted successfully!");
            } else {
                statusLabel.setText("Invalid credentials or user does not exist.");
            }
            statusLabel.setOpacity(1.0);
            statusLabel.setVisible(true);
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            statusLabel.setText("An error occurred while deleting account.");
            statusLabel.setOpacity(1.0);
            statusLabel.setVisible(true);
        }
    }

    @FXML
    void handleBackButtonAction() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        ViewFactory.getInstance().showLoginScreen(stage);
    }

    @FXML
    public void initialize() {
        statusLabel.setVisible(false);
    }
}