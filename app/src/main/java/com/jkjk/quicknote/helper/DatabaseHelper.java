package com.jkjk.quicknote.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Update restore logic if new column is insert
    public final static String[] dbColumn = new String[]{"_id", "title", "content", "event_time", "starred", "type", "urgency", "done", "reminder_time", "repeat_interval", "lat_lng", "place_name"};
    public final static String DATABASE_NAME = "note";
    public final static int CURRENT_DB_VER = 8;
    private final static String CREATE_STRING = "CREATE TABLE IF NOT EXISTS " + DATABASE_NAME +
            " (" + dbColumn[0] + " INTEGER PRIMARY KEY AUTOINCREMENT, " + dbColumn[1] + " TEXT NOT NULL, " + dbColumn[2] + " TEXT NOT NULL" +
            ", " + dbColumn[3] + " INTEGER NOT NULL, " + dbColumn[4] + " INTEGER NOT NULL DEFAULT '0', " + dbColumn[5] + " INTEGER NOT NULL" +
            ", " + dbColumn[6] + " INTEGER NOT NULL DEFAULT '0', " + dbColumn[7] + " INTEGER NOT NULL DEFAULT '0', " + dbColumn[8] + " INTEGER NOT NULL DEFAULT '0'" +
            ", " + dbColumn[9] + " INTEGER NOT NULL DEFAULT '0', " + dbColumn[10] + " TEXT, " + dbColumn[11] + " TEXT)";

    private static final String DATABASE_ALTER_V2 = "ALTER TABLE "
            + DATABASE_NAME + " ADD COLUMN " + dbColumn[4] + " INTEGER;";

    private static final String DATABASE_ALTER_V3_1 = "ALTER TABLE "
            + DATABASE_NAME + " ADD COLUMN " + dbColumn[5] + " INTEGER NOT NULL DEFAULT '0';";
    private static final String DATABASE_ALTER_V3_2 = "ALTER TABLE "
            + DATABASE_NAME + " ADD COLUMN " + dbColumn[6] + " INTEGER NOT NULL DEFAULT '0';";
    private static final String DATABASE_ALTER_V3_3 = "ALTER TABLE "
            + DATABASE_NAME + " ADD COLUMN " + dbColumn[7] + " INTEGER NOT NULL DEFAULT '0';";

    private static final String DATABASE_ALTER_V4_1 = "ALTER TABLE "
            + DATABASE_NAME + " ADD COLUMN " + dbColumn[3] + " INTEGER NOT NULL DEFAULT '0';";
    private static final String DATABASE_ALTER_V4_2 = "CREATE TEMPORARY TABLE note_backup (" + dbColumn[0] + " INTEGER PRIMARY KEY AUTOINCREMENT" +
            ", " + dbColumn[1] + " TEXT NOT NULL, " + dbColumn[2] + " TEXT NOT NULL, " + dbColumn[3] + " INTEGER NOT NULL, " + dbColumn[4] + " INTEGER NOT NULL DEFAULT '0'" +
            ", " + dbColumn[5] + " INTEGER NOT NULL, " + dbColumn[6] + " INTEGER NOT NULL DEFAULT '0', " + dbColumn[7] + " INTEGER NOT NULL DEFAULT '0')";
    private static final String DATABASE_ALTER_V4_3 = "INSERT INTO note_backup SELECT " + dbColumn[0] + ", " + dbColumn[1] + ", " + dbColumn[2] + ", " + dbColumn[3] + ", " + dbColumn[4] + ", " + dbColumn[5] + ", " + dbColumn[6] + ", " + dbColumn[7] + " FROM "
            + DATABASE_NAME + ";";
    private static final String DATABASE_ALTER_V4_4 = "DROP TABLE "
            + DATABASE_NAME + ";";
    private static final String DATABASE_ALTER_V4_5 = "CREATE TABLE IF NOT EXISTS " + DATABASE_NAME +
            " (" + dbColumn[0] + " INTEGER PRIMARY KEY AUTOINCREMENT, " + dbColumn[1] + " TEXT NOT NULL, " + dbColumn[2] + " TEXT NOT NULL" +
            ", " + dbColumn[3] + " INTEGER NOT NULL, " + dbColumn[4] + " INTEGER NOT NULL DEFAULT '0', " + dbColumn[5] + " INTEGER NOT NULL" +
            ", " + dbColumn[6] + " INTEGER NOT NULL DEFAULT '0', " + dbColumn[7] + " INTEGER NOT NULL DEFAULT '0')";
    private static final String DATABASE_ALTER_V4_6 = "INSERT INTO " + DATABASE_NAME
            + " SELECT " + dbColumn[0] + ", " + dbColumn[1] + ", " + dbColumn[2] + ", " + dbColumn[3] + ", " + dbColumn[4] + ", " + dbColumn[5] + ", " + dbColumn[6] + ", " + dbColumn[7] + " FROM note_backup;";
    private static final String DATABASE_ALTER_V4_7 = "DROP TABLE note_backup;";

    private static final String DATABASE_ALTER_V5 =  "ALTER TABLE "
            + DATABASE_NAME + " ADD COLUMN " + dbColumn[8] + " INTEGER NOT NULL DEFAULT '0';";

    private static final String DATABASE_ALTER_V6 =  "ALTER TABLE "
            + DATABASE_NAME + " ADD COLUMN " + dbColumn[9] + " INTEGER NOT NULL DEFAULT '0';";

    private static final String DATABASE_ALTER_V7 =  "ALTER TABLE "
            + DATABASE_NAME + " ADD COLUMN " + dbColumn[10] + " TEXT;";
    
    private static final String DATABASE_ALTER_V8 =  "ALTER TABLE "
            + DATABASE_NAME + " ADD COLUMN " + dbColumn[11] + " TEXT;";

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context,name,factory,version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_STRING);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        
        switch(oldVersion){
            case 0:
            case 1:
               // Insert starred column for starring function
               sqLiteDatabase.execSQL(DATABASE_ALTER_V2); 
            case 2:
               // Insert 3 task list related column into database for task list function 
                sqLiteDatabase.execSQL(DATABASE_ALTER_V3_1);
                sqLiteDatabase.execSQL(DATABASE_ALTER_V3_2);
                sqLiteDatabase.execSQL(DATABASE_ALTER_V3_3);
            case 3:
                // rename column "time" to "event_time" and change its type to INTEGER
                sqLiteDatabase.execSQL(DATABASE_ALTER_V4_1);
                Cursor alterCursor = sqLiteDatabase.query(DATABASE_NAME, new String[]{"_id", "time"}, null, null, null
                        , null, null);
                if (alterCursor != null) {
                    ContentValues values = new ContentValues();

                    alterCursor.moveToFirst();
                    do {
                        String time = alterCursor.getString(1);
                        values.put("event_time", Long.valueOf(time));
                        sqLiteDatabase.update(DATABASE_NAME, values, "_id='" + alterCursor.getLong(0) + "'", null);
                    } while (alterCursor.moveToNext());
                    alterCursor.close();

                    sqLiteDatabase.execSQL(DATABASE_ALTER_V4_2);
                    sqLiteDatabase.execSQL(DATABASE_ALTER_V4_3);
                    sqLiteDatabase.execSQL(DATABASE_ALTER_V4_4);
                    sqLiteDatabase.execSQL(DATABASE_ALTER_V4_5);
                    sqLiteDatabase.execSQL(DATABASE_ALTER_V4_6);
                    sqLiteDatabase.execSQL(DATABASE_ALTER_V4_7);
                }
            case 4:
                // Insert reminder related column for corresponding function
                sqLiteDatabase.execSQL(DATABASE_ALTER_V5);
            case 5:
                // Insert repeat related column for corresponding function
                sqLiteDatabase.execSQL(DATABASE_ALTER_V6);
            case 6:
                // Insert location value for to do
                sqLiteDatabase.execSQL(DATABASE_ALTER_V7);
            case 7:
                // Insert location name for to do
                sqLiteDatabase.execSQL(DATABASE_ALTER_V8);
                break;
            default:
                break;
        }
    }
}
