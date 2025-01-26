package com.example.aoopproject.controllers.student;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.util.Duration;
import com.example.aoopproject.models.ExamManager;
import com.example.aoopproject.views.ViewFactory;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;

public class ExamStartController implements Initializable {

    @FXML private Label examLabel;
    @FXML private Label countdownLabel;
    @FXML private Button enterExamButton;

    private Timeline countdownTimer;
    private LocalDateTime examStartTime;
    private boolean examStarted;
    private ExamManager examManager;
    private int examId;

    public ExamStartController() {
        this.examStarted = false;
        this.examManager = ExamManager.getInstance();
        this.examId = -1;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupInitialUI();
    }

    private void setupInitialUI() {
        enterExamButton.setDisable(true);
        enterExamButton.setOnAction(event -> handleEnterExam());
        examLabel.setText("Loading exam information...");
        countdownLabel.setText("Please wait...");
    }

    public void initializeExam(int examId) {
        this.examId = examId;
        loadExamInfo();
        setupCountdownTimer();
    }

    private void loadExamInfo() {
        if (examId <= 0) {
            Platform.runLater(() -> {
                examLabel.setText("Error: Invalid exam ID");
                countdownLabel.setText("Please contact support");
            });
            return;
        }

        try {
            // Corrected to use getUpcomingExams() instead of non-existent getExamInfo()
            ExamManager.ExamInfo examInfo = examManager.getUpcomingExams().stream()
                    .filter(exam -> exam.getExamId() == examId)
                    .findFirst()
                    .orElse(null);

            if (examInfo != null) {
                examStartTime = examInfo.getExamDate(); // Changed from getStartTime() to getExamDate()
                Platform.runLater(() -> {
                    examLabel.setText(String.format("Upcoming: %s (#%d)",
                            examInfo.getExamName(), examId));
                });
            } else {
                Platform.runLater(() -> {
                    examLabel.setText("Error: Exam not found");
                    countdownLabel.setText("Please check exam ID");
                });
            }
        } catch (Exception e) {
            Platform.runLater(() -> {
                examLabel.setText("Error loading exam information");
                countdownLabel.setText("Please try again later");
            });
        }
    }

    private void setupCountdownTimer() {
        if (countdownTimer != null) {
            countdownTimer.stop();
        }

        countdownTimer = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> updateCountdown())
        );
        countdownTimer.setCycleCount(Timeline.INDEFINITE);
        countdownTimer.play();
    }

    private void updateCountdown() {
        if (examStartTime == null) return;

        LocalDateTime now = LocalDateTime.now();
        long totalSeconds = ChronoUnit.SECONDS.between(now, examStartTime);

        if (totalSeconds <= 0) {
            handleExamAvailable();
            return;
        }

        updateCountdownDisplay(now, totalSeconds);
    }

    private void handleExamAvailable() {
        if (!examStarted && examManager.isExamAvailable(examId)) {
            Platform.runLater(() -> {
                countdownLabel.setText("Exam is now available!");
                enterExamButton.setDisable(false);
            });
            examStarted = true;
        }
    }

    private void updateCountdownDisplay(LocalDateTime now, long totalSeconds) {
        long days = ChronoUnit.DAYS.between(now, examStartTime);
        String countdownText;

        if (days >= 1) {
            countdownText = "Starts in " + days + " day(s)";
        } else {
            long hours = totalSeconds / 3600;
            long remaining = totalSeconds % 3600;
            long minutes = remaining / 60;
            long seconds = remaining % 60;
            countdownText = String.format("Starts in %02dh %02dm %02ds",
                    hours, minutes, seconds);
        }

        Platform.runLater(() -> countdownLabel.setText(countdownText));
    }

    @FXML
    private void handleEnterExam() {
        if (examManager.isExamAvailable(examId)) {
            cleanup();
            ViewFactory.getInstance().showExamScreen(examId);
            examLabel.getScene().getWindow().hide();
        } else {
            Platform.runLater(() -> {
                countdownLabel.setText("Exam is not available yet");
                enterExamButton.setDisable(true);
            });
        }
    }

    public void cleanup() {
        if (countdownTimer != null) {
            countdownTimer.stop();
        }
    }
}