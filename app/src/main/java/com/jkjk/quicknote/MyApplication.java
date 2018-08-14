package com.jkjk.quicknote;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.preference.PreferenceManager;

import com.jkjk.quicknote.helper.DatabaseHelper;
import com.jkjk.quicknote.helper.NotificationHelper;

import java.util.Map;

import static com.jkjk.quicknote.helper.NotificationHelper.ACTION_PIN_ITEM;
import static com.jkjk.quicknote.helper.NotificationHelper.ACTION_TOOL_BAR;
import static com.jkjk.quicknote.noteeditscreen.NoteEditFragment.EXTRA_ITEM_ID;


public class MyApplication extends Application {

    public static final String PINNED_NOTIFICATION_IDS = "pinned_notification_ids";
    public SQLiteDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();
        DatabaseHelper helper = new DatabaseHelper(this, "note_db", null, DatabaseHelper.CURRENT_DB_VER);
        database = helper.getWritableDatabase();

        // if pinned notification tool bar is enable, start it
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPref.getBoolean(getString(R.string.notification_pin), false)) {
            Intent toolBarIntent = new Intent(this, NotificationHelper.class);
            toolBarIntent.setAction(ACTION_TOOL_BAR);
            PendingIntent toolbarPendingIntent = PendingIntent.getBroadcast(this, 0, toolBarIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            try {
                toolbarPendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }

        SharedPreferences idPref = this.getSharedPreferences(PINNED_NOTIFICATION_IDS, MODE_PRIVATE);
        Map<String, ?> pinnedItem = idPref.getAll();
        for (String key : pinnedItem.keySet()){
            Intent intent = new Intent(this, NotificationHelper.class);
            intent.setAction(ACTION_PIN_ITEM);
            intent.putExtra(EXTRA_ITEM_ID,  (Long) pinnedItem.get(key));
            PendingIntent pinNotificationPI = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            try {
                pinNotificationPI.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
    }

}
