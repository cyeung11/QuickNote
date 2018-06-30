package com.jkjk.quicknote.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.jkjk.quicknote.R;
import com.jkjk.quicknote.helper.AlarmHelper;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_PROVIDER;
import static com.jkjk.quicknote.widget.AppWidgetService.TASK_LIST_WIDGET_REQUEST_CODE;

public class TaskListWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Intent intent = new Intent(context, AppWidgetService.class).putExtra(EXTRA_APPWIDGET_ID, appWidgetIds)
                .putExtra(EXTRA_APPWIDGET_PROVIDER, new ComponentName(context, TaskListWidget.class));
        PendingIntent pendingIntent = PendingIntent.getService(context, TASK_LIST_WIDGET_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        try {
            pendingIntent.send();
        } catch (Exception e) {
            Toast.makeText(context, R.string.error_text, Toast.LENGTH_SHORT).show();
            Log.e(getClass().getName(), "error",e);
        }
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        // Set an alarm to update the list widget every mid night to present the correct due date
        AlarmHelper.setTaskWidgetUpdate(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        // canceling the daily widget update
        AlarmHelper.cancelTaskWidgetUpdate(context);
    }

    public static void updateTaskListWidget(Context context){
        // Update task list widget by calling notifyAppWidgetViewDataChanged after obtaining the widget id of the list widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, TaskListWidget.class));
        if (appWidgetIds.length > 0){
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.container);
        }
    }
}
