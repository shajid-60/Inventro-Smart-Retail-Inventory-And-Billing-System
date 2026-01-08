package com.shajid.app.inventro.controller;

public class SessionManager {
    private static Integer currentUserId = null;
    private static String currentUserEmail = null;
    private static String currentUserRole = null;

    public static void setCurrentUser(Integer userId, String email, String role) {
        currentUserId = userId;
        currentUserEmail = email;
        currentUserRole = role;
    }

    public static Integer getCurrentUserId() {
        return currentUserId;
    }

    public static String getCurrentUserEmail() {
        return currentUserEmail;
    }

    public static String getCurrentUserRole() {
        return currentUserRole;
    }

    public static void clearSession() {
        currentUserId = null;
        currentUserEmail = null;
        currentUserRole = null;
    }
}

