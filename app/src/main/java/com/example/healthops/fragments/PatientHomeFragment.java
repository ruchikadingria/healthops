package com.example.healthops.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.healthops.FileViewerActivity;
import com.example.healthops.PatientDashboardActivity;
import com.example.healthops.R;
import com.example.healthops.SessionPreferences;

public class PatientHomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_home, container, false);

        TextView greeting = view.findViewById(R.id.patientGreeting);
        String name = SessionPreferences.getDisplayName(requireContext());
        greeting.setText(getString(R.string.greeting_patient, name));

        view.findViewById(R.id.btnOpenNextVisit).setOnClickListener(v -> {
            Intent i = FileViewerActivity.newIntent(requireContext(),
                    "Outpatient visit — Dr. Rao",
                    "Tue 10:30 AM — Clinic B, Room 3",
                    "Upcoming appointment",
                    "Please arrive 15 minutes early with your insurance card and current medications list.",
                    "Check-in",
                    "Front desk — Clinic B",
                    "Preparation",
                    "Fast only if instructed by your care team", null);
            i.putExtra(FileViewerActivity.EXTRA_TIME_LABEL, getString(R.string.file_label_appointment_time));
            startActivity(i);
        });

        view.findViewById(R.id.btnOpenLabs).setOnClickListener(v -> {
            Intent i = FileViewerActivity.newIntent(requireContext(),
                    "Lab results summary",
                    "Updated Mar 18, 2025",
                    "Recent labs",
                    "Complete blood count and basic metabolic panel reviewed by your physician. No critical values.",
                    "Ordering provider",
                    "Dr. Rao",
                    "Follow-up",
                    "Routine follow-up at next scheduled visit", null);
            i.putExtra(FileViewerActivity.EXTRA_TIME_LABEL, getString(R.string.file_label_updated));
            startActivity(i);
        });

        Button notesBtn = view.findViewById(R.id.btnOpenMyNotes);
        notesBtn.setOnClickListener(v -> {
            if (getActivity() instanceof PatientDashboardActivity) {
                ((PatientDashboardActivity) getActivity()).navigateTo(R.id.nav_patient_notes);
            }
        });

        return view;
    }
}
