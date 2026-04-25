package com.example.healthops;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

public class FileViewerActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE = "TITLE";
    public static final String EXTRA_TIME = "TIME";
    public static final String EXTRA_SUBTITLE = "SUBTITLE";
    public static final String EXTRA_DESCRIPTION = "DESCRIPTION";
    public static final String EXTRA_ROW2_LABEL = "ROW2_LABEL";
    public static final String EXTRA_ROW2_VALUE = "ROW2_VALUE";
    public static final String EXTRA_ROW3_LABEL = "ROW3_LABEL";
    public static final String EXTRA_ROW3_VALUE = "ROW3_VALUE";
    public static final String EXTRA_TASK_ID = "TASK_ID";
    public static final String EXTRA_TIME_LABEL = "TIME_LABEL";   // ✅ FIXED

    // ✅ MAIN INTENT (SAFE - ALWAYS USE THIS)
    public static Intent newIntent(Context context,
                                   String title,
                                   String time,
                                   String subtitle,
                                   String description,
                                   String row2Label,
                                   String row2Value,
                                   String row3Label,
                                   String row3Value,
                                   String taskId) {

        Intent i = new Intent(context, FileViewerActivity.class);
        i.putExtra(EXTRA_TITLE, title);
        i.putExtra(EXTRA_TIME, time);
        i.putExtra(EXTRA_SUBTITLE, subtitle);
        i.putExtra(EXTRA_DESCRIPTION, description);
        i.putExtra(EXTRA_ROW2_LABEL, row2Label);
        i.putExtra(EXTRA_ROW2_VALUE, row2Value);
        i.putExtra(EXTRA_ROW3_LABEL, row3Label);
        i.putExtra(EXTRA_ROW3_VALUE, row3Value);
        i.putExtra(EXTRA_TASK_ID, taskId);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_viewer);

        Intent intent = getIntent();

        String title = intent.getStringExtra(EXTRA_TITLE);
        String time = intent.getStringExtra(EXTRA_TIME);
        String subtitle = intent.getStringExtra(EXTRA_SUBTITLE);
        String description = intent.getStringExtra(EXTRA_DESCRIPTION);
        String row2Label = intent.getStringExtra(EXTRA_ROW2_LABEL);
        String row2Value = intent.getStringExtra(EXTRA_ROW2_VALUE);
        String row3Label = intent.getStringExtra(EXTRA_ROW3_LABEL);
        String row3Value = intent.getStringExtra(EXTRA_ROW3_VALUE);
        String taskId = intent.getStringExtra(EXTRA_TASK_ID);

        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvSubtitle = findViewById(R.id.tvSubtitle);
        TextView tvDescription = findViewById(R.id.tvDescription);
        TextView tvTime = findViewById(R.id.tvTime);
        TextView tvRow2Label = findViewById(R.id.tvRow2Label);
        TextView tvRow2Value = findViewById(R.id.tvRow2Value);
        TextView tvRow3Label = findViewById(R.id.tvRow3Label);
        TextView tvRow3Value = findViewById(R.id.tvRow3Value);

        Button btnMarkDone = findViewById(R.id.btnMarkDone);
        ImageView btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> onBackPressed());

        if (!TextUtils.isEmpty(title)) tvTitle.setText(title);
        if (!TextUtils.isEmpty(time)) tvTime.setText(time);
        if (!TextUtils.isEmpty(subtitle)) tvSubtitle.setText(subtitle);
        if (!TextUtils.isEmpty(description)) tvDescription.setText(description);

        if (!TextUtils.isEmpty(row2Label)) tvRow2Label.setText(row2Label);
        if (!TextUtils.isEmpty(row2Value)) tvRow2Value.setText(row2Value);
        if (!TextUtils.isEmpty(row3Label)) tvRow3Label.setText(row3Label);
        if (!TextUtils.isEmpty(row3Value)) tvRow3Value.setText(row3Value);

        btnMarkDone.setOnClickListener(v -> {

            if (taskId == null) {
                Toast.makeText(this, "Task ID missing", Toast.LENGTH_SHORT).show();
                return;
            }

            new AlertDialog.Builder(this)
                    .setTitle("Confirm Completion")
                    .setMessage("This action cannot be undone.")
                    .setPositiveButton("Yes", (dialog, which) -> {

                        FirebaseFirestore.getInstance()
                                .collection("tasks")
                                .document(taskId)
                                .update("isDone", true, "completedAt", new Date())
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Completed", Toast.LENGTH_SHORT).show();
                                    btnMarkDone.setText("Completed");
                                    btnMarkDone.setEnabled(false);
                                });

                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }
}