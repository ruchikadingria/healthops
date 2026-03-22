package com.example.healthops;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class FileViewerActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE = "TITLE";
    public static final String EXTRA_TIME = "TIME";
    public static final String EXTRA_SUBTITLE = "SUBTITLE";
    public static final String EXTRA_DESCRIPTION = "DESCRIPTION";
    public static final String EXTRA_TIME_LABEL = "TIME_LABEL";
    public static final String EXTRA_ROW2_LABEL = "ROW2_LABEL";
    public static final String EXTRA_ROW2_VALUE = "ROW2_VALUE";
    public static final String EXTRA_ROW3_LABEL = "ROW3_LABEL";
    public static final String EXTRA_ROW3_VALUE = "ROW3_VALUE";

    public static Intent newIntent(Context context, String title, String timeValue) {
        Intent i = new Intent(context, FileViewerActivity.class);
        i.putExtra(EXTRA_TITLE, title);
        i.putExtra(EXTRA_TIME, timeValue);
        return i;
    }

    public static Intent newIntent(Context context, String title, String timeValue,
                                   String subtitle, String description,
                                   String row2Label, String row2Value,
                                   String row3Label, String row3Value) {
        Intent i = newIntent(context, title, timeValue);
        i.putExtra(EXTRA_SUBTITLE, subtitle);
        i.putExtra(EXTRA_DESCRIPTION, description);
        i.putExtra(EXTRA_ROW2_LABEL, row2Label);
        i.putExtra(EXTRA_ROW2_VALUE, row2Value);
        i.putExtra(EXTRA_ROW3_LABEL, row3Label);
        i.putExtra(EXTRA_ROW3_VALUE, row3Value);
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
        String timeLabel = intent.getStringExtra(EXTRA_TIME_LABEL);
        String row2Label = intent.getStringExtra(EXTRA_ROW2_LABEL);
        String row2Value = intent.getStringExtra(EXTRA_ROW2_VALUE);
        String row3Label = intent.getStringExtra(EXTRA_ROW3_LABEL);
        String row3Value = intent.getStringExtra(EXTRA_ROW3_VALUE);

        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvSubtitle = findViewById(R.id.tvSubtitle);
        TextView tvDescription = findViewById(R.id.tvDescription);
        TextView tvTimeLabel = findViewById(R.id.tvTimeLabel);
        TextView tvTime = findViewById(R.id.tvTime);
        TextView tvRow2Label = findViewById(R.id.tvRow2Label);
        TextView tvRow2Value = findViewById(R.id.tvRow2Value);
        TextView tvRow3Label = findViewById(R.id.tvRow3Label);
        TextView tvRow3Value = findViewById(R.id.tvRow3Value);

        if (!TextUtils.isEmpty(title)) {
            tvTitle.setText(title);
        }
        if (!TextUtils.isEmpty(time)) {
            tvTime.setText(time);
        }

        boolean extended = intent.hasExtra(EXTRA_SUBTITLE) || intent.hasExtra(EXTRA_DESCRIPTION)
                || intent.hasExtra(EXTRA_ROW2_LABEL) || intent.hasExtra(EXTRA_TIME_LABEL);

        if (extended) {
            tvSubtitle.setText(!TextUtils.isEmpty(subtitle) ? subtitle : getString(R.string.file_default_subtitle));
            tvDescription.setText(!TextUtils.isEmpty(description) ? description : getString(R.string.file_default_description));
            tvTimeLabel.setText(!TextUtils.isEmpty(timeLabel)
                    ? timeLabel
                    : getString(R.string.file_label_shift_time));
            tvRow2Label.setText(!TextUtils.isEmpty(row2Label) ? row2Label : getString(R.string.file_label_team));
            tvRow2Value.setText(!TextUtils.isEmpty(row2Value) ? row2Value : getString(R.string.file_default_row2));
            tvRow3Label.setText(!TextUtils.isEmpty(row3Label) ? row3Label : getString(R.string.file_label_tasks));
            tvRow3Value.setText(!TextUtils.isEmpty(row3Value) ? row3Value : getString(R.string.file_default_row3));
        } else {
            tvSubtitle.setText(R.string.file_duty_overview);
            tvDescription.setText(R.string.file_duty_description_short);
            tvTimeLabel.setText(R.string.file_label_shift_time);
            tvRow2Label.setText(R.string.file_label_location);
            tvRow2Value.setText(R.string.file_duty_location_hint);
            tvRow3Label.setText(R.string.file_label_tasks);
            tvRow3Value.setText(R.string.file_duty_tasks_hint);
        }
    }
}
