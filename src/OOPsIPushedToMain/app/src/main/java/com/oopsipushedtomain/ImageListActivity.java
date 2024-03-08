package com.oopsipushedtomain;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ImageListActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    private ImageAdapter adapter;
    private List<Bitmap> images = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_list); // Ensure you have this layout with RecyclerView

        recyclerView = findViewById(R.id.imagesRecyclerView); // Your RecyclerView ID
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ImageAdapter(this, images);
        recyclerView.setAdapter(adapter);

        // Check the intent for what to display
        String IMAGES_TYPE = getIntent().getStringExtra("IMAGES_TYPE");
        if ("events".equals(IMAGES_TYPE)) {
            fetchAllEventsAndDisplayImages();
        } else if ("profiles".equals(IMAGES_TYPE)) {
            fetchAllUsersAndDisplayImages();
        }

    }

    private void fetchAllEventsAndDisplayImages() {
    }

    public void fetchAllUsersAndDisplayImages() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot documentSnapshot : task.getResult()) {
                    if (documentSnapshot.exists()) {
                        // Assuming 'profileImage' field contains the storage path of the image
                        String imagePath = documentSnapshot.getString("profileImage");
                        if (imagePath != null && !imagePath.isEmpty()) {
                            fetchAndDisplayImage(imagePath);
                        } else {
                            Log.e("ImageListActivity", "No imagePath available for user: " + documentSnapshot.getId());
                        }
                    } else {
                        Log.e("ImageListActivity", "Document does not exist");
                    }
                }
            } else {
                Log.e("ImageListActivity", "Error fetching user documents", task.getException());
            }
        });
    }

    private void fetchAndDisplayImage(String imagePath) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child(imagePath);

        final long ONE_MEGABYTE = 1024 * 1024;
        storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            runOnUiThread(() -> {
                images.add(bitmap);
                adapter.notifyDataSetChanged();
            });
        }).addOnFailureListener(e -> {
            Log.e("ImageListActivity", "Error fetching image", e);
        });
    }


    // Placeholder for your method to fetch User objects. This could be fetching document IDs from Firestore and then creating User objects for each.
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void getUsers(OnUsersFetchedListener listener) {
        List<User> userList = new ArrayList<>();
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot document : task.getResult()) {
                    String userId = document.getId();
                    User user = new User(userId, new User.DataLoadedListener() {
                        @Override
                        public void onDataLoaded() {
                            // Handle user data loaded event if necessary
                        }
                    });
                    userList.add(user);
                }
                // Invoke the callback once all users are added to the list
                listener.onUsersFetched(userList);
            } else {
                Log.d("Firestore", "Error getting documents: ", task.getException());
            }
        });
    }
}

