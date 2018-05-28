package com.jkjk.quicknote.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public final static String[] dbColumn = new String[]{"_id", "title", "content", "time","starred"};
    public final static String DATABASE_NAME = "note";
    // Update setting fragment's restore verify method if column is added
    private final static String CREATE_STRING = "CREATE TABLE IF NOT EXISTS " + DATABASE_NAME +
            " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " + "title TEXT NOT NULL, " +
            "content TEXT NOT NULL, " + "time TEXT NOT NULL, " + "starred INTEGER)";
    private static final String DATABASE_ALTER_V2 = "ALTER TABLE "
            + DATABASE_NAME + " ADD COLUMN starred INTEGER;";

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context,name,factory,version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_STRING);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            sqLiteDatabase.execSQL(DATABASE_ALTER_V2);
        }
    }


}
