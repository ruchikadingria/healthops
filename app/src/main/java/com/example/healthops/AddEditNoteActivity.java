package com.example.healthops;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddEditNoteActivity extends AppCompatActivity {

    private EditText etNoteTitle;
    private EditText etNoteBody;
    private Button btnSave;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_note);

        etNoteTitle = findViewById(R.id.etNoteTitle);
        etNoteBody = findViewById(R.id.etNoteBody);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> onBackPressed());

        btnSave.setOnClickListener(v -> saveNote());
    }

    private void saveNote() {
        String title = etNoteTitle.getText().toString().trim();
        String body = etNoteBody.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show();
            return;
        }

        if (body.isEmpty()) {
            Toast.makeText(this, "Please enter note content", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra("title", title);
        resultIntent.putExtra("body", body);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
