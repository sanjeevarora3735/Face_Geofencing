package com.sanjeev.face_geofencing.Helper;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceHelper {
    private static final String BOOLEAN_KEY = "Am_I_Inside_The_GeoFence";
    private SharedPreferences sharedPreferences;

    public SharedPreferenceHelper(Context context) {
        // Initialize the SharedPreferences object with the given context
        sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
    }

    public boolean getBooleanValue() {
        // Retrieve the boolean value associated with the BOOLEAN_KEY
        // If the key is not found, return false as the default value
        return sharedPreferences.getBoolean(BOOLEAN_KEY, false);
    }

    public void setBooleanValue(boolean value) {
        // Store the provided boolean value associated with the BOOLEAN_KEY
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(BOOLEAN_KEY, value);
        editor.apply();
    }
}
