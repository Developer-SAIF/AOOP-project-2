package com.example.aoopproject.models;

public class UserSession {
    private static UserSession instance;
    private String userId;
    private String userType;
    private String nickname;

    private UserSession() {
        // Private constructor to enforce singleton pattern
    }

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void startSession(String userId, String userType, String nickname) {
        this.userId = userId;
        this.userType = userType;
        this.nickname = nickname;
    }

    public void endSession() {
        userId = null;
        userType = null;
        nickname = null;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserType() {
        return userType;
    }

    public String getNickname() {
        return nickname;
    }

    public boolean isLoggedIn() {
        return userId != null;
    }
}