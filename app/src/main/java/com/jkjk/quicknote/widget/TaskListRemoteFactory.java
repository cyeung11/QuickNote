package com.jkjk.quicknote.widget;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v7.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.jkjk.quicknote.MyApplication;
import com.jkjk.quicknote.R;

import java.util.Calendar;

import static com.jkjk.quicknote.helper.DatabaseHelper.DATABASE_NAME;
import static com.jkjk.quicknote.listscreen.TaskListAdapter.isTomorrow;
import static com.jkjk.quicknote.noteeditscreen.NoteEditFragment.EXTRA_NOTE_ID;
import static com.jkjk.quicknote.taskeditscreen.TaskEditFragment.DATE_NOT_SET_INDICATOR;
import static com.jkjk.quicknote.taskeditscreen.TaskEditFragment.TIME_NOT_SET_HOUR_INDICATOR;
import static com.jkjk.quicknote.taskeditscreen.TaskEditFragment.TIME_NOT_SET_MILLISECOND_INDICATOR;
import static com.jkjk.quicknote.taskeditscreen.TaskEditFragment.TIME_NOT_SET_MINUTE_SECOND_INDICATOR;

public class TaskListRemoteFactory implements RemoteViewsService.RemoteViewsFactory {
    private Cursor taskCursor;
    private Context context;
    private int widgetLayout, noUrgencyLayout;

    TaskListRemoteFactory(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean byUrgencyByDefault = sharedPref.getBoolean(context.getString(R.string.change_default_sorting), false);

        if (byUrgencyByDefault){
            taskCursor = MyApplication.database.query(DATABASE_NAME, new String[]{"_id", "title", "urgency", "event_time"}, "type=1 AND done=0", null, null
                    , null, "urgency DESC, event_time ASC");
        } else {
            taskCursor = MyApplication.database.query(DATABASE_NAME, new String[]{"_id", "title", "urgency", "event_time"}, "type=1 AND done=0", null, null
                    , null, "event_time ASC");
        }

        String widgetSize = sharedPref.getString(context.getString(R.string.font_size_widget), "m");
        switch (widgetSize) {
            case "s":
                widgetLayout = R.layout.widget_task_s;
                noUrgencyLayout = R.layout.widget_task_no_urgency_s;
                break;
            case "m":
                widgetLayout = R.layout.widget_task_m;
                noUrgencyLayout = R.layout.widget_task_no_urgency_m;
                break;
            case "l":
                widgetLayout = R.layout.widget_task_l;
                noUrgencyLayout = R.layout.widget_task_no_urgency_l;
                break;
            case "xl":
                widgetLayout = R.layout.widget_task_xl;
                noUrgencyLayout = R.layout.widget_task_no_urgency_xl;
                break;
            default:
                widgetLayout = R.layout.widget_task_m;
                noUrgencyLayout = R.layout.widget_task_no_urgency_m;
        }
    }

    @Override
    public void onDestroy() {
        taskCursor.close();
    }

    @Override
    public int getCount() {
        return taskCursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        RemoteViews remoteViews;
        if (taskCursor == null){
            remoteViews = new RemoteViews("com.jkjk.quicknote", R.layout.list_widget_loading);
            remoteViews.setTextViewText(R.id.text, context.getString(R.string.error_loading));
            return remoteViews;
        }

        taskCursor.moveToPosition(i);

        switch (taskCursor.getInt(2)){
            case 2:
                remoteViews = new RemoteViews("com.jkjk.quicknote", widgetLayout);
                remoteViews.setTextColor(R.id.task_urgency, context.getResources().getColor(R.color.colorPrimary));
                remoteViews.setTextViewText(R.id.task_urgency, context.getString(R.string.asap));
                break;
            case 1:
                remoteViews = new RemoteViews("com.jkjk.quicknote", widgetLayout);
                remoteViews.setTextColor(R.id.task_urgency, context.getResources().getColor(R.color.darkGrey));
                remoteViews.setTextViewText(R.id.task_urgency, context.getString(R.string.important));
                break;
            case 0:
                remoteViews = new RemoteViews("com.jkjk.quicknote", noUrgencyLayout);
                break;
            default:
                remoteViews = new RemoteViews("com.jkjk.quicknote", R.layout.list_widget_loading);
                remoteViews.setTextViewText(R.id.text, context.getString(R.string.error_loading));
                return remoteViews;
        }

        long time = taskCursor.getLong(3);
        remoteViews.setTextColor(R.id.task_date, context.getResources().getColor(R.color.darkGrey));

        if (time!=DATE_NOT_SET_INDICATOR) {
            if (DateUtils.isToday(time)) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(time);

                //get the time to see if the time was set by user
                if (calendar.get(Calendar.MILLISECOND) == TIME_NOT_SET_MILLISECOND_INDICATOR
                        && calendar.get(Calendar.SECOND) ==  TIME_NOT_SET_MINUTE_SECOND_INDICATOR
                        && calendar.get(Calendar.MINUTE) ==  TIME_NOT_SET_MINUTE_SECOND_INDICATOR
                        && calendar.get(Calendar.HOUR_OF_DAY) ==  TIME_NOT_SET_HOUR_INDICATOR) {

                    remoteViews.setTextViewText(R.id.task_date, context.getString(R.string.today));

                } else remoteViews.setTextViewText(R.id.task_date, DateUtils.formatDateTime(context, time, DateUtils.FORMAT_SHOW_TIME));

            } else if (isTomorrow(time)) {
                remoteViews.setTextViewText(R.id.task_date, context.getString(R.string.tomorrow));
            } else {
                remoteViews.setTextViewText(R.id.task_date, DateUtils.formatDateTime(context, time, DateUtils.FORMAT_SHOW_DATE));
                if (Calendar.getInstance().getTimeInMillis()>time){
                    remoteViews.setTextColor(R.id.task_date, context.getResources().getColor(R.color.alternative));
                }
            }
        } else {
            remoteViews.setTextViewText(R.id.task_date, "");
        }

        remoteViews.setTextViewText(R.id.task_title, taskCursor.getString(1));

        Intent openTaskIntent = new Intent();
        openTaskIntent.putExtra(EXTRA_NOTE_ID, taskCursor.getLong(0));
        remoteViews.setOnClickFillInIntent(R.id.container, openTaskIntent);
        return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
        return new RemoteViews("com.jkjk.quicknote", R.layout.list_widget_loading);
    }

    @Override
    public int getViewTypeCount() {
        // 2 each for 4 sizes and one for error layout
        return 9;
    }

    @Override
    public long getItemId(int i) {
        if (taskCursor != null) {
            taskCursor.moveToPosition(i);
            return taskCursor.getLong(0);
        } else return 0;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
