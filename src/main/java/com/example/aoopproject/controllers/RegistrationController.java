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
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RegistrationController {

    @FXML
    public AnchorPane mainPane;

    @FXML
    public MFXPasswordField passwordField;

    @FXML
    public Label quoteLabel;

    @FXML
    private TextField userIdField;

    @FXML
    private TextField nicknameField;

    @FXML
    public Button registerButton;

    @FXML
    private Label statusLabel;

    @FXML
    private void handleRegisterButtonAction() {
        String userId = userIdField.getText();
        String password = passwordField.getText();
        String nickname = nicknameField.getText();

        // Validate input (e.g., non-empty, password length, etc.)
        if (userId.isBlank() || password.isBlank() || nickname.isBlank()) {
            statusLabel.setText("Please fill all fields.");
            statusLabel.setOpacity(1.0);
            statusLabel.setVisible(true);
            return;
        }

        // Insert the new user record into the remote database
        try (Connection connection = DatabaseConnection.getConnection()) {
            String insertQuery = "INSERT INTO Users (ID, Password, Nickname, Type) VALUES (?, ?, ?, 'student')";
            PreparedStatement statement = connection.prepareStatement(insertQuery);
            statement.setString(1, userId);
            statement.setString(2, password);
            statement.setString(3, nickname);

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
        fetchRandomQuote();
    }

    private void fetchRandomQuote() {
        try {
            URL url = new URL("https://zenquotes.io/api/random");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            JSONArray jsonArray = new JSONArray(content.toString());
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            String quote = jsonObject.getString("q");

            quoteLabel.setText(quote);

        } catch (Exception e) {
            e.printStackTrace();
            quoteLabel.setText("Failed to load quote.");
        }
    }
}