package com.example.healthops;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final int SPLASH_TIME = 2500; // 2.5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Handler().postDelayed(() -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            
            if (currentUser != null) {
                // User is logged in, check role from SharedPreferences
                if (SessionPreferences.isPatient(this)) {
                    startActivity(new Intent(MainActivity.this, PatientDashboardActivity.class));
                } else {
                    startActivity(new Intent(MainActivity.this, DashboardActivity.class));
                }
            } else {
                // No user logged in, go to Login
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
            finish();
        }, SPLASH_TIME);
    }
}
