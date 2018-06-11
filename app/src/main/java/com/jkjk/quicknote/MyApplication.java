package com.jkjk.quicknote;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.preference.PreferenceManager;

import com.jkjk.quicknote.helper.AlarmReceiver;
import com.jkjk.quicknote.helper.DatabaseHelper;

import static com.jkjk.quicknote.helper.AlarmReceiver.ACTION_TOOL_BAR;


public class MyApplication extends Application {

    public static SQLiteDatabase database;
    public static final int CURRENT_DB_VER = 5;

    @Override
    public void onCreate() {
        super.onCreate();
        DatabaseHelper helper = new DatabaseHelper(this, "note_db", null, CURRENT_DB_VER);
        database = helper.getWritableDatabase();

        // if pinned notification tool bar is enable, start it
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPref.getBoolean(getString(R.string.notification_pin), false)) {
            Intent toolBarIntent = new Intent(this, AlarmReceiver.class);
            toolBarIntent.setAction(ACTION_TOOL_BAR);
            PendingIntent toolbarPendingIntent = PendingIntent.getBroadcast(this, 0, toolBarIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            try {
                toolbarPendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
    }

}
