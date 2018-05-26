package com.jkjk.quicknote;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import com.jkjk.quicknote.helper.DatabaseHelper;


public class MyApplication extends Application {

    public static SQLiteDatabase database;
    private DatabaseHelper helper = null;

    @Override
    public void onCreate() {
        super.onCreate();
        helper = new DatabaseHelper(this, "note_db", null, 1);
        database = helper.getWritableDatabase();
    }

    @Override
    public void onTerminate() {
        helper.close();
        super.onTerminate();
    }

}
