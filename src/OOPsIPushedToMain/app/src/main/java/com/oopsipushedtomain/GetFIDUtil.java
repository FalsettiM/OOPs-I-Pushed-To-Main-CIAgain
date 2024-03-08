package com.oopsipushedtomain;

import android.util.Log;

import com.google.firebase.installations.FirebaseInstallations;

/**
 * Helper function to quickly get the Firebase Installation ID (fid)
 */
public final class GetFIDUtil {
    private GetFIDUtil(){};
    private static String fid = "";

    /**
     * Interface for checking when data was received
     */
    public interface DataLoadedListener {
        void onDataLoaded(String fid);
    }

    /**
     * Get the fid from firebase
     * @param listener The listener for determining when the data transfer is complete
     */
    public static void GetFID(DataLoadedListener listener) {
        // Get the FID
        FirebaseInstallations.getInstance().getId().addOnCompleteListener(getFIDTask -> {
            if (getFIDTask.isSuccessful()) {
                // Store the FID in a String
                fid = getFIDTask.getResult();
                Log.d("GetFIDUtil", "FID is " + fid);
            } else {
                Log.e("GetFIDUtil", "Couldn't get FID");
            }
            listener.onDataLoaded(fid);
        });
    }
}
