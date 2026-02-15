package com.example.healthops.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

        // Find the container where tasks will be added
        scheduleContainer = view.findViewById(R.id.scheduleContainer);

        // Show default tasks (Today)
        showTodayTasks();

        return view;
    }

    private void showTodayTasks() {
        scheduleContainer.removeAllViews();

        addTask("Ward A - Morning Shift", "8:00 AM - 2:00 PM, General Ward");
        addTask("Emergency Ward", "8:00 PM - 8:00 AM");
    }

    private void addTask(String title, String time) {
        // Create a horizontal task layout
        LinearLayout taskLayout = new LinearLayout(getContext());
        taskLayout.setOrientation(LinearLayout.HORIZONTAL);
        taskLayout.setPadding(20, 20, 20, 20);
        taskLayout.setBackgroundResource(R.drawable.white_card);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 20, 0, 0);
        taskLayout.setLayoutParams(layoutParams);

        // Vertical layout for title & time
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
        tvTitle.setPadding(0, 0, 0, 5);

        TextView tvTime = new TextView(getContext());
        tvTime.setText(time);
        tvTime.setTextSize(14f);
        tvTime.setTextColor(getResources().getColor(android.R.color.darker_gray));

        textLayout.addView(tvTitle);
        textLayout.addView(tvTime);

        taskLayout.addView(textLayout);

        // Add the task layout to the schedule container
        scheduleContainer.addView(taskLayout);
    }
}
