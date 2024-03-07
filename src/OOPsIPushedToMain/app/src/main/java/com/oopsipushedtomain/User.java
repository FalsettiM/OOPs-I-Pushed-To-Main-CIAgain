package com.oopsipushedtomain;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.AggregateQuery;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;

public class User {

    // User parameters
    private String uid;
    private String address = null;
    private Date birthday = null;
    private String email = null;
    private String homepage = null;
    private String name = null;
    private String nickname = null;
    private String phone = null;


    // Database parameters
    private FirebaseFirestore db;
    private CollectionReference userRef;
    private DocumentReference userDocRef;
    private CollectionReference userEventRef;

    /**
     * Initializes the database parameters for accessing firestore
     */
    public void InitDatabase(){
        db = FirebaseFirestore.getInstance();
        userRef = db.collection("users");
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

        // Create a hash map for all string variables and set all fields to null
        HashMap<String, String> data = new HashMap<>();
        data.put("address", address);
        data.put("birthday", null);
        data.put("email", email);
        data.put("homepage", homepage);
        data.put("name", name);
        data.put("nickname", nickname);
        data.put("phone", phone);


        // Create a new document and set all parameters
        userRef.document(uid).set(data);

        // Set the document reference to this document
        userDocRef = userRef.document(uid);

        // Create the inner collection for events
        userEventRef =  userDocRef.collection("events");

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
    public User(String userID) {
        // Initialize database
        InitDatabase();

        // Set the document ID
        uid = userID;
        userDocRef = userRef.document(uid);

        // Set the fields of the class
        UpdateAllDataFields();

        // Set the events document id
        userEventRef = userDocRef.collection("events");

    }

    /**
     * Updates all fields in the class
     * Needs to be called before getting any data
     */
    public void UpdateAllDataFields() {
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

                    } else {
                        Log.d("Firebase", "No such document");
                    }
                } else {
                    Log.d("Firebase Failure",  "get failed with ", task.getException());
                }
            }
        });
    }


    public String getUid() {
        this.UpdateAllDataFields();
        return uid;
    }

    public String getAddress() {
        this.UpdateAllDataFields();
        return address;
    }

    public Date getBirthday() {
        this.UpdateAllDataFields();
        return birthday;
    }

    public String getEmail() {
        this.UpdateAllDataFields();
        return email;
    }

    public String getHomepage() {
        this.UpdateAllDataFields();
        return homepage;
    }

    public String getName() {
        this.UpdateAllDataFields();
        return name;
    }

    public String getNickname() {
        this.UpdateAllDataFields();
        return nickname;
    }

    public String getPhone() {
        this.UpdateAllDataFields();
        return phone;
    }

    /**
     * Checks a user into the specified event
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
                        internalMap.put("location", new GeoPoint(0,0));

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
