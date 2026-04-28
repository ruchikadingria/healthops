package com.example.healthops;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.healthops.fragments.DutyFragment;
import com.example.healthops.fragments.HomeFragment;
import com.example.healthops.fragments.MedicineFragment;
import com.example.healthops.fragments.NotesFragment;
import com.example.healthops.fragments.ProfileFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class DashboardActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    // Location
    private static final int LOCATION_PERMISSION_CODE = 1001;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleManager.applyLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        bottomNav = findViewById(R.id.bottomNav);

        loadFragment(new HomeFragment());

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (item.getItemId() == R.id.nav_duty) {
                selectedFragment = new DutyFragment();
            } else if (item.getItemId() == R.id.nav_notes) {
                selectedFragment = new NotesFragment();
            } else if (item.getItemId() == R.id.nav_medicine) {
                selectedFragment = new MedicineFragment();
            } else if (item.getItemId() == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }

            return true;
        });

        // 🔥 INIT LOCATION CLIENT
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 🔥 ASK PERMISSION AFTER LOGIN (Dashboard opens only after login)
        checkLocationPermission();
    }

    // ==============================
    // LOCATION PERMISSION
    // ==============================
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            getUserLocation();

        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_CODE
            );
        }
    }

    // ==============================
    // HANDLE USER RESPONSE (IMPORTANT)
    // ==============================
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_CODE) {

            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                getUserLocation();

            } else {
                Toast.makeText(this,
                        "Location permission denied",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ==============================
    // GET LOCATION
    // ==============================
    private void getUserLocation() {

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {

                    if (location != null) {

                        double lat = location.getLatitude();
                        double lng = location.getLongitude();

                        saveLocationToFirestore(lat, lng);

                    } else {
                        Toast.makeText(this,
                                "Turn on GPS to get location",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ==============================
    // SAVE TO FIRESTORE
    // ==============================
    private void saveLocationToFirestore(double lat, double lng) {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .update(
                        "latitude", lat,
                        "longitude", lng
                )
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this,
                            "Location updated",
                            Toast.LENGTH_SHORT).show();
                });
    }

    // ==============================
    // NAVIGATION HELPERS
    // ==============================
    public void navigateTo(int bottomNavItemId) {
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(bottomNavItemId);
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}