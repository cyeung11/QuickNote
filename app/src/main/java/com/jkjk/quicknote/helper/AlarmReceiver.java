package com.jkjk.quicknote.helper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;

import com.jkjk.quicknote.R;
import com.jkjk.quicknote.noteeditscreen.NoteEdit;
import com.jkjk.quicknote.taskeditscreen.TaskEdit;

import java.util.Calendar;

import static com.jkjk.quicknote.helper.AlarmHelper.EVENT_TIME;
import static com.jkjk.quicknote.helper.AlarmHelper.ITEM_TITLE;
import static com.jkjk.quicknote.helper.AlarmHelper.ITEM_TYPE;
import static com.jkjk.quicknote.noteeditscreen.NoteEditFragment.DEFAULT_NOTE_ID;
import static com.jkjk.quicknote.noteeditscreen.NoteEditFragment.EXTRA_NOTE_ID;
import static com.jkjk.quicknote.taskeditscreen.TaskEditFragment.DATE_NOT_SET_INDICATOR;
import static com.jkjk.quicknote.taskeditscreen.TaskEditFragment.TIME_NOT_SET_HOUR_INDICATOR;
import static com.jkjk.quicknote.taskeditscreen.TaskEditFragment.TIME_NOT_SET_MILLISECOND_INDICATOR;
import static com.jkjk.quicknote.taskeditscreen.TaskEditFragment.TIME_NOT_SET_MINUTE_SECOND_INDICATOR;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


        if (intent != null && intent.hasExtra(EXTRA_NOTE_ID) && intent.hasExtra(ITEM_TYPE)
                && intent.hasExtra(ITEM_TITLE)){
            Intent openItemIntent = new Intent();
            if (intent.getCharExtra(ITEM_TYPE, 'A') == 'T'){
                openItemIntent.setClass(context, TaskEdit.class);
            } else if (intent.getCharExtra(ITEM_TYPE, 'A') == 'N'){
                openItemIntent.setClass(context, NoteEdit.class);
            }
            openItemIntent.putExtra(EXTRA_NOTE_ID, intent.getLongExtra(EXTRA_NOTE_ID, DEFAULT_NOTE_ID));
            PendingIntent startPendingIntent = PendingIntent.getActivity(context, 0, openItemIntent, PendingIntent.FLAG_UPDATE_CURRENT);


            Notification.Builder builder = new Notification.Builder(context).setContentTitle(intent.getStringExtra(ITEM_TITLE))
                    .setSmallIcon(R.drawable.baseline_event_note_24).setContentIntent(startPendingIntent).setAutoCancel(true);

            long eventTime = intent.getLongExtra(EVENT_TIME, DATE_NOT_SET_INDICATOR);

            if ( eventTime != DATE_NOT_SET_INDICATOR) {
                if (DateUtils.isToday(eventTime)) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(eventTime);

                    //get the time to see if the time was set by user
                    if (calendar.get(Calendar.MILLISECOND) == TIME_NOT_SET_MILLISECOND_INDICATOR
                            && calendar.get(Calendar.SECOND) ==  TIME_NOT_SET_MINUTE_SECOND_INDICATOR
                            && calendar.get(Calendar.MINUTE) ==  TIME_NOT_SET_MINUTE_SECOND_INDICATOR
                            && calendar.get(Calendar.HOUR_OF_DAY) ==  TIME_NOT_SET_HOUR_INDICATOR) {

                        builder.setContentText(context.getText(R.string.today));

                    } else {
                        builder.setContentText( context.getText(R.string.today).toString()
                                + context.getText(R.string.at).toString()
                                + DateUtils.formatDateTime(context, eventTime, DateUtils.FORMAT_SHOW_TIME));
                    }

                } else if (isTomorrow(eventTime)) {
                    builder.setContentText(context.getText(R.string.tomorrow));
                } else {
                    builder.setContentText(DateUtils.formatDateTime(context, eventTime, DateUtils.FORMAT_SHOW_DATE));
                }
            }

            notificationManager.notify((int)intent.getLongExtra(EXTRA_NOTE_ID, DEFAULT_NOTE_ID), builder.build());
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
