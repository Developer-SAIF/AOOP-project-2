package com.example.aoopproject.views;

import com.example.aoopproject.App;
import com.example.aoopproject.controllers.admin.AdminController;
import com.example.aoopproject.controllers.student.StudentController;
import com.example.aoopproject.models.UserSession;
import javafx.scene.Parent;
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

            AdminController controller = loader.getController();

            String userNickname = UserSession.getInstance().getNickname();
            stage.setTitle(userNickname + "'s Dashboard");

            stage.setOnCloseRequest(event -> handleWindowClose(event, controller));

            stage.setScene(scene);
            stage.setTitle("Admin Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleLoginWindowClose(WindowEvent event) {
        Stage stage = (Stage) event.getSource();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Confirmation");
        alert.setHeaderText("You're about to exit the application");
        alert.setContentText("Are you sure you want to exit?");

        event.consume();

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                stage.close();
                // Ensure proper application shutdown
                App.getInstance().stop();
                System.exit(0);
            }
        });
    }

    public void showStudentDashboard(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/student/Student.fxml"));
            Parent root = loader.load();

            StudentController controller = loader.getController();
            App.getInstance().registerController(controller);

            Scene scene = new Scene(root);

            String userNickname = UserSession.getInstance().getNickname();
            stage.setTitle(userNickname + "'s Dashboard");

            stage.setOnCloseRequest(event -> {
                handleWindowClose(event, controller);
                App.getInstance().unregisterController(controller);
            });

            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleWindowClose(WindowEvent event, StudentController controller) {
        Stage stage = (Stage) event.getSource();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText("You're about to logout");
        alert.setContentText("Are you sure you want to logout?");

        event.consume();

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (controller != null) {
                    controller.cleanup();
                }
                UserSession.getInstance().endSession();
                showLoginScreen(stage);
            }
        });
    }

    private void handleWindowClose(WindowEvent event, AdminController controller) {
        Stage stage = (Stage) event.getSource();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText("You're about to logout");
        alert.setContentText("Are you sure you want to logout?");

        event.consume();

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Call cleanup before ending session
                if (controller != null) {
                    controller.cleanup();
                }
                UserSession.getInstance().endSession();
                showLoginScreen(stage);
            }
        });
    }

    public void showLoginScreen(Stage stage) {
        try {
            System.out.println("Attempting to load login screen...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            if (loader.getLocation() == null) {
                System.err.println("FXML file not found!");
                return;
            }
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle("Study Helper App Login");

            // Add proper window close handling for login screen
            stage.setOnCloseRequest(event -> handleLoginWindowClose(event));

            System.out.println("Login screen loaded successfully");
        } catch (Exception e) {
            System.err.println("Error loading login screen: " + e.getMessage());
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