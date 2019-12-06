package com.jkjk.quicknote.widget;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.jkjk.quicknote.R;
import com.jkjk.quicknote.noteeditscreen.Note;

import java.util.ArrayList;
import java.util.Calendar;

import static com.jkjk.quicknote.listscreen.NoteListAdapter.isYesterday;
import static com.jkjk.quicknote.noteeditscreen.NoteEditFragment.EXTRA_ITEM_ID;

public class NoteListRemoteFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context context;
    private int widgetLayout;

    private ArrayList<Note> notes = new ArrayList<>();

    NoteListRemoteFactory(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
        notes = Note.Companion.getAllNotes(context);
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
    }

    @Override
    public int getCount() {
        return notes.size();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), widgetLayout);

        if (i >= notes.size()){
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.list_widget_loading);
            remoteViews.setTextViewText(R.id.text, context.getString(R.string.error_loading));
            return remoteViews;
        }

        Note note = notes.get(i);

        remoteViews.setTextViewText(R.id.item_title, note.getTitle());

        remoteViews.setTextViewText(R.id.note_content, note.getContent());

        long time = note.getEditTime().getTimeInMillis();
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

        if (!note.isStarred()){
            remoteViews.setViewVisibility(R.id.flag, View.GONE);
        } else {
            remoteViews.setViewVisibility(R.id.flag, View.VISIBLE);
        }

        Intent openNoteIntent = new Intent();
        openNoteIntent.putExtra(EXTRA_ITEM_ID, note.getId());
        remoteViews.setOnClickFillInIntent(R.id.container, openNoteIntent);
        return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
        return new RemoteViews(context.getPackageName(), R.layout.list_widget_loading);
    }

    @Override
    public int getViewTypeCount() {
        return 4;
    }

    @Override
    public long getItemId(int i) {
        if (i < notes.size()) {
            return notes.get(i).getId();
        } else return 0;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
