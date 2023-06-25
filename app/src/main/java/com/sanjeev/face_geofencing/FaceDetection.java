package com.sanjeev.face_geofencing;

import static com.sanjeev.face_geofencing.Helper.MacAddress.getSavedMacAddress;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.Face;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sanjeev.face_geofencing.Helper.SharedPreferenceHelper;
import com.sanjeev.face_geofencing.Request.AuthenticationRepository;

import java.util.Collections;

public class FaceDetection extends AppCompatActivity {
    // Request code for camera permission
    private static final int REQUEST_CAMERA_PERMISSION = 200;

    // Request code for fine location access
    private static final int FINE_LOCATION_ACCESS_REQUEST_CODE = 96413;

    // Request code for background location access
    private static final int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 96414;

    // Helper class for managing shared preferences
    private SharedPreferenceHelper helper;

    // TextView for displaying the face count
    private TextView FaceCount;

    // TextView for displaying the device's MAC address
    private TextView DeviceMacAddress;

    // TextView for displaying error messages
    private TextView Error;

    // Capture callback for camera capture session
    private final CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            try {
                // Retrieve faces detected in the captured frame
                Face[] faces = result.get(CaptureResult.STATISTICS_FACES);
                int numFaces;
                if (faces != null && faces.length > 0) {
                    numFaces = faces.length;
                    if (helper.getBooleanValue()) {
                        // Retrieve the token passed from the previous activity
                        String Token = getIntent().getStringExtra("token");
                        // Mark attendance by making a POST request to the server
                        new AuthenticationRepository().markAttendance(Token, "25-06-2023", numFaces, getSavedMacAddress(getApplicationContext()));
                        Toast.makeText(FaceDetection.this, "POST request successful in GeoFence area.", Toast.LENGTH_SHORT).show();
                    } else {
                        // Display a toast message indicating that the POST request could not be fulfilled
                        runOnUiThread(() -> Toast.makeText(FaceDetection.this, "Unable to fulfill POST request. Not in GeoFence area.", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    numFaces = 0;
                }
                // Update the face count text view with the number of faces detected
                runOnUiThread(() -> FaceCount.setText(String.valueOf(numFaces)));
            } catch (Exception e) {
                Log.d("FaceDetection", e.getMessage());
            }
        }
    };

    // Variables for camera setup and configuration
    private int width, height;
    private TextureView textureView;
    private CameraDevice cameraDevice;
    private CaptureRequest.Builder captureRequestBuilder;
    private CameraCaptureSession cameraCaptureSession;
    private Handler backgroundHandler;

    // Camera device state callback
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    // TextureView surface texture listener
    private final TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
            // Handle surface texture size change if needed
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            // Handle surface texture update if needed
        }
    };

    // Background thread for camera operations
    private HandlerThread backgroundThread;

    // FloatingActionButton for marking geofencing
    private FloatingActionButton GeofencingMarkingFloatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_detection);

        // Set the app's default night mode to MODE_NIGHT_NO (disables night mode)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Find and assign the FaceCount TextView from the layout
        FaceCount = findViewById(R.id.FaceCount);

        // Find and assign the Error TextView from the layout
        Error = findViewById(R.id.Error);
        Error.setText("");

        // Find and assign the DeviceMacAddress TextView from the layout
        DeviceMacAddress = findViewById(R.id.MacAddress);

        // Find and assign the GeofencingMarkingFloatingActionButton from the layout
        GeofencingMarkingFloatingActionButton = findViewById(R.id.GeoFencingMarking);

        // Set an OnClickListener for the GeofencingMarkingFloatingActionButton
        GeofencingMarkingFloatingActionButton.setOnClickListener(v -> {
            // Start the GeoFencing activity
            startActivity(new Intent(FaceDetection.this, GeoFencing.class));
        });

        // Request location permission
        RequestingLocationPermission();

        // Initialize the shared preference helper
        helper = new SharedPreferenceHelper(getApplicationContext());
        helper.setBooleanValue(false);

        // Set the device's MAC address in the DeviceMacAddress TextView
        DeviceMacAddress.setText(getSavedMacAddress(getApplicationContext()));

        // Find and assign the TextureView from the layout
        textureView = findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(textureListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        if (textureView.isAvailable()) {
            // If the texture view is already available, open the camera
            openCamera();
        } else {
            // Set the surface texture listener if the texture view is not yet available
            textureView.setSurfaceTextureListener(textureListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Close the camera and stop the background thread
        closeCamera();
        stopBackgroundThread();
    }

    private void openCamera() {
        // Get the camera manager
        CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            // Get the ID of the first camera
            String cameraId = manager.getCameraIdList()[0];

            // Get the characteristics of the camera
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

            // Get the supported output sizes for JPEG format
            Size[] jpegSizes = null;
            if (characteristics != null) {
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            }

            // Set default width and height values
            width = 640;
            height = 480;

            // Calculate the aspect ratio of the TextureView
            float viewAspectRatio = (float) textureView.getWidth() / textureView.getHeight();

            // Find the best matching preview size based on aspect ratio
            if (jpegSizes != null && jpegSizes.length > 0) {
                for (Size size : jpegSizes) {
                    float sizeAspectRatio = (float) size.getWidth() / size.getHeight();
                    if (Math.abs(sizeAspectRatio - viewAspectRatio) <= 0.1) {
                        width = size.getWidth();
                        height = size.getHeight();
                        break;
                    }
                }
            }

            // Check camera permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // Request camera permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                return;
            }

            // Open the camera
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void RequestingLocationPermission() {
        // Check fine location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request fine location permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
        }

        // Check background location permission for Android 11 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request background location permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
        }

        // Permission is already granted, continue with your logic
        // ...
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Fine_Location Granted", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == BACKGROUND_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // We have the permission
                Toast.makeText(this, "You can add geofences...", Toast.LENGTH_SHORT).show();
            } else {
                // We do not have the permission
                Toast.makeText(this, "Background location access is necessary for geofences to trigger...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void createCameraPreview() {
        try {
            // Get the SurfaceTexture from the TextureView
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(width, height);
            Surface surface = new Surface(texture);

            // Create a capture request builder for preview
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            captureRequestBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, CaptureRequest.STATISTICS_FACE_DETECT_MODE_FULL);

            // Create a capture session with the surface
            cameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (cameraDevice == null) return;
                    cameraCaptureSession = session;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Toast.makeText(FaceDetection.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();
    }

    private void updatePreview() {
        if (cameraDevice == null) Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), captureCallback, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (cameraCaptureSession != null) {
            cameraCaptureSession.close();
            cameraCaptureSession = null;
        }
    }

    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("Camera Background");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        if (backgroundThread != null) {
            backgroundThread.quitSafely();
            try {
                backgroundThread.join();
                backgroundThread = null;
                backgroundHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}