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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.healthops.AddEditNoteActivity;
import com.example.healthops.FileViewerActivity;
import com.example.healthops.LocaleManager;
import com.example.healthops.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.Locale;

public class NotesFragment extends Fragment {

    private LinearLayout listContainer;
    private EditText searchInput;

    private FirebaseFirestore db;
    private String userId;

    private final ActivityResultLauncher<Intent> addNoteLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == requireActivity().RESULT_OK && result.getData() != null) {

                            String title = result.getData().getStringExtra("title");
                            String body = result.getData().getStringExtra("body");

                            if (title == null || body == null) {
                                Toast.makeText(getContext(), "Invalid note data", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // SAVE TO FIRESTORE
                            db.collection("notes")
                                    .add(new NoteModel(title, body, System.currentTimeMillis(), userId))
                                    .addOnSuccessListener(doc ->
                                            Toast.makeText(getContext(), "Note saved", Toast.LENGTH_SHORT).show()
                                    )
                                    .addOnFailureListener(e ->
                                            Toast.makeText(getContext(), "Save failed", Toast.LENGTH_SHORT).show()
                                    );
                        }
                    });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Apply saved language
        LocaleManager.applyLanguage(requireContext());

        View view = inflater.inflate(R.layout.fragment_notes, container, false);

        listContainer = view.findViewById(R.id.notesContainer);
        searchInput = view.findViewById(R.id.searchNotes);

        db = FirebaseFirestore.getInstance();

        // Check if user is logged in
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            requireActivity().finish();
            return view;
        }

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FloatingActionButton fab = view.findViewById(R.id.fabAddPatientNote);
        fab.setOnClickListener(v -> {
            Intent i = new Intent(getActivity(), AddEditNoteActivity.class);
            addNoteLauncher.launch(i);
        });

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadNotes(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadNotes("");

        return view;
    }

    private void loadNotes(String query) {

        listContainer.removeAllViews();

        db.collection("notes")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {

                    if (value == null) return;

                    listContainer.removeAllViews();

                    for (QueryDocumentSnapshot doc : value) {

                        String title = doc.getString("title");
                        String body = doc.getString("body");

                        if (title == null) title = "";
                        if (body == null) body = "";

                        String search = (title + body).toLowerCase(Locale.ROOT);

                        if (!query.isEmpty() && !search.contains(query.toLowerCase())) {
                            continue;
                        }

                        addRow(title, body, doc.getId());
                    }
                });
    }

    private void addRow(String title, String body, String noteId) {

        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(24, 24, 24, 24);
        row.setBackgroundResource(R.drawable.white_card);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        lp.setMargins(0, 16, 0, 0);
        row.setLayoutParams(lp);

        LinearLayout textCol = new LinearLayout(getContext());
        textCol.setOrientation(LinearLayout.VERTICAL);
        textCol.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvTitle = new TextView(getContext());
        tvTitle.setText(title);
        tvTitle.setTextSize(16f);
        tvTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));

        TextView tvBody = new TextView(getContext());
        tvBody.setText(body);
        tvBody.setTextSize(13f);
        tvBody.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));

        Button open = new Button(getContext());
        open.setText("Open");

        open.setOnClickListener(v -> {
            Intent i = FileViewerActivity.newIntent(
                    requireContext(),
                    title,
                    "Note",
                    "Patient Note",
                    body,
                    "Note ID",
                    noteId,
                    "",
                    "",
                    null
            );
            startActivity(i);
        });

        textCol.addView(tvTitle);
        textCol.addView(tvBody);

        row.addView(textCol);
        row.addView(open);

        listContainer.addView(row);
    }

    // FIRESTORE MODEL
    public static class NoteModel {
        String title, body, userId;
        long timestamp;

        public NoteModel() {}

        public NoteModel(String title, String body, long timestamp, String userId) {
            this.title = title;
            this.body = body;
            this.timestamp = timestamp;
            this.userId = userId;
        }
    }
}