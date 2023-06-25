package com.sanjeev.face_geofencing;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.sanjeev.face_geofencing.Request.AuthenticationRepository;

public class Authentication extends AppCompatActivity {
    // Request code for the Wi-Fi state permission
    private static final int REQUEST_CODE_WIFI_STATE_PERMISSION = 1000;

    // Button for email login
    private Button EmailLogin;

    // Repository for authentication
    private AuthenticationRepository authenticationRepository;

    // TextInputEditText fields for email and password
    private TextInputEditText EmailTextInputEditText, PasswordTextInputEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        // Set the app's default night mode to MODE_NIGHT_NO (disables night mode)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Initialize the authentication repository
        authenticationRepository = new AuthenticationRepository();

        // Find and assign the email login button from the layout
        EmailLogin = findViewById(R.id.button);

        // Find and assign the email text input field from the layout
        EmailTextInputEditText = findViewById(R.id.EmailEditText);

        // Find and assign the password text input field from the layout
        PasswordTextInputEditText = findViewById(R.id.PasswordEditText);

        // Set an OnClickListener for the email login button
        EmailLogin.setOnClickListener(v -> GetToken());
    }


    private void GetToken() {
        String username = EmailTextInputEditText.getText().toString().trim();
        String password = PasswordTextInputEditText.getText().toString().trim();

        // Check if the ACCESS_WIFI_STATE permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED) {
            // Permission is granted, proceed with retrieving the token
            authenticationRepository.requestToken(username, password, getApplicationContext(), new AuthenticationRepository.OnTokenRequestListener() {
                @Override
                public void onTokenRequestSuccess(String token) {
                    // Handle successful token request
                    // Create an intent to start the FaceDetection activity
                    Intent FaceDetectionIntent = new Intent(Authentication.this, FaceDetection.class);
                    // Pass the token to the FaceDetection activity
                    FaceDetectionIntent.putExtra("Token", token);
                    // Start the FaceDetection activity
                    startActivity(FaceDetectionIntent);
                }

                @Override
                public void onTokenRequestError() {
                    // Handle token request error, e.g., display an error message
                }
            });

            // Save or use the token as needed
        } else {
            // Permission is not granted, request the ACCESS_WIFI_STATE permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_WIFI_STATE}, REQUEST_CODE_WIFI_STATE_PERMISSION);
        }
    }


}