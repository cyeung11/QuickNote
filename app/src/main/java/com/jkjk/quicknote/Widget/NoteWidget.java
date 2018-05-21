package com.jkjk.quicknote.Widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.jkjk.quicknote.R;

import java.util.ArrayList;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.content.Context.MODE_PRIVATE;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link NoteWidgetConfigureActivity NoteWidgetConfigureActivity}
 */
public class NoteWidget extends AppWidgetProvider {


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        // There may be multiple widgets active, so update all of them
        SharedPreferences idPref = context.getSharedPreferences("widget_id", MODE_PRIVATE);
        SharedPreferences colorPref = context.getSharedPreferences("widget_color", MODE_PRIVATE);

        //Weird case: onUpdate called before configuration finish. thus generating exception.
        // Add checking to see if the widget is newly created. If so, id will not send to widget service to prevent calling onUpdate before user selected note
        ArrayList <Integer> checking = new ArrayList<>();
        for (int appWidgetId : appWidgetIds) {

            if (idPref.contains(Integer.toString(appWidgetId))) {
                checking.add(appWidgetId);
            }
        }
        int [] result = new int [checking.size()];
        for (int i = 0 ; i<result.length; i++){
            result[i] = checking.get(i);
        }

        Intent intent = new Intent(context, AppWidgetService.class).putExtra(EXTRA_APPWIDGET_ID, result);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        try {
            pendingIntent.send();
        } catch (Exception e) {
            Toast.makeText(context, R.string.error_text, Toast.LENGTH_SHORT).show();
            Log.e(getClass().getName(), "error",e);
        }
    }


    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        SharedPreferences idPref = context.getSharedPreferences("widget_id", MODE_PRIVATE);
        SharedPreferences colorPref = context.getSharedPreferences("widget_color", MODE_PRIVATE);
        for (int appWidgetId : appWidgetIds) {
            idPref.edit().remove(Integer.toString(appWidgetId)).commit();
            colorPref.edit().remove(Integer.toString(appWidgetId)).commit();
        }
        super.onDeleted(context, appWidgetIds);
    }

}