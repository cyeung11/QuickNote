package com.jkjk.quicknote.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public final static String[] dbColumn = new String[]{"_id", "title", "content", "event_time", "starred", "type", "urgency", "done", "reminder_time"};
    public final static String DATABASE_NAME = "note";
    private final static String CREATE_STRING = "CREATE TABLE IF NOT EXISTS " + DATABASE_NAME +
            " (_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, content TEXT NOT NULL" +
            ", event_time INTEGER NOT NULL, starred INTEGER NOT NULL DEFAULT '0', type INTEGER NOT NULL" +
            ", urgency INTEGER NOT NULL DEFAULT '0', done INTEGER NOT NULL DEFAULT '0', reminder_time INTEGER NOT NULL DEFAULT '0')";

    private static final String DATABASE_ALTER_V2 = "ALTER TABLE "
            + DATABASE_NAME + " ADD COLUMN starred INTEGER;";

    private static final String DATABASE_ALTER_V3_1 = "ALTER TABLE "
            + DATABASE_NAME + " ADD COLUMN type INTEGER NOT NULL DEFAULT '0';";
    private static final String DATABASE_ALTER_V3_2 = "ALTER TABLE "
            + DATABASE_NAME + " ADD COLUMN urgency INTEGER NOT NULL DEFAULT '0';";
    private static final String DATABASE_ALTER_V3_3 = "ALTER TABLE "
            + DATABASE_NAME + " ADD COLUMN done INTEGER NOT NULL DEFAULT '0';";

    private static final String DATABASE_ALTER_V4_1 = "ALTER TABLE "
            + DATABASE_NAME + " ADD COLUMN event_time INTEGER NOT NULL DEFAULT '0';";
    private static final String DATABASE_ALTER_V4_2 = "CREATE TEMPORARY TABLE note_backup (_id INTEGER PRIMARY KEY AUTOINCREMENT" +
            ", title TEXT NOT NULL, content TEXT NOT NULL, event_time INTEGER NOT NULL, starred INTEGER NOT NULL DEFAULT '0'" +
            ", type INTEGER NOT NULL, urgency INTEGER NOT NULL DEFAULT '0', done INTEGER NOT NULL DEFAULT '0')";
    private static final String DATABASE_ALTER_V4_3 = "INSERT INTO note_backup SELECT _id, title, content, event_time, starred, type, urgency, done FROM "
            + DATABASE_NAME + ";";
    private static final String DATABASE_ALTER_V4_4 = "DROP TABLE "
            + DATABASE_NAME + ";";
    private static final String DATABASE_ALTER_V4_5 = "CREATE TABLE IF NOT EXISTS " + DATABASE_NAME +
            " (_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, content TEXT NOT NULL" +
            ", event_time INTEGER NOT NULL, starred INTEGER NOT NULL DEFAULT '0', type INTEGER NOT NULL" +
            ", urgency INTEGER NOT NULL DEFAULT '0', done INTEGER NOT NULL DEFAULT '0')";
    private static final String DATABASE_ALTER_V4_6 = "INSERT INTO " + DATABASE_NAME
            + " SELECT _id, title, content, event_time, starred, type, urgency, done FROM note_backup;";
    private static final String DATABASE_ALTER_V4_7 = "DROP TABLE note_backup;";

    private static final String DATABASE_ALTER_V5 =  "ALTER TABLE "
            + DATABASE_NAME + " ADD COLUMN reminder_time INTEGER NOT NULL DEFAULT '0';";

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context,name,factory,version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_STRING);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // Insert starred column for starring function
        if (oldVersion < 2) {
            sqLiteDatabase.execSQL(DATABASE_ALTER_V2);
        }
        // Insert 3 task list related column into database for task list function
        if (oldVersion < 3) {
            sqLiteDatabase.execSQL(DATABASE_ALTER_V3_1);
            sqLiteDatabase.execSQL(DATABASE_ALTER_V3_2);
            sqLiteDatabase.execSQL(DATABASE_ALTER_V3_3);
        }
        // rename column "time" to "event_time" and change its type to INTEGER
        if (oldVersion < 4) {
            sqLiteDatabase.execSQL(DATABASE_ALTER_V4_1);
            Cursor alterCursor = sqLiteDatabase.query(DATABASE_NAME, new String[]{"_id", "time"}, null, null, null
                    , null, null);
            if (alterCursor != null){
                ContentValues values = new ContentValues();

                alterCursor.moveToFirst();
                do {
                    String time = alterCursor.getString(1);
                    values.put("event_time", Long.valueOf(time));
                    sqLiteDatabase.update(DATABASE_NAME, values, "_id='" + alterCursor.getLong(0) +"'", null);
                } while (alterCursor.moveToNext());
                alterCursor.close();

                sqLiteDatabase.execSQL(DATABASE_ALTER_V4_2);
                sqLiteDatabase.execSQL(DATABASE_ALTER_V4_3);
                sqLiteDatabase.execSQL(DATABASE_ALTER_V4_4);
                sqLiteDatabase.execSQL(DATABASE_ALTER_V4_5);
                sqLiteDatabase.execSQL(DATABASE_ALTER_V4_6);
                sqLiteDatabase.execSQL(DATABASE_ALTER_V4_7);
            }
        }
        // Insert reminder related column for corresponding function
        if (oldVersion < 5) {
            sqLiteDatabase.execSQL(DATABASE_ALTER_V5);
        }
    }


}
