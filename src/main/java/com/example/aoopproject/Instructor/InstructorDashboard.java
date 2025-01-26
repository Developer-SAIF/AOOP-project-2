package com.example.aoopproject.Instructor;

import com.example.onlineexamsystem.Instructor.Model.ManageSubjects;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class InstructorDashboard extends Application {

    private Stage window;

    @Override
    public void start(Stage primaryStage) {
        window = primaryStage;
        window.setTitle("Instructor Dashboard");

        TabPane tabPane = new TabPane();

        // Add Question Tab
        Tab addQuestionTab = new Tab("Add Question");
        VBox addQuestionLayout = new VBox(20);
        addQuestionLayout.setPadding(new Insets(30));
        Button addQuestionButton = new Button("Add Question");
        styleButton(addQuestionButton);
        addQuestionButton.setOnAction(e -> new AddQuestion().start(new Stage()));
        addQuestionLayout.getChildren().add(addQuestionButton);
        addQuestionTab.setContent(addQuestionLayout);

        // View Questions Tab
        Tab viewQuestionsTab = new Tab("View Questions");
        VBox viewQuestionsLayout = new VBox(20);
        viewQuestionsLayout.setPadding(new Insets(30));
        Button viewQuestionsButton = new Button("View Questions");
        styleButton(viewQuestionsButton);
        viewQuestionsButton.setOnAction(e -> new ViewQuestions().start(new Stage()));
        viewQuestionsLayout.getChildren().add(viewQuestionsButton);
        viewQuestionsTab.setContent(viewQuestionsLayout);

        // Manage Subjects Tab
        Tab manageSubjectsTab = new Tab("Manage Subjects");
        VBox manageSubjectsLayout = new VBox(20);
        manageSubjectsLayout.setPadding(new Insets(30));
        Button manageSubjectsButton = new Button("Manage Subjects");
        styleButton(manageSubjectsButton);
        manageSubjectsButton.setOnAction(e -> {
            try {
                new ManageSubjects().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        manageSubjectsLayout.getChildren().add(manageSubjectsButton);
        manageSubjectsTab.setContent(manageSubjectsLayout);

        // Set Exam Tab
        Tab setExamTab = new Tab("Set Exam");
        VBox setExamLayout = new VBox(20);
        setExamLayout.setPadding(new Insets(30));
        setExamLayout.getChildren().add(new ScheduleExam().getView());
        setExamTab.setContent(setExamLayout);

        tabPane.getTabs().addAll(addQuestionTab, viewQuestionsTab, manageSubjectsTab, setExamTab);

        Scene scene = new Scene(tabPane, 600, 400);
        window.setScene(scene);
        window.show();
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
