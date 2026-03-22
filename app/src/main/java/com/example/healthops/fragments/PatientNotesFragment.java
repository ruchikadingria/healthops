package com.example.healthops.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.healthops.FileViewerActivity;
import com.example.healthops.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PatientNotesFragment extends Fragment {

    private enum Filter {
        ALL, RECENT, CRITICAL
    }

    private static final class PatientNote {
        final String title;
        final String meta;
        final boolean recent;
        final boolean critical;
        final String body;
        final String fromCareTeam;
        final String nextStep;

        PatientNote(String title, String meta, boolean recent, boolean critical, String body,
                    String fromCareTeam, String nextStep) {
            this.title = title;
            this.meta = meta;
            this.recent = recent;
            this.critical = critical;
            this.body = body;
            this.fromCareTeam = fromCareTeam;
            this.nextStep = nextStep;
        }
    }

    private final List<PatientNote> source = new ArrayList<>();
    private LinearLayout listContainer;
    private EditText searchInput;
    private Filter filter = Filter.ALL;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        source.add(new PatientNote(
                "After-visit summary",
                "Mar 20, 2025 — Primary care",
                true,
                false,
                "Continue current medications. Walk 20 minutes daily. Follow-up in 6 weeks.",
                "Dr. Rao’s team",
                "Schedule follow-up if symptoms worsen"));
        source.add(new PatientNote(
                "Medication instruction",
                "Mar 19, 2025 — Pharmacy",
                true,
                false,
                "Take the new prescription with food. Do not skip doses. Finish the full course.",
                "Hospital pharmacy",
                "Questions? Call the pharmacy line"));
        source.add(new PatientNote(
                "Lab result available",
                "Mar 18, 2025 — Labs",
                false,
                true,
                "One value was slightly outside the goal range. Your physician will discuss at next visit.",
                "Lab services",
                "Monitor symptoms; seek care for chest pain or shortness of breath"));
        source.add(new PatientNote(
                "Physical therapy plan",
                "Mar 15, 2025 — Therapy",
                false,
                false,
                "Home exercises 2x daily. Use ice after activity for 10 minutes.",
                "PT — Outpatient",
                "Next session Tue 3:00 PM"));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_notes, container, false);
        listContainer = view.findViewById(R.id.patientNotesContainer);
        searchInput = view.findViewById(R.id.patientNotesSearch);

        view.findViewById(R.id.patientBtnAll).setOnClickListener(v -> {
            filter = Filter.ALL;
            refresh();
        });
        view.findViewById(R.id.patientBtnRecent).setOnClickListener(v -> {
            filter = Filter.RECENT;
            refresh();
        });
        view.findViewById(R.id.patientBtnCritical).setOnClickListener(v -> {
            filter = Filter.CRITICAL;
            refresh();
        });

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                refresh();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        refresh();
        return view;
    }

    private void refresh() {
        if (listContainer == null) return;
        listContainer.removeAllViews();
        String q = searchInput.getText().toString().trim().toLowerCase(Locale.US);

        for (PatientNote note : source) {
            if (!matchesFilter(note)) continue;
            if (!q.isEmpty()) {
                String hay = (note.title + " " + note.meta).toLowerCase(Locale.US);
                if (!hay.contains(q)) continue;
            }
            addRow(note);
        }
    }

    private boolean matchesFilter(PatientNote note) {
        switch (filter) {
            case RECENT:
                return note.recent;
            case CRITICAL:
                return note.critical;
            default:
                return true;
        }
    }

    private void addRow(PatientNote note) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(24, 24, 24, 24);
        row.setBackgroundResource(R.drawable.white_card);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 16, 0, 0);
        row.setLayoutParams(lp);
        row.setElevation(4f);

        LinearLayout textCol = new LinearLayout(getContext());
        textCol.setOrientation(LinearLayout.VERTICAL);
        textCol.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvTitle = new TextView(getContext());
        tvTitle.setText(note.title);
        tvTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        tvTitle.setTextSize(16f);
        tvTitle.setTypeface(tvTitle.getTypeface(), android.graphics.Typeface.BOLD);

        TextView tvMeta = new TextView(getContext());
        tvMeta.setText(note.meta);
        tvMeta.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        tvMeta.setTextSize(14f);

        textCol.addView(tvTitle);
        textCol.addView(tvMeta);

        Button open = new Button(getContext());
        open.setText(R.string.open);
        open.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        open.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.blue_custom));
        open.setLayoutParams(new LinearLayout.LayoutParams(200, LinearLayout.LayoutParams.WRAP_CONTENT));
        open.setOnClickListener(v -> {
            Intent i = FileViewerActivity.newIntent(requireContext(),
                    note.title,
                    note.meta,
                    "Care team note",
                    note.body,
                    "From",
                    note.fromCareTeam,
                    "Next step",
                    note.nextStep);
            i.putExtra(FileViewerActivity.EXTRA_TIME_LABEL, getString(R.string.file_label_recorded));
            startActivity(i);
        });

        row.addView(textCol);
        row.addView(open);
        listContainer.addView(row);
    }
}
