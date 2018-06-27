package com.jkjk.quicknote.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.preference.PreferenceManager;
import android.util.TypedValue;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.jkjk.quicknote.MyApplication;
import com.jkjk.quicknote.R;
import com.jkjk.quicknote.listscreen.List;
import com.jkjk.quicknote.noteeditscreen.NoteEdit;
import com.jkjk.quicknote.taskeditscreen.TaskEdit;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_PROVIDER;
import static com.jkjk.quicknote.helper.DatabaseHelper.DATABASE_NAME;
import static com.jkjk.quicknote.noteeditscreen.NoteEditFragment.EXTRA_NOTE_ID;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 */
public class AppWidgetService extends IntentService {

////    static final String EXTRA_WIDGET_CODE = "widgetCode";
    public static final int NOTE_WIDGET_REQUEST_CODE = 0;
    public static final int TASK_LIST_WIDGET_REQUEST_CODE = 1;
    public static final int NOTE_LIST_WIDGET_REQUEST_CODE = 2;

    public AppWidgetService(){
        super("AppWidgetService");
    }



    @Override
    protected void onHandleIntent(Intent intent){

        if (intent!=null && intent.hasExtra(EXTRA_APPWIDGET_PROVIDER)) {
            int[] appWidgetIds;
            RemoteViews[] views;

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            appWidgetIds = intent.getIntArrayExtra(EXTRA_APPWIDGET_ID);
            views = new RemoteViews[appWidgetIds.length];

            if (intent.getParcelableExtra(EXTRA_APPWIDGET_PROVIDER).equals(new ComponentName(getBaseContext(), NoteWidget.class))) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                String widgetSize = sharedPref.getString(getString(R.string.font_size_widget), "m");

                int[] noteId = new int[appWidgetIds.length];
                int[] color = new int[appWidgetIds.length];

                //get the correspond database id with the widget id
                SharedPreferences idPref = getSharedPreferences("widget_id", MODE_PRIVATE);
                SharedPreferences colorPref = getSharedPreferences("widget_color", MODE_PRIVATE);
                for (int i = 0; i < appWidgetIds.length; i++) {
                    noteId[i] = (int) idPref.getLong(Integer.toString(appWidgetIds[i]), 0);
                    color[i] = colorPref.getInt(Integer.toString(appWidgetIds[i]), Color.parseColor("#FFFFFF"));
                }

                Intent openNoteIntent = new Intent(this, NoteEdit.class);

                for (int i = 0; i < appWidgetIds.length; i++) {

                    views[i] = new RemoteViews("com.jkjk.quicknote", R.layout.note_preview_widget);

                    Cursor cursorForWidget = MyApplication.database.query(DATABASE_NAME, new String[]{"_id", "title", "content"}, "_id='" + noteId[i] + "'", null, null
                            , null, null);
                    try {
                        if (cursorForWidget != null) {
                            cursorForWidget.moveToFirst();
                            views[i].setTextViewText(R.id.widget_title, cursorForWidget.getString(1));
                            views[i].setTextViewText(R.id.widget_content, cursorForWidget.getString(2));
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

                            openNoteIntent.putExtra(EXTRA_NOTE_ID, cursorForWidget.getLong(0))
                                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                            PendingIntent openNotePI = PendingIntent.getActivity(this, (int) cursorForWidget.getLong(0), openNoteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                            views[i].setOnClickPendingIntent(R.id.widget, openNotePI);

                            cursorForWidget.close();

                        } else {
                            views[i].setTextViewText(R.id.widget_title, getResources().getString(R.string.error_loading));
                            views[i].setTextViewText(R.id.widget_content, "");
                        }

                    } catch (Exception e) {
                        Toast.makeText(this, R.string.error_widget, Toast.LENGTH_SHORT).show();
                        views[i].setTextViewText(R.id.widget_title, getResources().getString(R.string.error_loading));
                        views[i].setTextViewText(R.id.widget_content, "");
                    }

                    appWidgetManager.updateAppWidget(appWidgetIds[i], views[i]);
                }
            } else if (intent.getParcelableExtra(EXTRA_APPWIDGET_PROVIDER).equals(new ComponentName(getBaseContext(), TaskListWidget.class))) {
                Intent startAppIntent = new Intent(this, List.class);
                PendingIntent startAppPendingIntent = PendingIntent.getActivity(this, 0, startAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                Intent newToDoIntent = new Intent(this, TaskEdit.class);
                newToDoIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent newToDoPendingIntent = PendingIntent.getActivity(this, 0, newToDoIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                Intent taskListWidgetAdapterIntent = new Intent(this, TaskListWidgetRemoteService.class);
                Intent taskIntentTemplate = new Intent(this, TaskEdit.class);
                taskIntentTemplate.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent taskPendingIntentTemplate = PendingIntent.getActivity(this, TASK_LIST_WIDGET_REQUEST_CODE, taskIntentTemplate, PendingIntent.FLAG_UPDATE_CURRENT);

                for (int i = 0; i < appWidgetIds.length; i++) {
                    views[i] = new RemoteViews("com.jkjk.quicknote", R.layout.task_list_widget);
                    views[i].setOnClickPendingIntent(R.id.app_bar, startAppPendingIntent);
                    views[i].setOnClickPendingIntent(R.id.add_button, newToDoPendingIntent);

                    views[i].setRemoteAdapter(R.id.container, taskListWidgetAdapterIntent);
                    views[i].setPendingIntentTemplate(R.id.container, taskPendingIntentTemplate);

                    appWidgetManager.updateAppWidget(appWidgetIds[i], views[i]);
                }
            } else if (intent.getParcelableExtra(EXTRA_APPWIDGET_PROVIDER).equals( new ComponentName(getBaseContext(), NoteListWidget.class))) {
                Intent startAppIntent = new Intent(this, List.class);
                PendingIntent startAppPendingIntent = PendingIntent.getActivity(this, 0, startAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                Intent newNoteIntent = new Intent(this, NoteEdit.class);
                newNoteIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent newNotePendingIntent = PendingIntent.getActivity(this, 0, newNoteIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                Intent noteListWidgetAdapterIntent = new Intent(this, NoteListWidgetRemoteService.class);
                Intent noteIntentTemplate = new Intent(this, NoteEdit.class);
                noteIntentTemplate.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent notePendingIntentTemplate = PendingIntent.getActivity(this, NOTE_LIST_WIDGET_REQUEST_CODE, noteIntentTemplate, PendingIntent.FLAG_UPDATE_CURRENT);

                for (int i = 0; i < appWidgetIds.length; i++) {
                    views[i] = new RemoteViews("com.jkjk.quicknote", R.layout.note_list_widget);
                    views[i].setOnClickPendingIntent(R.id.app_bar, startAppPendingIntent);
                    views[i].setOnClickPendingIntent(R.id.add_button, newNotePendingIntent);

                    views[i].setRemoteAdapter(R.id.container, noteListWidgetAdapterIntent);
                    views[i].setPendingIntentTemplate(R.id.container, notePendingIntentTemplate);

                    appWidgetManager.updateAppWidget(appWidgetIds[i], views[i]);
                }

            }
        }
    }

}

