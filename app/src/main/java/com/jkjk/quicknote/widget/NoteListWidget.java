package com.jkjk.quicknote.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.jkjk.quicknote.R;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_PROVIDER;
import static com.jkjk.quicknote.widget.AppWidgetService.NOTE_LIST_WIDGET_REQUEST_CODE;

/**
 * Implementation of App Widget functionality.
 */
public class NoteListWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AppWidgetJobService.enqueueWidget(context, NoteListWidget.class, appWidgetIds);
        } else {
            Intent intent = new Intent(context, AppWidgetService.class).putExtra(EXTRA_APPWIDGET_ID, appWidgetIds)
                    .putExtra(EXTRA_APPWIDGET_PROVIDER, new ComponentName(context, NoteListWidget.class));
            PendingIntent pendingIntent = PendingIntent.getService(context, NOTE_LIST_WIDGET_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
            try {
                pendingIntent.send();
            } catch (Exception e) {
                Toast.makeText(context, R.string.error_text, Toast.LENGTH_SHORT).show();
                Log.e(getClass().getName(), "error", e);
            }
        }
    }

    public static void updateNoteListWidget(Context context){
        // Update task list widget by calling notifyAppWidgetViewDataChanged after obtaining the widget id of the list widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, NoteListWidget.class));
        if (appWidgetIds.length > 0){
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.container);
        }
    }
}

