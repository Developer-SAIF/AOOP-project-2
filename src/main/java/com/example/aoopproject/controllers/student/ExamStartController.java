package com.example.aoopproject.controllers.student;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.util.Duration;

// Import your own classes here as needed;
// Adjust package names to match your existing structure.
import com.example.aoopproject.models.ExamManager;    // example import
import com.example.aoopproject.views.ViewFactory;     // example import

import java.net.URL;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;

/**
 * Controller class for StartingExam.fxml
 * Displays a countdown or time-remaining panel until the exam becomes active.
 * When the exam is available, the user can click "Enter Exam" to navigate to
 * the actual exam screen (ExamView.fxml), controlled by ExamController.
 */
public class ExamStartController implements Initializable {

    @FXML private Label examLabel;
    @FXML private Label countdownLabel;
    @FXML private Button enterExamButton;

    private Timeline countdownTimer;
    private final int examId;
    private LocalDateTime examStartTime;
    private boolean examStarted;

    /**
     * Constructor with the exam ID.
     * @param examId ID of the exam to monitor (e.g., from DB or ExamManager).
     */
    public ExamStartController(int examId) {
        this.examId = examId;
        this.examStarted = false;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Example: retrieve exam info (start time, exam name, etc.) from your DB or managers.
        // Here we just simulate an upcoming exam. Replace with real data.
        this.examStartTime = LocalDateTime.now().plusMinutes(2);
        String examName = "Sample Exam"; // e.g., from your database
        examLabel.setText("Upcoming: " + examName + " (#" + examId + ")");

        enterExamButton.setDisable(true);
        enterExamButton.setOnAction(event -> handleEnterExam());

        setupCountdownTimer();
    }

    /**
     * Sets up a Timeline that updates every second to refresh the countdown.
     */
    private void setupCountdownTimer() {
        countdownTimer = new Timeline(new KeyFrame(Duration.seconds(1), event -> updateCountdown()));
        countdownTimer.setCycleCount(Timeline.INDEFINITE);
        countdownTimer.play();
    }

    /**
     * Called once per second by the Timeline.
     * Updates the countdownLabel with days/hours/min/sec until the exam is open.
     * Enables the button when the start time is reached.
     */
    private void updateCountdown() {
        LocalDateTime now = LocalDateTime.now();
        long totalSeconds = ChronoUnit.SECONDS.between(now, examStartTime);

        // If it’s time or past it, unlock the exam
        if (totalSeconds <= 0) {
            if (!examStarted) {
                countdownLabel.setText("Exam is now available!");
                enterExamButton.setDisable(false);
                examStarted = true;
            }
            return;
        }

        // If the exam is more than 1 day away, just show days
        long days = ChronoUnit.DAYS.between(now, examStartTime);
        if (days >= 1) {
            countdownLabel.setText("Starts in " + days + " day(s).");
        } else {
            // Show hours, minutes, and seconds
            long hours = totalSeconds / 3600;
            long remaining = totalSeconds % 3600;
            long minutes = remaining / 60;
            long seconds = remaining % 60;
            countdownLabel.setText(String.format("Starts in %02dh %02dm %02ds", hours, minutes, seconds));
        }
    }

    /**
     * Triggered when the user clicks the Enter Exam button.
     * Navigates to the main exam view (ExamView.fxml), e.g., via ViewFactory.
     */
    private void handleEnterExam() {
        // Stop the countdown, so we don’t waste resources:
        if (countdownTimer != null) {
            countdownTimer.stop();
        }

        // Use your existing method to show the actual exam:
        // e.g. ViewFactory.getInstance().showExamScreen(examId);
        // You might also close the current window if that's appropriate.

        ViewFactory.getInstance().showExamScreen(examId);

        // Optionally hide this window:
        examLabel.getScene().getWindow().hide();
    }
}