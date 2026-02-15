package com.example.healthops;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int SPLASH_TIME = 2500; // 2.5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Redirect to Login after splash delay
        new Handler().postDelayed(() -> {

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);

            // Close splash so user can't return to it
            finish();

        }, SPLASH_TIME);
    }
}
