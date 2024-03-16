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

import java.util.HashMap;
import java.util.Map;

public class FirebaseAccessUnitTest {

    FirebaseAccess database;

    @Before
    public void setUp() {
        database = new FirebaseAccess(FirestoreAccessType.EVENTS);
    }

    @Test
    public void testAddToCollection() {
        HashMap<String, Object> data = new HashMap<>();

        data.put("test1", 1);
        data.put("test2", "two");


        database.storeDataInFirestore("EVNT-0", data);
    }

    @Test
    public void testAddToInnerCollection() {
        HashMap<String, Object> data = new HashMap<>();

        data.put("test1", 1);
        data.put("test2", "two");


        database.storeDataInFirestoreInnerCollection("EVNT-0", FirebaseInnerCollection.eventPosters, "IMGE-0", data);
    }

    @Test
    public void testImageUpload() {
        // Create the bitmap
        Context testContext = InstrumentationRegistry.getInstrumentation().getContext();
        Bitmap image = BitmapFactory.decodeResource(testContext.getResources(), com.oopsipushedtomain.test.R.drawable.test_image);

        // Upload to the database
        database.storeImageInFirebaseStorage("EVNT-0", null, ImageType.PROMO_QRCODE, image);
    }

    @Test
    public void testGetFromCollection() {
        // Create data storage
        Map<String, Object> outerData = new HashMap<>();

        // Get the data from the outer collection
        outerData = database.getDataFromFirestore("EVNT-0");

        // Print the data
        Log.d("FirebaseAccessTest", "Outer Data: " + outerData.toString());

        // Get the data from the inner collection
        Map<String, Object> innerData = new HashMap<>();
        innerData = database.getDataFromFirestore("EVNT-0", FirebaseInnerCollection.eventPosters, "IMGE-0");
        Log.d("FirebaseAccessTest", "Inner Data: " + innerData.toString());

        // Test document not found
        assertNull(database.getDataFromFirestore("EVNT-0", FirebaseInnerCollection.announcements, "HHHHH"));

    }

    @Test
    public void testDeleteDocument() {
        // Test deleting an inner collection
//        database.deleteDataFromFirestore("EVNT-0", FirebaseInnerCollection.eventPosters, "IMGE-0");

        // Test deleting a main document
        database.deleteDataFromFirestore("EVNT-0");

        // Test deleting a non existent documen
//        database.deleteDataFromFirestore("Hi", null, null);
    }


}
