package com.oopsipushedtomain;

import android.content.Intent;
import android.icu.util.TimeUnit;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.installations.FirebaseInstallations;

import java.util.HashMap;
import java.util.Map;

/**
 * The main activity the app launches to
 * It checks if the user has a profile then automatically moves to the profile page
 */
public class MainActivity extends AppCompatActivity {
    Button button;
    private FirebaseFirestore db;
    private CollectionReference usersRef;
    private Boolean foundFID = false;  // Flag used to check if the FID exists in the database
    private User user;

    /**
     * Sets the on click listener for the button on the page
     * The button will open the profile page with a set user id
     * It also intiializes the database parameters
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.hello_button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        // Initialize database reference to Announcements collection
        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("users");

        // Obtain FID and work with it
        getUserFID();
    }

    /**
     * Obtain this installation's Firebase Installation ID (unique to each installation).
     * Query the 'fid' field in the 'users' collection in the database, if this FID already
     * exists in the database, proceed as usual. If it does not already exist, create a new
     * 'users' document and store the FID in the 'fid' field.
     */
    private void getUserFID() {
        // Get the FID using the helper function
        GetFIDUtil.GetFID(new GetFIDUtil.DataLoadedListener() {
            @Override
            public void onDataLoaded(String fid) {
                Log.d("FID", "Received FID from helper: " + fid);
                findUser(fid);
            }
        });
    }

    /**
     * Finds the user given the fid
     *
     * @param fid The fid for this user
     */
    void findUser(String fid) {
        // Get all documents in 'users' where the 'fid' field is equal to the current FID
        usersRef.whereEqualTo("fid", fid).get().addOnCompleteListener(getUserTask -> {
            String userId = "";
            if (getUserTask.isSuccessful()) {
                for (DocumentSnapshot document : getUserTask.getResult()) {
                    // If we get here, that means we have found a document matching the
                    // FID we're looking for, so set the flag to true.
                    this.setFoundFID(true);
                    userId = document.getId();
                    Log.d("FSDF", "Found user " + userId);
                    break;
                }
                if (this.getFoundFID()) {
                    Log.d("FID", "User already exists, opening their page");
                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                    intent.putExtra("userId", userId);
                    startActivity(intent);
                } else {  // FID does not already exist, add to database
                    Log.d("FID", "User not found, creating new and opening their page");
                    user = new User(new User.UserCreatedListener() {
                        @Override
                        public void onDataLoaded(User user) {
                            MainActivity.this.user = user;
                            user.setFid(fid);
                            registerFID(fid);
                        }
                    });
                }
            }
        });
    }

    /**
     * The FID does not already exist in the database, so we create a new 'users' document and
     * register the FID in the 'fid' field.
     *
     * @param fid String of the FID to register
     */
    private void registerFID(String fid) {
        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        user.UpdateAllDataFields(new User.DataLoadedListener() {
            @Override
            public void onDataLoaded() {
                intent.putExtra("userId", user.getUid());
                startActivity(intent);
            }
        });
    }

    /**
     * Returns whether the fid was found in the database
     *
     * @return If the fid was found
     */
    public Boolean getFoundFID() {
        return foundFID;
    }

    /**
     * Sets the flag that the fid was found
     *
     * @param foundFID The value to set it too
     */
    public void setFoundFID(Boolean foundFID) {
        this.foundFID = foundFID;
    }
}