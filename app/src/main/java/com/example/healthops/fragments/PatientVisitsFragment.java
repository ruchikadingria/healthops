package com.example.healthops.fragments;

import android.content.Intent;
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

import com.example.healthops.FileViewerActivity;
import com.example.healthops.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class PatientVisitsFragment extends Fragment {

    private LinearLayout visitsContainer;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_visits, container, false);
        visitsContainer = view.findViewById(R.id.visitsContainer);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        fetchScheduledVisits();
        
        return view;
    }

    private void fetchScheduledVisits() {
        if (mAuth.getCurrentUser() == null) return;
        String patientId = mAuth.getCurrentUser().getUid();

        db.collection("appointments")
                .whereEqualTo("patientId", patientId)
                .whereEqualTo("status", "approved")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && getContext() != null) {
                        visitsContainer.removeAllViews();
                        if (task.getResult().isEmpty()) {
                            TextView tv = new TextView(getContext());
                            tv.setText("No scheduled visits found.");
                            tv.setPadding(20, 40, 20, 20);
                            visitsContainer.addView(tv);
                        } else {
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                String doctorName = doc.getString("doctorName");
                                String time = doc.getString("scheduledTime");
                                String doctorEmail = doc.getString("doctorEmail");
                                
                                addVisit("Appointment with Dr. " + doctorName, 
                                        time != null ? time : "Time not set", 
                                        "Hospital Main Wing",
                                        "Confirmed Visit",
                                        "Your appointment has been approved and scheduled.",
                                        "Doctor Email",
                                        doctorEmail != null ? doctorEmail : "N/A",
                                        "Status",
                                        "Approved");
                            }
                        }
                    } else if (getContext() != null) {
                        Toast.makeText(getContext(), "Error fetching visits", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addVisit(String title, String when, String place, String subtitle, String description,
                          String row2Label, String row2Val, String row3Label, String row3Val) {
        if (getContext() == null) return;

        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(30, 30, 30, 30);
        row.setBackgroundResource(R.drawable.white_card);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 25, 0, 0);
        row.setLayoutParams(lp);

        LinearLayout textCol = new LinearLayout(getContext());
        textCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        textCol.setLayoutParams(textLp);

        TextView tvTitle = new TextView(getContext());
        tvTitle.setText(title);
        tvTitle.setTextSize(16f);
        tvTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));

        TextView tvWhen = new TextView(getContext());
        tvWhen.setText(when + " · " + place);
        tvWhen.setTextSize(14f);
        tvWhen.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));

        textCol.addView(tvTitle);
        textCol.addView(tvWhen);

        Button open = new Button(getContext());
        open.setText(R.string.open);
        open.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        open.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.blue_custom));
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(220,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        open.setLayoutParams(bp);
        open.setOnClickListener(v -> {
            Intent i = FileViewerActivity.newIntent(requireContext(), title, when, subtitle, description,
                    row2Label, row2Val, row3Label, row3Val, null);
            startActivity(i);
        });

        row.addView(textCol);
        row.addView(open);
        visitsContainer.addView(row);
    }
}
