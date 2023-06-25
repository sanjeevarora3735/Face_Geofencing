package com.sanjeev.face_geofencing.Helper;

import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.maps.model.LatLng;
import com.sanjeev.face_geofencing.Receiver.GeofenceBroadcastReceiver;

public class GeoFencingHelper extends ContextWrapper {

    public static final String TAG = "GeoFencingHelper";
    PendingIntent pendingIntent;

    public GeoFencingHelper(Context base) {
        super(base);
    }

    // Create a GeofencingRequest with a single geofence
    public GeofencingRequest getGeofencingRequest(Geofence geofence) {
        return new GeofencingRequest.Builder()
                .addGeofence(geofence)
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .build();
    }

    // Create a Geofence object with the specified ID, location, radius, and transition types
    public Geofence getGeofence(String ID, LatLng latLng, float radius, int transitionTypes) {
        return new Geofence.Builder()
                .setCircularRegion(latLng.latitude, latLng.longitude, radius)
                .setRequestId(ID)
                .setTransitionTypes(transitionTypes)
                .setLoiteringDelay(2000)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();
    }

    // Get the PendingIntent for the GeofenceBroadcastReceiver
    public PendingIntent getPendingIntent() {
        if (pendingIntent != null) {
            return pendingIntent;
        } else {
            // Create a new intent for the GeofenceBroadcastReceiver
            Intent intent = new Intent(getApplicationContext(), GeofenceBroadcastReceiver.class);
            // Get a unique PendingIntent using the FLAG_MUTABLE flag
            pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 2607, intent, PendingIntent.FLAG_MUTABLE);
        }
        return pendingIntent;
    }
}
