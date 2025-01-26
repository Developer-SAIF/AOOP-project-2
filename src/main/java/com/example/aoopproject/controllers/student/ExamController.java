package com.example.aoopproject.controllers.student;

import com.example.aoopproject.models.ExamSession;
import com.example.aoopproject.models.UserSession;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;

public class ExamController implements Initializable {
    @FXML
    private Label questionLabel;
    @FXML
    private VBox optionsContainer;
    @FXML
    private Label timerLabel;
    @FXML
    private Label scoreLabel;
    @FXML
    private Label questionNumberLabel;
    @FXML
    private Button nextButton;
    @FXML
    private Button previousButton;
    @FXML
    private Button submitButton;

    private final int examId;

    private ExamSession examSession;
    private ToggleGroup optionsGroup;
    private Timeline timer;
    private LocalDateTime endTime;

    public ExamController(int examId) {
        this.examId = examId;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        optionsGroup = new ToggleGroup();
        setupExamSession();
        setupUI();
        setupTimer();
        loadQuestion();
        updateNavigationButtons();
    }

    private void setupExamSession() {
        examSession = new ExamSession(
                getCurrentExamId(),
                UserSession.getInstance().getUserId()
        );
    }

    private int getCurrentExamId() {
            return this.examId; // Now returns the examId passed through constructor
    }

    private void setupUI() {
        examSession.currentScoreProperty().addListener((obs, oldVal, newVal) ->
                updateScoreLabel());
        examSession.currentQuestionIndexProperty().addListener((obs, oldVal, newVal) -> {
            loadQuestion();
            updateNavigationButtons();
        });

        submitButton.setOnAction(e -> handleSubmit());
        nextButton.setOnAction(e -> examSession.nextQuestion());
        previousButton.setOnAction(e -> examSession.previousQuestion());
    }

    private void setupTimer() {
        endTime = examSession.getStartTime().plusMinutes(examSession.getDuration());

        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTimer()));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    private void updateTimer() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(endTime)) {
            timer.stop();
            handleTimeUp();
            return;
        }

        long minutes = ChronoUnit.MINUTES.between(now, endTime);
        long seconds = ChronoUnit.SECONDS.between(now, endTime) % 60;
        Platform.runLater(() ->
                timerLabel.setText(String.format("%02d:%02d", minutes, seconds)));
    }

    private void loadQuestion() {
        if (examSession.getQuestions().isEmpty()) {
            showError("No questions available");
            return;
        }

        int currentIndex = examSession.getCurrentQuestionIndex();
        ExamSession.ExamQuestion currentQuestion =
                examSession.getQuestions().get(currentIndex);

        questionLabel.setText(currentQuestion.getQuestionText());
        questionNumberLabel.setText(String.format("Question %d of %d",
                currentIndex + 1, examSession.getTotalQuestions()));

        optionsContainer.getChildren().clear();
        String[] options = currentQuestion.getOptions();

        for (int i = 0; i < options.length; i++) {
            RadioButton rb = new RadioButton(options[i]);
            rb.setToggleGroup(optionsGroup);
            rb.setUserData(i + 1); // 1-based index for options
            optionsContainer.getChildren().add(rb);

            // Select if previously answered
            Integer userAnswer = examSession.getUserAnswer(currentQuestion.getQuestionId());
            if (userAnswer != null && userAnswer == i + 1) {
                rb.setSelected(true);
            }
        }
    }

    private void updateNavigationButtons() {
        previousButton.setDisable(examSession.getCurrentQuestionIndex() == 0);
        nextButton.setDisable(
                examSession.getCurrentQuestionIndex() ==
                        examSession.getTotalQuestions() - 1);
    }

    private void updateScoreLabel() {
        scoreLabel.setText(String.format("Score: %d/%d",
                examSession.getCurrentScore(), examSession.getTotalQuestions()));
    }

    @FXML
    private void handleSubmit() {
        if (optionsGroup.getSelectedToggle() == null) {
            showError("Please select an answer");
            return;
        }

        int currentIndex = examSession.getCurrentQuestionIndex();
        ExamSession.ExamQuestion currentQuestion =
                examSession.getQuestions().get(currentIndex);
        int selectedOption =
                (int) optionsGroup.getSelectedToggle().getUserData();

        examSession.submitAnswer(currentQuestion.getQuestionId(), selectedOption);

        if (currentIndex < examSession.getTotalQuestions() - 1) {
            examSession.nextQuestion();
        } else {
            finishExam();
        }
    }

    private void handleTimeUp() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Time's Up");
            alert.setHeaderText(null);
            alert.setContentText("The exam time has expired!");
            alert.showAndWait();
            finishExam();
        });
    }

    private void finishExam() {
        examSession.finishExam();
        timer.stop();
        // Navigate to results screen or show final score
        showResults();
    }

    private void showResults() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Exam Results");
        alert.setHeaderText(null);
        alert.setContentText(String.format("Final Score: %d/%d",
                examSession.getCurrentScore(), examSession.getTotalQuestions()));
        alert.showAndWait();
        // Navigate back to dashboard or results view
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void cleanup() {
        if (timer != null) {
            timer.stop();
        }
    }
}