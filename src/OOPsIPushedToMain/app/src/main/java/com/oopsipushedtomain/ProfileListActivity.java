package com.oopsipushedtomain;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * ProfileListActivity is responsible for displaying a list of user profiles retrieved from a database.
 * It utilizes a RecyclerView to display the profiles using ProfileListAdapter.
 */
public class ProfileListActivity extends AppCompatActivity {
    /**
     * RecyclerView to display profiles
     */
    RecyclerView profilesRecyclerView;
    /**
     * Adapter for profiles
     */
    ProfileListAdapter profileAdapter;
    /**
     * List to store profiles
     */
    private List<Profile> profileList = new ArrayList<>();

    /**
     * Initializes the parameters of the class
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_list); // Layout for activity

        profilesRecyclerView = findViewById(R.id.profilesRecyclerView); // Initialize RecyclerView
        profilesRecyclerView.setLayoutManager(new LinearLayoutManager(this)); // Set layout manager

        // Initialize adapter and set it to RecyclerView
        profileAdapter = new ProfileListAdapter(this, profileList, new OnItemClickListener() {
            @Override
            public void onItemClick(Profile profile) {
                showDeleteConfirmationDialog(profile);
            }
        });

        profilesRecyclerView.setAdapter(profileAdapter); // Don't forget to set the adapter to the RecyclerView

        fetchProfiles(); // Fetch profiles from Firestore and populate the RecyclerView
    }

    /**
     * Shows a confirmation dialog for deleting a profile
     * @param profile The profile selected
     */
    private void showDeleteConfirmationDialog(Profile profile) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Profile")
                .setMessage("Are you sure you want to delete this profile?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    deleteProfile(profile);
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    /**
     * Deletes a profile on the database
     * @param profile The profile to delete
     */
    private void deleteProfile(Profile profile) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Toast Toast = null;
        db.collection("users").document(profile.getUserId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Handle successful deletion
                    fetchProfiles(); // Refresh the list after deletion
                    Toast.makeText(ProfileListActivity.this, "Profile deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Toast.makeText(ProfileListActivity.this, "Error deleting profile", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Method to fetch profiles from the database.
     * Uses Firebase-Firestore to query the "users" collection.
     */
    private void fetchProfiles() {
        FirebaseFirestore db = FirebaseFirestore.getInstance(); // Get instance of Firestore database
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                profileList.clear(); // Clear existing profile list
                for (DocumentSnapshot document : task.getResult()) {
                    Profile profile = new Profile();
                    profile.setUserId(document.getId()); // Set user ID

                    // Set profile details from document fields
                    profile.setName(document.getString("name"));
                    profile.setNickname(document.getString("nickname"));

                    // Convert birthday from Timestamp to String
                    Timestamp birthdayTimestamp = document.getTimestamp("birthday");
                    if (birthdayTimestamp != null) {
                        Date birthdayDate = birthdayTimestamp.toDate();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        profile.setBirthday(sdf.format(birthdayDate)); // Format birthday as string
                    } else {
                        profile.setBirthday(null);
                    }

                    profile.setHomepage(document.getString("homepage"));
                    profile.setAddress(document.getString("address"));
                    profile.setPhone(document.getString("phone"));

                    profile.setEmail(document.getString("email"));

                    profileList.add(profile); // Add profile to list
                }
                profileAdapter.notifyDataSetChanged(); // Notify adapter of data change
            } else {
                Log.d("ProfileListActivity", "Error getting documents: ", task.getException());
            }
        });
    }
}
