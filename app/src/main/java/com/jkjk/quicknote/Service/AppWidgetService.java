package com.jkjk.quicknote.Service;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.widget.RemoteViews;

import com.jkjk.quicknote.MyApplication;
import com.jkjk.quicknote.R;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static com.jkjk.quicknote.DatabaseHelper.DATABASE_NAME;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 */
public class AppWidgetService extends IntentService {

    public AppWidgetService(){
        super("AppWidgetService");
    }



    @Override
    protected void onHandleIntent(Intent intent){

        if (intent!=null) {

            int[] appWidgetIds = intent.getIntArrayExtra(EXTRA_APPWIDGET_ID);
            int[] noteId  = new int[appWidgetIds.length];

            //get the correspond database id with the widget id
            SharedPreferences pref = getSharedPreferences("widget", MODE_PRIVATE);
            for (int i = 0; i < appWidgetIds.length; i++) {
                noteId[i] = (int) pref.getLong(Integer.toString(appWidgetIds[i]), 0);
            }

            RemoteViews [] views = new RemoteViews[appWidgetIds.length];

            for (int i = 0; i < appWidgetIds.length; i++){

                views[i] = new RemoteViews("com.jkjk.quicknote", R.layout.note_preview);

                Cursor tempNote = MyApplication.getDatabase().query(DATABASE_NAME, new String[]{"title", "content"}, "_id='"+noteId[i]+"'", null, null
                        , null, null);
                if (tempNote != null) {
                    tempNote.moveToFirst();
                    views[i].setTextViewText(R.id.widget_title, tempNote.getString(0));
                    views[i].setTextViewText(R.id.widget_content, tempNote.getString(1));
                }
                tempNote.close();

                Intent startAppIntent = new Intent();
                startAppIntent.setClassName("com.jkjk.quicknote", "com.jkjk.quicknote.NoteList");
                PendingIntent pendingIntent = PendingIntent.getActivity(this,0,startAppIntent,PendingIntent.FLAG_UPDATE_CURRENT);
                views[i].setOnClickPendingIntent(R.id.widget, pendingIntent);

                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
                appWidgetManager.updateAppWidget(appWidgetIds[i], views[i]);
            }
        }
    }
}

