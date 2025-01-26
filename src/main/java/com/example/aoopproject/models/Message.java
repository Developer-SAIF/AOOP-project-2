package com.example.aoopproject.models;

import com.example.aoopproject.database.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Message {
    private int id;
    private String senderId;
    private String receiverId;
    private String content;
    private LocalDateTime timestamp;
    private boolean read;

    public String getSenderId() {
        return senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Message(String senderId, String receiverId, String content) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.read = false;
    }

    public static void saveMessage(Message message) {
        String sql = "INSERT INTO messages (sender_id, receiver_id, content, timestamp, is_read) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, message.senderId);
            pstmt.setString(2, message.receiverId);
            pstmt.setString(3, message.content);
            pstmt.setTimestamp(4, Timestamp.valueOf(message.timestamp));
            pstmt.setBoolean(5, message.read);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Message> getMessageHistory(String user1Id, String user2Id) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM messages WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?) ORDER BY timestamp";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user1Id);
            pstmt.setString(2, user2Id);
            pstmt.setString(3, user2Id);
            pstmt.setString(4, user1Id);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Message message = new Message(
                        rs.getString("sender_id"),
                        rs.getString("receiver_id"),
                        rs.getString("content")
                );
                message.id = rs.getInt("id");
                message.timestamp = rs.getTimestamp("timestamp").toLocalDateTime();
                message.read = rs.getBoolean("is_read");
                messages.add(message);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    public static List<Message> getNewMessages(String userId) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM messages WHERE receiver_id = ? AND is_read = false ORDER BY timestamp";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Message message = new Message(
                        rs.getString("sender_id"),
                        rs.getString("receiver_id"),
                        rs.getString("content")
                );
                message.id = rs.getInt("id");
                message.timestamp = rs.getTimestamp("timestamp").toLocalDateTime();
                message.read = rs.getBoolean("is_read");
                messages.add(message);
            }

            // Mark all retrieved messages as read in a batch update
            markMessagesAsRead(messages);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    private static void markMessagesAsRead(List<Message> messages) {
        String sql = "UPDATE messages SET is_read = true WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (Message message : messages) {
                pstmt.setInt(1, message.id);
                pstmt.addBatch();
            }

            pstmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
