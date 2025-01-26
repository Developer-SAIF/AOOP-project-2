package com.example.aoopproject;

import com.example.aoopproject.controllers.student.StudentController;
import com.example.aoopproject.views.ViewFactory;
import javafx.application.Application;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

public class App extends Application {
    private static App instance;
    private List<StudentController> activeControllers = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        instance = this; // Set instance in start method
        ViewFactory.getInstance().showLoginScreen(primaryStage);
        primaryStage.show();
    }

    public static App getInstance() {
        return instance;
    }

    public void registerController(StudentController controller) {
        activeControllers.add(controller);
    }

    public void unregisterController(StudentController controller) {
        activeControllers.remove(controller);
    }

    private void shutdown() {
        for (StudentController controller : activeControllers) {
            controller.cleanup();
        }
        activeControllers.clear();
    }

    @Override
    public void stop() {
        shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
