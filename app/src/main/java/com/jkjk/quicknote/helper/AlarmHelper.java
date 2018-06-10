package com.jkjk.quicknote.helper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.jkjk.quicknote.R;

import static com.jkjk.quicknote.noteeditscreen.NoteEditFragment.EXTRA_NOTE_ID;

public class AlarmHelper {

    static final String ITEM_TYPE = "type";
    static final String ITEM_TITLE = "title";
    static final String ITEM_CONTENT = "content";
    static final String EVENT_TIME = "eventTime";

    public AlarmHelper(){
    }

    public static void setReminder(Context context, char itemType, long id, String title, String content, long eventTime, long remindTime){
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(EXTRA_NOTE_ID, id);
        intent.putExtra(ITEM_TYPE, itemType);
        intent.putExtra(ITEM_TITLE, title);
        intent.putExtra(ITEM_CONTENT, content);
        intent.putExtra(EVENT_TIME, eventTime);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int)id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, remindTime, pendingIntent);
        } else Toast.makeText(context, R.string.error_reminder, Toast.LENGTH_SHORT).show();
    }

    public static void cancelReminder(Context context, long id){
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int)id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        } else Toast.makeText(context, R.string.error_reminder, Toast.LENGTH_SHORT).show();
    }
}
