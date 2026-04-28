package com.example.healthops.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.healthops.LocaleManager;
import com.example.healthops.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResourceFragment extends Fragment {

    private LinearLayout bedsContainer;
    private LinearLayout icuContainer;
    private LinearLayout equipmentContainer;
    private FirebaseFirestore db;
    private static final String TAG = "ResourceFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Apply saved language
        LocaleManager.applyLanguage(requireContext());

        View view = inflater.inflate(R.layout.fragment_resources, container, false);

        bedsContainer = view.findViewById(R.id.bedsContainer);
        icuContainer = view.findViewById(R.id.icuContainer);
        equipmentContainer = view.findViewById(R.id.equipmentContainer);
        db = FirebaseFirestore.getInstance();

        // Load all resources
        loadBeds();
        loadICU();
        loadEquipment();

        return view;
    }

    private void loadBeds() {
        bedsContainer.removeAllViews();

        db.collection("resources")
                .document("beds")
                .collection("bed_list")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading beds: " + error.getMessage());
                        return;
                    }

                    bedsContainer.removeAllViews();

                    if (value == null || value.isEmpty()) {
                        initializeBeds();
                        return;
                    }

                    for (QueryDocumentSnapshot doc : value) {
                        addBedCard(doc.getId(), doc);
                    }
                });
    }

    private void initializeBeds() {
        // Initialize default beds if collection is empty
        List<Map<String, Object>> defaultBeds = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Map<String, Object> bed = new HashMap<>();
            bed.put("bed_id", "BED-" + i);
            bed.put("status", "vacant");
            bed.put("patient_id", "");
            bed.put("ward", "Ward A");
            bed.put("lastUpdated", com.google.firebase.Timestamp.now());

            db.collection("resources")
                    .document("beds")
                    .collection("bed_list")
                    .document("BED-" + i)
                    .set(bed);
        }
        loadBeds();
    }

    private void addBedCard(String bedId, QueryDocumentSnapshot doc) {
        String status = doc.getString("status");
        String patientId = doc.getString("patient_id");
        String ward = doc.getString("ward");

        LinearLayout bedCard = new LinearLayout(requireContext());
        bedCard.setOrientation(LinearLayout.VERTICAL);
        bedCard.setPadding(20, 20, 20, 20);
        bedCard.setBackgroundResource(R.drawable.white_card);
        bedCard.setTag(bedId);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 10, 0, 10);
        bedCard.setLayoutParams(cardParams);

        // Bed ID
        TextView bedIdView = new TextView(requireContext());
        bedIdView.setText("Bed: " + bedId);
        bedIdView.setTextSize(16);
        bedIdView.setTypeface(null, android.graphics.Typeface.BOLD);
        bedCard.addView(bedIdView);

        // Ward
        TextView wardView = new TextView(requireContext());
        wardView.setText("Ward: " + ward);
        wardView.setTextSize(12);
        wardView.setPadding(0, 10, 0, 0);
        wardView.setTextColor(0xFF666666);
        bedCard.addView(wardView);

        // Status Spinner
        Spinner statusSpinner = new Spinner(requireContext());
        String[] statuses = {"vacant", "occupied", "maintenance"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                statuses
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(statusAdapter);

        // Set current status
        int statusIndex = java.util.Arrays.asList(statuses).indexOf(status);
        statusSpinner.setSelection(statusIndex >= 0 ? statusIndex : 0);

        statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String newStatus = statuses[position];
                if (!newStatus.equals(status)) {
                    updateBedStatus(bedId, newStatus);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        spinnerParams.setMargins(0, 15, 0, 0);
        statusSpinner.setLayoutParams(spinnerParams);
        bedCard.addView(statusSpinner);

        // Patient ID (if occupied)
        if ("occupied".equals(status) && patientId != null && !patientId.isEmpty()) {
            TextView patientView = new TextView(requireContext());
            patientView.setText("Patient: " + patientId);
            patientView.setTextSize(12);
            patientView.setPadding(0, 10, 0, 0);
            patientView.setTextColor(0xFFFF6B6B);
            bedCard.addView(patientView);
        }

        bedsContainer.addView(bedCard);
    }

    private void updateBedStatus(String bedId, String newStatus) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("status", newStatus);
        updateData.put("lastUpdated", com.google.firebase.Timestamp.now());

        db.collection("resources")
                .document("beds")
                .collection("bed_list")
                .document(bedId)
                .update(updateData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Bed " + bedId + " status updated to " + newStatus);
                    Toast.makeText(requireContext(), "Bed status updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating bed: " + e.getMessage());
                    Toast.makeText(requireContext(), "Error updating bed", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadICU() {
        icuContainer.removeAllViews();

        db.collection("resources")
                .document("icu")
                .collection("icu_beds")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading ICU: " + error.getMessage());
                        return;
                    }

                    icuContainer.removeAllViews();

                    if (value == null || value.isEmpty()) {
                        initializeICU();
                        return;
                    }

                    for (QueryDocumentSnapshot doc : value) {
                        addICUCard(doc.getId(), doc);
                    }
                });
    }

    private void initializeICU() {
        List<Map<String, Object>> defaultICU = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Map<String, Object> icuBed = new HashMap<>();
            icuBed.put("bed_id", "ICU-" + i);
            icuBed.put("status", "vacant");
            icuBed.put("patient_id", "");
            icuBed.put("ventilator", false);
            icuBed.put("lastUpdated", com.google.firebase.Timestamp.now());

            db.collection("resources")
                    .document("icu")
                    .collection("icu_beds")
                    .document("ICU-" + i)
                    .set(icuBed);
        }
        loadICU();
    }

    private void addICUCard(String icuId, QueryDocumentSnapshot doc) {
        String status = doc.getString("status");
        Boolean hasVentilator = doc.getBoolean("ventilator");

        LinearLayout icuCard = new LinearLayout(requireContext());
        icuCard.setOrientation(LinearLayout.VERTICAL);
        icuCard.setPadding(20, 20, 20, 20);
        icuCard.setBackgroundResource(R.drawable.white_card);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 10, 0, 10);
        icuCard.setLayoutParams(cardParams);

        // ICU ID
        TextView icuIdView = new TextView(requireContext());
        icuIdView.setText("ICU Bed: " + icuId);
        icuIdView.setTextSize(16);
        icuIdView.setTypeface(null, android.graphics.Typeface.BOLD);
        icuCard.addView(icuIdView);

        // Status Spinner
        Spinner statusSpinner = new Spinner(requireContext());
        String[] statuses = {"vacant", "occupied"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                statuses
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(statusAdapter);

        int statusIndex = java.util.Arrays.asList(statuses).indexOf(status);
        statusSpinner.setSelection(statusIndex >= 0 ? statusIndex : 0);

        statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String newStatus = statuses[position];
                if (!newStatus.equals(status)) {
                    updateICUStatus(icuId, newStatus);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        spinnerParams.setMargins(0, 15, 0, 0);
        statusSpinner.setLayoutParams(spinnerParams);
        icuCard.addView(statusSpinner);

        // Ventilator Status
        TextView ventilatorView = new TextView(requireContext());
        ventilatorView.setText("Ventilator: " + (hasVentilator != null && hasVentilator ? "In Use" : "Available"));
        ventilatorView.setTextSize(12);
        ventilatorView.setPadding(0, 10, 0, 0);
        ventilatorView.setTextColor(hasVentilator != null && hasVentilator ? 0xFFFF6B6B : 0xFF51CF66);
        icuCard.addView(ventilatorView);

        icuContainer.addView(icuCard);
    }

    private void updateICUStatus(String icuId, String newStatus) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("status", newStatus);
        updateData.put("lastUpdated", com.google.firebase.Timestamp.now());

        db.collection("resources")
                .document("icu")
                .collection("icu_beds")
                .document(icuId)
                .update(updateData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "ICU " + icuId + " status updated to " + newStatus);
                    Toast.makeText(requireContext(), "ICU status updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating ICU: " + e.getMessage());
                    Toast.makeText(requireContext(), "Error updating ICU", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadEquipment() {
        equipmentContainer.removeAllViews();

        db.collection("resources")
                .document("equipment")
                .collection("ventilators")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading equipment: " + error.getMessage());
                        return;
                    }

                    equipmentContainer.removeAllViews();

                    if (value == null || value.isEmpty()) {
                        initializeEquipment();
                        return;
                    }

                    for (QueryDocumentSnapshot doc : value) {
                        addEquipmentCard(doc.getId(), doc);
                    }
                });
    }

    private void initializeEquipment() {
        List<Map<String, Object>> defaultEquipment = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            Map<String, Object> equipment = new HashMap<>();
            equipment.put("equipment_id", "VENTILATOR-" + i);
            equipment.put("status", "available");
            equipment.put("location", "ICU Room A");
            equipment.put("lastUpdated", com.google.firebase.Timestamp.now());

            db.collection("resources")
                    .document("equipment")
                    .collection("ventilators")
                    .document("VENTILATOR-" + i)
                    .set(equipment);
        }
        loadEquipment();
    }

    private void addEquipmentCard(String equipmentId, QueryDocumentSnapshot doc) {
        String status = doc.getString("status");
        String location = doc.getString("location");

        LinearLayout equipCard = new LinearLayout(requireContext());
        equipCard.setOrientation(LinearLayout.VERTICAL);
        equipCard.setPadding(20, 20, 20, 20);
        equipCard.setBackgroundResource(R.drawable.white_card);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 10, 0, 10);
        equipCard.setLayoutParams(cardParams);

        // Equipment ID
        TextView equipIdView = new TextView(requireContext());
        equipIdView.setText("Equipment: " + equipmentId);
        equipIdView.setTextSize(16);
        equipIdView.setTypeface(null, android.graphics.Typeface.BOLD);
        equipCard.addView(equipIdView);

        // Location
        TextView locView = new TextView(requireContext());
        locView.setText("Location: " + location);
        locView.setTextSize(12);
        locView.setPadding(0, 10, 0, 0);
        locView.setTextColor(0xFF666666);
        equipCard.addView(locView);

        // Status Spinner
        Spinner statusSpinner = new Spinner(requireContext());
        String[] statuses = {"available", "in_use", "maintenance"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                statuses
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(statusAdapter);

        int statusIndex = java.util.Arrays.asList(statuses).indexOf(status);
        statusSpinner.setSelection(statusIndex >= 0 ? statusIndex : 0);

        statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String newStatus = statuses[position];
                if (!newStatus.equals(status)) {
                    updateEquipmentStatus(equipmentId, newStatus);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        spinnerParams.setMargins(0, 15, 0, 0);
        statusSpinner.setLayoutParams(spinnerParams);
        equipCard.addView(statusSpinner);

        equipmentContainer.addView(equipCard);
    }

    private void updateEquipmentStatus(String equipmentId, String newStatus) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("status", newStatus);
        updateData.put("lastUpdated", com.google.firebase.Timestamp.now());

        db.collection("resources")
                .document("equipment")
                .collection("ventilators")
                .document(equipmentId)
                .update(updateData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Equipment " + equipmentId + " status updated to " + newStatus);
                    Toast.makeText(requireContext(), "Equipment status updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating equipment: " + e.getMessage());
                    Toast.makeText(requireContext(), "Error updating equipment", Toast.LENGTH_SHORT).show();
                });
    }
}
