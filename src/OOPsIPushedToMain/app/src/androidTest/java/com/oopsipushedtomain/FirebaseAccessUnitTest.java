package com.oopsipushedtomain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;

import com.google.firebase.firestore.Blob;
import com.google.firebase.firestore.FirebaseFirestore;
import com.oopsipushedtomain.Database.FirebaseAccess;
import com.oopsipushedtomain.Database.FirebaseInnerCollection;
import com.oopsipushedtomain.Database.FirestoreAccessType;
import com.oopsipushedtomain.Database.ImageType;

import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FirebaseAccessUnitTest {

    /**
     * A toggle for performing the DeleteAll test
     */
    boolean performDeleteAll = true;

    /**
     * The object being tested
     */
    FirebaseAccess database;

    /**
     * The UID of the outer collection
     */
    String outerUID = "TEST-0";
    /**
     * The UID of the inner document for testing
     */
    String innerUID = "INNE-0";

    /**
     * The UID for the image
     */
    String imageUID = "IMGE-0";

    /**
     * The inner collection to use for data
     */
    FirebaseInnerCollection innerColl = FirebaseInnerCollection.eventPosters;

    /**
     * The inner collection to use for images
     */
    FirebaseInnerCollection innerImagesColl = FirebaseInnerCollection.eventQRCodes;


    /**
     * The data used to test storing to the database
     */
    Map<String, Object> outerTestData;
    /**
     * The data used for testing inner collections
     */
    Map<String, Object> innerTestData;
    /**
     * The data used for testing images
     */
    Map<String, Object> imageData;

    /**
     * The image for testing image uploads
     */
    Bitmap testImage;

    /**
     * The type of the test image
     */
    ImageType imageType = ImageType.eventQRCodes;


    /**
     * Set up for testing the database access
     */
    @Before
    public void setUp() {
        // Create a new database access
        database = new FirebaseAccess(FirestoreAccessType.EVENTS);

        // Create the bitmap
        Context testContext = InstrumentationRegistry.getInstrumentation().getContext();
        testImage = BitmapFactory.decodeResource(testContext.getResources(), com.oopsipushedtomain.test.R.drawable.test_image);
        Blob imageBlob = FirebaseAccess.bitmapToBlob(testImage);

        // Create test data
        outerTestData = new HashMap<>();
        outerTestData.put("Test1", "Testing");
        outerTestData.put("Test2", 22);

        innerTestData = new HashMap<>();
        innerTestData.put("Inner1", "Testing");
        innerTestData.put("Inner2", 447);

        imageData = new HashMap<>();
        imageData.put("image", imageBlob);
        imageData.put("origin", outerUID);
        imageData.put("type", ImageType.eventQRCodes.name());


        // Store a Test file in the database manually
        FirebaseFirestore.getInstance().collection("events").document(outerUID).set(outerTestData);

        // Store in the inner collection of the same document as well
        FirebaseFirestore.getInstance().collection("events").document(outerUID).collection(innerColl.name()).document(innerUID).set(innerTestData);

        // Store the image in the images collection manually
        FirebaseFirestore.getInstance().collection("qrcodes").document(imageUID).set(imageData);
        FirebaseFirestore.getInstance().collection("events").document(outerUID).collection(innerImagesColl.name()).document(imageUID).set(new HashMap<>()); // Link


        // Delay for a few seconds to complete the transfer
        try {
            // Pause the current thread for 5000 milliseconds (5 seconds)
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // Handle the interruption
            Log.e("FirebaseAcessTest", "Failure to stop for testing");
        }

        // Add the UID of the document to the test data for comparison
        outerTestData.put("UID", outerUID);
        innerTestData.put("UID", innerUID);


    }


    @Test
    public void testGetDataFromFirestore() {
        // Get the outer collection data from Firestore
        Map<String, Object> data = database.getDataFromFirestore(outerUID);
        assertEquals(data.toString(), outerTestData.toString());

        // Get the inner collection data from Firestore
        data = database.getDataFromFirestore(outerUID, innerColl, innerUID);
        assertEquals(data.toString(), innerTestData.toString());

        // Test getting data that doesn't exist in Firestore
        data = database.getDataFromFirestore("NOEXIST");
        assertNull(data);

        // Test getting non-existent data for an inner collection
        data = database.getDataFromFirestore(outerUID, innerColl, "NOEXIST");
        assertNull(data);

        // Inner collection is valid, outer collection is not
        data = database.getDataFromFirestore("NOEXIST", innerColl, innerUID);
        assertNull(data);
    }


    @Test
    public void testGetImageFromFirestore() {
        // Get the preloaded image from firestore
        Bitmap image = database.getImageFromFirestore(imageUID, imageType);

        // Due to the compression algorithm, it is not possible to compare the images
        assertNotNull(image);

        // Get an image that does not exist
        image = database.getImageFromFirestore("NOEXIST", imageType);

        // Due to the compression algorithm, it is not possible to compare the images
        assertNull(image);
    }


    @Test
    public void testDeleteFromFirestore() {
        // Delete the inner document from Firestore
        database.deleteDataFromFirestore(outerUID, innerColl, innerUID);

        // Check if it was deleted
        Map<String, Object> data = database.getDataFromFirestore(outerUID, innerColl, innerUID);
        assertNull(data);

        // Delete the outer document
        database.deleteDataFromFirestore(outerUID);

        // Check if it was deleted
        data = database.getDataFromFirestore(outerUID);
        assertNull(data);

        // Ensure the app does not crash when deleting a non-existent documen
        database.deleteDataFromFirestore("NOEXIST");
    }

    @Test
    public void testStoreDatainFirestore() {
        // Store data into an outer collection
        Map<String, String> storeUID = database.storeDataInFirestore(outerUID, outerTestData);
        Map<String, Object> data = database.getDataFromFirestore(outerUID);
        assertEquals(data.toString(), outerTestData.toString());
        assertEquals(outerUID, storeUID.get("outer"));

        // Store data into an inner collection
        storeUID = database.storeDataInFirestore(outerUID, innerColl, innerUID, innerTestData);
        data = database.getDataFromFirestore(outerUID, innerColl, innerUID);
        assertEquals(data.toString(), innerTestData.toString());
        assertEquals(innerUID, storeUID.get("inner"));

        // Test creating a new outer document
        storeUID = database.storeDataInFirestore(null, outerTestData);
        data = database.getDataFromFirestore(storeUID.get("outer"));
        assertEquals(data.get("UID"), storeUID.get("outer"));

        // Test creating a new inner document (announcements)
        storeUID = database.storeDataInFirestore(storeUID.get("outer"), FirebaseInnerCollection.announcements, null, innerTestData);
        data = database.getDataFromFirestore(storeUID.get("outer"), FirebaseInnerCollection.announcements, storeUID.get("inner"));
        assertEquals(data.get("UID"), storeUID.get("inner"));

        // Test creating a new inner document for an image
        assertThrows(IllegalArgumentException.class, () -> database.storeDataInFirestore(outerUID, FirebaseInnerCollection.eventPosters, null, innerTestData));

        // Test creating a new event to check in to
        assertThrows(IllegalArgumentException.class, () -> database.storeDataInFirestore(outerUID, FirebaseInnerCollection.checkedInEvents, null, innerTestData));

        // Test not specifying inner collection
        assertThrows(IllegalArgumentException.class, () -> database.storeDataInFirestore(outerUID, null, innerUID, innerTestData));
        
    }


    @Test
    public void testStoreData() {
        // Test storing some data in the database
    }


    @Test
    public void testDeleteAll() {
        // Delete all of the items in the database to prepare for testing
        if (performDeleteAll) {
            database.deleteAllDataInFireStore();
        }
    }

    @Test
    public void testAddToCollection() {
        HashMap<String, Object> data = new HashMap<>();

        data.put("test1", 5);
        data.put("test2", "seven");


        database.storeDataInFirestore("EVNT-1", data);
    }

    @Test
    public void testAddToInnerCollection() {
        HashMap<String, Object> data = new HashMap<>();

        data.put("test1", 1);
        data.put("test2", "two");


        database.storeDataInFirestore("EVNT-1", FirebaseInnerCollection.eventPosters, "IMGE-1", data);
    }

    @Test
    public void testImageUpload() {
        // Create the bitmap
        Context testContext = InstrumentationRegistry.getInstrumentation().getContext();
        Bitmap image = BitmapFactory.decodeResource(testContext.getResources(), com.oopsipushedtomain.test.R.drawable.test_image);

        // Upload to the database
        imageUID = database.storeImageInFirestore("EVNT-1", null, ImageType.promoQRCodes, image);
    }

    @Test
    public void testGetFromCollection() {
        // Create data storage
        Map<String, Object> outerData = new HashMap<>();

        // Get the data from the outer collection
        outerData = database.getDataFromFirestore("EVNT-1");

        // Print the data
        Log.d("FirebaseAccessTest", "Outer Data: " + outerData.toString());

        // Get the data from the inner collection
        Map<String, Object> innerData = new HashMap<>();
        innerData = database.getDataFromFirestore("EVNT-1", FirebaseInnerCollection.eventPosters, "IMGE-1");
        Log.d("FirebaseAccessTest", "Inner Data: " + innerData.toString());

        // Test document not found
        assertNull(database.getDataFromFirestore("EVNT-1", FirebaseInnerCollection.announcements, "HHHHH"));

    }

    @Test
    public void testDeleteDocument() {
        // Test deleting an inner collection
//        database.deleteDataFromFirestore("EVNT-0", FirebaseInnerCollection.eventPosters, "IMGE-1");

        // Test deleting a main document
        database.deleteDataFromFirestore("EVNT-1");

        // Test deleting a non existent document
        database.deleteDataFromFirestore("Hi");
    }

    @Test
    public void testGetImage() {
        // Upload an image
        testImageUpload();


        // Get the image
        Bitmap image = database.getImageFromFirestore(imageUID, ImageType.promoQRCodes);
    }


    @Test
    public void testDeleteImage() {
        // Upload an image
        testImageUpload();

        // Delete the image
        database.deleteImageFromFirestore("EVNT-0", imageUID, ImageType.promoQRCodes);
    }

    @Test
    public void testGetAllRelatedImages() {
        // Upload an image
        testImageUpload();

        // Get all the images
        ArrayList<Map<String, Object>> data = database.getAllRelatedImagesFromFirestore("EVNT-0", ImageType.promoQRCodes);

        // Print out the data
        Log.d("Testing", "Data: " + data);
    }

    @Test
    public void testGetAllImages() {
        // Upload an image
        testImageUpload();

        // Get all the images
        ArrayList<Map<String, Object>> data = database.getAllImagesFromFirestore(ImageType.promoQRCodes);

        // Print out the data
        Log.d("Testing", "Data: " + data);
    }

//    @Test
//    public void testDeleteAll(){
//        // Delete everything in the database
//        database.deleteAllDataInFireStore();
//    }


}
