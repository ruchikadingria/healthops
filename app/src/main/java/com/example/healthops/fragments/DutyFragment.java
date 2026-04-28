package com.example.healthops.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.healthops.FileViewerActivity;
import com.example.healthops.LocaleManager;
import com.example.healthops.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DutyFragment extends Fragment {

    private LinearLayout tasksContainer;
    private FirebaseFirestore db;
    private String userId;
    private static final String TAG = "DutyFragment";
    private ListenerRegistration tasksListener;
    
    // Current date references
    private TextView tvCurrentDay;
    private TextView tvCurrentDate;
    private TextView tvCurrentDateDisplay;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Apply saved language
        if (getContext() != null) {
            LocaleManager.applyLanguage(getContext());
        }

        View view = inflater.inflate(R.layout.fragment_duty, container, false);

        tasksContainer = view.findViewById(R.id.tasksContainer);
        tvCurrentDay = view.findViewById(R.id.tvCurrentDay);
        tvCurrentDate = view.findViewById(R.id.tvCurrentDate);
        tvCurrentDateDisplay = view.findViewById(R.id.tvCurrentDateDisplay);
        
        db = FirebaseFirestore.getInstance();

        // Set current date display
        updateCurrentDateDisplay();

        // Check if user is logged in
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            }
            return view;
        }

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d(TAG, "Loading tasks for user: " + userId);

        // Load tasks for current date
        loadTasks();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (tasksListener != null) {
            tasksListener.remove();
        }
    }
    
    private void updateCurrentDateDisplay() {
        Calendar today = Calendar.getInstance();
        
        // Format: "Mon" (day name)
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        String dayName = dayFormat.format(today.getTime());
        tvCurrentDay.setText(dayName);
        
        // Format: "28" (day number)
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd", Locale.getDefault());
        String dateNum = dateFormat.format(today.getTime());
        tvCurrentDate.setText(dateNum);
        
        // Format: "Monday, April 28"
        SimpleDateFormat fullFormat = new SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault());
        String fullDate = fullFormat.format(today.getTime());
        tvCurrentDateDisplay.setText(fullDate);
    }

    private void loadTasks() {
        if (!isAdded() || getContext() == null) return;
        
        tasksContainer.removeAllViews();

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d(TAG, "========== TASK LOADING DEBUG ==========");
        Log.d(TAG, "Current User ID: " + currentUserId);

        // Get today's date in yyyy-MM-dd format
        Calendar today = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayDateStr = sdf.format(today.getTime());
        Log.d(TAG, "Today's Date: " + todayDateStr);

        // Query for THIS user's tasks for today
        tasksListener = db.collection("tasks")
                .whereEqualTo("assignedTo", currentUserId)
                .addSnapshotListener((value, error) -> {
                    if (!isAdded() || getContext() == null) return;

                    if (error != null) {
                        String errorMsg = error.getMessage();
                        Log.e(TAG, "❌ FIRESTORE ERROR: " + errorMsg);
                        Log.e(TAG, "Error Code: " + error.getCode());

                        tasksContainer.removeAllViews();
                        TextView errorView = new TextView(getContext());
                        errorView.setText("ERROR:\n\n" + errorMsg);
                        errorView.setPadding(20, 20, 20, 20);
                        errorView.setTextColor(0xFFFF0000); // Red
                        tasksContainer.addView(errorView);

                        Toast.makeText(getContext(), "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (value == null || value.isEmpty()) {
                        // No tasks found for this user
                        Log.w(TAG, "⚠️ No tasks found for user: " + currentUserId);

                        tasksContainer.removeAllViews();
                        LinearLayout container = new LinearLayout(getContext());
                        container.setOrientation(LinearLayout.VERTICAL);
                        container.setPadding(20, 20, 20, 20);

                        TextView noTasks = new TextView(getContext());
                        noTasks.setText("No tasks assigned to you");
                        noTasks.setTextSize(18);
                        container.addView(noTasks);

                        TextView hint = new TextView(getContext());
                        hint.setText("\nYour User ID:\n" + currentUserId + "\n\nAsk admin to create a task with this user ID");
                        hint.setTextSize(12);
                        hint.setPadding(0, 20, 0, 0);
                        container.addView(hint);

                        tasksContainer.addView(container);
                        return;
                    }

                    tasksContainer.removeAllViews();
                    Log.d(TAG, "✅ Found " + value.size() + " total tasks for user");
                    
                    int tasksForTodayCount = 0;

                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : value) {
                        try {
                            Boolean isDone = doc.getBoolean("isDone");
                            if (isDone != null && isDone) {
                                continue;
                            }
                            
                            // Filter by today's date
                            String taskDateStr = doc.getString("date");
                            if (taskDateStr != null && !taskDateStr.isEmpty()) {
                                if (!taskDateStr.equals(todayDateStr)) {
                                    Log.d(TAG, "Skipping task - not for today: " + taskDateStr);
                                    continue;  // Skip tasks not for today
                                }
                            }

                            tasksForTodayCount++;
                            String taskId = doc.getId();
                            String title = doc.getString("title");
                            String description = doc.getString("description");
                            String ward = doc.getString("ward");

                            Log.d(TAG, "  ✓ Task for today: " + title);

                            LinearLayout taskCard = new LinearLayout(getContext());
                            taskCard.setOrientation(LinearLayout.VERTICAL);
                            taskCard.setPadding(32, 32, 32, 32);
                            taskCard.setBackgroundResource(R.drawable.white_card);

                            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            cardParams.setMargins(0, 0, 0, 24);
                            taskCard.setLayoutParams(cardParams);

                            TextView titleView = new TextView(getContext());
                            titleView.setText(title != null ? title : "Task");
                            titleView.setTextSize(18);
                            titleView.setTypeface(null, android.graphics.Typeface.BOLD);
                            titleView.setTextColor(getResources().getColor(R.color.text_primary));
                            taskCard.addView(titleView);

                            TextView descView = new TextView(getContext());
                            descView.setText(description != null ? description : "No description");
                            descView.setTextSize(14);
                            descView.setPadding(0, 10, 0, 0);
                            descView.setTextColor(getResources().getColor(R.color.text_secondary));
                            taskCard.addView(descView);

                            TextView wardView = new TextView(getContext());
                            wardView.setText(String.format("Ward: %s", ward != null ? ward : "N/A"));
                            wardView.setTextSize(12);
                            wardView.setPadding(0, 10, 0, 0);
                            wardView.setTextColor(getResources().getColor(R.color.text_secondary));
                            taskCard.addView(wardView);

                            taskCard.setOnClickListener(v -> {
                                if (getContext() != null) {
                                    Intent i = FileViewerActivity.newIntent(
                                            getContext(),
                                            title != null ? title : "Task",
                                            "Task Details",
                                            ward != null ? ward : "No ward",
                                            description != null ? description : "No description",
                                            "Status",
                                            "Pending",
                                            "Ward",
                                            ward != null ? ward : "N/A",
                                            taskId
                                    );
                                    startActivity(i);
                                }
                            });

                            tasksContainer.addView(taskCard);
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing task: " + e.getMessage());
                        }
                    }
                    
                    // Show message if no tasks for today
                    if (tasksForTodayCount == 0) {
                        LinearLayout emptyContainer = new LinearLayout(getContext());
                        emptyContainer.setOrientation(LinearLayout.VERTICAL);
                        emptyContainer.setPadding(20, 20, 20, 20);
                        
                        TextView noTasksView = new TextView(getContext());
                        noTasksView.setText("No tasks assigned for today");
                        noTasksView.setTextSize(16);
                        noTasksView.setTextColor(0xFF999999);
                        emptyContainer.addView(noTasksView);
                        
                        tasksContainer.addView(emptyContainer);
                    }
                });
    }
}
