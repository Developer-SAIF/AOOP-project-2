package com.example.aoopproject.models;

import com.example.aoopproject.database.DatabaseConnection;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExamSession {
    private final int examId;
    private final int userId;
    private final Map<Integer, Integer> userAnswers;
    private final IntegerProperty currentScore;
    private final IntegerProperty totalQuestions;
    private final IntegerProperty currentQuestionIndex;
    private boolean isExamFinished;
    private List<ExamQuestion> questions;
    private LocalDateTime startTime;
    private int duration;

    public ExamSession(int examId, String userIdStr) {
        this.examId = examId;
        try {
            this.userId = Integer.parseInt(userIdStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid user ID format", e);
        }
        this.userAnswers = new HashMap<>();
        this.currentScore = new SimpleIntegerProperty(0);
        this.totalQuestions = new SimpleIntegerProperty(0);
        this.currentQuestionIndex = new SimpleIntegerProperty(0);
        this.isExamFinished = false;
        this.questions = new ArrayList<>();
        this.startTime = LocalDateTime.now();

        loadExamDetails();
        loadQuestions();
        loadTotalQuestions();
    }

    private void loadExamDetails() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT duration FROM examschedules WHERE examID = ?"
             )) {
            stmt.setInt(1, examId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                this.duration = rs.getInt("duration");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadQuestions() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT q.*, s.subjectName FROM examquestions q " +
                             "JOIN subjects s ON q.subjectID = s.subjectID " +
                             "WHERE q.subjectID = (SELECT subjectID FROM exams WHERE examID = ?)"
             )) {
            stmt.setInt(1, examId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String[] options = new String[]{
                        rs.getString("option1"),
                        rs.getString("option2"),
                        rs.getString("option3"),
                        rs.getString("option4")
                };

                questions.add(new ExamQuestion(
                        rs.getInt("questionID"),
                        rs.getString("questionText"),
                        options,
                        rs.getInt("correctOption")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadTotalQuestions() {
        totalQuestions.set(questions.size());
    }

    public void submitAnswer(int questionId, int selectedOption) {
        if (!isExamFinished) {
            userAnswers.put(questionId, selectedOption);
            updateScore(questionId, selectedOption);
            saveResponse(questionId, selectedOption);
        }
    }

    private void updateScore(int questionId, int selectedOption) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT correctOption FROM examquestions WHERE questionID = ?"
             )) {
            stmt.setInt(1, questionId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int correctOption = rs.getInt("correctOption");
                if (selectedOption == correctOption) {
                    currentScore.set(currentScore.get() + 1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveResponse(int questionId, int selectedOption) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO responses (examID, userID, questionID, selectedOption, score) " +
                             "VALUES (?, ?, ?, ?, ?) " +
                             "ON DUPLICATE KEY UPDATE selectedOption = ?, score = ?"
             )) {
            int score = isCorrectAnswer(questionId, selectedOption) ? 1 : 0;

            stmt.setInt(1, examId);
            stmt.setInt(2, userId);
            stmt.setInt(3, questionId);
            stmt.setInt(4, selectedOption);
            stmt.setInt(5, score);
            stmt.setInt(6, selectedOption);
            stmt.setInt(7, score);

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean isCorrectAnswer(int questionId, int selectedOption) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT correctOption FROM examquestions WHERE questionID = ?"
             )) {
            stmt.setInt(1, questionId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("correctOption") == selectedOption;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void finishExam() {
        if (!isExamFinished) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO exam_results (exam_id, user_id, total_marks, total_questions, " +
                                 "correct_answers, completion_status) VALUES (?, ?, ?, ?, ?, ?)"
                 )) {
                stmt.setInt(1, examId);
                stmt.setInt(2, userId);
                stmt.setInt(3, currentScore.get());
                stmt.setInt(4, totalQuestions.get());
                stmt.setInt(5, currentScore.get());
                stmt.setString(6, "COMPLETED");
                stmt.executeUpdate();
                isExamFinished = true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void nextQuestion() {
        if (currentQuestionIndex.get() < totalQuestions.get() - 1) {
            currentQuestionIndex.set(currentQuestionIndex.get() + 1);
        }
    }

    public void previousQuestion() {
        if (currentQuestionIndex.get() > 0) {
            currentQuestionIndex.set(currentQuestionIndex.get() - 1);
        }
    }

    public List<ExamQuestion> getQuestions() {
        if (questions == null) {
            questions = new ArrayList<>();
            loadQuestions();
        }
        return questions;
    }

    public int getExamId() {
        return examId;
    }

    public int getUserId() {
        return userId;
    }

    public Integer getUserAnswer(int questionId) {
        return userAnswers.get(questionId);
    }

    public boolean isExamFinished() {
        return isExamFinished;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public int getDuration() {
        return duration;
    }

    public IntegerProperty currentScoreProperty() {
        return currentScore;
    }

    public IntegerProperty totalQuestionsProperty() {
        return totalQuestions;
    }

    public IntegerProperty currentQuestionIndexProperty() {
        return currentQuestionIndex;
    }

    public int getCurrentScore() {
        return currentScore.get();
    }

    public int getTotalQuestions() {
        return totalQuestions.get();
    }

    public int getCurrentQuestionIndex() {
        return currentQuestionIndex.get();
    }

    public static class ExamQuestion {
        private final int questionId;
        private final String questionText;
        private final String[] options;
        private final int correctOption;

        public ExamQuestion(int questionId, String questionText, String[] options, int correctOption) {
            this.questionId = questionId;
            this.questionText = questionText;
            this.options = options;
            this.correctOption = correctOption;
        }

        public int getQuestionId() {
            return questionId;
        }

        public String getQuestionText() {
            return questionText;
        }

        public String[] getOptions() {
            return options;
        }

        public int getCorrectOption() {
            return correctOption;
        }
    }
}