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

public class NotesFragment extends Fragment {

    private enum Filter {
        ALL, RECENT, CRITICAL
    }

    private static final class NoteItem {
        final String title;
        final String meta;
        final boolean recent;
        final boolean critical;
        final String body;
        final String wardLine;
        final String statusLine;

        NoteItem(String title, String meta, boolean recent, boolean critical, String body,
                 String wardLine, String statusLine) {
            this.title = title;
            this.meta = meta;
            this.recent = recent;
            this.critical = critical;
            this.body = body;
            this.wardLine = wardLine;
            this.statusLine = statusLine;
        }
    }

    private final List<NoteItem> source = new ArrayList<>();
    private LinearLayout listContainer;
    private EditText searchInput;
    private Filter filter = Filter.ALL;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        source.add(new NoteItem(
                "Patient 101 — Vitals update",
                "Updated 09:30 AM, Ward A",
                true,
                false,
                "BP 122/78, HR 72, SpO2 98% on room air. Patient tolerated breakfast well.",
                "Ward A — Bed 12",
                "Stable"));
        source.add(new NoteItem(
                "Patient 205 — Medication change",
                "Updated 11:00 AM, ICU",
                false,
                true,
                "New IV antibiotic started per pharmacy. Monitor renal function per protocol.",
                "ICU — Bed 4",
                "Critical monitoring"));
        source.add(new NoteItem(
                "Patient 312 — Observation",
                "Updated 01:15 PM, Emergency",
                true,
                false,
                "Complaint of mild dizziness resolved after fluids. Discharge pending ride.",
                "Emergency — Bay 7",
                "Observation"));
        source.add(new NoteItem(
                "Ward B — Handoff summary",
                "Updated 06:45 AM, Ward B",
                true,
                false,
                "Three admissions overnight, one pending OR. Staffing: full team on duty.",
                "Ward B",
                "Operational"));
        source.add(new NoteItem(
                "Patient 088 — Fall precaution",
                "Updated 08:10 AM, Ward C",
                false,
                true,
                "Increased fall risk after sedative dose. Bed alarm on, assist x2 for mobility.",
                "Ward C — Bed 3",
                "High risk"));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notes, container, false);
        listContainer = view.findViewById(R.id.notesListContainer);
        searchInput = view.findViewById(R.id.notesSearchInput);

        view.findViewById(R.id.btnFilterAll).setOnClickListener(v -> {
            filter = Filter.ALL;
            refresh();
        });
        view.findViewById(R.id.btnFilterRecent).setOnClickListener(v -> {
            filter = Filter.RECENT;
            refresh();
        });
        view.findViewById(R.id.btnFilterCritical).setOnClickListener(v -> {
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

        for (NoteItem note : source) {
            if (!matchesFilter(note)) continue;
            if (!q.isEmpty()) {
                String hay = (note.title + " " + note.meta).toLowerCase(Locale.US);
                if (!hay.contains(q)) continue;
            }
            addRow(note);
        }
    }

    private boolean matchesFilter(NoteItem note) {
        switch (filter) {
            case RECENT:
                return note.recent;
            case CRITICAL:
                return note.critical;
            default:
                return true;
        }
    }

    private void addRow(NoteItem note) {
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
        open.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.primary));
        open.setLayoutParams(new LinearLayout.LayoutParams(200, LinearLayout.LayoutParams.WRAP_CONTENT));
        open.setOnClickListener(v -> {
            Intent i = FileViewerActivity.newIntent(requireContext(),
                    note.title,
                    note.meta,
                    "Clinical note",
                    note.body,
                    getString(R.string.file_label_ward),
                    note.wardLine,
                    getString(R.string.file_label_status),
                    note.statusLine);
            i.putExtra(FileViewerActivity.EXTRA_TIME_LABEL, getString(R.string.file_label_recorded));
            startActivity(i);
        });

        row.addView(textCol);
        row.addView(open);
        listContainer.addView(row);
    }
}
