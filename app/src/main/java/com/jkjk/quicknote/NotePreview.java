package com.jkjk.quicknote;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;
import static com.jkjk.quicknote.MainActivity.ACTION_UPDATE_NOTE;

/**
 * Implementation of App Widget functionality.
 */
public class NotePreview extends AppWidgetProvider {


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.note_preview);

        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {

            context.startService(new Intent(context, AppWidgetService.class));

            Intent intent = new Intent(context,AppWidgetService.class);
            intent.putExtra(ACTION_UPDATE_NOTE,true);
            PendingIntent pendingIntent = PendingIntent.getService(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
            try { pendingIntent.send();
            } catch (Exception e){
                Toast.makeText(context, R.string.error_text, Toast.LENGTH_SHORT).show();
                Log.e("widget","error",e);
            }

        }
    }

}

