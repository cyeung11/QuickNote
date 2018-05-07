package com.jkjk.quicknote;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.widget.RemoteViews;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import static com.jkjk.quicknote.MainActivity.ACTION_UPDATE_NOTE;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class AppWidgetService extends IntentService {

    public AppWidgetService(){
        super("AppWidgetService");
    }



    @Override
    protected void onHandleIntent(Intent intent){

        RemoteViews views = new RemoteViews("com.jkjk.quicknote", R.layout.note_preview);


        if (intent!=null && intent.getBooleanExtra(ACTION_UPDATE_NOTE, false)){
            if (getFileStreamPath("noteSaved").canRead()) {
                try {
                    InputStreamReader in = new InputStreamReader(openFileInput("noteSaved"));
                    BufferedReader reader = new BufferedReader(in);
                    String mNote = reader.readLine();
                    if (mNote!=null){
                        String mNoteNotNull = mNote.replaceAll("<br>","\n");
                        views.setTextViewText(R.id.widgetText, mNoteNotNull);
                    }else {
                        views.setTextViewText(R.id.widgetText, "");
                    }
                    reader.close();
                } catch (Exception e) {
                    views.setTextViewText(R.id.widgetText, getString(R.string.error_text));
                }
            }
        }

        Intent startAppIntent = new Intent();
        startAppIntent.setClassName("com.jkjk.quicknote", "com.jkjk.quicknote.MainActivity");
        startAppIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,startAppIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget, pendingIntent);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        ComponentName name = new ComponentName(getApplication(),NotePreview.class);
        appWidgetManager.updateAppWidget(name, views);
    }


}