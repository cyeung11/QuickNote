package com.jkjk.quicknote.taskeditscreen;

import android.app.ActionBar;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.jkjk.quicknote.R;
import com.jkjk.quicknote.helper.AlarmHelper;

import java.util.Calendar;

import static com.jkjk.quicknote.noteeditscreen.NoteEditFragment.EXTRA_ITEM_ID;

public class SnoozeDurationDialog extends AppCompatActivity {
    RadioGroup snoozeDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_snooze_duration);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.hide();
        }
        snoozeDuration = findViewById(R.id.snooze_duration);

        snoozeDuration.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                long snoozeDuration;
                if (i == findViewById(R.id.five_minute).getId()){
                    snoozeDuration = 300000L;
                } else if (i == findViewById(R.id.fifthteen_minute).getId()) {
                    snoozeDuration = 900000L;
                } else if (i == findViewById(R.id.thirty_minute).getId()) {
                    snoozeDuration = 1800000L;
                } else if (i == findViewById(R.id.one_hour).getId()) {
                    snoozeDuration = 3600000L;
                } else if (i == findViewById(R.id.three_hour).getId()) {
                    snoozeDuration = 10800000L;
                } else if (i == findViewById(R.id.twelve_hours).getId()) {
                    snoozeDuration = 43200000L;
                } else if (i == findViewById(R.id.twenty_four_hour).getId()) {
                    snoozeDuration = 86400000L;
                } else {
                    Toast.makeText(SnoozeDurationDialog.this, R.string.error_text, Toast.LENGTH_SHORT).show();
                    SnoozeDurationDialog.this.finish();
                    return;
                }

                long taskId;

                if (getIntent().hasExtra(EXTRA_ITEM_ID)
                        && (taskId = getIntent().getLongExtra(EXTRA_ITEM_ID, 98876146L)) != 98876146L) {

                    long newReminderTime = Calendar.getInstance().getTimeInMillis() + snoozeDuration;

                    AlarmHelper.setReminder(getApplicationContext(), taskId, newReminderTime);

                    Task task = Task.Companion.getTask(getApplicationContext(), taskId);
                    if (task != null) {
                        task.getReminderTime().setTimeInMillis(newReminderTime);
                        task.save(getApplicationContext(), taskId);
                    }

                    NotificationManager notificationManager = (NotificationManager) SnoozeDurationDialog.this.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (notificationManager != null) {
                        notificationManager.cancel((int)taskId);
                    }
                    SnoozeDurationDialog.this.finish();
                }
            }
        });
    }
}