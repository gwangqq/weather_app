package io.abx.myapplication;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.igaworks.v2.core.AdBrixRm;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token) {
        Log.d("jake", "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.

        // Send token data to Adbrix
        AdBrixRm.setRegistrationId(token);
    }

}