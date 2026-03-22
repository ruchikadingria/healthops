package com.example.healthops.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.healthops.LoginActivity;
import com.example.healthops.PatientLoginActivity;
import com.example.healthops.R;
import com.example.healthops.SessionPreferences;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        TextView name = view.findViewById(R.id.profileName);
        TextView subtitle = view.findViewById(R.id.profileSubtitle);
        String display = SessionPreferences.getDisplayName(requireContext());
        name.setText(display);
        if (SessionPreferences.isPatient(requireContext())) {
            subtitle.setText(R.string.profile_subtitle_patient);
        } else {
            subtitle.setText(R.string.profile_subtitle_staff);
        }

        view.findViewById(R.id.rowSaved).setOnClickListener(v ->
                Toast.makeText(requireContext(), R.string.toast_saved, Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.rowAppointment).setOnClickListener(v ->
                Toast.makeText(requireContext(), R.string.toast_appointments, Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.rowFaqs).setOnClickListener(v ->
                Toast.makeText(requireContext(), R.string.toast_faqs, Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.rowLogout).setOnClickListener(v -> logout());

        return view;
    }

    private void logout() {
        boolean patient = SessionPreferences.isPatient(requireContext());
        
        // Sign out from Firebase
        FirebaseAuth.getInstance().signOut();
        // Clear local session
        SessionPreferences.clear(requireContext());

        Class<?> dest = patient ? PatientLoginActivity.class : LoginActivity.class;
        Intent i = new Intent(requireContext(), dest);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        requireActivity().finish();
    }
}
