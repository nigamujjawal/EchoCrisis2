package com.uj.echocrisismain;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;

/**
 * Application class to initialize Firebase App Check with debug provider.
 */
public class EchoCrisisApp extends Application {

    private static final String TAG = "EchoCrisisApp";

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "Initializing EchoCrisisApp...");

        // Initialize Firebase
        FirebaseApp.initializeApp(this);

        // Initialize App Check with debug provider
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance());

        Log.d(TAG, "Firebase App Check initialized with debug provider");
    }
}
