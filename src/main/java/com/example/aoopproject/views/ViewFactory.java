package com.example.aoopproject.views;

import com.example.aoopproject.App;
import com.example.aoopproject.controllers.admin.AdminController;
import com.example.aoopproject.controllers.student.ExamController;
import com.example.aoopproject.controllers.student.StudentController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminDashboard.fxml"));
            Scene scene = new Scene(loader.load());
            AdminController controller = loader.getController();
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
                App.getInstance().stop();
                System.exit(0);
            }
        });
    }

    public void showStudentDashboard(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/student/Student.fxml"));
            Scene scene = new Scene(loader.load());
            StudentController controller = loader.getController();
            stage.setOnCloseRequest(event -> handleWindowClose(event, controller));
            stage.setScene(scene);
            stage.setTitle("Student Dashboard");
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
                if (controller != null) {
                    controller.cleanup();
                }
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

    // New method for showing exam screen
    public void showExamScreen(int examId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ExamView.fxml"));

            // Set the controller with examId
            loader.setController(new ExamController(examId));

            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle("Exam");
            stage.setScene(scene);

            // Make it full screen or set specific size
            stage.setMaximized(true);

            // Prevent closing with X button during exam
            stage.setOnCloseRequest(event -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning");
                alert.setHeaderText("Exam in Progress");
                alert.setContentText("Please complete and submit the exam properly.");
                event.consume();
            });

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}