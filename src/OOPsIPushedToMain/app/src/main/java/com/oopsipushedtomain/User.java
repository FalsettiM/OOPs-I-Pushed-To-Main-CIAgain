/*
 Example Use:
     user = new User("USER-9DRH1BAQZQMGZJEZFMGL", new User.DataLoadedListener() {
        @Override
        public void onDataLoaded() {
            AfterDone();
        }
    });
 */

package com.oopsipushedtomain;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.oopsipushedtomain.ProfileActivity;

import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class defines and represents a user
 * It also includes all database accesses for user functions
 *
 * @author Matteo Falsetti
 * @version 1.0
 * @see Event
 * @see ProfileActivity
 */
public class User {

    boolean dataLoaded = false;
    CountDownLatch latch;

    // User parameters
    private String uid;
    private String address = "Hello";
    private Date birthday = null;
    private String email = null;
    private String homepage = null;
    private String name = null;
    private String nickname = null;
    private String phone = null;

    private String imageUID = null;

    // Annoucements
    


    // Database parameters
    private FirebaseFirestore db;
    private CollectionReference userRef;
    private DocumentReference userDocRef;
    private CollectionReference userEventRef;


    // Firebase storage
    private FirebaseStorage storage;
    private StorageReference storageRef;

    /**
     * Interface for checking when data is loaded into the user
     */
    public interface DataLoadedListener {
        void onDataLoaded();
    }

    /**
     * Interface for checking when the image is loaded from the database
     */
    public interface OnBitmapReceivedListener {
        void onBitmapReceived(Bitmap bitmap);
    }

    /**
     * Initializes the database parameters for accessing firestore
     */
    public void InitDatabase() {
        db = FirebaseFirestore.getInstance();
        userRef = db.collection("users");
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }


    /**
     * Generates a new user and uploads them to the database
     * Instantiates all parameters to null. They need to be set later
     */
    public User() {
        // Initialize the database
        InitDatabase();

        // Get the user id of a new document
        uid = userRef.document().getId().toUpperCase();
        uid = "USER-" + uid;

        // Get the UID for an image
        imageUID = userRef.document().getId().toUpperCase();
        imageUID = "IMGE-" + imageUID;

        // Create a hash map for all string variables and set all fields to null
        HashMap<String, Object> data = new HashMap<>();
        data.put("address", address);
        data.put("birthday", birthday);
        data.put("email", email);
        data.put("homepage", homepage);
        data.put("name", name);
        data.put("nickname", nickname);
        data.put("phone", phone);
        data.put("profileImage", imageUID);


        // Create a new document and set all parameters
        userRef.document(uid).set(data);

        // Set the document reference to this document
        userDocRef = userRef.document(uid);

        // Create the inner collection for events
        userEventRef = userDocRef.collection("events");

        // Create empty data to force creation of the document
        HashMap<String, String> emptyData = new HashMap<>();

        // Add the documents
        userEventRef.document("checkedin").set(emptyData);
        userEventRef.document("created").set(emptyData);
        userEventRef.document("signedup").set(emptyData);
    }

    /**
     * Creates an instance of the new user class given a UID
     * @param userID The UID of the user
     */
    public User(String userID, DataLoadedListener listener) {
        // Set data loaded to false
        dataLoaded = false;

        // Initialize database
        InitDatabase();

        // Set the document ID
        uid = userID;
        userDocRef = userRef.document(uid);

        // Set the events document id
        userEventRef = userDocRef.collection("events");

        // Set the fields of the class
        new Thread(new Runnable() {
            @Override
            public void run() {
                UpdateAllDataFields(listener);;
            }
        }).start();

    }



    /**
     * Updates all fields in the class
     * Needs to be called before getting any data
     */
    public void UpdateAllDataFields(DataLoadedListener listener) {
        // Get the data in the document
        userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Get the data from the query - there should only be 1 document
                        Map<String, Object> data = document.getData();

                        // Update the object
                        assert data != null;
                        address = (String) data.get("address");
                        birthday = ((Timestamp) Objects.requireNonNull(data.get("birthday"))).toDate();
                        email = (String) data.get("email");
                        homepage = (String) data.get("homepage");
                        name = (String) data.get("name");
                        nickname = (String) data.get("nickname");
                        phone = (String) data.get("phone");
                        imageUID = (String) data.get("profileImage");

                        Log.d("Firebase", "Data Loaded");

                        listener.onDataLoaded();

                    } else {
                        Log.d("Firebase", "No such document");
                    }
                } else {
                    Log.d("Firebase Failure",  "get failed with ", task.getException());
                }
            }
        });
    }


    /**
     * Updates the user's address
     *
     * @param address The address of the user
     */
    public void setAddress(String address) {
        // Update in the class
        this.address = address;

        // Update in database
        HashMap<String, Object> data = new HashMap<>();
        data.put("address", this.address);
        userDocRef.update(data);
    }

    /**
     * Updates the user's birthday
     *
     * @param birthday The birthday of the user
     */
    public void setBirthday(Date birthday) {
        // Update in the class
        this.birthday = birthday;

        // Update in database
        HashMap<String, Object> data = new HashMap<>();
        data.put("birthday", this.birthday);
        userDocRef.update(data);
    }

    /**
     * Updates the user's email
     *
     * @param email The email of the user
     */
    public void setEmail(String email) {
        // Update in the class
        this.email = email;

        // Update in database
        HashMap<String, Object> data = new HashMap<>();
        data.put("email", this.email);
        userDocRef.update(data);
    }

    /**
     * Updates the user's homepage
     *
     * @param homepage the homepage of the user
     */
    public void setHomepage(String homepage) {
        // Update in the class
        this.homepage = homepage;

        // Update in database
        HashMap<String, Object> data = new HashMap<>();
        data.put("homepage", this.homepage);
        userDocRef.update(data);
    }

    /**
     * Updates the user's name
     *
     * @param name The name of the user
     */
    public void setName(String name) {
        // Update in the class
        this.name = name;

        // Update in database
        HashMap<String, Object> data = new HashMap<>();
        data.put("name", this.name);
        userDocRef.update(data);
    }

    /**
     * Updates the user's nickname
     *
     * @param nickname The nickname of the user
     */
    public void setNickname(String nickname) {
        // Update in the class
        this.nickname = nickname;

        // Update in database
        HashMap<String, Object> data = new HashMap<>();
        data.put("nickname", this.nickname);
        userDocRef.update(data);
    }

    /**
     * Updates the user's phone number
     *
     * @param phone The phone number of the user
     */
    public void setPhone(String phone) {
        // Update in the class
        this.phone = phone;

        // Update in database
        HashMap<String, Object> data = new HashMap<>();
        data.put("phone", this.phone);
        userDocRef.update(data);
    }


    // ChatGPT: How can you upload an image to firebase?
    public void setProfileImage(Bitmap profileImage) {
        // Update in the class
        this.profileImage = profileImage;

        // Convert the bitmap to PNG for upload
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        profileImage.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        // Upload the image to firebase
        UploadTask uploadTask = storageRef.child(imageUID).putBytes(data);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            Log.d("Firebase Storage", "Image upload successful");
        }).addOnFailureListener(exception -> {
            Log.d("Firebase Storage", "Image upload failed");
        });

    }

    /**
     * Gets the UID for the user
     *
     * @return The UID of the user
     */
    public String getUid() {
//        this.UpdateAllDataFields();
        return uid;
    }

    /**
     * Gets the address of the user
     *
     * @return The address of the user
     */
    public String getAddress() {
//        this.UpdateAllDataFields();
        return address;
    }

    /**
     * Gets the birthday of the user
     *
     * @return The birthday of the user
     */
    public Date getBirthday() {
//        this.UpdateAllDataFields();
        return birthday;
    }

    /**
     * Gets the email of the user
     *
     * @return The email of the user
     */
    public String getEmail() {
//        this.UpdateAllDataFields();
        return email;
    }

    /**
     * Gets the homepage of the user
     *
     * @return The homepage of the user
     */
    public String getHomepage() {
//        this.UpdateAllDataFields();
        return homepage;
    }

    /**
     * Gets the name of the user
     *
     * @return The name of the user
     */
    public String getName() {
//        this.UpdateAllDataFields();
        return name;
    }

    /**
     * Gets the nickname of the user
     *
     * @return The nickname of the user
     */
    public String getNickname() {
//        this.UpdateAllDataFields();
        return nickname;
    }

    /**
     * Gets the phone number of the user
     *
     * @return The phone number of the user
     */
    public String getPhone() {
//        this.UpdateAllDataFields();
        return phone;
    }

    // ChatGPT: Now i want to do the reverse and load the image and convert it back to a bitmap
    public void getProfileImage(OnBitmapReceivedListener listener) {
        StorageReference profileImageRef = storageRef.child(imageUID);

        // Down load the image
        final long ONE_MEGABYTE = 1024*1024;
        profileImageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
            // Convert to a bitmap
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            Log.d("Firebase Storage", "Image successfully loaded");
            listener.onBitmapReceived(bitmap);
        });
    }



    /**
     * Checks a user into the specified event.
     * It will create a new entry if it does not exist already
     *
     * @param eventID The UID of the event
     */
    public void checkIn(String eventID) {
        // Check to see if the user has checked in before
        DocumentReference checkInRef = userEventRef.document("checkedin");
        checkInRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();
                // Check if document exists
                if (document.exists()) {
                    // Check if this event is already there, if it is, increment that count
                    if (document.get(eventID) != null) {
                        // Get the field in the map
                        String countField = eventID + ".count";

                        // Update the count
                        checkInRef.update(
                                countField, FieldValue.increment(1)
                        );

                    } else {
                        // Event does not already exist, make a new event
                        HashMap<String, Object> data = new HashMap<>();
                        HashMap<String, Object> internalMap = new HashMap<>();

                        // Add the data to the internal map
                        internalMap.put("count", 1);
                        internalMap.put("date-time", new Date());
                        internalMap.put("location", new GeoPoint(0, 0));

                        // Add the map to the document
                        data.put(eventID, internalMap);
                        checkInRef.update(data);
                    }

                } else {
                    Log.d("Firebase Check In", "No such document");
                }

            }
        });

    }
}
