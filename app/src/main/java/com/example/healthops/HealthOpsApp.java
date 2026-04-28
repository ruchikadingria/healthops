package com.example.healthops;

import android.app.Application;

import com.google.firebase.FirebaseApp;

public class HealthOpsApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
    }
}
