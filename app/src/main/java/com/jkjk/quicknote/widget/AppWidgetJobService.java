package com.jkjk.quicknote.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.util.TypedValue;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.preference.PreferenceManager;

import com.jkjk.quicknote.R;
import com.jkjk.quicknote.listscreen.List;
import com.jkjk.quicknote.noteeditscreen.Note;
import com.jkjk.quicknote.noteeditscreen.NoteEdit;
import com.jkjk.quicknote.taskeditscreen.TaskEdit;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_PROVIDER;
import static com.jkjk.quicknote.helper.AlarmHelper.ITEM_TYPE;
import static com.jkjk.quicknote.noteeditscreen.NoteEditFragment.EXTRA_ITEM_ID;


public class AppWidgetJobService extends JobIntentService {


    public AppWidgetJobService(){
        super();
    }

    static void enqueueWidget(Context context, Class<?> cls, int[] widgetIds) {
        Intent intent = new Intent().putExtra(EXTRA_APPWIDGET_ID, widgetIds)
                .putExtra(EXTRA_APPWIDGET_PROVIDER, new ComponentName(context, cls));
        enqueueWork(context, AppWidgetJobService.class, 1, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        handleIntent(this, intent);
    }

    private static void updateListWidget(Context context, boolean itemIsNote, int[] appWidgetIds, RemoteViews[] views, AppWidgetManager appWidgetManager){
        Intent startAppIntent = new Intent(context, List.class);
        startAppIntent.putExtra(ITEM_TYPE, itemIsNote ?'N' :'T');
        PendingIntent startAppPendingIntent = PendingIntent.getActivity(context
                , itemIsNote ? AppWidgetService.NOTE_LIST_WIDGET_START_APP_REQUEST_CODE : AppWidgetService.TASK_LIST_WIDGET_START_APP_REQUEST_CODE
                , startAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent newItemIntent = new Intent(context, itemIsNote ?NoteEdit.class :TaskEdit.class);
        newItemIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent newItemPendingIntent = PendingIntent.getActivity(context, 0, newItemIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent listWidgetAdapterIntent = new Intent(context, itemIsNote ?NoteListWidgetRemoteService.class :TaskListWidgetRemoteService.class);
        Intent itemIntentTemplate = new Intent(context, itemIsNote ?NoteEdit.class :TaskEdit.class);
        PendingIntent pendingIntentTemplate = PendingIntent.getActivity(context, 455463, itemIntentTemplate, PendingIntent.FLAG_UPDATE_CURRENT);

        int currentNightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean darkModeOn = currentNightMode == Configuration.UI_MODE_NIGHT_YES;

        for (int i = 0; i < appWidgetIds.length; i++) {
            views[i] = new RemoteViews(context.getPackageName(), itemIsNote ? R.layout.note_list_widget : R.layout.task_list_widget);
            views[i].setOnClickPendingIntent(R.id.app_bar, startAppPendingIntent);
            views[i].setOnClickPendingIntent(R.id.add_button, newItemPendingIntent);

            views[i].setInt(R.id.background, "setBackgroundColor", darkModeOn ? Color.DKGRAY : Color.WHITE);

            views[i].setRemoteAdapter(R.id.container, listWidgetAdapterIntent);
            views[i].setPendingIntentTemplate(R.id.container, pendingIntentTemplate);

            appWidgetManager.updateAppWidget(appWidgetIds[i], views[i]);
        }
    }

    static void handleIntent(Context context, Intent intent) {
        if (intent.hasExtra(EXTRA_APPWIDGET_PROVIDER)) {
            int[] appWidgetIds;
            RemoteViews[] views;

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            appWidgetIds = intent.getIntArrayExtra(EXTRA_APPWIDGET_ID);
            views = new RemoteViews[appWidgetIds.length];

            ComponentName name = intent.getParcelableExtra(EXTRA_APPWIDGET_PROVIDER);

            if (name.equals(new ComponentName(context, NoteWidget.class))) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                String widgetSize = sharedPref.getString(context.getString(R.string.font_size_widget), "m");

                long[] noteId = new long[appWidgetIds.length];
                int[] color = new int[appWidgetIds.length];

                //get the correspond database id with the widget id
                SharedPreferences idPref = context.getSharedPreferences("widget_id", MODE_PRIVATE);
                SharedPreferences colorPref = context.getSharedPreferences("widget_color", MODE_PRIVATE);
                for (int i = 0; i < appWidgetIds.length; i++) {
                    noteId[i] = idPref.getLong(Integer.toString(appWidgetIds[i]), 0);
                    color[i] = colorPref.getInt(Integer.toString(appWidgetIds[i]), Color.parseColor("#FFFFFF"));
                }

                Intent openNoteIntent = new Intent(context, NoteEdit.class);

                for (int i = 0; i < appWidgetIds.length; i++) {

                    views[i] = new RemoteViews(context.getPackageName(), R.layout.note_preview_widget);

                    Note note = Note.Companion.getNote(context, noteId[i]);

                    try {
                        if (note != null) {
                            views[i].setTextViewText(R.id.widget_title, note.getTitle());
                            views[i].setTextViewText(R.id.widget_content, note.getContent());
                            views[i].setInt(R.id.widget, "setBackgroundColor", color[i]);

                            switch (widgetSize) {
                                case "s":
                                    views[i].setTextViewTextSize(R.id.widget_title, TypedValue.COMPLEX_UNIT_SP, 16);
                                    views[i].setTextViewTextSize(R.id.widget_content, TypedValue.COMPLEX_UNIT_SP, 14);
                                    break;
                                case "m":
                                    views[i].setTextViewTextSize(R.id.widget_title, TypedValue.COMPLEX_UNIT_SP, 18);
                                    views[i].setTextViewTextSize(R.id.widget_content, TypedValue.COMPLEX_UNIT_SP, 16);
                                    break;
                                case "l":
                                    views[i].setTextViewTextSize(R.id.widget_title, TypedValue.COMPLEX_UNIT_SP, 22);
                                    views[i].setTextViewTextSize(R.id.widget_content, TypedValue.COMPLEX_UNIT_SP, 20);
                                    break;
                                case "xl":
                                    views[i].setTextViewTextSize(R.id.widget_title, TypedValue.COMPLEX_UNIT_SP, 26);
                                    views[i].setTextViewTextSize(R.id.widget_content, TypedValue.COMPLEX_UNIT_SP, 24);
                                    break;
                                default:
                                    views[i].setTextViewTextSize(R.id.widget_title, TypedValue.COMPLEX_UNIT_SP, 18);
                                    views[i].setTextViewTextSize(R.id.widget_content, TypedValue.COMPLEX_UNIT_SP, 16);
                            }

                            openNoteIntent.putExtra(EXTRA_ITEM_ID, noteId[i])
                                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                            PendingIntent openNotePI = PendingIntent.getActivity(context, (int) noteId[i], openNoteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                            views[i].setOnClickPendingIntent(R.id.widget, openNotePI);

                        } else {
                            views[i].setTextViewText(R.id.widget_title, context.getString(R.string.error_loading));
                            views[i].setTextViewText(R.id.widget_content, "");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        views[i].setTextViewText(R.id.widget_title, context.getString(R.string.error_loading));
                        views[i].setTextViewText(R.id.widget_content, "");
                    }

                    appWidgetManager.updateAppWidget(appWidgetIds[i], views[i]);
                }
            } else if (name.equals(new ComponentName(context, TaskListWidget.class))) {
                updateListWidget(context, false, appWidgetIds, views, appWidgetManager);

            } else if (name.equals( new ComponentName(context, NoteListWidget.class))) {
                updateListWidget(context, true, appWidgetIds, views, appWidgetManager);
            }
        }
    }
}

