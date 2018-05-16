package com.jkjk.quicknote.Widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.jkjk.quicknote.R;
import com.jkjk.quicknote.Service.AppWidgetService;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link NoteWidgetConfigureActivity NoteWidgetConfigureActivity}
 */
public class NoteWidget extends AppWidgetProvider {

    public final static String SELECT_NOTE_ID = "selectedNoteId";
    public static int [] existWidgetId;


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        // There may be multiple widgets active, so update all of them

        for (int appWidgetId : appWidgetIds) {

            context.startService(new Intent(context, AppWidgetService.class));

            Intent intent = new Intent(context,AppWidgetService.class);
            intent.putExtra(EXTRA_APPWIDGET_ID, appWidgetId);
            PendingIntent pendingIntent = PendingIntent.getService(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
            try { pendingIntent.send();
            } catch (Exception e){
                Toast.makeText(context, R.string.error_text, Toast.LENGTH_SHORT).show();
                Log.e(this.getClass().getName(),"error",e);
            }
        }
    }
}

