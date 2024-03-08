package com.oopsipushedtomain;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * This class will hold a QR code.
 * It will contain the bitmap image as well as functions to generate and read a code
 */
public class QRCode {
    // Storing the string and bitmap of the qrcode
    private String qrString;
    private String qrCodeUID;


    private String imageUID;

    private Bitmap qrCodeImage = null;

    // Database parameters
    private FirebaseFirestore db;
    private CollectionReference qrCodeRef;
    private DocumentReference qrCodeDocRef;


    // Firebase storage
    private FirebaseStorage storage;
    private StorageReference storageRef;


    /**
     * Interface for checking when data is loaded into the qrcode
     */
    public interface DataLoadedListener {
        void onDataLoaded();
    }

    /**
     * Interface for checking when the image is loaded from the database
     */
    public interface OnBitmapReceivedListener {
        void onBitmapReceived(Bitmap bitmap);
    }

    public interface NewCodeListener {
        void onDataInitialized();
    }

    /**
     * Initializes the database parameters for accessing firestore
     */
    public void InitDatabase() {
        db = FirebaseFirestore.getInstance();
        qrCodeRef = db.collection("qrcodes");
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    public QRCode(String text) {
        // Initialize the database
        InitDatabase();

        // Get the user id of a new document
        qrCodeUID = qrCodeRef.document().getId().toUpperCase();
        qrCodeUID = "QRCD-" + qrCodeUID;

        // Get the UID for an image
        imageUID = qrCodeRef.document().getId().toUpperCase();
        imageUID = "IMGE-" + imageUID;

        // Create a hash map for all variables
        HashMap<String, Object> data = new HashMap<>();
        data.put("qrString", null);
        data.put("qrImage", imageUID);

        // Create a new document and set all parameters
        qrCodeRef.document(qrCodeUID).set(data);

        // Set the document reference to this document
        qrCodeDocRef = qrCodeRef.document(qrCodeUID);

        // Generate the code
        try {
            generateCode(text);
        } catch (WriterException e) {
            Log.d("QR Code","Generating Failed");
        }

    }

    public QRCode(String qrCodeID, DataLoadedListener listener) {
        // Initialize database
        InitDatabase();

        // Set the document ID
        qrCodeUID = qrCodeID;
        qrCodeDocRef = qrCodeRef.document(qrCodeUID);

        // Set the fields of the class
        new Thread(new Runnable() {
            @Override
            public void run() {
                UpdateAllDataFields(new NewCodeListener() {
                    @Override
                    public void onDataInitialized() {
                        // Generate a new code
                        Log.d("QR Code","Data init");
                        getQrCodeImage(new OnBitmapReceivedListener() {
                            @Override
                            public void onBitmapReceived(Bitmap bitmap) {
                                if (bitmap != null){
                                    qrCodeImage = bitmap;
                                }
                                listener.onDataLoaded();
                            }
                        });


                    }
                });

            }
        }).start();

    }

    public void UpdateAllDataFields(QRCode.NewCodeListener listener) {
        // Get the data in the document
        qrCodeDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Get the data from the query - there should only be 1 document
                        Map<String, Object> data = document.getData();

                        // Get Single element fields
                        assert data != null;
                        qrString = (String) data.get("qrString");
                        imageUID = (String) data.get("qrImage");

                        assert imageUID != null;
                        Log.d("QR Code", imageUID);

                        // Inform complete
                        Log.d("Firebase", "Data Loaded");
                        listener.onDataInitialized();

                    } else {
                        Log.d("Firebase", "No such document");
                    }
                } else {
                    Log.d("Firebase Failure", "get failed with ", task.getException());
                }
            }
        });
    }


    public void getQrCodeImage(QRCode.OnBitmapReceivedListener listener) {
        StorageReference profileImageRef = storageRef.child(imageUID);

        // Down load the image
        final long ONE_MEGABYTE = 1024 * 1024;
        profileImageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
            // Convert to a bitmap
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            Log.d("QR Code", "Image successfully loaded");
            listener.onBitmapReceived(bitmap);
        }).addOnFailureListener(e -> {
            Log.d("QR Code", "Image failed to download");
            listener.onBitmapReceived(null);
        });
    }


    private void setQrString(String qrString) {
        // Update in the class
        this.qrString = qrString;

        // Update in database
        HashMap<String, Object> data = new HashMap<>();
        data.put("qrString", this.qrString);
        qrCodeDocRef.update(data);
    }


    private void setQrImage(Bitmap qrCodeImage) {
        // Store the image in the object
        this.qrCodeImage = qrCodeImage;

        // Convert the bitmap to PNG for upload
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        qrCodeImage.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        // Upload the image to firebase
        UploadTask uploadTask = storageRef.child(imageUID).putBytes(data);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            Log.d("QR Code", "Image upload successful");
        }).addOnFailureListener(exception -> {
            Log.d("QR Code", "Image upload failed");
        });

    }

    public String getQrString() {
        return this.qrString;
    }


    // ChatGPT, How would I use ZXing to generate a QR code?
    public void generateCode(String text) throws WriterException {
        // The size of the QR Code
        int qrSize = 400;

        // Create the MultiFormatWriter to generate the qr code
        MultiFormatWriter writer = new MultiFormatWriter();
        // Encode the text into a QR code format
        BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, qrSize, qrSize);
        // Create a new bitmap
        Bitmap bmp = Bitmap.createBitmap(qrSize, qrSize, Bitmap.Config.RGB_565);

        // Create the bitmap pixel by pixel
        for (int x = 0; x < qrSize; x++) {
            for (int y = 0; y < qrSize; y++) {
                bmp.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }

        // Store the bitmap in the class
        qrCodeImage = bmp;
        qrString = text;

        // Store in the database
        setQrString(text);
        setQrImage(bmp);


    }

//    public void scanCode(Context context) {
//        // Switch to the scanning activity and scan
//        Intent intent = new Intent(context, QRScanner.class);
//        // Start the activity
//        context.startActivity(intent);
//
//    }

}
