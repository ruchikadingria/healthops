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

import com.example.healthops.DashboardActivity;
import com.example.healthops.FileViewerActivity;
import com.example.healthops.LocaleManager;
import com.example.healthops.R;
import com.example.healthops.SessionPreferences;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Apply saved language
        LocaleManager.applyLanguage(requireContext());

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        TextView greeting = view.findViewById(R.id.greeting);
        String name = SessionPreferences.getDisplayName(requireContext());
        greeting.setText(getString(R.string.greeting_staff, name));

        // ✅ SHIFT CARD CLICK
        view.findViewById(R.id.shiftCard).setOnClickListener(v -> {
            Intent i = FileViewerActivity.newIntent(
                    requireContext(),
                    getString(R.string.home_shift_title),
                    getString(R.string.home_shift_time_line),
                    getString(R.string.home_shift_subtitle),
                    getString(R.string.home_shift_body),
                    getString(R.string.file_label_location),
                    getString(R.string.home_shift_location),
                    getString(R.string.file_label_tasks),
                    getString(R.string.home_shift_tasks),
                    null
            );
            startActivity(i);
        });

        // DUTY BUTTON
        Button dutyOpen = view.findViewById(R.id.btnHomeDutyOpen);
        dutyOpen.setOnClickListener(v -> {
            if (getActivity() instanceof DashboardActivity) {
                ((DashboardActivity) getActivity()).navigateTo(R.id.nav_duty);
            }
        });

        // MEDICINE BUTTON (Back by popular demand)
        Button medicineOpen = view.findViewById(R.id.btnHomeMedicineOpen);
        medicineOpen.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new MedicineFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // NOTES BUTTON
        Button notesOpen = view.findViewById(R.id.btnHomeNotesOpen);
        notesOpen.setOnClickListener(v -> {
            if (getActivity() instanceof DashboardActivity) {
                ((DashboardActivity) getActivity()).navigateTo(R.id.nav_notes);
            }
        });

        // RESOURCES BUTTON
        Button resourcesOpen = view.findViewById(R.id.btnHomeResourcesOpen);
        resourcesOpen.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new ResourceFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }
}
