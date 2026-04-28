package com.example.healthops.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.healthops.DashboardActivity;
import com.example.healthops.LocaleManager;
import com.example.healthops.LoginActivity;
import com.example.healthops.PatientDashboardActivity;
import com.example.healthops.PatientLoginActivity;
import com.example.healthops.R;
import com.example.healthops.SessionPreferences;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Apply saved language
        LocaleManager.applyLanguage(requireContext());

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

        view.findViewById(R.id.rowLanguage).setOnClickListener(v -> showLanguageDialog());
        view.findViewById(R.id.rowLogout).setOnClickListener(v -> logout());

        return view;
    }

    private void showLanguageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.select_language);

        String[] languages = {"English", "हिंदी (Hindi)"};
        String[] languageCodes = {"en", "hi"};

        // Get current language
        String currentLanguage = LocaleManager.getLanguage(requireContext());
        int currentSelection = currentLanguage.equals("hi") ? 1 : 0;

        builder.setSingleChoiceItems(languages, currentSelection, (dialog, which) -> {
            String selectedLanguage = languageCodes[which];
            LocaleManager.setLocale(requireContext(), selectedLanguage);
            Toast.makeText(requireContext(), R.string.language_changed, Toast.LENGTH_SHORT).show();

            // Restart the entire activity to reload all fragments with new language
            if (getActivity() instanceof DashboardActivity) {
                DashboardActivity activity = (DashboardActivity) getActivity();
                activity.recreate();
            } else if (getActivity() instanceof PatientDashboardActivity) {
                PatientDashboardActivity activity = (PatientDashboardActivity) getActivity();
                activity.recreate();
            }
            dialog.dismiss();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
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
