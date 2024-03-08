package com.oopsipushedtomain;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.FirebaseAuthCredentialsProvider;
import com.google.firebase.firestore.auth.User;
import com.oopsipushedtomain.Announcements.AnnouncementListActivity;
import com.oopsipushedtomain.Announcements.SendAnnouncementActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * EventDetailsActivityOrganizer allows organizers to view and edit details of an event.
 * It provides editable fields for the event's title, start time, end time, and description,
 * as well as an image view for the event's poster. Organizers can update event details,
 * which are intended to be saved to a persistent storage or backend upon confirmation.
 *
 * This activity also offers options to send notifications, view or limit attendees, view event QR code,
 * and delete the event. Each action is represented by a button, with placeholders for their functionalities.
 *
 * Outstanding issues:
 * 1. The implementation of dynamic image loading for the event poster is missing.
 * 2. The validation logic in validateInput() may need to be expanded based on additional requirements
 *    for the event details (e.g., date format validation).
 * 3. The saveEventDetails() method currently updates the EditText fields without performing any actual
 *    data persistence. Saving the updated event details to a database or backend service is required.
 * 4. The placeholder methods for sending notifications, viewing/limiting attendees, viewing event QR code,
 *    and deleting the event need to be fully implemented.
 */

public class EventDetailsActivity extends AppCompatActivity {
    // TODO: Change logic to hide buttons depending on user type
    private EditText eventTitleEdit;
    private EditText eventStartTimeEdit;
    private EditText eventEndTimeEdit;
    private EditText eventDescriptionEdit;
    private ImageView eventPosterEdit;
    private Button eventSaveButton, sendNotificationButton, viewAnnouncementsButton, signUpButton, viewLimitAttendeeButton, deleteButton, viewEventQRCodeButton;

    private String currentUserUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        eventTitleEdit = findViewById(R.id.event_details_organizer_title_e);
        eventStartTimeEdit = findViewById(R.id.event_details_organizer_start_time_e);
        eventEndTimeEdit = findViewById(R.id.event_details_organizer_end_time_e);
        eventDescriptionEdit = findViewById(R.id.event_details_organizer_description_e);
        eventPosterEdit = findViewById(R.id.eventPosterImageViewEdit);
        eventSaveButton = findViewById(R.id.btnSaveEventDetails);
        sendNotificationButton = findViewById(R.id.btnSendNotification);
        viewAnnouncementsButton = findViewById(R.id.btnViewAnnouncements);
        signUpButton = findViewById(R.id.btnSignUpEvent);
        viewLimitAttendeeButton = findViewById(R.id.btnViewLimitAttendees);
        deleteButton = findViewById(R.id.btnDeleteEvent);
        viewEventQRCodeButton = findViewById(R.id.btnViewEventQRCode);
        currentUserUID = CustomFirebaseAuth.getInstance().getCurrentUserID();

        eventStartTimeEdit.setOnClickListener(v -> showDateTimePicker(eventStartTimeEdit));
        eventEndTimeEdit.setOnClickListener(v -> showDateTimePicker(eventEndTimeEdit));



        Event event = (Event) getIntent().getSerializableExtra("selectedEvent");
        if (event != null) {
            // Set the text for the TextViews with event details
            eventTitleEdit.setText(event.getTitle());
            eventStartTimeEdit.setText(event.getStartTime());
            eventEndTimeEdit.setText(event.getEndTime());
            eventDescriptionEdit.setText(event.getDescription());
            String eventId = event.getEventId();
            determineUserRole(currentUserUID, event.getEventId(), this::updateUIForRole);
        }



        eventPosterEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Intent to start a new activity
                //Intent intent = new Intent(EventDetailsActivityOrganizer.this, ImageDetailsActivity.class);
                //startActivity(intent);
            }
        });

        // Set OnClickListener for the save button
        eventSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateInput()){
                    saveEventDetails();
                    event.setTitle(eventTitleEdit.getText().toString());
                    event.setStartTime(eventStartTimeEdit.getText().toString());
                    event.setEndTime(eventEndTimeEdit.getText().toString());
                    event.setDescription(eventDescriptionEdit.getText().toString());
                    Intent intent = new Intent(EventDetailsActivity.this, EventListActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Clears the back stack
                    startActivity(intent);
                }
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpForEvent(event.getEventId());
            }
        });

        sendNotificationButton.setOnClickListener(v -> {
            Intent intent = new Intent(EventDetailsActivity.this, SendAnnouncementActivity.class);
            intent.putExtra("eventId", event.getEventId());
            startActivity(intent);
        });

        viewAnnouncementsButton.setOnClickListener(v -> {
            Intent intent = new Intent(EventDetailsActivity.this, AnnouncementListActivity.class);
            Log.e("adsfhk", event.getEventId());
            intent.putExtra("eventId", event.getEventId());
            startActivity(intent);
        });

        viewLimitAttendeeButton.setOnClickListener(v -> {
            //Intent intent = new Intent(this, ViewLimitAttendeesActivity.class);
            //startActivity(intent);
        });

        viewEventQRCodeButton.setOnClickListener(v -> {
            //Intent intent = new Intent(this, ViewEventQRCodeActivity.class);
            //startActivity(intent);
        });

        deleteButton.setOnClickListener(v -> {
            final String eventId = getIntent().getStringExtra("eventId");
            // Call deleteEvent with the eventId
            if (eventId != null) {
                deleteEvent(eventId);
            } else {
                Toast.makeText(this, "Event ID is not available.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void saveEventDetails() {

        String title = eventTitleEdit.getText().toString();
        String startTime = eventStartTimeEdit.getText().toString();
        String endTime = eventEndTimeEdit.getText().toString();
        String description = eventDescriptionEdit.getText().toString();

        eventTitleEdit.setText(title);
        eventStartTimeEdit.setText(startTime);
        eventEndTimeEdit.setText(endTime);
        eventDescriptionEdit.setText(description);

    }

    private void showDateTimePicker(final EditText editText) {
        Calendar currentDate = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar time = Calendar.getInstance();
            new TimePickerDialog(this, (view1, hourOfDay, minute) -> {
                time.set(Calendar.YEAR, year);
                time.set(Calendar.MONTH, month);
                time.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                time.set(Calendar.HOUR_OF_DAY, hourOfDay);
                time.set(Calendar.MINUTE, minute);
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                editText.setText(dateFormat.format(time.getTime()));
            }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), false).show();
        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE)).show();
    }

    private boolean validateInput() {
        if (eventTitleEdit.getText().toString().trim().isEmpty() ||
                eventStartTimeEdit.getText().toString().trim().isEmpty() ||
                eventEndTimeEdit.getText().toString().trim().isEmpty() ||
                eventDescriptionEdit.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return false;
        }
        // Additional validation logic can go here
        return true;
    }

    private void deleteEvent(String eventId) { // eventId passed as a parameter
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").document(eventId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EventDetailsActivity.this, "Event deleted successfully", Toast.LENGTH_SHORT).show();
                    // Intent to navigate back or simply finish this activity
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(EventDetailsActivity.this, "Error deleting event", Toast.LENGTH_SHORT).show());
    }

    private void signUpForEvent(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference eventRef = db.collection("events").document(eventId);

        eventRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Event event = document.toObject(Event.class);
                    if (event != null && event.getSignedUpAttendees().size() < event.getAttendeeLimit()) {
                        // Proceed with signing up the user
                        eventRef.update("signedUpAttendees", FieldValue.arrayUnion(currentUserUID))
                                .addOnSuccessListener(aVoid -> Toast.makeText(EventDetailsActivity.this, "Signed up successfully", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(EventDetailsActivity.this, "Sign up failed, limit exceeded", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(EventDetailsActivity.this, "Event is full", Toast.LENGTH_SHORT).show();
                    }
                }
            }

        });
    }

    public enum UserRole {
        ORGANIZER, ATTENDEE_NOT_SIGNED_UP, ATTENDEE_SIGNED_UP
    }

    public void determineUserRole(String userId, String eventId, final UserRoleCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference createdRef = db.collection("users").document(userId).collection("created").document(eventId);
        DocumentReference signedUpRef = db.collection("users").document(userId).collection("signedup").document(eventId);

        createdRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                callback.onRoleDetermined(UserRole.ORGANIZER);
            } else {
                // Not an organizer, check if attendee
                signedUpRef.get().addOnCompleteListener(task2 -> {
                    if (task2.isSuccessful() && task2.getResult() != null && task2.getResult().exists()) {
                        callback.onRoleDetermined(UserRole.ATTENDEE_SIGNED_UP);
                    } else {
                        callback.onRoleDetermined(UserRole.ATTENDEE_NOT_SIGNED_UP);
                    }
                });
            }
        });
    }

    public interface UserRoleCallback {
        void onRoleDetermined(UserRole role);
    }

    private void updateUIForRole(UserRole role) {
        switch (role) {
            case ORGANIZER:
                // Organizer should see all buttons except sign up button
                eventSaveButton.setVisibility(View.VISIBLE);
                sendNotificationButton.setVisibility(View.VISIBLE);
                viewAnnouncementsButton.setVisibility(View.VISIBLE);
                signUpButton.setVisibility(View.GONE);
                deleteButton.setVisibility(View.VISIBLE);
                viewLimitAttendeeButton.setVisibility(View.VISIBLE);
                viewEventQRCodeButton.setVisibility(View.VISIBLE);
                break;
            case ATTENDEE_SIGNED_UP:
                // Signed-up attendee should not see any button
                eventSaveButton.setVisibility(View.GONE);
                sendNotificationButton.setVisibility(View.GONE);
                viewAnnouncementsButton.setVisibility(View.GONE);
                signUpButton.setVisibility(View.GONE);
                deleteButton.setVisibility(View.GONE);
                viewLimitAttendeeButton.setVisibility(View.GONE);
                viewEventQRCodeButton.setVisibility(View.GONE);
                break;
            case ATTENDEE_NOT_SIGNED_UP:
                // Unsigned-up attendee should see the sign up button
                eventSaveButton.setVisibility(View.GONE);
                sendNotificationButton.setVisibility(View.GONE);
                viewAnnouncementsButton.setVisibility(View.GONE);
                signUpButton.setVisibility(View.VISIBLE);
                deleteButton.setVisibility(View.GONE);
                viewLimitAttendeeButton.setVisibility(View.GONE);
                viewEventQRCodeButton.setVisibility(View.GONE);
                break;
        }
    }





}