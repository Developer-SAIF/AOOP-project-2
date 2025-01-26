package com.example.aoopproject.controllers.student;

import com.example.aoopproject.models.ExamManager;
import com.example.aoopproject.models.ExamManager.ExamInfo;
import com.example.aoopproject.views.ViewFactory;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;

public class ExamListController implements Initializable {
    @FXML private TableView<ExamInfo> examTable;
    @FXML private TableColumn<ExamInfo, String> examNameCol;
    @FXML private TableColumn<ExamInfo, String> subjectCol;
    @FXML private TableColumn<ExamInfo, LocalDateTime> timeCol;
    @FXML private TableColumn<ExamInfo, ExamInfo> actionCol;

    private final ExamManager examManager = ExamManager.getInstance();
    private Timeline refreshTimeline;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        setupRefreshTimer();
        loadExams();
    }

    private void setupTable() {
        examNameCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getExamName()));

        subjectCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getSubjectName()));

        timeCol.setCellValueFactory(data ->
                new SimpleObjectProperty<>(data.getValue().getExamDate()));

        timeCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                    return;
                }

                Timeline timeline = new Timeline(
                        new KeyFrame(Duration.seconds(1), event -> {
                            setText(formatTimeRemaining(date));
                        })
                );
                timeline.setCycleCount(Animation.INDEFINITE);
                timeline.play();
            }
        });

        actionCol.setCellFactory(column -> new TableCell<>() {
            private final Button enterButton = new Button("Enter Exam");

            @Override
            protected void updateItem(ExamInfo exam, boolean empty) {
                super.updateItem(exam, empty);
                if (empty || exam == null) {
                    setGraphic(null);
                    return;
                }

                enterButton.setOnAction(event -> startExam(exam));
                enterButton.setDisable(!examManager.isExamAvailable(exam.getExamId()));
                setGraphic(enterButton);
            }
        });
    }

    private void setupRefreshTimer() {
        refreshTimeline = new Timeline(
                new KeyFrame(Duration.seconds(30), event -> loadExams())
        );
        refreshTimeline.setCycleCount(Animation.INDEFINITE);
        refreshTimeline.play();
    }

    private void loadExams() {
        examTable.setItems(examManager.getUpcomingExams());
    }

    private String formatTimeRemaining(LocalDateTime examDate) {
        LocalDateTime now = LocalDateTime.now();
        long days = ChronoUnit.DAYS.between(now, examDate);

        if (days < 1) {
            long hours = ChronoUnit.HOURS.between(now, examDate);
            long minutes = ChronoUnit.MINUTES.between(now, examDate) % 60;
            long seconds = ChronoUnit.SECONDS.between(now, examDate) % 60;
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }

        return days + " days remaining";
    }

    private void startExam(ExamInfo exam) {
        ViewFactory.getInstance().showExamScreen(exam.getExamId());
    }

    public void cleanup() {
        if (refreshTimeline != null) {
            refreshTimeline.stop();
        }
    }
}