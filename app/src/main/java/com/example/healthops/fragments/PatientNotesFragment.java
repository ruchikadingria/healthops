package com.example.healthops.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.healthops.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PatientNotesFragment extends Fragment {

    private LinearLayout container;
    private EditText search;

    private final List<String> notes = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup containerParent,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_patient_notes, containerParent, false);

        container = view.findViewById(R.id.patientNotesContainer);
        search = view.findViewById(R.id.patientNotesSearch);

        // dummy initial data (REMOVE LATER when DB added)
        notes.add("BP stable, patient improving");
        notes.add("Doctor visit completed, follow-up next week");
        notes.add("Medication updated for hypertension");

        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                render(s.toString());
            }

            @Override public void afterTextChanged(Editable s) {}
        });

        render("");

        return view;
    }

    private void render(String query) {
        container.removeAllViews();

        String q = query.toLowerCase(Locale.ROOT);

        for (String note : notes) {

            if (!q.isEmpty() && !note.toLowerCase().contains(q)) continue;

            TextView tv = new TextView(getContext());
            tv.setText(note);
            tv.setPadding(20, 20, 20, 20);
            tv.setTextSize(16f);

            container.addView(tv);
        }
    }
}