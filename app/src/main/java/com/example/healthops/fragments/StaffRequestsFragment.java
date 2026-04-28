package com.example.healthops.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.healthops.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Calendar;
import java.util.Locale;

public class StaffRequestsFragment extends Fragment {

    private LinearLayout requestsContainer;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_staff_requests, container, false);
        requestsContainer = view.findViewById(R.id.requestsContainer);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        fetchRequests();

        return view;
    }

    private void fetchRequests() {
        if (mAuth.getCurrentUser() == null) return;
        String currentUserEmail = mAuth.getCurrentUser().getEmail();

        // Fetch requests where the logged-in doctor's email matches the request's doctorEmail
        db.collection("appointments")
                .whereEqualTo("doctorEmail", currentUserEmail)
                .whereEqualTo("status", "pending")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        requestsContainer.removeAllViews();
                        if (task.getResult().isEmpty()) {
                            TextView tvNoRequests = new TextView(getContext());
                            tvNoRequests.setText("No pending requests");
                            tvNoRequests.setPadding(32, 32, 32, 32);
                            requestsContainer.addView(tvNoRequests);
                        } else {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                addRequestCard(document);
                            }
                        }
                    } else {
                        Toast.makeText(getContext(), "Error fetching requests", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addRequestCard(QueryDocumentSnapshot doc) {
        String requestId = doc.getId();
        String patientEmail = doc.getString("patientEmail");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);
        layout.setBackgroundResource(R.drawable.white_card);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 24);
        layout.setLayoutParams(params);

        TextView tvPatient = new TextView(getContext());
        tvPatient.setText("Appointment Request from: " + patientEmail);
        tvPatient.setTextSize(16f);
        tvPatient.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));

        LinearLayout btnLayout = new LinearLayout(getContext());
        btnLayout.setOrientation(LinearLayout.HORIZONTAL);
        btnLayout.setPadding(0, 16, 0, 0);

        Button btnApprove = new Button(getContext());
        btnApprove.setText("Approve");
        btnApprove.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.primary));
        btnApprove.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        btnApprove.setOnClickListener(v -> showDateTimePicker(requestId));

        Button btnReject = new Button(getContext());
        btnReject.setText("Reject");
        btnReject.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.status_critical));
        btnReject.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        LinearLayout.LayoutParams rejectParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rejectParams.setMargins(16, 0, 0, 0);
        btnReject.setLayoutParams(rejectParams);
        btnReject.setOnClickListener(v -> updateStatus(requestId, "rejected", null));

        btnLayout.addView(btnApprove);
        btnLayout.addView(btnReject);

        layout.addView(tvPatient);
        layout.addView(btnLayout);

        requestsContainer.addView(layout);
    }

    private void showDateTimePicker(String requestId) {
        final Calendar currentDate = Calendar.getInstance();
        final Calendar date = Calendar.getInstance();
        
        new DatePickerDialog(requireContext(), (view, year, monthOfYear, dayOfMonth) -> {
            date.set(year, monthOfYear, dayOfMonth);
            new TimePickerDialog(requireContext(), (view1, hourOfDay, minute) -> {
                date.set(Calendar.HOUR_OF_DAY, hourOfDay);
                date.set(Calendar.MINUTE, minute);
                
                String scheduledTime = String.format(Locale.getDefault(), "%02d/%02d/%d %02d:%02d", 
                        dayOfMonth, monthOfYear + 1, year, hourOfDay, minute);
                
                updateStatus(requestId, "approved", scheduledTime);
                
            }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), false).show();
        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE)).show();
    }

    private void updateStatus(String requestId, String status, String scheduledTime) {
        var updateMap = new java.util.HashMap<String, Object>();
        updateMap.put("status", status);
        if (scheduledTime != null) {
            updateMap.put("scheduledTime", scheduledTime);
        }

        db.collection("appointments").document(requestId)
                .update(updateMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Appointment " + status, Toast.LENGTH_SHORT).show();
                    fetchRequests();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
