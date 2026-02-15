package com.example.healthops;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class FileViewerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_viewer);

        String title = getIntent().getStringExtra("TITLE");
        String time = getIntent().getStringExtra("TIME");

        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvTime = findViewById(R.id.tvTime);

        if (title != null) tvTitle.setText(title);
        if (time != null) tvTime.setText(time);
    }
}
