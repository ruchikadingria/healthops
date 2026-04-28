package com.example.healthops.fragments;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PatientAppointmentsFragment extends Fragment {

    private LinearLayout doctorsListContainer;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_appointments, container, false);
        doctorsListContainer = view.findViewById(R.id.doctorsListContainer);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Seed doctor data if it doesn't exist
        seedDoctorData();

        fetchDoctors();

        return view;
    }

    private void seedDoctorData() {
        List<Map<String, Object>> doctors = new ArrayList<>();

        doctors.add(createDoctorMap("Ananya Sharma", "ananya@example.com", "8 years", "Cardiologist"));
        doctors.add(createDoctorMap("Rajesh Patel", "rajesh@example.com", "12 years", "Orthopedic"));
        doctors.add(createDoctorMap("Meera Iyer", "meera@example.com", "6 years", "Dermatologist"));
        doctors.add(createDoctorMap("Arjun Verma", "arjun@example.com", "10 years", "Neurologist"));
        doctors.add(createDoctorMap("Sneha Kulkarni", "sneha@example.com", "5+ years", "General Physician"));

        for (Map<String, Object> doctor : doctors) {
            String email = (String) doctor.get("email");
            
            // Seed into 'doctors' collection
            db.collection("doctors")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().isEmpty()) {
                            db.collection("doctors").add(doctor);
                        }
                    });

            // Seed into 'users' collection for authentication/profile compatibility
            db.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().isEmpty()) {
                            db.collection("users").add(doctor);
                        }
                    });
        }
    }

    private Map<String, Object> createDoctorMap(String name, String email, String experience, String treatment) {
        Map<String, Object> doc = new HashMap<>();
        doc.put("name", name);
        doc.put("email", email);
        doc.put("experience", experience);
        doc.put("treatment", treatment);
        doc.put("role", "staff");
        return doc;
    }

    private void fetchDoctors() {
        // Fetch from 'doctors' collection instead of 'users'
        db.collection("doctors")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && getContext() != null) {
                        doctorsListContainer.removeAllViews();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            addDoctorCard(document);
                        }
                    } else if (getContext() != null) {
                        Toast.makeText(getContext(), "Error fetching doctors", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addDoctorCard(QueryDocumentSnapshot doctorDoc) {
        String doctorId = doctorDoc.getId();
        String name = doctorDoc.getString("name");
        String email = doctorDoc.getString("email");
        if (name == null) name = email != null ? email : "Unknown Doctor";
        String experience = doctorDoc.getString("experience");
        if (experience == null) experience = "5+ years";
        String treatment = doctorDoc.getString("treatment");
        if (treatment == null) treatment = "General Physician";

        if (getContext() == null) return;

        // Building the doctor card programmatically
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);
        layout.setBackgroundResource(R.drawable.white_card);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 24);
        layout.setLayoutParams(params);

        TextView tvName = new TextView(getContext());
        tvName.setText("Dr. " + name);
        tvName.setTextSize(18f);
        tvName.setPadding(0, 0, 0, 8);
        tvName.setTextColor(ContextCompat.getColor(getContext(), R.color.text_primary));

        TextView tvEmail = new TextView(getContext());
        tvEmail.setText("Email: " + email);
        tvEmail.setTextColor(ContextCompat.getColor(getContext(), R.color.text_secondary));

        TextView tvExp = new TextView(getContext());
        tvExp.setText("Experience: " + experience);
        tvExp.setTextColor(ContextCompat.getColor(getContext(), R.color.text_secondary));

        TextView tvTreat = new TextView(getContext());
        tvTreat.setText("Specialization: " + treatment);
        tvTreat.setTextColor(ContextCompat.getColor(getContext(), R.color.text_secondary));

        Button btnRequest = new Button(getContext());
        btnRequest.setText("Request Appointment");
        btnRequest.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.blue_custom));
        btnRequest.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        
        final String finalName = name;
        final String finalEmail = email;
        btnRequest.setOnClickListener(v -> requestAppointment(doctorId, finalName, finalEmail));

        layout.addView(tvName);
        layout.addView(tvEmail);
        layout.addView(tvExp);
        layout.addView(tvTreat);
        layout.addView(btnRequest);

        doctorsListContainer.addView(layout);
    }

    private void requestAppointment(String doctorId, String doctorName, String doctorEmail) {
        if (mAuth.getCurrentUser() == null) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Please log in first", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        String patientId = mAuth.getCurrentUser().getUid();
        String patientEmail = mAuth.getCurrentUser().getEmail();

        Map<String, Object> appointment = new HashMap<>();
        appointment.put("patientId", patientId);
        appointment.put("patientEmail", patientEmail);
        appointment.put("doctorId", doctorId);
        appointment.put("doctorName", doctorName);
        appointment.put("doctorEmail", doctorEmail);
        appointment.put("status", "pending");
        appointment.put("timestamp", com.google.firebase.Timestamp.now());

        db.collection("appointments")
                .add(appointment)
                .addOnSuccessListener(documentReference -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Appointment Requested", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Failed to request appointment", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
