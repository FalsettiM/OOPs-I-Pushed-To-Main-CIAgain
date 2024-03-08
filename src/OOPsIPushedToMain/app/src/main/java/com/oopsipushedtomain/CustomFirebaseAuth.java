package com.oopsipushedtomain;

/**
 * Class for authenticating a user with Firebase
 */
public class CustomFirebaseAuth {
    /**
     * An instance of the class
     */
    private static CustomFirebaseAuth instance;
    /**
     * The UID of the current user
     */
    private String currentUserID;

    /**
     * An empty constructor to enforce the singleton pattern
     */
    private CustomFirebaseAuth() {
        // Private constructor to enforce singleton pattern
    }

    /**
     * Creates a new instance if there is not one created already.
     * This method can only be run by one thread at a time
     *
     * @return A reference to the instance
     */
    public static synchronized CustomFirebaseAuth getInstance() {
        if (instance == null) {
            instance = new CustomFirebaseAuth();
        }
        return instance;
    }

    /**
     * Simulates the user sign in
     * @param userId The UID of the user
     */
    public void signIn(String userId) {
        // Simulate user sign-in
        currentUserID = userId;
    }

    /**
     * Simulates a user signing out.
     * Resets the current UID to null
     */
    public void signOut() {
        // Simulate user sign-out
        currentUserID = null;
    }

    /**
     * Gets the UID of the current user
     * @return The UID of the user
     */
    public String getCurrentUserID() {
        return currentUserID;
    }
}