package com.example.aoopproject.views;

import com.example.aoopproject.models.UserSession;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.WindowEvent;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

public class ViewFactory {
    private static ViewFactory instance;

    private ViewFactory() {
    }

    public static ViewFactory getInstance() {
        if (instance == null) {
            instance = new ViewFactory();
        }
        return instance;
    }

    public void showAdminDashboard(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/Admin.fxml"));
            Scene scene = new Scene(loader.load());

            stage.setOnCloseRequest(this::handleWindowClose);

            stage.setScene(scene);
            stage.setTitle("Admin Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showStudentDashboard(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/student/Student.fxml"));
            Scene scene = new Scene(loader.load());

            stage.setOnCloseRequest(this::handleWindowClose);

            stage.setScene(scene);
            stage.setTitle("Student Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleWindowClose(WindowEvent event) {
        Stage stage = (Stage) event.getSource();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText("You're about to logout");
        alert.setContentText("Are you sure you want to logout?");

        event.consume();

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                UserSession.getInstance().endSession();
                showLoginScreen(stage);
            }
        });
    }

    public void showLoginScreen(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Scene scene = new Scene(loader.load());
            stage.setOnCloseRequest(null);
            stage.setScene(scene);
            stage.setTitle("Study Helper App Login");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showRegistrationForm(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Registration.fxml"));
            Scene scene = new Scene(loader.load());
            stage.setOnCloseRequest(null);
            stage.setScene(scene);
            stage.setTitle("Registration");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showDeleteUserScreen(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DeleteUser.fxml"));
            Scene scene = new Scene(loader.load());
            stage.setOnCloseRequest(null);
            stage.setScene(scene);
            stage.setTitle("Delete Account");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}