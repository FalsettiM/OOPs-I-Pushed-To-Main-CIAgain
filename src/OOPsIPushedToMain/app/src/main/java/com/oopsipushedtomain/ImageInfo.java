package com.oopsipushedtomain;

import android.graphics.Bitmap;

public class ImageInfo {
    private Bitmap image;
    private String storagePath; // Firebase Storage path
    private String firestoreDocumentId; // Optional: Firestore document ID

    public ImageInfo(Bitmap image, String storagePath, String firestoreDocumentId) {
        this.image = image;
        this.storagePath = storagePath;
        this.firestoreDocumentId = firestoreDocumentId;
    }

    // Getters and Setters
    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public String getFirestoreDocumentId() {
        return firestoreDocumentId;
    }

    public void setFirestoreDocumentId(String firestoreDocumentId) {
        this.firestoreDocumentId = firestoreDocumentId;
    }
}

