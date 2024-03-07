package com.oopsipushedtomain;

import android.os.Bundle;
import android.util.Log;

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
    private RecyclerView profilesRecyclerView; // RecyclerView to display profiles
    private ProfileListAdapter profileAdapter; // Adapter for profiles
    private List<Profile> profileList = new ArrayList<>(); // List to store profiles

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_list); // Layout for activity

        profilesRecyclerView = findViewById(R.id.profilesRecyclerView); // Initialize RecyclerView
        profilesRecyclerView.setLayoutManager(new LinearLayoutManager(this)); // Set layout manager

        // Initialize adapter and set it to RecyclerView
        profileAdapter = new ProfileListAdapter(this, profileList);
        profilesRecyclerView.setAdapter(profileAdapter);

        fetchProfiles(); // Fetch profiles from database
    }

    /**
     * Method to fetch profiles from the database.
     * Uses FirebaseFirestore to query the "users" collection.
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

                    // Convert phone number from Long to String
                    Long phoneLong = document.getLong("phone");
                    profile.setPhone(phoneLong != null ? phoneLong.toString() : null);

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
