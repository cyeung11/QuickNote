package com.jkjk.quicknote.widget;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.jkjk.quicknote.MyApplication;
import com.jkjk.quicknote.R;

import java.util.Calendar;

import static com.jkjk.quicknote.helper.DatabaseHelper.DATABASE_NAME;
import static com.jkjk.quicknote.listscreen.NoteListAdapter.isYesterday;
import static com.jkjk.quicknote.noteeditscreen.NoteEditFragment.EXTRA_ITEM_ID;

public class NoteListRemoteFactory implements RemoteViewsService.RemoteViewsFactory {
    private Cursor noteCursor;
    private Context context;
    private int widgetLayout;
    private SQLiteDatabase database;

    NoteListRemoteFactory(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate() {
        database = ((MyApplication)context.getApplicationContext()).database;
    }

    @Override
    public void onDataSetChanged() {
        noteCursor = database.query(DATABASE_NAME, new String[]{"_id", "title", "content", "event_time","starred"},  "type = 0", null, null
                , null, "event_time DESC");
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String widgetSize = sharedPref.getString(context.getString(R.string.font_size_widget), "m");
        switch (widgetSize){
            case "s":
                widgetLayout = R.layout.widget_note_s;
                break;
            case "m":
                widgetLayout = R.layout.widget_note_m;
                break;
            case "l":
                widgetLayout = R.layout.widget_note_l;
                break;
            case "xl":
                widgetLayout = R.layout.widget_note_xl;
                break;
            default:
                widgetLayout = R.layout.widget_note_m;
        }
    }

    @Override
    public void onDestroy() {
        noteCursor.close();
    }

    @Override
    public int getCount() {
        return noteCursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        RemoteViews remoteViews = new RemoteViews("com.jkjk.quicknote", widgetLayout);

        if (noteCursor == null){
            remoteViews = new RemoteViews("com.jkjk.quicknote", R.layout.list_widget_loading);
            remoteViews.setTextViewText(R.id.text, context.getString(R.string.error_loading));
            return remoteViews;
        }

        noteCursor.moveToPosition(i);

        remoteViews.setTextViewText(R.id.item_title, noteCursor.getString(1));

        remoteViews.setTextViewText(R.id.note_content, noteCursor.getString(2));

        long time = noteCursor.getLong(3);
        String shownTime;
        // Get current time from Calendar and check how long ago was the note edited
        long timeSpan = Calendar.getInstance().getTimeInMillis() - time;

        if (timeSpan < 300000L){
            //less than 5 minutes
            shownTime = context.getString(R.string.just_now);

        } else if (timeSpan < 3600000L){
            //less than an hour
            shownTime = DateUtils.getRelativeTimeSpanString(time).toString();

        }else if (DateUtils.isToday(time)) {
            shownTime = DateUtils.formatDateTime(context, time, DateUtils.FORMAT_SHOW_TIME);

        } else if (isYesterday(time)){
            shownTime = context.getString(R.string.yesterday);

        } else {
            shownTime = DateUtils.formatDateTime(context, time, DateUtils.FORMAT_SHOW_DATE);
        }
        remoteViews.setTextViewText(R.id.item_date, shownTime);

        if (noteCursor.getInt(4) == 0){
            remoteViews.setViewVisibility(R.id.flag, View.GONE);
        } else {
            remoteViews.setViewVisibility(R.id.flag, View.VISIBLE);
        }

        Intent openNoteIntent = new Intent();
        openNoteIntent.putExtra(EXTRA_ITEM_ID, noteCursor.getLong(0));
        remoteViews.setOnClickFillInIntent(R.id.container, openNoteIntent);
        return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
        return new RemoteViews("com.jkjk.quicknote", R.layout.list_widget_loading);
    }

    @Override
    public int getViewTypeCount() {
        return 4;
    }

    @Override
    public long getItemId(int i) {
        if (noteCursor != null) {
            noteCursor.moveToPosition(i);
            return noteCursor.getLong(0);
        } else return 0;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
