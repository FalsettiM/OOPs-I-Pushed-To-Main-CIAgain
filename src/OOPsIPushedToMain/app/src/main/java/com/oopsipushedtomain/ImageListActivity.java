package com.oopsipushedtomain;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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
        adapter = new ImageAdapter(this, images, position -> {
            // Show confirmation dialog
            new AlertDialog.Builder(this)
                    .setTitle("Delete Image")
                    .setMessage("Are you sure you want to delete this image?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Delete the image from Firebase Storage and Firestore
                        deleteImage(position);
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
        recyclerView.setAdapter(adapter);

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
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot documentSnapshot : task.getResult()) {
                    if (documentSnapshot.exists()) {
                        // Assuming 'eventImage' field contains the storage path of the image
                        String imagePath = documentSnapshot.getString("eventImage");
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
                Log.e("ImageListActivity", "Error fetching event documents", task.getException());
            }
        });
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

    private void deleteImage(int position) {
        // Example: Deleting an image from Firebase Storage
        String imagePath = "path/to/your/image"; // Determine the path based on your logic
        FirebaseStorage.getInstance().getReference().child(imagePath).delete().addOnSuccessListener(aVoid -> {
            // Remove the image reference from Firestore document if needed
            // Remove the Bitmap from your adapter's data set and notify the adapter
            images.remove(position);
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "Image deleted successfully", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to delete image", Toast.LENGTH_SHORT).show();
        });
    }

}

