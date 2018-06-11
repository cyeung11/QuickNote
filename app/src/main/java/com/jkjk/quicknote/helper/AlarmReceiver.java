package com.jkjk.quicknote.helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v7.preference.PreferenceManager;
import android.text.format.DateUtils;

import com.jkjk.quicknote.MyApplication;
import com.jkjk.quicknote.R;
import com.jkjk.quicknote.listscreen.List;
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

    public static final String ACTION_TOOL_BAR = "showToolBar";
    public static final int TOOL_BAR_REQUEST_CODE = 9981;

    @Override
    public void onReceive(Context context, Intent intent) {
        long taskId;
        final String GROUP_KEY = context.getPackageName();
        final String ACTION_SNOOZE = "snoozeReminder";


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

                builder.setContentTitle(intent.getStringExtra(ITEM_TITLE)).setSmallIcon(R.drawable.ic_stat_name)
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.mipmap.ic_launcher_round))
                        .setContentIntent(startPendingIntent).setAutoCancel(true);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT_WATCH) {
                    builder.addAction(R.drawable.snooze_action, context.getString(R.string.snooze), snoozePendingIntent);
                } else {
                    builder.addAction(new Notification.Action.Builder(R.drawable.sharp_snooze_24, context.getString(R.string.snooze), snoozePendingIntent).build());
                }

                // if reminder has content field, show it
                // On kit katm big text style is showing improperly
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
                    if (!intent.getStringExtra(ITEM_CONTENT).equals("")) {
                        builder.setStyle(new Notification.BigTextStyle().bigText(intent.getStringExtra(ITEM_CONTENT)));
                    }
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
            Cursor cursor;
            switch (intent.getAction()) {
                case Intent.ACTION_BOOT_COMPLETED:

                    cursor = MyApplication.database.query(DATABASE_NAME, new String[]{"_id", "title", "type", "event_time", "reminder_time", "content"}, "reminder_time > 0", null, null
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
                    break;

                case ACTION_SNOOZE:
                    long id;

                    if (intent.hasExtra(EXTRA_NOTE_ID) && (id = intent.getLongExtra(EXTRA_NOTE_ID, 98876146L)) != 98876146L) {
                        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        if (notificationManager != null) {
                            notificationManager.cancel((int) id);
                        }

                        cursor = MyApplication.database.query(DATABASE_NAME, new String[]{"title", "type", "event_time", "content"}
                                , "_id= " + id
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

                            // grab preference at snooze duration
                            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                            long snoozeDuration = Long.valueOf(sharedPref.getString(context.getString(R.string.snooze_duration), "300000"));
                            // setReminder(Context context, char itemType, long id, String title, String content, long eventTime, long remindTime)
                            AlarmHelper.setReminder(context, itemType, id, cursor.getString(0)
                                    , cursor.getString(3), cursor.getLong(2), Calendar.getInstance().getTimeInMillis() + snoozeDuration);
                            ContentValues values = new ContentValues();
                            values.put("reminder_time", Calendar.getInstance().getTimeInMillis() + snoozeDuration);
                            MyApplication.database.update(DATABASE_NAME, values, "_id='" + id + "'", null);
                            cursor.close();

                            if (sharedPref.getBoolean(context.getString(R.string.notification_pin), false)) {
                                AlarmHelper.setToolbarUpdate(context);
                            }
                        }
                    }
                    break;

                case ACTION_TOOL_BAR: {
                    Intent startNoteActivity = new Intent(context, NoteEdit.class);
                    startNoteActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    PendingIntent notePendingIntent = PendingIntent.getActivity(context, 0, startNoteActivity, PendingIntent.FLAG_UPDATE_CURRENT);
                    Intent startTaskActivity = new Intent(context, TaskEdit.class);
                    startTaskActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    PendingIntent taskPendingIntent = PendingIntent.getActivity(context, 0, startTaskActivity, PendingIntent.FLAG_UPDATE_CURRENT);
                    Intent startMainActivity = new Intent(context, List.class);
                    startMainActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    PendingIntent mainPendingIntent = PendingIntent.getActivity(context, 0, startMainActivity, PendingIntent.FLAG_UPDATE_CURRENT);

                    // Get task due today
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    long firstSecond = calendar.getTimeInMillis();
                    calendar.set(Calendar.HOUR_OF_DAY, 23);
                    calendar.set(Calendar.MINUTE, 59);
                    calendar.set(Calendar.SECOND, 59);
                    calendar.set(Calendar.MILLISECOND, 999);
                    long lastSecond = calendar.getTimeInMillis();

                    cursor = MyApplication.database.query(DATABASE_NAME, new String[]{"title"}, "type = 1 AND event_time BETWEEN " + firstSecond + " AND " + lastSecond
                            , null, null, null, null);
                    int taskCount = 0;
                    StringBuilder stringBuilder = new StringBuilder();
                    if (cursor.moveToFirst()) {
                        do {
                            taskCount += 1;
                            stringBuilder.append(cursor.getString(0)).append(", ");
                        } while (cursor.moveToNext());
                    }
                    cursor.close();
                    if (taskCount > 0) {
                        stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
                    }
                    String notificationTitle = context.getResources().getQuantityString(R.plurals.notification_title, taskCount, taskCount);

                    Notification.Builder builder;
                    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationChannel notificationChannel = new NotificationChannel(context.getPackageName(), context.getString(R.string.notification_channel_name), NotificationManager.IMPORTANCE_HIGH);
                        notificationChannel.enableLights(false);
                        notificationChannel.enableVibration(false);
                        notificationManager.createNotificationChannel(notificationChannel);
                        builder = new Notification.Builder(context, context.getPackageName());
                    } else {
                        builder = new Notification.Builder(context);
                        builder.setSound(null).setVibrate(null);
                    }


                    builder.setContentTitle(notificationTitle).setSmallIcon(R.drawable.ic_stat_name)
                            .setContentIntent(mainPendingIntent).setAutoCancel(false).setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_round));
                    if (taskCount > 0){
                        builder.setContentText(stringBuilder.toString());
                    }
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT_WATCH) {
                        builder.addAction(R.drawable.add_note_action, context.getString(R.string.add_note), notePendingIntent);
                        builder.addAction(R.drawable.add_task_action, context.getString(R.string.add_task), taskPendingIntent);
                    } else {
                        builder.addAction(new Notification.Action.Builder(R.drawable.sharp_note_add_24, context.getString(R.string.add_note), notePendingIntent).build());
                        builder.addAction(new Notification.Action.Builder(R.drawable.sharp_task_add_24, context.getString(R.string.add_task), taskPendingIntent).build());
                    }

                    Notification notification = builder.build();
                    notification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;

                    if (notificationManager != null) {
                        notificationManager.notify(TOOL_BAR_REQUEST_CODE, notification);
                    } else
                    AlarmHelper.setToolbarUpdate(context);

                    break;
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
