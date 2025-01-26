package com.example.aoopproject.services;

import com.example.aoopproject.controllers.student.StudentController;
import com.example.aoopproject.models.Message;
import com.example.aoopproject.models.UserSession;
import javafx.application.Platform;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;
import org.json.JSONObject;

import java.util.List;

public class MessagePollingService extends ScheduledService<Void> {
    private final StudentController studentController;
    private static final int POLLING_INTERVAL_MS = 100; // milliseconds

    public MessagePollingService(StudentController studentController) {
        this.studentController = studentController;
        this.setPeriod(Duration.millis(POLLING_INTERVAL_MS));
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<>() {
            @Override
            protected Void call() {
                checkNewMessages();
                return null;
            }
        };
    }

    private void checkNewMessages() {
        String currentUserId = UserSession.getInstance().getUserId();
        List<Message> newMessages = Message.getNewMessages(currentUserId);

        if (!newMessages.isEmpty()) {
            Platform.runLater(() -> {
                for (Message message : newMessages) {
                    JSONObject jsonMessage = new JSONObject();
                    jsonMessage.put("type", "message");
                    jsonMessage.put("from", message.getSenderId());
                    jsonMessage.put("content", message.getContent());
                    studentController.displayNewMessage(jsonMessage);
                }
            });
        }
    }
}
