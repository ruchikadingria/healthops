package com.example.healthops.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.healthops.AddMedicineActivity;
import com.example.healthops.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MedicineFragment extends Fragment {

    private LinearLayout medicineContainer;
    private EditText searchInput;
    private FirebaseFirestore db;
    private static final String TAG = "MedicineFragment";
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_medicine, container, false);

        medicineContainer = view.findViewById(R.id.medicineContainer);
        searchInput = view.findViewById(R.id.searchMedicine);
        db = FirebaseFirestore.getInstance();

        // Add Medicine Button
        FloatingActionButton fabAdd = view.findViewById(R.id.fabAddMedicine);
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddMedicineActivity.class);
            startActivity(intent);
        });

        // Search functionality
        searchInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadMedicines(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        loadMedicines("");

        return view;
    }

    private void loadMedicines(String query) {
        medicineContainer.removeAllViews();

        db.collection("medicines")
                .addSnapshotListener((value, error) -> {

                    if (error != null) {
                        Log.e(TAG, "Error loading medicines: " + error.getMessage());
                        Toast.makeText(getContext(), "Error loading medicines", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value == null || value.isEmpty()) {
                        medicineContainer.removeAllViews();
                        TextView noMedicines = new TextView(requireContext());
                        noMedicines.setText("No medicines in inventory");
                        noMedicines.setPadding(20, 20, 20, 20);
                        medicineContainer.addView(noMedicines);
                        return;
                    }

                    medicineContainer.removeAllViews();

                    for (QueryDocumentSnapshot doc : value) {
                        String medicineName = doc.getString("medicine_name");
                        Long quantity = doc.getLong("quantity");
                        String expiryDate = doc.getString("expiry_date");
                        Long lowStockThreshold = doc.getLong("low_stock_threshold");

                        // Apply search filter
                        if (!query.isEmpty() && (medicineName == null ||
                            !medicineName.toLowerCase().contains(query.toLowerCase()))) {
                            continue;
                        }

                        // Create medicine card
                        LinearLayout medicineCard = new LinearLayout(requireContext());
                        medicineCard.setOrientation(LinearLayout.VERTICAL);
                        medicineCard.setPadding(20, 20, 20, 20);
                        medicineCard.setBackgroundResource(R.drawable.white_card);

                        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        cardParams.setMargins(0, 10, 0, 10);
                        medicineCard.setLayoutParams(cardParams);

                        // Medicine Name
                        TextView nameView = new TextView(requireContext());
                        nameView.setText(medicineName != null ? medicineName : "Unknown");
                        nameView.setTextSize(18);
                        nameView.setTypeface(null, android.graphics.Typeface.BOLD);
                        medicineCard.addView(nameView);

                        // Quantity
                        TextView quantityView = new TextView(requireContext());
                        int quantityInt = quantity != null ? quantity.intValue() : 0;
                        String quantityText = "Quantity: " + quantityInt;

                        // Check if low stock
                        int threshold = lowStockThreshold != null ? lowStockThreshold.intValue() : 10;
                        if (quantityInt <= threshold) {
                            quantityText += " ⚠️ LOW STOCK";
                            quantityView.setTextColor(0xFFFF0000); // Red
                        } else {
                            quantityView.setTextColor(0xFF00AA00); // Green
                        }

                        quantityView.setText(quantityText);
                        quantityView.setTextSize(14);
                        quantityView.setPadding(0, 10, 0, 0);
                        medicineCard.addView(quantityView);

                        // Expiry Date
                        TextView expiryView = new TextView(requireContext());
                        String expiryText = "Expiry: " + (expiryDate != null ? expiryDate : "N/A");

                        // Check if expired
                        if (expiryDate != null && isExpired(expiryDate)) {
                            expiryText += " ❌ EXPIRED";
                            expiryView.setTextColor(0xFFFF0000); // Red
                        } else {
                            expiryView.setTextColor(0xFF666666);
                        }

                        expiryView.setText(expiryText);
                        expiryView.setTextSize(12);
                        expiryView.setPadding(0, 10, 0, 0);
                        medicineCard.addView(expiryView);

                        // Edit button
                        TextView editBtn = new TextView(requireContext());
                        editBtn.setText("Edit");
                        editBtn.setTextSize(12);
                        editBtn.setPadding(0, 10, 0, 0);
                        editBtn.setTextColor(0xFF0066CC); // Blue
                        editBtn.setPadding(10, 10, 10, 10);

                        String docId = doc.getId();
                        editBtn.setOnClickListener(v -> {
                            Intent intent = new Intent(getActivity(), AddMedicineActivity.class);
                            intent.putExtra("docId", docId);
                            intent.putExtra("medicineName", medicineName);
                            intent.putExtra("quantity", quantityInt);
                            intent.putExtra("expiryDate", expiryDate);
                            intent.putExtra("lowStockThreshold", threshold);
                            startActivity(intent);
                        });
                        medicineCard.addView(editBtn);

                        medicineContainer.addView(medicineCard);
                    }
                });
    }

    private boolean isExpired(String expiryDate) {
        try {
            Date expiry = dateFormat.parse(expiryDate);
            Date today = new Date();
            return expiry != null && today.after(expiry);
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date: " + e.getMessage());
            return false;
        }
    }
}
