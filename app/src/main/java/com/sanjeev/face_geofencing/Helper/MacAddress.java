package com.sanjeev.face_geofencing.Helper;

import android.content.Context;
import android.content.SharedPreferences;

public class MacAddress {
    // Save the MAC address to SharedPreferences
    public static void saveMacAddress(Context context, String macAddress) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("macAddress", macAddress);
        editor.apply();
    }

    // Retrieve the saved MAC address from SharedPreferences
    public static String getSavedMacAddress(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        // Return the saved MAC address, defaulting to "00:00:00:00:00:00" if not found
        return sharedPreferences.getString("macAddress", "00:00:00:00:00:00");
    }
}
