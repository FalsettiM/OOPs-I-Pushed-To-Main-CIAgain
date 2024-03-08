package com.oopsipushedtomain;

public class CustomFirebaseAuth {
    private static CustomFirebaseAuth instance;
    private String currentUserID;

    private CustomFirebaseAuth() {
        // Private constructor to enforce singleton pattern
    }

    public static synchronized CustomFirebaseAuth getInstance() {
        if (instance == null) {
            instance = new CustomFirebaseAuth();
        }
        return instance;
    }

    public void signIn(String userId) {
        // Simulate user sign-in
        currentUserID = userId;
    }

    public void signOut() {
        // Simulate user sign-out
        currentUserID = null;
    }

    public String getCurrentUserID() {
        return currentUserID;
    }
}