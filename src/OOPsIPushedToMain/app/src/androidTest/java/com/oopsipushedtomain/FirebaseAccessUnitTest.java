package com.oopsipushedtomain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FirebaseAccessUnitTest {

    /**
     * A toggle for performing the DeleteAll test
     */
    boolean performDeleteAll = false;

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
        Map<String, Object> image = database.getImageFromFirestore(imageUID, imageType);

        // Due to the compression algorithm, it is not possible to compare the images
        assertNotNull(image);
        assertEquals(imageData.get("origin"), image.get("origin"));
        assertEquals(imageData.get("type"), image.get("type"));

        // Get an image that does not exist
        image = database.getImageFromFirestore("NOEXIST", imageType);

        // Due to the compression algorithm, it is not possible to compare the images
        assertNull(image);
    }


    @Test
    public void testDeleteDataFromFirestore() {
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

        // Ensure the app does not crash when deleting a non-existent document
        database.deleteDataFromFirestore("NOEXIST");
    }


    @Test
    public void testDeleteImageFromFirestore() {
        // Delete the test image from Firestore
        database.deleteImageFromFirestore(outerUID, imageUID, imageType);

        // Attempt to retrieve the image and its link
        Map<String, Object> imageData = database.getImageFromFirestore(imageUID, imageType);
        Map<String, Object> linkData = database.getDataFromFirestore(outerUID, innerImagesColl, imageUID);

        // The result should be null
        assertNull(imageData);
        assertNull(linkData);
    }

    @Test
    public void testStoreDataInFirestore() {
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

        // Delete the new document
        database.deleteDataFromFirestore(storeUID.get("outer"));

        // Test creating a new inner document for an image
        assertThrows(IllegalArgumentException.class, () -> database.storeDataInFirestore(outerUID, FirebaseInnerCollection.eventPosters, null, innerTestData));

        // Test creating a new event to check in to
        assertThrows(IllegalArgumentException.class, () -> database.storeDataInFirestore(outerUID, FirebaseInnerCollection.checkedInEvents, null, innerTestData));

        // Test not specifying inner collection
        assertThrows(IllegalArgumentException.class, () -> database.storeDataInFirestore(outerUID, null, innerUID, innerTestData));

    }

    @Test
    public void testStoreImageInFirestore() {
        // Store an image in Firestore
        String storeUID = database.storeImageInFirestore(outerUID, imageUID, ImageType.eventPosters, testImage);

        // Retrieve the image
        Map<String, Object> image = database.getImageFromFirestore(imageUID, ImageType.eventPosters);

        // Get the data from the linked collection
        Map<String, Object> data = database.getDataFromFirestore(outerUID, FirebaseInnerCollection.eventPosters, storeUID);

        // Test
        assertEquals(storeUID, imageUID);           // Image creation
        assertEquals(data.get("UID"), imageUID);    // Link
        assertNotNull(image);                       // Actual image


        // Store an image with data in firestore
        storeUID = database.storeImageInFirestore(outerUID, imageUID, ImageType.eventQRCodes, testImage, innerTestData);

        // Retrieve the image
        image = database.getImageFromFirestore(imageUID, ImageType.eventQRCodes);

        // Get the data from the linked collection
        data = database.getDataFromFirestore(outerUID, FirebaseInnerCollection.eventQRCodes, storeUID);


        // Test
        assertEquals(storeUID, imageUID);           // Image creation
        assertEquals(data.get("UID"), imageUID);    // Link
        assertNotNull(image);                       // Actual image
        assertEquals(innerTestData.get("Inner1"), image.get("Inner1"));

        // Store an image with no UID given
        storeUID = database.storeImageInFirestore(outerUID, null, ImageType.promoQRCodes, testImage);

        // Retrieve the image
        image = database.getImageFromFirestore(storeUID, ImageType.promoQRCodes);

        // Get the data from the linked collection
        data = database.getDataFromFirestore(outerUID, FirebaseInnerCollection.promoQRCodes, storeUID);

        // Test
        assertEquals(storeUID, data.get("UID"));
        assertNotNull(image);

        // Delete the image to clean up
        database.deleteImageFromFirestore(outerUID, storeUID, ImageType.promoQRCodes);

        // Test invalid database
        FirebaseAccess database2 = new FirebaseAccess(FirestoreAccessType.IMAGES);
        assertThrows(IllegalArgumentException.class, () -> database2.storeImageInFirestore(outerUID, imageUID, imageType, testImage));
    }

    @Test
    public void testDeleteAllDocumentsFromFirestore() {
        if (performDeleteAll) {
            // Delete all the documents in Firestore
            database.deleteAllDataInFirestore();

            // Check if the data was deleted and the collection still exits
            Map<String, Object> data = database.getDataFromFirestore("XXXX");
            assertEquals("XXXX", data.get("UID"));
        } else {
            Log.d("FirebaseAccessTest", "Skipped Delete All");
        }
    }

    @Test
    public void testGetAllDocumentsFromFirestore() {
        // Add a document to the collection
        database.storeDataInFirestore(outerUID, outerTestData);

        // Get the list of all documents in the outer collection
        ArrayList<Map<String, Object>> dataList = database.getAllDocuments();
        assertNotNull(dataList);

        // Iterate through the list and confirm the added document is in the list
        boolean addedDocExists = false;
        for (Map<String, Object> data : dataList) {
            if (Objects.equals((String) data.get("UID"), outerUID)) {
                addedDocExists = true;
            }
        }
        assertTrue(addedDocExists);

        // Try to get a document from a non-existent inner collection
        // Simulates an empty collection
        dataList = database.getAllDocuments(outerUID, FirebaseInnerCollection.profilePictures);
        assertNull(dataList);

        // Add a document to this inner collection
        database.storeDataInFirestore(outerUID, innerColl, innerUID, innerTestData);

        // Get the list of all documents in the inner collection
        dataList = database.getAllDocuments(outerUID, innerColl);
        assertNotNull(dataList);

        // Confirm the added document is in the list
        addedDocExists = false;
        for (Map<String, Object> data : dataList) {
            if (Objects.equals((String) data.get("UID"), innerUID)) {
                addedDocExists = true;
            }
        }
        assertTrue(addedDocExists);

    }

    @Test
    public void testGetAllRelatedImagesFromFirestore() {
        // Add an image to the collection
        database.storeImageInFirestore(outerUID, imageUID, imageType, testImage);

        // Get all the images from this collection
        ArrayList<Map<String, Object>> dataList = database.getAllRelatedImagesFromFirestore(outerUID, imageType);
        assertNotNull(dataList);

        // Check to see if the added image is in the list
        boolean addedDocExists = false;
        for (Map<String, Object> data : dataList) {
            if (Objects.equals((String) data.get("UID"), imageUID)) {
                addedDocExists = true;
            }
        }
        assertTrue(addedDocExists);

        // Try to get images from an empty collection
        dataList = database.getAllRelatedImagesFromFirestore(outerUID, ImageType.profilePictures);
        assertNull(dataList);
    }

    @Test
    public void testGetAllImagesFromFirestore() {
        // Add an image to the collection
        database.storeImageInFirestore(outerUID, imageUID, imageType, testImage);

        // Get all of the images of this type
        ArrayList<Map<String, Object>> dataList = database.getAllImagesFromFirestore(imageType);
        assertNotNull(dataList);

        // Check to see if the added image is in the list
        boolean addedDocExists = false;
        for (Map<String, Object> data : dataList) {
            Log.d("FirebaseAccessTest", "UID: " + data.get("UID"));
            if (Objects.equals((String) data.get("UID"), imageUID)) {
                addedDocExists = true;
            }
        }
        assertTrue(addedDocExists);

    }

    @After
    public void cleanUpDatabase() {
        // Delete all of the new documents and images
        database.deleteImageFromFirestore(outerUID, imageUID, imageType);
        database.deleteImageFromFirestore(outerUID, imageUID, ImageType.eventPosters);
        database.deleteDataFromFirestore(outerUID);
    }


}