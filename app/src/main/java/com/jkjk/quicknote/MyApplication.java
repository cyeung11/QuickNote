package com.jkjk.quicknote;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;


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
        super.onTerminate();
        helper.close();
    }

}
