package com.jkjk.quicknote;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public final static String DATABASE_NAME = "note";
    public final static String CREATE_STRING = "CREATE TABLE IF NOT EXISTS " + DATABASE_NAME +
            " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " + "title TEXT NOT NULL, " +
            "content TEXT NOT NULL, " + "time TEXT NOT NULL)";
    public final static String DROP_STRING = "DROP TABLE IF EXISTS " + DATABASE_NAME + ";";
    public final static String QUERY_STRING = "SELECT * FROM "+ DATABASE_NAME + ";";

    DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context,name,factory,version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_STRING);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL(DROP_STRING);
        onCreate(sqLiteDatabase);
    }
}
