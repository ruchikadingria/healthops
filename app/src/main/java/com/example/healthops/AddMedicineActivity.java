package com.example.healthops;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddMedicineActivity extends AppCompatActivity {

    private EditText etMedicineName;
    private EditText etQuantity;
    private EditText etExpiryDate;
    private EditText etLowStockThreshold;
    private Button btnSave;
    private Button btnDelete;
    private ImageView btnBack;
    private FirebaseFirestore db;
    private String docId;
    private static final String TAG = "AddMedicineActivity";
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medicine);

        etMedicineName = findViewById(R.id.etMedicineName);
        etQuantity = findViewById(R.id.etQuantity);
        etExpiryDate = findViewById(R.id.etExpiryDate);
        etLowStockThreshold = findViewById(R.id.etLowStockThreshold);
        btnSave = findViewById(R.id.btnSaveMedicine);
        btnDelete = findViewById(R.id.btnDeleteMedicine);
        btnBack = findViewById(R.id.btnBack);
        db = FirebaseFirestore.getInstance();

        btnBack.setOnClickListener(v -> onBackPressed());

        // Date picker for expiry date
        etExpiryDate.setOnClickListener(v -> showDatePicker());

        btnSave.setOnClickListener(v -> saveMedicine());
        btnDelete.setOnClickListener(v -> deleteMedicine());

        // Check if editing existing medicine
        docId = getIntent().getStringExtra("docId");
        if (docId != null) {
            // Edit mode
            String medicineName = getIntent().getStringExtra("medicineName");
            int quantity = getIntent().getIntExtra("quantity", 0);
            String expiryDate = getIntent().getStringExtra("expiryDate");
            int threshold = getIntent().getIntExtra("lowStockThreshold", 10);

            etMedicineName.setText(medicineName);
            etQuantity.setText(String.valueOf(quantity));
            etExpiryDate.setText(expiryDate);
            etLowStockThreshold.setText(String.valueOf(threshold));

            btnDelete.setVisibility(android.view.View.VISIBLE);
        } else {
            btnDelete.setVisibility(android.view.View.GONE);
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog picker = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    etExpiryDate.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        picker.show();
    }

    private void saveMedicine() {
        String medicineName = etMedicineName.getText().toString().trim();
        String quantityStr = etQuantity.getText().toString().trim();
        String expiryDate = etExpiryDate.getText().toString().trim();
        String thresholdStr = etLowStockThreshold.getText().toString().trim();

        if (medicineName.isEmpty()) {
            Toast.makeText(this, "Enter medicine name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (quantityStr.isEmpty()) {
            Toast.makeText(this, "Enter quantity", Toast.LENGTH_SHORT).show();
            return;
        }

        if (expiryDate.isEmpty()) {
            Toast.makeText(this, "Enter expiry date", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityStr);
            int threshold = Integer.parseInt(thresholdStr.isEmpty() ? "10" : thresholdStr);

            Map<String, Object> medicine = new HashMap<>();
            medicine.put("medicine_name", medicineName);
            medicine.put("quantity", quantity);
            medicine.put("expiry_date", expiryDate);
            medicine.put("low_stock_threshold", threshold);
            medicine.put("lastUpdated", com.google.firebase.Timestamp.now());

            if (docId != null) {
                // Update existing
                db.collection("medicines").document(docId)
                        .set(medicine)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(AddMedicineActivity.this, "Medicine updated", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error updating medicine: " + e.getMessage());
                            Toast.makeText(AddMedicineActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                // Add new
                db.collection("medicines")
                        .add(medicine)
                        .addOnSuccessListener(docRef -> {
                            Toast.makeText(AddMedicineActivity.this, "Medicine added", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error adding medicine: " + e.getMessage());
                            Toast.makeText(AddMedicineActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid quantity or threshold", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteMedicine() {
        if (docId == null) return;

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Medicine")
                .setMessage("Are you sure?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    db.collection("medicines").document(docId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(AddMedicineActivity.this, "Medicine deleted", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error deleting medicine: " + e.getMessage());
                                Toast.makeText(AddMedicineActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("No", null)
                .show();
    }
}
