package com.ghosh.trainrot;

import android.app.Application;
import dagger.hilt.android.HiltAndroidApp;
import com.google.firebase.FirebaseApp;

@HiltAndroidApp
public class TrainRotApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize Firebase
        FirebaseApp.initializeApp(this);
    }
} 