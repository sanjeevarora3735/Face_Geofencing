package com.sanjeev.face_geofencing.Request;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import com.sanjeev.face_geofencing.Helper.MacAddress;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class AuthenticationRepository {

    private static final String TAG = AuthenticationRepository.class.getSimpleName();
    private static final String LOGIN_API_ENDPOINT = "http://91.203.133.225:8000/api/new-attendance/device/login";
    private static final String MARK_API_ENDPOINT = "http://91.203.133.225:8000/api/new-attendance/mark";

    // Method to request a token for authentication
    public void requestToken(String username, String password, Context context, OnTokenRequestListener listener) {
        JSONObject requestBody = new JSONObject();
        new MacAddress().saveMacAddress(context, getMacAddress(context));
        try {
            requestBody.put("username", username);
            requestBody.put("device_mac", getMacAddress(context));
            requestBody.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Perform the network request on a separate thread
        new Thread(() -> {
            try {
                URL url = new URL(LOGIN_API_ENDPOINT);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(requestBody.toString().getBytes());
                outputStream.flush();
                outputStream.close();

                int statusCode = connection.getResponseCode();
                if (statusCode == HttpURLConnection.HTTP_OK || statusCode == HttpURLConnection.HTTP_CREATED) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    if (listener != null) {
                        JSONObject jsonResponse = new JSONObject(response.toString());
                        String token = jsonResponse.optString("token");
                        listener.onTokenRequestSuccess(token);
                    }
                } else {
                    Log.e(TAG, "Request failed with status code: " + statusCode);
                    if (listener != null) {
                        listener.onTokenRequestError();
                    }
                }

                connection.disconnect();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                if (listener != null) {
                    listener.onTokenRequestError();
                }
            }
        }).start();
    }

    // Method to mark attendance using the token
    public void markAttendance(String token, String date, int count, String deviceMac) {
        try {
            // Create the JSON request body
            JSONObject requestBody = new JSONObject();
            requestBody.put("m_dat", date);
            requestBody.put("count", count);
            requestBody.put("device_mac", deviceMac);
            requestBody.put("eData", new JSONArray());

            // Create the HTTP connection
            URL url = new URL(MARK_API_ENDPOINT);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "bearer " + token);

            // Enable writing the request body
            connection.setDoOutput(true);

            // Write the request body to the connection's output stream
            OutputStream outputStream = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
            writer.write(requestBody.toString());
            writer.flush();
            writer.close();
            outputStream.close();

            // Send the request and check the response code
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                // Request successful
                Log.d(TAG, "We have updated the numface count in the mark API");
                // Handle the response as needed
            } else {
                // Request failed
                // Handle the error response
            }

            // Close the connection
            connection.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Interface for token request callbacks
    public interface OnTokenRequestListener {
        void onTokenRequestSuccess(String token);

        void onTokenRequestError();
    }

    // Method to get the MAC address of the device
    public String getMacAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String macAddress = wifiInfo.getMacAddress();

        // On newer Android versions, if the MAC address is unavailable, use the WLAN MAC address instead
        if (macAddress == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            macAddress = wifiInfo.getMacAddress();
        }

        return macAddress;
    }
}
