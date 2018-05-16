package com.jkjk.quicknote.Service;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;

import com.jkjk.quicknote.MyApplication;
import com.jkjk.quicknote.R;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static com.jkjk.quicknote.Adapter.WidgetAdapter.NEW_WIDGET;
import static com.jkjk.quicknote.DatabaseHelper.DATABASE_NAME;
import static com.jkjk.quicknote.Widget.NoteWidget.SELECT_NOTE_ID;


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

        RemoteViews views = new RemoteViews("com.jkjk.quicknote", R.layout.note_preview);

        if (intent!=null){
            if (intent.getBooleanExtra(NEW_WIDGET, false)) {
                int noteId = (int)intent.getLongExtra(SELECT_NOTE_ID, 0L);

                Cursor tempNote = MyApplication.getDatabase().query(DATABASE_NAME, new String[]{"title", "content"}, "_id='"+noteId+"'", null, null
                        , null, null);
                if (tempNote != null) {
                    tempNote.moveToFirst();
                    views.setTextViewText(R.id.widget_title, tempNote.getString(0));
                    views.setTextViewText(R.id.widget_content, tempNote.getString(1));
                }
                tempNote.close();

                Intent startAppIntent = new Intent();
                startAppIntent.setClassName("com.jkjk.quicknote", "com.jkjk.quicknote.NoteList");
                PendingIntent pendingIntent = PendingIntent.getActivity(this,0,startAppIntent,PendingIntent.FLAG_UPDATE_CURRENT);
                views.setOnClickPendingIntent(R.id.widget, pendingIntent);
            }

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            appWidgetManager.updateAppWidget(intent.getIntExtra(EXTRA_APPWIDGET_ID, 0), views);
        }

//            if (getFileStreamPath("noteSaved").canRead()) {
//                try {
//                    InputStreamReader in = new InputStreamReader(openFileInput("noteSaved"));
//                    BufferedReader reader = new BufferedReader(in);
//                    StringBuilder stringBuilder = new StringBuilder();
//                    String mReadLine;
//                    while ((mReadLine = reader.readLine()) != null) {
//                        stringBuilder.append(mReadLine).append("\n");
//                    }
//                    reader.close();
//                    if (stringBuilder.length() != 0) {
//                        stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length() + 1);
//                    }
//                    views.setTextViewText(R.id.widget_content, stringBuilder.toString());
//                } catch (Exception e) {
//                    views.setTextViewText(R.id.widget_content, getString(R.string.error_loading));
//                    Log.e(this.getClass().getName(),"error",e);}




    }


}