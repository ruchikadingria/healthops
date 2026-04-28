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
import com.example.healthops.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class DutyFragment extends Fragment {

    private LinearLayout tasksContainer;
    private FirebaseFirestore db;
    private String userId;
    private static final String TAG = "DutyFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_duty, container, false);

        tasksContainer = view.findViewById(R.id.tasksContainer);
        db = FirebaseFirestore.getInstance();

        // Check if user is logged in
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return view;
        }

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d(TAG, "Loading tasks for user: " + userId);

        // Load tasks for this user
        loadTasks();

        return view;
    }

    private void loadTasks() {
        tasksContainer.removeAllViews();

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d(TAG, "========== TASK LOADING DEBUG ==========");
        Log.d(TAG, "Current User ID: " + currentUserId);
        Log.d(TAG, "Query: tasks where assignedTo == " + currentUserId);

        // First, let's see ALL tasks in the database (for debugging)
        db.collection("tasks")
                .addSnapshotListener((allTasksSnapshot, allTasksError) -> {
                    if (allTasksSnapshot != null) {
                        Log.d(TAG, "Total tasks in database: " + allTasksSnapshot.size());
                        for (int i = 0; i < allTasksSnapshot.size(); i++) {
                            String assignedTo = allTasksSnapshot.getDocuments().get(i).getString("assignedTo");
                            String title = allTasksSnapshot.getDocuments().get(i).getString("title");
                            Log.d(TAG, "  [" + i + "] Title: " + title + ", AssignedTo: " + assignedTo);
                        }
                    }
                });

        // Now query for THIS user's tasks
        db.collection("tasks")
                .whereEqualTo("assignedTo", currentUserId)
                .addSnapshotListener((value, error) -> {

                    if (error != null) {
                        String errorMsg = error.getMessage();
                        Log.e(TAG, "❌ FIRESTORE ERROR: " + errorMsg);
                        Log.e(TAG, "Error Code: " + error.getCode());

                        tasksContainer.removeAllViews();
                        TextView errorView = new TextView(requireContext());
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
                        Log.w(TAG, "This could mean:");
                        Log.w(TAG, "  1. No tasks assigned to this user");
                        Log.w(TAG, "  2. Tasks collection is empty");
                        Log.w(TAG, "  3. 'assignedTo' field doesn't match user ID");

                        tasksContainer.removeAllViews();
                        LinearLayout container = new LinearLayout(requireContext());
                        container.setOrientation(LinearLayout.VERTICAL);
                        container.setPadding(20, 20, 20, 20);

                        TextView noTasks = new TextView(requireContext());
                        noTasks.setText("No tasks assigned to you");
                        noTasks.setTextSize(18);
                        container.addView(noTasks);

                        TextView hint = new TextView(requireContext());
                        hint.setText("\nYour User ID:\n" + currentUserId + "\n\nAsk admin to create a task with this user ID");
                        hint.setTextSize(12);
                        hint.setPadding(0, 20, 0, 0);
                        container.addView(hint);

                        tasksContainer.addView(container);
                        return;
                    }

                    tasksContainer.removeAllViews();
                    Log.d(TAG, "✅ Found " + value.size() + " tasks for user");

                    value.forEach(doc -> {
                        try {
                            Boolean isDone = doc.getBoolean("isDone");
                            if (isDone != null && isDone) {
                                return;
                            }

                            String taskId = doc.getId();
                            String title = doc.getString("title");
                            String description = doc.getString("description");
                            String ward = doc.getString("ward");

                            Log.d(TAG, "  ✓ Task: " + title);

                            LinearLayout taskCard = new LinearLayout(requireContext());
                            taskCard.setOrientation(LinearLayout.VERTICAL);
                            taskCard.setPadding(20, 20, 20, 20);
                            taskCard.setBackgroundResource(R.drawable.white_card);

                            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            cardParams.setMargins(0, 10, 0, 10);
                            taskCard.setLayoutParams(cardParams);

                            TextView titleView = new TextView(requireContext());
                            titleView.setText(title != null ? title : "Task");
                            titleView.setTextSize(18);
                            titleView.setTypeface(null, android.graphics.Typeface.BOLD);
                            taskCard.addView(titleView);

                            TextView descView = new TextView(requireContext());
                            descView.setText(description != null ? description : "No description");
                            descView.setTextSize(14);
                            descView.setPadding(0, 10, 0, 0);
                            taskCard.addView(descView);

                            TextView wardView = new TextView(requireContext());
                            wardView.setText(String.format("Ward: %s", ward != null ? ward : "N/A"));
                            wardView.setTextSize(12);
                            wardView.setPadding(0, 10, 0, 0);
                            taskCard.addView(wardView);

                            taskCard.setOnClickListener(v -> {
                                Intent i = FileViewerActivity.newIntent(
                                        requireContext(),
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
                            });

                            tasksContainer.addView(taskCard);
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing task: " + e.getMessage());
                        }
                    });
                });
    }
}
