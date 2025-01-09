package com.example.aoopproject.views;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

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

            stage.setScene(scene);
            stage.setTitle("Student Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showRegistrationForm(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Registration.fxml"));
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle("Registration");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showLoginScreen(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle("Study Helper App Login");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showDeleteUserScreen(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DeleteUser.fxml"));
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle("Delete Account");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
