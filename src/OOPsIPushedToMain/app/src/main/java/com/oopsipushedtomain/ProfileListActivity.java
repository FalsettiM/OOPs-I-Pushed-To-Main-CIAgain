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

public class ProfileListActivity extends AppCompatActivity {
    private RecyclerView profilesRecyclerView;
    private ProfileListAdapter profileAdapter;
    private List<Profile> profileList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_list); // Make sure you have this layout defined

        profilesRecyclerView = findViewById(R.id.profilesRecyclerView); // Make sure you have a RecyclerView in your layout with this ID
        profilesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Here you would fetch your profiles from the database or any data source
        // For now, let's assume profileList is populated

        profileAdapter = new ProfileListAdapter(this, profileList);
        profilesRecyclerView.setAdapter(profileAdapter);

        fetchProfiles();
    }

    // You might want to have a method to fetch profiles from your data source
    private void fetchProfiles() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                profileList.clear(); // Clear existing data
                for (DocumentSnapshot document : task.getResult()) {
                    Profile profile = new Profile();
                    profile.setUserId(document.getId()); // Assuming the document ID is the user ID
                    profile.setName(document.getString("name"));
                    profile.setNickname(document.getString("nickname"));
                    // Convert the birthday from Timestamp to String
                    Timestamp birthdayTimestamp = document.getTimestamp("birthday");
                    if (birthdayTimestamp != null) {
                        Date birthdayDate = birthdayTimestamp.toDate();
                        // Format the date as a String. Customize the format as needed.
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        profile.setBirthday(sdf.format(birthdayDate));
                    } else {
                        profile.setBirthday(null);
                    }
                    profile.setHomepage(document.getString("homepage"));
                    profile.setAddress(document.getString("address"));
                    // Convert the phone number from Long to String
                    Long phoneLong = document.getLong("phone");
                    profile.setPhone(phoneLong != null ? phoneLong.toString() : null);
                    profile.setEmail(document.getString("email"));

                    profileList.add(profile);
                }
                profileAdapter.notifyDataSetChanged(); // Refresh the adapter with new data
            } else {
                Log.d("ProfileListActivity", "Error getting documents: ", task.getException());
            }
        });
    }

}
