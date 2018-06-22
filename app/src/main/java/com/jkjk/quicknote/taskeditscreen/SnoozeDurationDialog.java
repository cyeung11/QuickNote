package com.jkjk.quicknote.taskeditscreen;

import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.jkjk.quicknote.MyApplication;
import com.jkjk.quicknote.R;
import com.jkjk.quicknote.helper.AlarmHelper;

import java.util.Calendar;

import static com.jkjk.quicknote.helper.DatabaseHelper.DATABASE_NAME;
import static com.jkjk.quicknote.noteeditscreen.NoteEditFragment.EXTRA_NOTE_ID;

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
                    snoozeDuration = 300000;
                } else if (i == findViewById(R.id.fifthteen_minute).getId()) {
                    snoozeDuration = 900000;
                } else if (i == findViewById(R.id.thirty_minute).getId()) {
                    snoozeDuration = 1800000;
                } else if (i == findViewById(R.id.one_hour).getId()) {
                    snoozeDuration = 3600000;
                } else if (i == findViewById(R.id.three_hour).getId()) {
                    snoozeDuration = 10800000;
                } else if (i == findViewById(R.id.twelve_hours).getId()) {
                    snoozeDuration = 43200000;
                } else if (i == findViewById(R.id.twenty_four_hour).getId()) {
                    snoozeDuration = 86400000;
                } else {
                    Toast.makeText(SnoozeDurationDialog.this, R.string.error_text, Toast.LENGTH_SHORT).show();
                    SnoozeDurationDialog.this.finish();
                    return;
                }

                long taskId;

                if (getIntent().hasExtra(EXTRA_NOTE_ID)
                        && (taskId = getIntent().getLongExtra(EXTRA_NOTE_ID, 98876146L)) != 98876146L) {

                    Cursor cursor = MyApplication.database.query(DATABASE_NAME, new String[]{"title", "type", "event_time", "content"}
                            , "_id= " + taskId
                            , null, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        char itemType;
                        switch (cursor.getInt(1)) {
                            case 0:
                                itemType = 'N';
                                break;
                            default:
                                itemType = 'T';
                        }

                        // setReminder(Context context, char itemType, long id, String title, String content, long eventTime, long remindTime)
                        AlarmHelper.setReminder(SnoozeDurationDialog.this, itemType, taskId, cursor.getString(0)
                                , cursor.getString(3), cursor.getLong(2), Calendar.getInstance().getTimeInMillis() + snoozeDuration);
                        ContentValues values = new ContentValues();
                        values.put("reminder_time", Calendar.getInstance().getTimeInMillis() + snoozeDuration);
                        MyApplication.database.update(DATABASE_NAME, values, "_id='" + taskId + "'", null);
                        cursor.close();

                        NotificationManager notificationManager = (NotificationManager) SnoozeDurationDialog.this.getSystemService(Context.NOTIFICATION_SERVICE);
                        if (notificationManager != null) {
                            notificationManager.cancel((int)taskId);
                        }
                        SnoozeDurationDialog.this.finish();
                    }
                }
            }
        });
    }
}