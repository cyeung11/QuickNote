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
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v7.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.widget.Toast;

import com.jkjk.quicknote.MyApplication;
import com.jkjk.quicknote.R;
import com.jkjk.quicknote.listscreen.List;
import com.jkjk.quicknote.noteeditscreen.NoteEdit;
import com.jkjk.quicknote.taskeditscreen.SnoozeDurationDialog;
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
import static com.jkjk.quicknote.widget.TaskListWidget.updateTaskListWidget;

public class NotificationHelper extends BroadcastReceiver {

    public static final String ACTION_TOOL_BAR = "showToolBar";
    public static final String ACTION_PIN_ITEM = "pinItemToNofification";
    public static final String ACTION_POST_REMINDER = "postReminderToNofification";
    public static final String ACTION_DAILY_UPDATE = "updateDailyPendingTask";
    public static final String ACTION_MARK_AS_DONE = "markTaskAsDone";
    public static final int TOOL_BAR_NOTIFICATION_ID = 3221;
    public static final int PIN_ITEM_NOTIFICATION_ID = 887;
    public static final int TOOL_BAR_REQUEST_CODE = 2417;
    public static final int DAILY_UPDATE_REQUEST_CODE = 2207;
    private SQLiteDatabase database;

    @Override
    public void onReceive(Context context, Intent intent) {
        database = ((MyApplication)context.getApplicationContext()).database;;
        final String GROUP_KEY = context.getPackageName();

        if (intent != null && intent.getAction() != null) {
            long taskId;
            Cursor cursor;

            switch (intent.getAction()) {
                case ACTION_POST_REMINDER:

                    if (intent.hasExtra(EXTRA_NOTE_ID)
                            && (taskId = intent.getLongExtra(EXTRA_NOTE_ID, DEFAULT_NOTE_ID)) != DEFAULT_NOTE_ID
                            && intent.hasExtra(ITEM_TYPE)
                            && intent.hasExtra(ITEM_TITLE)
                            && intent.hasExtra(ITEM_CONTENT)) {

                        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        if (notificationManager == null){
                            Toast.makeText(context,R.string.error_reminder,Toast.LENGTH_SHORT).show();
                            break;
                        }

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
                            builder.setPriority(Notification.PRIORITY_HIGH);
                        }

                        // Intent for launching the corresponding task
                        Intent openItemIntent = new Intent();
                        if (intent.getCharExtra(ITEM_TYPE, 'A') == 'T') {
                            openItemIntent.setClass(context, TaskEdit.class);
                        } else if (intent.getCharExtra(ITEM_TYPE, 'A') == 'N') {
                            openItemIntent.setClass(context, NoteEdit.class);
                        }
                        openItemIntent.putExtra(EXTRA_NOTE_ID, taskId);
                        PendingIntent startPendingIntent = PendingIntent.getActivity(context, (int)taskId, openItemIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                        // Intent for snoozing
                        Intent snoozeIntent = new Intent(context, SnoozeDurationDialog.class);
                        snoozeIntent.putExtra(EXTRA_NOTE_ID, taskId);
                        // Distinguish snooze & open item
                        PendingIntent snoozePendingIntent = PendingIntent.getActivity(context, (int)taskId, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                        Intent markAsDoneIntent = new Intent(context, NotificationHelper.class);
                        markAsDoneIntent.setAction(ACTION_MARK_AS_DONE).putExtra(EXTRA_NOTE_ID, taskId);
                        PendingIntent markAsDonePendingIntent = PendingIntent.getBroadcast(context, (int)taskId, markAsDoneIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                        builder.setContentTitle(intent.getStringExtra(ITEM_TITLE)).setSmallIcon(R.drawable.ic_stat_name)
                                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_round))
                                .setContentIntent(startPendingIntent).setAutoCancel(true);
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT_WATCH) {
                            builder.addAction(R.drawable.snooze_action, context.getString(R.string.snooze), snoozePendingIntent);
                            builder.addAction(R.drawable.ic_done, context.getString(R.string.done), markAsDonePendingIntent);
                        } else {
                            builder.addAction(new Notification.Action.Builder(R.drawable.sharp_snooze_24, context.getString(R.string.snooze), snoozePendingIntent).build());
                            builder.addAction(new Notification.Action.Builder(R.drawable.sharp_done_24, context.getString(R.string.done), markAsDonePendingIntent).build());
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

                                    builder.setContentText(context.getString(R.string.due_today));

                                } else {
                                    builder.setContentText(context.getString(R.string.due_today_with_time, DateUtils.formatDateTime(context, eventTime, DateUtils.FORMAT_SHOW_TIME)));
                                }

                            } else if (isTomorrow(eventTime)) {
                                builder.setContentText(context.getString(R.string.due_tomorrow));
                            } else {
                                builder.setContentText(context.getString(R.string.due_someday, DateUtils.formatDateTime(context, eventTime, DateUtils.FORMAT_SHOW_DATE)));
                            }
                        }

                        notificationManager.notify((int) taskId, builder.build());

                        // Reset reminder option to "No reminder" after presenting the notification
                        ContentValues values = new ContentValues();
                        values.put("reminder_time", 0L);
                        database.update(DATABASE_NAME, values, "_id='" + taskId + "'", null);
                    }
                    break;


                case Intent.ACTION_BOOT_COMPLETED:
                    cursor = database.query(DATABASE_NAME, new String[]{"_id", "title", "type", "event_time", "reminder_time", "content"}, "reminder_time > 0", null, null
                            , null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        do {
                            char itemType;
                            switch (cursor.getInt(2)) {
                                case 0:
                                    itemType = 'N';
                                    break;
                                case 1:
                                    itemType = 'T';
                                    break;
                                default:
                                    itemType = 'T';
                            }
                            // setReminder(Context context, char itemType, long id, String title, String content, long eventTime, long remindTime)
                            AlarmHelper.setReminder(context, itemType, cursor.getLong(0), cursor.getString(1), cursor.getString(5), cursor.getLong(3), cursor.getLong(4));

                        } while (cursor.moveToNext());
                        cursor.close();
                    }

                    // Schedule daily update if setting is on
                    if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.daily_update), false)){
                        setDailyUpdate(context);
                    }

                    break;

                case ACTION_MARK_AS_DONE:

                    if (intent.hasExtra(EXTRA_NOTE_ID) && (taskId = intent.getLongExtra(EXTRA_NOTE_ID, 98876146L)) != 98876146L) {
                        ContentValues values = new ContentValues();
                        values.put("done", 1);
                        database.update(DATABASE_NAME, values, "_id='" + taskId + "'", null);
                        updateTaskListWidget(context);

                        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        if (notificationManager != null) {
                            notificationManager.cancel((int)taskId);
                        }
                    }
                    break;

                case ACTION_TOOL_BAR: {
                    Intent startNoteActivity = new Intent(context, NoteEdit.class);
                    startNoteActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    PendingIntent notePendingIntent = PendingIntent.getActivity(context, 0, startNoteActivity, PendingIntent.FLAG_UPDATE_CURRENT);
                    Intent startTaskActivity = new Intent(context, TaskEdit.class);
                    startTaskActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
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

                    cursor = database.query(DATABASE_NAME, new String[]{"title"}, "type = 1 AND event_time BETWEEN " + firstSecond + " AND " + lastSecond
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
                    if (notificationManager == null){
                        Toast.makeText(context,R.string.error_reminder,Toast.LENGTH_SHORT).show();
                        break;
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationChannel notificationChannel = new NotificationChannel(context.getPackageName(), context.getString(R.string.notification_channel_name), NotificationManager.IMPORTANCE_MIN);
                        notificationChannel.enableLights(false);
                        notificationChannel.enableVibration(false);
                        notificationManager.createNotificationChannel(notificationChannel);
                        builder = new Notification.Builder(context, context.getPackageName());
                    } else {
                        builder = new Notification.Builder(context);
                        builder.setPriority(Notification.PRIORITY_MIN).setSound(null).setVibrate(null);
                    }


                    builder.setContentTitle(notificationTitle).setSmallIcon(R.drawable.ic_stat_name)
                            .setContentIntent(mainPendingIntent).setAutoCancel(false).setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_round));
                    if (taskCount > 0) {
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

                    notificationManager.notify(TOOL_BAR_NOTIFICATION_ID, notification);

                    AlarmHelper.setDailyUpdate(context, ACTION_TOOL_BAR, TOOL_BAR_REQUEST_CODE);

                    break;
                }
                case ACTION_PIN_ITEM: {
                    long itemId;

                    if (intent.hasExtra(EXTRA_NOTE_ID) && (itemId = intent.getLongExtra(EXTRA_NOTE_ID, 98876146L)) != 98876146L) {

                        Notification.Builder builder;
                        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        if (notificationManager == null){
                            Toast.makeText(context,R.string.error_reminder,Toast.LENGTH_SHORT).show();
                            break;
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            NotificationChannel notificationChannel = new NotificationChannel(context.getPackageName(), context.getString(R.string.notification_channel_name), NotificationManager.IMPORTANCE_MIN);
                            notificationChannel.enableLights(false);
                            notificationChannel.enableVibration(false);
                            notificationManager.createNotificationChannel(notificationChannel);
                            builder = new Notification.Builder(context, context.getPackageName());
                        } else {
                            builder = new Notification.Builder(context);
                            builder.setPriority(Notification.PRIORITY_MIN).setSound(null).setVibrate(null);
                        }

                        Intent openItemIntent = new Intent();
                        if (intent.getCharExtra(ITEM_TYPE, 'A') == 'T') {
                            openItemIntent.setClass(context, TaskEdit.class);
                        } else if (intent.getCharExtra(ITEM_TYPE, 'A') == 'N') {
                            openItemIntent.setClass(context, NoteEdit.class);
                        }
                        openItemIntent.putExtra(EXTRA_NOTE_ID, itemId);
                        PendingIntent startPendingIntent = PendingIntent.getActivity(context, (int)itemId, openItemIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                        builder.setContentTitle(intent.getStringExtra(ITEM_TITLE)).setSmallIcon(R.drawable.ic_stat_name).setAutoCancel(false).setContentIntent(startPendingIntent)
                                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_round));

                        if (intent.getCharExtra(ITEM_TYPE, 'A') == 'T') {
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

                                        builder.setContentText(context.getString(R.string.due_today));

                                    } else {
                                        builder.setContentText(context.getString(R.string.due_today_with_time, DateUtils.formatDateTime(context, eventTime, DateUtils.FORMAT_SHOW_TIME)));
                                    }
                                } else if (isTomorrow(eventTime)) {
                                    builder.setContentText(context.getString(R.string.due_tomorrow));
                                } else {
                                    builder.setContentText(context.getString(R.string.due_someday, DateUtils.formatDateTime(context, eventTime, DateUtils.FORMAT_SHOW_DATE)));
                                }
                            }
                        } else if (intent.getCharExtra(ITEM_TYPE, 'A') == 'N') {
                            builder.setContentText(intent.getStringExtra(ITEM_CONTENT)).setStyle(new Notification.BigTextStyle().bigText(intent.getStringExtra(ITEM_CONTENT)));
                        }

                        Notification notification = builder.build();
                        notification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
                        // to distinguish reminder and pin item, id of pin item is the item id * PIN_ITEM_NOTIFICATION_ID
                        notificationManager.notify(((int)itemId*PIN_ITEM_NOTIFICATION_ID), notification);
                    }
                    break;
                }
                case ACTION_DAILY_UPDATE: {
                    setDailyUpdate(context);
                    break;
                }
            }
        }
    }

    private void setDailyUpdate(Context context){
        AlarmHelper.setDailyUpdate(context, ACTION_DAILY_UPDATE, DAILY_UPDATE_REQUEST_CODE);

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

        Cursor cursor = database.query(DATABASE_NAME, new String[]{"title"}, "type = 1 AND event_time BETWEEN " + firstSecond + " AND " + lastSecond
                , null, null, null, null);
        int taskCount = cursor.getCount();
        StringBuilder stringBuilder = new StringBuilder();
        if (taskCount > 0) {
            if (cursor.moveToFirst()) {
                do {
                    stringBuilder.append(cursor.getString(0)).append(", ");
                } while (cursor.moveToNext());
            }
            cursor.close();
            stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
            // Dont post notification if no task that day
        } else return;

        String notificationTitle = context.getResources().getQuantityString(R.plurals.notification_title, taskCount, taskCount);

        Intent startTaskActivity = new Intent(context, List.class);
        startTaskActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent taskPendingIntent = PendingIntent.getActivity(context, 0, startTaskActivity, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null){
            Toast.makeText(context,R.string.error_reminder,Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(context.getPackageName(), context.getString(R.string.notification_channel_name), NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(notificationChannel);
            builder = new Notification.Builder(context, context.getPackageName());
        } else {
            builder = new Notification.Builder(context);
            builder.setPriority(Notification.PRIORITY_DEFAULT);
        }

        builder.setContentTitle(notificationTitle).setSmallIcon(R.drawable.ic_stat_name)
                .setContentIntent(taskPendingIntent).setAutoCancel(false).setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_round))
                .setContentText(stringBuilder.toString());

        notificationManager.notify(TOOL_BAR_NOTIFICATION_ID, builder.build());
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
