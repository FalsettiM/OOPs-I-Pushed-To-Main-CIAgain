package com.oopsipushedtomain.Database;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Class for all Firebase accesses
 * Has methods for accessing both storage and Firestore database
 *
 * @author Matteo Falsetti
 * @version 1.0
 */
public class FirebaseAccess {

    /**
     * A reference to the Firestore database
     */
    private FirebaseFirestore db = null;
    /**
     * A reference to the collection
     */
    private CollectionReference collRef = null;
    /**
     * A reference to the document
     */
    private DocumentReference docRef = null;
    /**
     * A reference to the inner collection (if applicable)
     */
    private CollectionReference innerCollRef = null;

    /**
     * A reference to Firebase Storage
     */
    private FirebaseStorage storage = null;
    /**
     * A reference to the selected storage pool
     */
    private StorageReference poolRef = null;

    private FirestoreAccessType databaseType;

    /**
     * Creates a new FirebaseAccess object
     *
     * @param databaseType The data you want to access in the database
     */
    public FirebaseAccess(FirestoreAccessType databaseType) {
        // Set the database and storage type
        this.databaseType = databaseType;

        // Set the database reference (same for the entire database)
        db = FirebaseFirestore.getInstance();

        // Set the storage reference (same for the entire database)
        storage = FirebaseStorage.getInstance();

        // Set the collection
        switch (databaseType) {
            case ANNOUNCEMENTS:
                collRef = db.collection("announcements");
                break;
            case EVENTS:
                collRef = db.collection("events");
                poolRef = storage.getReference().child("eventposters");
                break;
            case IMAGES:
                collRef = db.collection("images");
                break;
            case QRCODES:
                collRef = db.collection("qrcodes");
                poolRef = storage.getReference().child("qrcodes");
                break;
            case USERS:
                collRef = db.collection("users");
                poolRef = storage.getReference().child("profilepictures");
                break;
        }
    }

    // Chat GPT: Is there a way to wait until data is confirmed stored in Firebase database using a future

    /**
     * Attach a CompletableFuture to a Firebase read/write
     *
     * @param task The task to attach the future to
     * @param <T>  The parameter for the Completable future
     * @return The CompletableFuture to it was attached to
     */
    public static <T> CompletableFuture<T> toCompletableFuture(Task<T> task) {
        // Create the new completable future
        CompletableFuture<T> future = new CompletableFuture<>();

        // Add the listeners for the the task
        task.addOnSuccessListener(future::complete).addOnFailureListener(future::completeExceptionally);

        return future;
    }

    /**
     * Converts a bitmap image to a byte array for storing into the database
     *
     * @param bitmap The bitmap to convert
     * @return The output byte array
     */
    public static byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);

        return baos.toByteArray();
    }


    // Chat GPT: Is there a way to wait until data is confirmed stored in Firebase database using a future

    /**
     * Stores data in firestore, given the UID of the document.
     * If it is a new document, it will create a UID
     *
     * @param docName The UID of the document to write to
     * @param data    The data to write to the document
     */
    public void storeDataInFirestore(String docName, Map<String, Object> data) {
        // Set the document reference
        docRef = collRef.document(docName);

        // Create a completable future for waiting until the transfer is complete
        CompletableFuture<Void> future = toCompletableFuture(docRef.set(data));

        // Catch any errors
        try {
            // Block until data is written
            future.get();
            Log.d("StoreinFirestore", "Document has been saved successfully");
        } catch (InterruptedException e) {
            // Handle the interrupted execption
            Thread.currentThread().interrupt();
            Log.e("StoreinFirestore", "Task was interrupted: " + e.getMessage());
        } catch (ExecutionException e) {
            // Handle any other exception
            Log.e("StoreinFirestore", "Error writing document: " + Objects.requireNonNull(e.getCause()).getMessage());
        }

    }

    /**
     * Stores data in firestore, given the UID of the document and the UID of the inner collection
     *
     * @param outerDocName  The UID of the outer document (Ex. Event UID)
     * @param innerCollName The name of the inner collection
     * @param innerDocName  The UID of the inner document (Ex. eventPosters)
     * @param data          The data to write to the inner document
     */
    public void storeDataInFirestoreInnerCollection(String outerDocName, FirebaseInnerCollection innerCollName, String innerDocName, Map<String, Object> data) {
        // Set the document reference
        docRef = collRef.document(outerDocName);

        // Set the collection reference for the inner collection
        innerCollRef = docRef.collection(innerCollName.name());

        // Store the data in the collection
        // Create a completable future for waiting until the transfer is complete
        CompletableFuture<Void> future = toCompletableFuture(innerCollRef.document(innerDocName).set(data));

        // Catch any errors
        try {
            // Block until data is written
            future.get();
            Log.d("StoreinFirestore", "Inner document has been saved successfully");
        } catch (InterruptedException e) {
            // Handle the interrupted exception
            Thread.currentThread().interrupt();
            Log.e("StoreinFirestore", "Task was interrupted: " + e.getMessage());
        } catch (ExecutionException e) {
            // Handle any other exception
            Log.e("StoreinFirestore", "Error writing inner document: " + Objects.requireNonNull(e.getCause()).getMessage());
        }
    }

    /**
     * Saves an image to the database and creates a link to the image file if necessary
     *
     * @param attachedTo The UID/name of the document this image is linked to. Ex: USER-0000000000
     * @param imageUID   The UID/name of the image (will create new if null)
     * @param imageType  The type of image you are uploading (only used for events)
     * @param image      The image to upload
     */
    public void storeImageInFirebaseStorage(String attachedTo, String imageUID, ImageType imageType, Bitmap image) {
        // Check if this the constructor database parameters are correct
        boolean isValidDatabase = this.databaseType == FirestoreAccessType.USERS || this.databaseType == FirestoreAccessType.EVENTS;

        // Throw exception if the database is not valid
        if (!isValidDatabase) {
            throw new IllegalArgumentException("This object is not valid for storing images");
        }

        // Database is valid, set the document reference for the origin document
        docRef = collRef.document(attachedTo);

        // If no image UID is given, create a new one
        boolean newImage = false;
        if (imageUID == null) {
            imageUID = "IMGE-" + collRef.document().getId().toUpperCase();
            newImage = true;
        }

        /*
            Store the image into storage
         */
        // Convert the image to a byte array
        byte[] imageArray = bitmapToByteArray(image);

        // Create the upload task to upload to storage
        UploadTask uploadTask = poolRef.child(imageUID).putBytes(imageArray);

        // Convert the Upload task to a CompletableFuture to wait for any tasks to complete
        // Also convert the output of the upload (TaskSnapshot) to a StorageReference using thenApply()
        CompletableFuture<StorageReference> future = toCompletableFuture(uploadTask).thenApply(taskSnapshot -> poolRef);

        // Perform the operation
        // Catch any errors
        try {
            // Block until data is written
            future.get();
            Log.d("StoreinFirebaseStorage", "Image has been saved successfully");
        } catch (InterruptedException e) {
            // Handle the interrupted exception
            Thread.currentThread().interrupt();
            Log.e("StoreinFirebaseStorage", "Task was interrupted: " + e.getMessage());
            return;
        } catch (ExecutionException e) {
            // Handle any other exception
            Log.e("StoreinFirebaseStorage", "Error writing image: " + Objects.requireNonNull(e.getCause()).getMessage());
            return;
        }

        // Image upload complete, if you are updating an image, you are done
        // No need to update the link to the image
        if (!newImage) {
            return;
        }
        // Otherwise, create the link to the image


        /*
            Attach the link to the origin document
         */

        // Create an empty hash map to create just a new document
        HashMap<String, Object> newData = new HashMap<>();

        // Get a reference to the images collection for creating the link later
        FirebaseAccess imageAccess = null;

        // Create the new document in the appropriate collection
        switch (this.databaseType) {
            case EVENTS:
                switch (imageType) {
                    case EVENT_POSTER:
                        imageAccess = new FirebaseAccess(FirestoreAccessType.IMAGES);
                        ;
                        this.storeDataInFirestoreInnerCollection(attachedTo, FirebaseInnerCollection.eventPosters, imageUID, newData);
                    case EVENT_QRCODE:
                        this.storeDataInFirestoreInnerCollection(attachedTo, FirebaseInnerCollection.eventQRCodes, imageUID, newData);
                        imageAccess = new FirebaseAccess(FirestoreAccessType.QRCODES);
                    case PROMO_QRCODE:
                        this.storeDataInFirestoreInnerCollection(attachedTo, FirebaseInnerCollection.promoQRCodes, imageUID, newData);
                        imageAccess = new FirebaseAccess(FirestoreAccessType.QRCODES);
                }
            case USERS:
                this.storeDataInFirestoreInnerCollection(attachedTo, FirebaseInnerCollection.profilePictures, imageUID, newData);
                imageAccess = new FirebaseAccess(FirestoreAccessType.IMAGES);
        }


        /*
            Create the link in the image collection
         */

        // Create the map of the data
        HashMap<String, Object> data = new HashMap<>();

        // Add the relevant data for the link
        data.put("origin", attachedTo);
        data.put("image", imageUID);

        // Create the link
        assert imageAccess != null;
        imageAccess.storeDataInFirestore(imageUID, data);


    }

    // Chat GPT: Can you give me some java code within android to retrieve data from Firestore using a Completable future

    /**
     * Gets the data from a document and returns it as a map
     * Assumes there is only one document to return
     *
     * @param outerDocName  The UID of the document in the outer collection. If the other inputs are null, will return data from this document
     * @param innerCollName The name of the inner collection. If this is null, will return data from the outer collection
     * @param innerDocName  The UID of the document in the inner collection. If this is null, will return data from the outer collection
     * @return The data in the document, or null if the document was not found
     */
    public Map<String, Object> getDataFromFirestore(String outerDocName, FirebaseInnerCollection innerCollName, String innerDocName) {
        // Get the document reference for the outer main collection
        docRef = collRef.document(outerDocName);

        Task<DocumentSnapshot> task = null;

        // Get the document from firebase
        if (innerCollName != null && innerDocName != null){
            task = docRef.collection(innerCollName.name()).document(innerDocName).get();
        } else {
            task = docRef.get();
        }


        // Convert the task to a CompletableFuture
        CompletableFuture<DocumentSnapshot> future = toCompletableFuture(task);

        // Get the output
        DocumentSnapshot document = null;
        try {
            // Block until data is retrieved
            document = future.get();
            Log.d("GetFromFirestore", "Data has been received");
        } catch (InterruptedException e) {
            // Handle the interrupted exception
            Thread.currentThread().interrupt();
            Log.e("GetFromFirestore", "Task was interrupted: " + e.getMessage());
            return null;
        } catch (ExecutionException e) {
            // Handle any other exception
            Log.e("GetFromFirestore", "Error retrieving document: " + Objects.requireNonNull(e.getCause()).getMessage());
            return null;
        }

        // Data was retrieved successfully
        Map<String, Object> data = null;
        if (document != null && document.exists()) {
            // Get the data from the query - assume only one document with this ID
            data = document.getData();

            // Attach the document id to the data
            assert data != null;
            data.put("UID", document.getId());

        } else {
            Log.e("GetFromFirestore", "The document does not exist");
        }

        // Return the received data
        return data;
    }

}
