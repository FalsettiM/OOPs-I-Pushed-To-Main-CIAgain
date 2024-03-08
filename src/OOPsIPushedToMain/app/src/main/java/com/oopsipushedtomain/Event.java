package com.oopsipushedtomain;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an event within the application.
 * This class is used to model events, including their details such as title, start and end times,
 * description, location, poster URL, QR code data, and attendee limit. It implements Serializable
 * to allow event instances to be passed between activities or components.
 *
 * Outstanding issues: None known at this time.
 */

public class Event implements Serializable {
    private String eventId;
    private String title;
    private String startTime;
    private String endTime;
    private String description;
    private String location; // Optional
    private String posterUrl;
    private int attendeeLimit; // Optional

    // Database parameters
    private FirebaseFirestore db;
    private CollectionReference eventRef;
    private DocumentReference eventDocRef;

    // Firebase storage
    private FirebaseStorage storage;
    private StorageReference storageRef;

    /**
     * Constructs a new Event instance.
     *
     * @param title The title of the event.
     * @param startTime The start time of the event.
     * @param endTime The end time of the event.
     * @param description A description of the event.
     * @param location The location of the event.
     * @param posterUrl The URL to an image for the event.
     * @param attendeeLimit The maximum number of attendees for the event. Use 0 or a negative number to indicate no limit.
     */
    public Event(String title, String startTime, String endTime, String description, String location, String posterUrl, int attendeeLimit) {
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
        this.location = location; // Optional
        this.posterUrl = posterUrl;
        this.attendeeLimit = attendeeLimit; // Optional
    }
    public Event(String eventId, String title, String startTime, String endTime, String description, String location, String posterUrl, int attendeeLimit) {
        this.eventId = eventId;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
        this.location = location; // Optional
        this.posterUrl = posterUrl;
        this.attendeeLimit = attendeeLimit; // Optional
    }

    /**
     * No-argument constructor so that Event can be deserialized
     */
    public Event() {
    }

    private void InitDatabase() {
        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("events");
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        eventId = eventRef.document().getId().toUpperCase();
        eventId = "EVNT-" + eventId;
    }

    /**
     * Adds the current object to the database
     */
    public void addEventToDatabase() {
        InitDatabase();
        generateQRcodeData();

        Map<String, Object> event = new HashMap<>();
        event.put("title", title);
        event.put("startTime", startTime);
        event.put("endTime", endTime);
        event.put("description", description);
        event.put("location", location);
        event.put("posterUrl", posterUrl);
        event.put("attendeeLimit", attendeeLimit);

        db.collection("events").document(eventId).set(event)
                .addOnSuccessListener(aVoid -> {
                    // Successfully added/updated event with specific ID
                    System.out.println("Event successfully added/updated with ID: " + eventId);
                })
                .addOnFailureListener(e -> {
                    // Failed to add/update event
                    System.err.println("Error adding/updating event: " + e.getMessage());
                });
    }


    private void generateQRcodeData() {
        QRCode qrCode = new QRCode(eventId);
    }

    /**
     * Gets the unique identifier for the event.
     * @return the event's unique identifier
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * Sets the unique identifier for the event.
     * @param eventId the unique identifier to set
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * Gets the title of the event.
     * @return the event's title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the event.
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the start time of the event.
     * @return the event's start time
     */
    public String getStartTime() {
        return startTime;
    }

    /**
     * Sets the start time of the event.
     * @param startTime the start time to set
     */
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    /**
     * Gets the end time of the event.
     * @return the event's end time
     */
    public String getEndTime() {
        return endTime;
    }

    /**
     * Sets the end time of the event.
     * @param endTime the end time to set
     */
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    /**
     * Gets the description of the event.
     * @return the event's description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the event.
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the location of the event. This field is optional.
     * @return the event's location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the location of the event. This field is optional.
     * @param location the location to set
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Gets the URL of the event's poster.
     * @return the URL of the event's poster
     */
    public String getPosterUrl() {
        return posterUrl;
    }

    /**
     * Sets the URL of the event's poster.
     * @param posterUrl the URL to set
     */
    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    /**
     * Gets the limit of attendees for the event. This field is optional.
     * @return the attendee limit
     */
    public int getAttendeeLimit() {
        return attendeeLimit;
    }

    /**
     * Sets the limit of attendees for the event. This field is optional.
     * @param attendeeLimit the attendee limit to set
     */
    public void setAttendeeLimit(int attendeeLimit) {
        this.attendeeLimit = attendeeLimit;
    }
}
