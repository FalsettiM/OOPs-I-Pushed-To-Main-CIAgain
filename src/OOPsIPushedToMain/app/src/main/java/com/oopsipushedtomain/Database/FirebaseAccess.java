package com.oopsipushedtomain.Database;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
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
        poolRef = storage.getReference();

        // Set the collection
        switch (databaseType) {
            case ANNOUNCEMENTS:
                collRef = db.collection("announcements");
                break;
            case EVENTS:
                collRef = db.collection("events");
                break;
            case IMAGES:
                collRef = db.collection("images");
                break;
            case QRCODES:
                collRef = db.collection("qrcodes");
                break;
            case USERS:
                collRef = db.collection("users");
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

    /**
     * Converts a byte array to a bitmap image
     *
     * @param byteArray The byte array to convert
     * @return The bitmap image
     */
    public static Bitmap byteArraytoBitmap(byte[] byteArray){
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }


    // Chat GPT: Is there a way to wait until data is confirmed stored in Firebase database using a future

    /**
     * Stores data in firestore, given the UID of the document.
     * If it is a new document, it will create a UID
     *
     * @param docName The UID of the document to write to
     * @param data    The data to write to the document
     * @return The UID of the document, or null if there was an error
     */
    public String storeDataInFirestore(String docName, Map<String, Object> data) {
        return this.storeDataInFirestore(docName,null, null, data);
    }

    /**
     * Stores data in firestore, given the UID of the document and inner collection/document if needed
     * If the inner document or collection are null, the data is stored in the outer document
     * If the outer doc name is not given, make a new one
     *
     * @param outerDocName  The UID of the outer document (Ex. Event UID)
     * @param innerCollName The name of the inner collection, if null, store in outer document
     * @param innerDocName  The UID of the inner document (Ex. eventPosters), if null, store in outer document
     * @param data          The data to write to the inner document
     * @return The UID of the outer document, or null if there was an error
     */
    public String storeDataInFirestore(String outerDocName, FirebaseInnerCollection innerCollName, String innerDocName, Map<String, Object> data) {
        // If the outerDocName is not given, make a new one
        if (outerDocName == null){
            switch (databaseType) {
                case EVENTS:
                    outerDocName = "EVNT-" + collRef.document().getId().toUpperCase();
                    break;
                case USERS:
                    outerDocName = "USER-" + collRef.document().getId().toUpperCase();
                    break;
                case ANNOUNCEMENTS:
                    outerDocName = "ANMT-" + collRef.document().getId().toUpperCase();
                    break;
                case QRCODES:
                    Log.e("StoreinFirestore", "Use storeImageInFirebaseStorage() to store qrcodes");
                    return null;
                case IMAGES:
                    Log.e("StoreinFirestore", "Use storeImageInFirebaseStorage() to store an image");
                    return null;
            }
        }

        // Set the document reference
        docRef = collRef.document(outerDocName);

        // Store the data
        Task<Void> task = null;
        if (innerCollName != null && innerDocName != null) {
            task = docRef.collection(innerCollName.name()).document(innerDocName).set(data);
        } else {
            task = docRef.set(data);
        }

        // Create a completable future for waiting until the transfer is complete
        CompletableFuture<Void> future = toCompletableFuture(task);

        // Catch any errors
        try {
            // Block until data is written
            future.get();
            Log.d("StoreinFirestore", "Document has been saved successfully");
        } catch (InterruptedException e) {
            // Handle the interrupted exception
            Thread.currentThread().interrupt();
            Log.e("StoreinFirestore", "Task was interrupted: " + e.getMessage());
        } catch (ExecutionException e) {
            // Handle any other exception
            Log.e("StoreinFirestore", "Error writing document: " + Objects.requireNonNull(e.getCause()).getMessage());
        }

        // Return the UID of the outer document
        return outerDocName;
    }

    /**
     * Saves an image to the database and creates a link to the image file if necessary
     *
     * @param attachedTo The UID/name of the document this image is linked to. Ex: USER-0000000000
     * @param imageUID   The UID/name of the image (will create new if null)
     * @param imageType  The type of image you are uploading (only used for events)
     * @param image      The image to upload
     * @return The UID of the image or null if there was an error
     */
    public String storeImageInFirebaseStorage(String attachedTo, String imageUID, ImageType imageType, Bitmap image) {
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
        UploadTask uploadTask = poolRef.child(imageType.name()).child(imageUID).putBytes(imageArray);

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
            return null;
        } catch (ExecutionException e) {
            // Handle any other exception
            Log.e("StoreinFirebaseStorage", "Error writing image: " + Objects.requireNonNull(e.getCause()).getMessage());
            return null;
        }

        // Image upload complete, if you are updating an image, you are done
        // No need to update the link to the image
        if (!newImage) {
            return imageUID;
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
                    case eventPosters:
                        imageAccess = new FirebaseAccess(FirestoreAccessType.IMAGES);
                        this.storeDataInFirestore(attachedTo, FirebaseInnerCollection.eventPosters, imageUID, newData);
                        break;
                    case eventQRCodes:
                        this.storeDataInFirestore(attachedTo, FirebaseInnerCollection.eventQRCodes, imageUID, newData);
                        imageAccess = new FirebaseAccess(FirestoreAccessType.QRCODES);
                        break;
                    case promoQRCodes:
                        this.storeDataInFirestore(attachedTo, FirebaseInnerCollection.promoQRCodes, imageUID, newData);
                        imageAccess = new FirebaseAccess(FirestoreAccessType.QRCODES);
                        break;
                }
                break;
            case USERS:
                Log.d("Testing", "WHY??");
                this.storeDataInFirestore(attachedTo, FirebaseInnerCollection.profilePictures, imageUID, newData);
                imageAccess = new FirebaseAccess(FirestoreAccessType.IMAGES);
                break;
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

        // Return the UID of the image
        return imageUID;


    }

    /**
     * Gets the data from a document and returns it as a map
     * Assumes there is only one document to return
     * Assumes the given document is an outer document
     *
     * @param docName The UID of the document in the outer collection.
     * @return The data in the document, or null if the document was not found
     */
    public Map<String, Object> getDataFromFirestore(String docName) {
        return this.getDataFromFirestore(docName, null, null);
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
        // Get the document reference for the outer/main collection
        docRef = collRef.document(outerDocName);

        // Get the document from firebase
        Task<DocumentSnapshot> task = null;
        if (innerCollName != null && innerDocName != null) {
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

    /**
     * Retrieves an image from Firebase Storage given the UID and the type of the image.
     * The maximum size of the image is 20MB
     * @param imageUID The UID/name of the stored image
     * @param imageType The type of image (Ex. ProfilePicture
     * @return The bitmap image
     */
    public Bitmap getImageFromFirebaseStorage(String imageUID, ImageType imageType) {
        // Create a reference to the correct storage pool
        StorageReference imageRef = poolRef.child(imageType.name()).child(imageUID);

        // Create a task to get the image as a byte array
        final long TWENTY_MEGABYTE = 20 * 1024 * 1024;
        Task<byte[]> task = imageRef.getBytes(TWENTY_MEGABYTE);

        // Convert the task to a completable future
        CompletableFuture<byte[]> future = toCompletableFuture(task);

        // Get the output
        byte[] imageArray = null;
        try {
            // Block until data is retrieved
            imageArray = future.get();
            Log.d("GetImageFromFirestore", "Data has been received");
        } catch (InterruptedException e) {
            // Handle the interrupted exception
            Thread.currentThread().interrupt();
            Log.e("GetImageFromFirestore", "Task was interrupted: " + e.getMessage());
            return null;
        } catch (ExecutionException e) {
            // Handle any other exception
            Log.e("GetImageFromFirestore", "Error retrieving image: " + Objects.requireNonNull(e.getCause()).getMessage());
            return null;
        }

        // Convert the image to a byte array
        if (imageArray != null) {
           return byteArraytoBitmap(imageArray);
        } else {
            return null;
        }
    }


    /**
     * Deletes a document from Firestore
     * Deletes all inner collections before deleting the outer document
     * Assumes the document UID entered is a outer document
     *
     * @param docName The UID of the outer document to delete
     */
    public void deleteDataFromFirestore(String docName) {
        this.deleteDataFromFirestore(docName, null, null);
    }

    /**
     * Deletes a document from Firestore.
     * Deletes all inner collections before deleting the outer document
     *
     * @param outerDocName  The UID of the document in the outer collection. If the other inputs are null, will delete this document
     * @param innerCollName The name of the inner collection. If this is null, will delete outer document
     * @param innerDocName  The UID of the document in the inner collection. If this is null, will delete outer document
     */
    public void deleteDataFromFirestore(String outerDocName, FirebaseInnerCollection innerCollName, String innerDocName) {
        // Get the document reference for the outer/main collection
        docRef = collRef.document(outerDocName);

        // If the document contains an inner collection, they need to be deleted first
        if (innerCollName == null || innerDocName == null) {
            // Get the list of inner collections
            ArrayList<FirebaseInnerCollection> innerCollList = new ArrayList<>();
            if (databaseType == FirestoreAccessType.EVENTS) {
                innerCollList.add(FirebaseInnerCollection.eventPosters);
                innerCollList.add(FirebaseInnerCollection.eventQRCodes);
                innerCollList.add(FirebaseInnerCollection.promoQRCodes);
                innerCollList.add(FirebaseInnerCollection.announcements);
            } else if (databaseType == FirestoreAccessType.USERS) {
                innerCollList.add(FirebaseInnerCollection.profilePictures);
            }

            // Delete all documents in the inner collection
            for (FirebaseInnerCollection currColl : innerCollList) {
                // Get the list of documents in the collection
                ArrayList<Map<String, Object>> allDocs = this.getAllDocumentsInCollection(outerDocName, currColl);

                // Go through the list and delete them all
                if (allDocs != null) {
                    for (Map<String, Object> currDoc : allDocs) {
                        deleteDocumentFromFirestore(docRef.collection(currColl.name()).document((String) Objects.requireNonNull(currDoc.get("UID"))));
                    }
                }
            }
        }

        // Delete the outer collection
        deleteDocumentFromFirestore(docRef);

    }

    /**
     * Deletes a single document from Firestore
     *
     * @param ref A reference to the document to delete
     */
    private void deleteDocumentFromFirestore(DocumentReference ref) {
        // Save the document id
        String docID = ref.getId();

        // Create the delete task
        Task<Void> task = null;
        task = ref.delete();

        // Convert the task to a CompletableFuture
        CompletableFuture<Void> future = toCompletableFuture(task);

        // Wait until delete is complete before returning
        try {
            // Wait for completion
            future.get();
            Log.d("DeleteFromFirestore", docID + "  has been deleted");
        } catch (InterruptedException e) {
            // Handle the interrupted exception
            Thread.currentThread().interrupt();
            Log.e("DeleteFromFirestore", "Task was interrupted: " + e.getMessage());
            return;
        } catch (ExecutionException e) {
            // Handle any other exception
            Log.e("DeleteFromFirestore", "Error deleting document: " + docID + " - " + Objects.requireNonNull(e.getCause()).getMessage());
            return;
        }
    }

    /**
     * Gets the list of the data of all documents in a collection
     *
     * @param outerDocName  The UID of the document containing the inner collection, if null gets the data from the main collection
     * @param innerCollName The name of the inner collection, if null gets the data from the main collection
     */
    public ArrayList<Map<String, Object>> getAllDocumentsInCollection(String outerDocName, FirebaseInnerCollection innerCollName) {
        // Create the get task
        Task<QuerySnapshot> task = null;

        // Get the documents from firebase
        if (innerCollName != null && outerDocName != null) {
            task = collRef.document(outerDocName).collection(innerCollName.name()).get();
        } else {
            task = collRef.get();
        }

        // Convert the task to a CompletableFuture
        CompletableFuture<QuerySnapshot> future = toCompletableFuture(task);

        // Get the output
        QuerySnapshot snapshot = null;
        try {
            // Block until data is retrieved
            snapshot = future.get();
            Log.d("GetAllFromFirestore", "Data has been received");
        } catch (InterruptedException e) {
            // Handle the interrupted exception
            Thread.currentThread().interrupt();
            Log.e("GetAllFromFirestore", "Task was interrupted: " + e.getMessage());
            return null;
        } catch (ExecutionException e) {
            // Handle any other exception
            Log.e("GetAllFromFirestore", "Error retrieving documents: " + Objects.requireNonNull(e.getCause()).getMessage());
            return null;
        }

        // Data was retrieved successfully
        ArrayList<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> docData = null;
        // If the data actually exists
        if (snapshot != null) {
            for (QueryDocumentSnapshot document : snapshot) {
                // Get the data from the document
                docData = document.getData();

                // Attach the UID
                docData.put("UID", document.getId());

                // Add to the array list
                data.add(docData);
            }
        } else {
            Log.e("GetAllFromFirestore", "The collection does not exist");
        }

        // Return the received data
        if (data.isEmpty()) {
            return null;
        } else {
            return data;
        }
    }

    /**
     * Deletes an image from Firebase Storage
     * The reference to the image is also removed from its origin
     * The image link is also deleted
     *
     * @param imageUID The UID of the image to delete
     * @param imageType The type of image (Ex. promoQRCCode)
     */
    public void deleteImageFromFirebaseStorage(String imageUID, ImageType imageType){
        /*
            Delete the image from Firebase Storage
         */
        // Get a reference to the correct image
        StorageReference imageRef = poolRef.child(imageType.name()).child(imageUID);

        // Create a task to delete the image
        Task<Void> task = imageRef.delete();

        // Convert the task to a CompletableFuture
        CompletableFuture<Void> future = toCompletableFuture(task);

        /*
            Delete image links
         */

        // Get a database link to the correct database
        FirebaseAccess database = null;
        switch (imageType){
            case eventPosters:
            case profilePictures:
                database = new FirebaseAccess(FirestoreAccessType.IMAGES);
                break;
            case eventQRCodes:
            case promoQRCodes:
                database = new FirebaseAccess(FirestoreAccessType.QRCODES);
                break;
        }

        // Get the data for the image
        Map<String, Object> data = database.getDataFromFirestore(imageUID);

        // Delete the document linking the origin to the link
        this.deleteDataFromFirestore((String) data.get("origin"), FirebaseInnerCollection.valueOf(imageType.name()), imageUID);

        // Delete the link
        database.deleteDataFromFirestore((String) data.get("UID"));


        // Wait until the delete operation is complete
        try {
            // Block until data is retrieved
            future.get();
            Log.d("GetAllFromFirestore", "Data has been received");
        } catch (InterruptedException e) {
            // Handle the interrupted exception
            Thread.currentThread().interrupt();
            Log.e("GetAllFromFirestore", "Task was interrupted: " + e.getMessage());
            return;
        } catch (ExecutionException e) {
            // Handle any other exception
            Log.e("GetAllFromFirestore", "Error retrieving documents: " + Objects.requireNonNull(e.getCause()).getMessage());
            return;
        }

    }


}
