package com.example.healthops;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PatientLoginActivity extends AppCompatActivity {

    private EditText patientIdInput, passwordInput;
    private Button loginBtn;
    private TextView linkStaff;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        patientIdInput = findViewById(R.id.patientIdInput);
        passwordInput = findViewById(R.id.patientPasswordInput);
        loginBtn = findViewById(R.id.patientLoginBtn);
        linkStaff = findViewById(R.id.linkStaffLogin);

        loginBtn.setOnClickListener(v -> login());
        
        linkStaff.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void login() {
        String email = patientIdInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            patientIdInput.setError("Enter patient email");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Enter password");
            return;
        }

        // Firebase Auth for Patient
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        saveUserInfoToFirestore(email, "patient");
                    } else {
                        Toast.makeText(PatientLoginActivity.this, "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserInfoToFirestore(String email, String role) {
        String uid = mAuth.getCurrentUser().getUid();
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("role", role);
        user.put("lastLogin", com.google.firebase.Timestamp.now());

        db.collection("users").document(uid)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(PatientLoginActivity.this, "Patient Login Successful", Toast.LENGTH_SHORT).show();
                    SessionPreferences.setPatientSession(this, email);
                    
                    startActivity(new Intent(this, PatientDashboardActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(PatientLoginActivity.this, "Error saving user info: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
