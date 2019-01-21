package com.jkjk.quicknote.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.support.v7.preference.PreferenceManager;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.jkjk.quicknote.MyApplication;
import com.jkjk.quicknote.R;
import com.jkjk.quicknote.listscreen.List;
import com.jkjk.quicknote.noteeditscreen.NoteEdit;
import com.jkjk.quicknote.taskeditscreen.TaskEdit;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_PROVIDER;
import static com.jkjk.quicknote.helper.AlarmHelper.ITEM_TYPE;
import static com.jkjk.quicknote.helper.DatabaseHelper.DATABASE_NAME;
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

        if (intent.hasExtra(EXTRA_APPWIDGET_PROVIDER)) {
            int[] appWidgetIds;
            RemoteViews[] views;

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            appWidgetIds = intent.getIntArrayExtra(EXTRA_APPWIDGET_ID);
            views = new RemoteViews[appWidgetIds.length];

            ComponentName name = intent.getParcelableExtra(EXTRA_APPWIDGET_PROVIDER);

            if (name.equals(new ComponentName(getBaseContext(), NoteWidget.class))) {
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

                    views[i] = new RemoteViews(getPackageName(), R.layout.note_preview_widget);

                    Cursor cursorForWidget = ((MyApplication)getApplicationContext()).database.query(DATABASE_NAME, new String[]{"_id", "title", "content"}, "_id='" + noteId[i] + "'", null, null
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

                            openNoteIntent.putExtra(EXTRA_ITEM_ID, cursorForWidget.getLong(0))
                                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                            PendingIntent openNotePI = PendingIntent.getActivity(this, (int) cursorForWidget.getLong(0), openNoteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                            views[i].setOnClickPendingIntent(R.id.widget, openNotePI);

                            cursorForWidget.close();

                        } else {
                            views[i].setTextViewText(R.id.widget_title, getResources().getString(R.string.error_loading));
                            views[i].setTextViewText(R.id.widget_content, "");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        views[i].setTextViewText(R.id.widget_title, getResources().getString(R.string.error_loading));
                        views[i].setTextViewText(R.id.widget_content, "");
                    }

                    appWidgetManager.updateAppWidget(appWidgetIds[i], views[i]);
                }
            } else if (name.equals(new ComponentName(getBaseContext(), TaskListWidget.class))) {
                updateListWidget(false, appWidgetIds, views, appWidgetManager);

            } else if (name.equals( new ComponentName(getBaseContext(), NoteListWidget.class))) {
                updateListWidget(true, appWidgetIds, views, appWidgetManager);

            }
        }
    }

    private void updateListWidget(boolean itemIsNote, int[] appWidgetIds, RemoteViews[] views, AppWidgetManager appWidgetManager){
        Intent startAppIntent = new Intent(this, List.class);
        startAppIntent.putExtra(ITEM_TYPE, itemIsNote ?'N' :'T');
        PendingIntent startAppPendingIntent = PendingIntent.getActivity(this
                , itemIsNote ? AppWidgetService.NOTE_LIST_WIDGET_START_APP_REQUEST_CODE : AppWidgetService.TASK_LIST_WIDGET_START_APP_REQUEST_CODE
                , startAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent newItemIntent = new Intent(this, itemIsNote ?NoteEdit.class :TaskEdit.class);
        newItemIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP| Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent newItemPendingIntent = PendingIntent.getActivity(this, 0, newItemIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent listWidgetAdapterIntent = new Intent(this, itemIsNote ?NoteListWidgetRemoteService.class :TaskListWidgetRemoteService.class);
        Intent itemIntentTemplate = new Intent(this, itemIsNote ?NoteEdit.class :TaskEdit.class);
        PendingIntent pendingIntentTemplate = PendingIntent.getActivity(this, 455463, itemIntentTemplate, PendingIntent.FLAG_UPDATE_CURRENT);

        for (int i = 0; i < appWidgetIds.length; i++) {
            views[i] = new RemoteViews(getPackageName(), itemIsNote ?R.layout.note_list_widget :R.layout.task_list_widget);
            views[i].setOnClickPendingIntent(R.id.app_bar, startAppPendingIntent);
            views[i].setOnClickPendingIntent(R.id.add_button, newItemPendingIntent);

            views[i].setRemoteAdapter(R.id.container, listWidgetAdapterIntent);
            views[i].setPendingIntentTemplate(R.id.container, pendingIntentTemplate);

            appWidgetManager.updateAppWidget(appWidgetIds[i], views[i]);
        }
    }
}

