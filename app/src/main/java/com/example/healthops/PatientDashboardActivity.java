package com.example.healthops;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.healthops.fragments.PatientAppointmentsFragment;
import com.example.healthops.fragments.PatientHomeFragment;
import com.example.healthops.fragments.PatientNotesFragment;
import com.example.healthops.fragments.PatientVisitsFragment;
import com.example.healthops.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class PatientDashboardActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleManager.applyLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_dashboard);

        bottomNav = findViewById(R.id.bottomNav);
        loadFragment(new PatientHomeFragment());

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            int id = item.getItemId();
            if (id == R.id.nav_patient_home) {
                selected = new PatientHomeFragment();
            } else if (id == R.id.nav_patient_appointments) {
                selected = new PatientAppointmentsFragment();
            } else if (id == R.id.nav_patient_visits) {
                selected = new PatientVisitsFragment();
            } else if (id == R.id.nav_patient_notes) {
                selected = new PatientNotesFragment();
            } else if (id == R.id.nav_patient_profile) {
                selected = new ProfileFragment();
            }
            if (selected != null) {
                loadFragment(selected);
            }
            return true;
        });
    }

    public void navigateTo(int menuItemId) {
        bottomNav.setSelectedItemId(menuItemId);
    }

    public void loadFragment(@NonNull Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
