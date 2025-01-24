package com.example.aoopproject.models;

import com.example.aoopproject.database.DatabaseConnection;
import java.sql.*;
import java.util.*;

public class User {
    private String userId;
    private String nickname;
    private String userType;

    public User(String userId, String nickname, String userType) {
        this.userId = userId;
        this.nickname = nickname;
        this.userType = userType;
    }

    public String getUserId() {
        return userId;
    }

    public String getNickname() {
        return nickname;
    }

    public String getUserType() {
        return userType;
    }

    public static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT ID, Nickname, Type FROM Users";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                User user = new User(
                        rs.getString("ID"),
                        rs.getString("Nickname"),
                        rs.getString("Type")
                );
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
}
