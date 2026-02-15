package com.example.healthops.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.healthops.FileViewerActivity;
import com.example.healthops.R;

public class DutyFragment extends Fragment {

    private LinearLayout scheduleContainer;

    public DutyFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_duty, container, false);

        scheduleContainer = view.findViewById(R.id.scheduleContainer);

        showTodayTasks();

        return view;
    }

    private void showTodayTasks() {
        scheduleContainer.removeAllViews();

        addTask("Ward A - Morning Shift", "8:00 AM - 2:00 PM, General Ward");
        addTask("Emergency Ward", "8:00 PM - 8:00 AM");
        addTask("ICU Checkup", "2:00 PM - 6:00 PM");
    }

    private void addTask(String title, String time) {

        LinearLayout taskLayout = new LinearLayout(getContext());
        taskLayout.setOrientation(LinearLayout.HORIZONTAL);
        taskLayout.setPadding(30, 30, 30, 30);
        taskLayout.setBackgroundResource(R.drawable.white_card);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 25, 0, 0);
        taskLayout.setLayoutParams(layoutParams);

        LinearLayout textLayout = new LinearLayout(getContext());
        textLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        textLayout.setLayoutParams(textParams);

        TextView tvTitle = new TextView(getContext());
        tvTitle.setText(title);
        tvTitle.setTextSize(16f);
        tvTitle.setTextColor(getResources().getColor(R.color.text_primary));

        TextView tvTime = new TextView(getContext());
        tvTime.setText(time);
        tvTime.setTextSize(14f);
        tvTime.setTextColor(getResources().getColor(R.color.text_secondary));

        textLayout.addView(tvTitle);
        textLayout.addView(tvTime);

        Button openButton = new Button(getContext());
        openButton.setText("OPEN");
        openButton.setTextColor(getResources().getColor(R.color.white));
        openButton.setBackgroundTintList(getResources().getColorStateList(R.color.primary));

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                220,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        openButton.setLayoutParams(buttonParams);

        openButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), FileViewerActivity.class);
            intent.putExtra("TITLE", title);
            intent.putExtra("TIME", time);
            startActivity(intent);
        });

        taskLayout.addView(textLayout);
        taskLayout.addView(openButton);

        scheduleContainer.addView(taskLayout);
    }
}
