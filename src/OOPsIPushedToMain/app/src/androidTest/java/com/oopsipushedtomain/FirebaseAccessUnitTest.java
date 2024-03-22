package com.oopsipushedtomain;

import static org.junit.Assert.assertNull;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;

import com.oopsipushedtomain.Database.FirebaseAccess;
import com.oopsipushedtomain.Database.FirebaseInnerCollection;
import com.oopsipushedtomain.Database.FirestoreAccessType;
import com.oopsipushedtomain.Database.ImageType;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FirebaseAccessUnitTest {

    /**
     * The object being tested
     */
    FirebaseAccess database;

    /**
     * The UID of the stored image
     */
    String imageUID;

    /**
     * The UID of the event used for testing
     */
    String eventUID;

    /**
     * The
     */

    /**
     * Set up for testing the database access
     */
    @Before
    public void setUp() {
        database = new FirebaseAccess(FirestoreAccessType.EVENTS);
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
    public void testGetImage(){
        // Upload an image
        testImageUpload();


        // Get the image
        Bitmap image = database.getImageFromFirestore(imageUID, ImageType.promoQRCodes);
    }


    @Test
    public void testDeleteImage(){
        // Upload an image
        testImageUpload();

        // Delete the image
        database.deleteImageFromFirestore("EVNT-0",imageUID, ImageType.promoQRCodes);
    }

    @Test
    public void testGetAllRelatedImages(){
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

    @Test
    public void testDeleteAll(){
        // Delete everything in the database
        database.deleteAllDataInFireStore();
    }


}
