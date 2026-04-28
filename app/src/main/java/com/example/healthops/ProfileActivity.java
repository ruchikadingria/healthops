package com.example.healthops;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private Spinner languageSpinner;
    private Button btnApplyLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleManager.applyLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        languageSpinner = findViewById(R.id.languageSpinner);
        btnApplyLanguage = findViewById(R.id.btnApplyLanguage);

        // Setup language options
        String[] languages = {"English", "हिंदी (Hindi)"};
        String[] languageCodes = {"en", "hi"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, languages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(adapter);

        // Set current language
        String currentLanguage = LocaleManager.getLanguage(this);
        if (currentLanguage.equals("hi")) {
            languageSpinner.setSelection(1);
        } else {
            languageSpinner.setSelection(0);
        }

        btnApplyLanguage.setOnClickListener(v -> {
            int selectedPosition = languageSpinner.getSelectedItemPosition();
            String selectedLanguage = languageCodes[selectedPosition];

            LocaleManager.setLocale(this, selectedLanguage);
            Toast.makeText(this, R.string.language_changed, Toast.LENGTH_SHORT).show();

            // Restart activity to apply changes
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        });
    }
}
