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

/**
 * Activity class to display a list of images from Firebase.
 * It allows users to view images categorized as either events or profiles,
 * and offers functionality to delete a selected image.
 */

public class ImageListActivity extends AppCompatActivity {
    // RecyclerView to display the list of images.
    RecyclerView recyclerView;
    // Adapter to manage the display and interaction of image items in the RecyclerView.
    private ImageAdapter adapter;
    // List to hold image information objects.
    private List<ImageInfo> imageInfos = new ArrayList<>();

    /**
     * Initializes the activity, sets up the RecyclerView and its adapter,
     * and decides whether to fetch event or profile images based on intent extras.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in onSaveInstanceState(Bundle). Otherwise it is null.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_list);

        recyclerView = findViewById(R.id.imagesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ImageAdapter(this, imageInfos, position -> {
            // Show confirmation dialog
            new AlertDialog.Builder(this)
                    .setTitle("Delete Image")
                    .setMessage("Are you sure you want to delete this image?")
                    .setPositiveButton("Yes", (dialog, which) -> deleteImage(position))
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

    /**
     * Fetches an image from Firebase storage by its path and adds it to the display list.
     *
     * @param imagePath The storage path of the image to fetch.
     * @param documentId The Firestore document ID associated with the image.
     */

    private void fetchAndDisplayImage(String imagePath, String documentId) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child(imagePath);

        final long ONE_MEGABYTE = 1024 * 1024;
        storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            runOnUiThread(() -> {
                imageInfos.add(new ImageInfo(bitmap, imagePath, documentId));
                adapter.notifyDataSetChanged();
            });
        }).addOnFailureListener(e -> {
            Log.e("ImageListActivity", "Error fetching image", e);
        });
    }

    /**
     * Fetches all event images from Firestore and displays them.
     * Assumes there's a field named 'eventImage' in the documents under 'events' collection.
     */

    private void fetchAllEventsAndDisplayImages() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot documentSnapshot : task.getResult()) {
                    if (documentSnapshot.exists()) {
                        // Assuming 'eventImage' field contains the storage path of the image
                        String imagePath = documentSnapshot.getString("eventImage");
                        String documentId = documentSnapshot.getId();
                        if (imagePath != null && !imagePath.isEmpty()) {
                            fetchAndDisplayImage(imagePath, documentId);
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

    /**
     * Fetches all user profile images from Firestore and displays them.
     * Assumes there's a field named 'profileImage' in the documents under 'users' collection.
     */
    public void fetchAllUsersAndDisplayImages() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot documentSnapshot : task.getResult()) {
                    if (documentSnapshot.exists()) {
                        // Assuming 'profileImage' field contains the storage path of the image
                        String imagePath = documentSnapshot.getString("profileImage");
                        String documentId = documentSnapshot.getId();
                        if (imagePath != null && !imagePath.isEmpty()) {
                            fetchAndDisplayImage(imagePath, documentId);
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

    /**
     * Deletes an image from Firebase storage and optionally updates Firestore to reflect the change.
     *
     * @param position The position of the image in the RecyclerView's adapter to delete.
     */

    private void deleteImage(int position) {
        ImageInfo imageInfo = imageInfos.get(position);
        String imagePath = imageInfo.getStoragePath(); // Now dynamically determined
        FirebaseStorage.getInstance().getReference().child(imagePath).delete().addOnSuccessListener(aVoid -> {
            // Optionally, delete or update the Firestore document reference
            if (imageInfo.getFirestoreDocumentId() != null) {
                FirebaseFirestore.getInstance().collection("yourCollectionName").document(imageInfo.getFirestoreDocumentId())
                        .delete() // or .update("fieldName", FieldValue.delete())
                        .addOnSuccessListener(aVoid2 -> Log.d("Delete", "DocumentSnapshot successfully deleted!"))
                        .addOnFailureListener(e -> Log.w("Delete", "Error deleting document", e));
            }
            imageInfos.remove(position);
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "Image deleted successfully", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to delete image", Toast.LENGTH_SHORT).show();
        });
    }
}

