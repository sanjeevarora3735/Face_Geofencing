# Face Geofencing Android App

![App Logo](https://developer.android.com/static/images/training/geofence_2x.png)

## Introduction

The Face Geofencing Android App is a powerful and secure mobile application that combines face detection, geofencing, and server integration to enhance safety and control in various scenarios. The app provides features for user authentication, real-time face detection using the Camera2 API, counting the number of faces detected, and communicating with a server using API endpoints.

## Features

- User Login: Secure login functionality with username and password fields.
- Authentication: Utilizes the phone's MAC address as input for authentication.
- Face Detection: Implements face detection using the Camera2 API and an open-source Android face detection library.
- Face Counting: Tracks and counts the number of faces detected in the live camera view over time.
- Server Integration: Utilizes API endpoints to communicate with a server and send the face count data.
- Geofencing: Implements geofencing functionality using a circular shape on a map.
- Geofence Marking: Allows users to interact with the map interface to mark a geofence circle.
- Geofence Control: Restricts face detection to occur only within the marked geofence area.
- Safety and Control: Enhances safety and control by regulating face detection within a designated area.

## Installation

1. Clone the repository: `git clone https://github.com/sanjeevarora3735/Face_Geofencing.git`
2. Open the project in Android Studio.
3. Build and run the app on your Android device or emulator.

## Requirements

- Android SDK version 26 or higher.
- Minimum SDK version 26.
- Required permissions for camera access and location services.

## Usage

1. Launch the app on your Android device.
2. Login using your username and password.
3. The app will automatically activate face detection and start counting the number of faces within the geofence area.
4. Interact with the map interface to mark the geofence circle.
5. Face detection will only occur within the marked geofence area.
6. The face count data will be sent to the server using the provided API endpoint.

## Contributions

Contributions are welcome! If you find any issues or have suggestions for improvements, please feel free to submit a pull request or create an issue in the repository.

