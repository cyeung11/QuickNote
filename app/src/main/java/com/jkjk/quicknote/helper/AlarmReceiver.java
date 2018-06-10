package com.jkjk.quicknote.helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.text.format.DateUtils;

import com.jkjk.quicknote.MyApplication;
import com.jkjk.quicknote.R;
import com.jkjk.quicknote.noteeditscreen.NoteEdit;
import com.jkjk.quicknote.taskeditscreen.TaskEdit;

import java.util.Calendar;

import static com.jkjk.quicknote.helper.AlarmHelper.EVENT_TIME;
import static com.jkjk.quicknote.helper.AlarmHelper.ITEM_CONTENT;
import static com.jkjk.quicknote.helper.AlarmHelper.ITEM_TITLE;
import static com.jkjk.quicknote.helper.AlarmHelper.ITEM_TYPE;
import static com.jkjk.quicknote.helper.DatabaseHelper.DATABASE_NAME;
import static com.jkjk.quicknote.noteeditscreen.NoteEditFragment.DEFAULT_NOTE_ID;
import static com.jkjk.quicknote.noteeditscreen.NoteEditFragment.EXTRA_NOTE_ID;
import static com.jkjk.quicknote.taskeditscreen.TaskEditFragment.DATE_NOT_SET_INDICATOR;
import static com.jkjk.quicknote.taskeditscreen.TaskEditFragment.TIME_NOT_SET_HOUR_INDICATOR;
import static com.jkjk.quicknote.taskeditscreen.TaskEditFragment.TIME_NOT_SET_MILLISECOND_INDICATOR;
import static com.jkjk.quicknote.taskeditscreen.TaskEditFragment.TIME_NOT_SET_MINUTE_SECOND_INDICATOR;

public class AlarmReceiver extends BroadcastReceiver {

    final String ACTION_SNOOZE = "snoozeReminder";

    @Override
    public void onReceive(Context context, Intent intent) {
        long taskId;
        final String GROUP_KEY = context.getPackageName();


        if (intent != null && intent.hasExtra(EXTRA_NOTE_ID)
                && (taskId = intent.getLongExtra(EXTRA_NOTE_ID, DEFAULT_NOTE_ID)) != DEFAULT_NOTE_ID
                && intent.hasExtra(ITEM_TYPE)
                && intent.hasExtra(ITEM_TITLE)
                && intent.hasExtra(ITEM_CONTENT)) {

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {

                Notification.Builder builder;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel notificationChannel = new NotificationChannel(context.getPackageName(), context.getString(R.string.notification_channel_name), NotificationManager.IMPORTANCE_HIGH);
                    notificationChannel.enableLights(true);
                    notificationChannel.enableVibration(true);
                    notificationManager.createNotificationChannel(notificationChannel);
                    builder = new Notification.Builder(context, context.getPackageName());
                } else {
                    builder = new Notification.Builder(context);
                    builder.setDefaults(Notification.DEFAULT_ALL);
                }

                // Intent for launching the corresponding task
                Intent openItemIntent = new Intent();
                if (intent.getCharExtra(ITEM_TYPE, 'A') == 'T') {
                    openItemIntent.setClass(context, TaskEdit.class);
                } else if (intent.getCharExtra(ITEM_TYPE, 'A') == 'N') {
                    openItemIntent.setClass(context, NoteEdit.class);
                }
                openItemIntent.putExtra(EXTRA_NOTE_ID, taskId);
                PendingIntent startPendingIntent = PendingIntent.getActivity(context, 0, openItemIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                // Intent for snoozing
                Intent snoozeIntent = new Intent(context, AlarmReceiver.class);
                snoozeIntent.setAction(ACTION_SNOOZE);
                snoozeIntent.putExtra(EXTRA_NOTE_ID, taskId);
                PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(context, 0, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                builder.setContentTitle(intent.getStringExtra(ITEM_TITLE)).setSmallIcon(R.drawable.sharp_event_note_24)
                        .setContentIntent(startPendingIntent).setAutoCancel(true);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT_WATCH) {
                    builder.addAction(R.drawable.sharp_snooze_24, context.getString(R.string.snooze), snoozePendingIntent);
                } else {
                    builder.addAction(new Notification.Action.Builder(R.drawable.sharp_snooze_24, context.getString(R.string.snooze), snoozePendingIntent).build());
                }

                // if reminder has content field, show it
                if (!intent.getStringExtra(ITEM_CONTENT).equals("")) {
                    builder.setStyle(new Notification.BigTextStyle().bigText(intent.getStringExtra(ITEM_CONTENT)));
                }
                // if devices supports, use group notification
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    builder.setGroup(GROUP_KEY);
                }

                long eventTime = intent.getLongExtra(EVENT_TIME, DATE_NOT_SET_INDICATOR);

                if (eventTime != DATE_NOT_SET_INDICATOR) {
                    if (DateUtils.isToday(eventTime)) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(eventTime);

                        //get the time to see if the time was set by user
                        if (calendar.get(Calendar.MILLISECOND) == TIME_NOT_SET_MILLISECOND_INDICATOR
                                && calendar.get(Calendar.SECOND) == TIME_NOT_SET_MINUTE_SECOND_INDICATOR
                                && calendar.get(Calendar.MINUTE) == TIME_NOT_SET_MINUTE_SECOND_INDICATOR
                                && calendar.get(Calendar.HOUR_OF_DAY) == TIME_NOT_SET_HOUR_INDICATOR) {

                            builder.setContentText(context.getString(R.string.due) + " " + context.getString(R.string.today));

                        } else {
                            builder.setContentText(context.getString(R.string.due) + " "
                                    + context.getString(R.string.today) + " "
                                    + context.getString(R.string.at) + " "
                                    + DateUtils.formatDateTime(context, eventTime, DateUtils.FORMAT_SHOW_TIME));
                        }

                    } else if (isTomorrow(eventTime)) {
                        builder.setContentText(context.getString(R.string.due) + " " + context.getString(R.string.tomorrow));
                    } else {
                        builder.setContentText(context.getString(R.string.due) + " " + DateUtils.formatDateTime(context, eventTime, DateUtils.FORMAT_SHOW_DATE));
                    }
                }

                notificationManager.notify((int) taskId, builder.build());

                // Reset reminder option to "No reminder" after presenting the notification
                ContentValues values = new ContentValues();
                values.put("reminder_time", 0L);
                MyApplication.database.update(DATABASE_NAME, values, "_id='" + taskId + "'", null);

            }

        } else if (intent != null && intent.getAction() != null) {

            if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {

                Cursor cursor = MyApplication.database.query(DATABASE_NAME, new String[]{"_id", "title", "type", "event_time", "reminder_time", "content"}, "reminder_time > 0", null, null
                        , null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        char itemType;
                        switch (cursor.getInt(2)) {
                            case 0:
                                itemType = 'N';
                                break;
                            default:
                                itemType = 'T';
                        }
                        // setReminder(Context context, char itemType, long id, String title, String content, long eventTime, long remindTime)
                        AlarmHelper.setReminder(context, itemType, cursor.getLong(0), cursor.getString(1), cursor.getString(5), cursor.getLong(3), cursor.getLong(4));

                    } while (cursor.moveToNext());
                    cursor.close();
                }
            } else if (intent.getAction().equals(ACTION_SNOOZE)) {
                long id;

                if (intent.hasExtra(EXTRA_NOTE_ID) && (id = intent.getLongExtra(EXTRA_NOTE_ID, 98876146L)) != 98876146L) {
                    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (notificationManager != null) {
                        notificationManager.cancel((int)id);
                    }

                    Cursor cursor = MyApplication.database.query(DATABASE_NAME, new String[]{"title", "type", "event_time", "content"}
                    , "_id= " + id
                            , null, null, null, null);
                    if (cursor!=null && cursor.moveToFirst()) {
                        char itemType;
                        switch (cursor.getInt(1)) {
                            case 0:
                                itemType = 'N';
                                break;
                            default:
                                itemType = 'T';
                        }

                        //TODO grab preference at snooze duration
                        // setReminder(Context context, char itemType, long id, String title, String content, long eventTime, long remindTime)
                        AlarmHelper.setReminder(context, itemType, id, cursor.getString(0)
                                , cursor.getString(3), cursor.getLong(2),  Calendar.getInstance().getTimeInMillis() + 600000);
                        ContentValues values = new ContentValues();
                        values.put("reminder_time", Calendar.getInstance().getTimeInMillis() + 600000);
                        MyApplication.database.update(DATABASE_NAME, values, "_id='" + id + "'", null);
                        cursor.close();
                    }
                }
            }
        }
    }

    private boolean isTomorrow(long time) {
        int currentDayOfYear, currentDay, currentYear, currentMonth, setTimeDayOfYear, setTimeYear;

        Calendar setTime = Calendar.getInstance();
        setTime.setTimeInMillis(time);

        Calendar currentTime = Calendar.getInstance();

        currentDay = currentTime.get(Calendar.DAY_OF_MONTH);
        currentDayOfYear = currentTime.get(Calendar.DAY_OF_YEAR);
        currentMonth = currentTime.get(Calendar.MONTH);
        currentYear = currentTime.get(Calendar.YEAR);
        setTimeDayOfYear = setTime.get(Calendar.DAY_OF_YEAR);
        setTimeYear = setTime.get(Calendar.YEAR);

        return (setTimeDayOfYear == currentDayOfYear + 1 && setTimeYear == currentYear) || (setTimeDayOfYear == 1 && setTimeYear == currentYear + 1 && currentMonth == 11 && currentDay == 31);
    }
}
