package com.jkjk.quicknote.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.jkjk.quicknote.R;

import java.util.ArrayList;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_PROVIDER;
import static android.content.Context.MODE_PRIVATE;
import static com.jkjk.quicknote.widget.AppWidgetService.NOTE_WIDGET_REQUEST_CODE;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link NoteWidgetConfigureActivity NoteWidgetConfigureActivity}
 */
public class NoteWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        SharedPreferences idPref = context.getSharedPreferences("widget_id", MODE_PRIVATE);

        //Weird case: onUpdate called before configuration finish. thus generating exception.
        // Add checking to see if the widget is newly created. If so, id will not send to widget service to prevent calling onUpdate before user selected note
        ArrayList <Integer> checkedIds = new ArrayList<>();
        for (int appWidgetId : appWidgetIds) {

            if (idPref.contains(Integer.toString(appWidgetId))) {
                checkedIds.add(appWidgetId);
            }
        }
        int [] resultIds = new int [checkedIds.size()];
        for (int i = 0 ; i < resultIds.length; i++){
            resultIds[i] = checkedIds.get(i);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AppWidgetJobService.enqueueWidget(context, NoteWidget.class, resultIds);
        } else {
            Intent intent = new Intent(context, AppWidgetService.class).putExtra(EXTRA_APPWIDGET_ID, resultIds)
                    .putExtra(EXTRA_APPWIDGET_PROVIDER, new ComponentName(context, NoteWidget.class));
            PendingIntent pendingIntent = PendingIntent.getService(context, NOTE_WIDGET_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            try {
                pendingIntent.send();
            } catch (Exception e) {
                Toast.makeText(context, R.string.error_text, Toast.LENGTH_SHORT).show();
                Log.e(getClass().getName(), "error", e);
            }
        }
    }


    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        SharedPreferences idPref = context.getSharedPreferences("widget_id", MODE_PRIVATE);
        SharedPreferences colorPref = context.getSharedPreferences("widget_color", MODE_PRIVATE);
        for (int appWidgetId : appWidgetIds) {
            idPref.edit().remove(Integer.toString(appWidgetId)).apply();
            colorPref.edit().remove(Integer.toString(appWidgetId)).apply();
        }
        super.onDeleted(context, appWidgetIds);
    }

    public static void updateNoteWidget(Context context){
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName name = new ComponentName(context, NoteWidget.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(name);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AppWidgetJobService.enqueueWidget(context, NoteWidget.class, appWidgetIds);
        } else {
            Intent intent = new Intent(context, AppWidgetService.class).putExtra(EXTRA_APPWIDGET_ID, appWidgetIds)
                    .putExtra(EXTRA_APPWIDGET_PROVIDER, name);
            PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            try {
                pendingIntent.send();
            } catch (Exception e) {
                Toast.makeText(context, R.string.error_text, Toast.LENGTH_SHORT).show();
                Log.e(context.getClass().getName(), "updating widget", e);
            }
        }
    }
}