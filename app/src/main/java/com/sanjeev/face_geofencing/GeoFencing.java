package com.sanjeev.face_geofencing;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.sanjeev.face_geofencing.Helper.GeoFencingHelper;
import com.sanjeev.face_geofencing.Helper.SharedPreferenceHelper;
import com.sanjeev.face_geofencing.Receiver.GeofenceBroadcastReceiver;
import com.sanjeev.face_geofencing.databinding.ActivityGeoFencingBinding;

public class GeoFencing extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private static final float PRE_DEFINE_GEOFENCING_RADIUS = 100;
    private static final String TAG = "GeoFencingActivity";
    private static final int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 96414;
    private final String GeoFenceId = "IDENTICAL_FACE_DETECTION_GEOFENCE";
    private GoogleMap mMap;
    private ActivityGeoFencingBinding binding;
    private GeoFencingHelper geoFencingHelper;
    private GeofencingClient geofencingClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityGeoFencingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        geofencingClient = LocationServices.getGeofencingClient(GeoFencing.this);
        geoFencingHelper = new GeoFencingHelper(getApplicationContext());
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        EnableUserLocation();
        mMap.setOnMapLongClickListener(this);
    }

    private void EnableUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        try {
            new SharedPreferenceHelper(getApplicationContext()).setBooleanValue(false);
            mMap.clear();
            addCircle(latLng, PRE_DEFINE_GEOFENCING_RADIUS);
            addMarker(latLng);
            addGeoFence(latLng, PRE_DEFINE_GEOFENCING_RADIUS);

        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }

    }

    private void addGeoFence(LatLng latLng, float preDefineGeofencingRadius) {
        Geofence geofence = geoFencingHelper.getGeofence(GeoFenceId, latLng, preDefineGeofencingRadius, Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT);
        GeofencingRequest geofencingRequest = geoFencingHelper.getGeofencingRequest(geofence);
        PendingIntent pendingIntent = geoFencingHelper.getPendingIntent();
        IntentFilter intentFilter = new IntentFilter("com.sanjeev.geo_fencing.GEOFENCE_EVENT");
        GeofenceBroadcastReceiver receiver = new GeofenceBroadcastReceiver();
        registerReceiver(receiver, intentFilter);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
            return;
        }
        geofencingClient.addGeofences(geofencingRequest, pendingIntent).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "onSuccess: Geofence Added ...");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: " + e.getMessage());
                Log.d(TAG, "onFailure: " + e);
            }
        });
    }

    private void addMarker(LatLng latLng) {
        MarkerOptions markerOptions = new MarkerOptions().position(latLng);
        mMap.addMarker(markerOptions);
    }

    private void addCircle(LatLng latLng, float preDefineGeofencingRadius) {
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.radius(preDefineGeofencingRadius);
        circleOptions.fillColor(Color.argb(64, 255, 0, 0));
        circleOptions.strokeWidth(4);
        circleOptions.strokeColor(Color.argb(255, 255, 0, 0));
        circleOptions.center(latLng);
        mMap.addCircle(circleOptions);
    }

}