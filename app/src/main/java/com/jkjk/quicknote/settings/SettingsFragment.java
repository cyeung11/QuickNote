package com.jkjk.quicknote.settings;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.widget.Toast;

import com.jkjk.quicknote.MyApplication;
import com.jkjk.quicknote.R;
import com.jkjk.quicknote.helper.AlarmHelper;
import com.jkjk.quicknote.helper.DatabaseHelper;
import com.jkjk.quicknote.helper.NotificationHelper;
import com.jkjk.quicknote.widget.NoteListWidget;
import com.jkjk.quicknote.widget.NoteWidget;
import com.jkjk.quicknote.widget.TaskListWidget;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static com.jkjk.quicknote.helper.DatabaseHelper.CURRENT_DB_VER;
import static com.jkjk.quicknote.helper.DatabaseHelper.DATABASE_NAME;
import static com.jkjk.quicknote.helper.DatabaseHelper.dbColumn;
import static com.jkjk.quicknote.helper.NotificationHelper.ACTION_DAILY_UPDATE;
import static com.jkjk.quicknote.helper.NotificationHelper.ACTION_TOOL_BAR;
import static com.jkjk.quicknote.helper.NotificationHelper.DAILY_UPDATE_REQUEST_CODE;
import static com.jkjk.quicknote.helper.NotificationHelper.TOOL_BAR_NOTIFICATION_ID;
import static com.jkjk.quicknote.helper.NotificationHelper.TOOL_BAR_REQUEST_CODE;
import static com.jkjk.quicknote.widget.NoteListWidget.updateNoteListWidget;
import static com.jkjk.quicknote.widget.NoteWidget.updateNoteWidget;
import static com.jkjk.quicknote.widget.TaskListWidget.updateTaskListWidget;


public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener{

    static final  int BACK_UP_REQUEST_CODE = 3433;
    static final  int RESTORE_REQUEST_CODE = 3449;
    static final int PERMISSION_REQUEST_CODE = 3444;
    private Context context;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        PreferenceManager.setDefaultValues(context, R.xml.preferences, false);


        Preference defaultScreen = findPreference(getString(R.string.default_screen));
        if (defaultScreen.getSharedPreferences().getBoolean(getString(R.string.default_screen), false)){
            defaultScreen.setSummary(R.string.task_as_home_page);
        } else defaultScreen.setSummary(R.string.note_as_home_page);

        Preference defaultSorting = findPreference(getString(R.string.change_default_sorting));
        if (defaultSorting.getSharedPreferences().getBoolean(getString(R.string.change_default_sorting), false)){
            defaultSorting.setSummary(R.string.default_sort_by_urgency);
        } else defaultSorting.setSummary(R.string.default_sort_by_time);

        Preference backup = findPreference(getString(R.string.back_up));
        backup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

//                if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                        != PackageManager.PERMISSION_GRANTED) {
//
//                    // Permission is not granted
//                    if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//                        new AlertDialog.Builder(context).setTitle(R.string.permission_required).setMessage(R.string.storage_permission_msg)
//                                .setPositiveButton(R.string.open_settings, new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialogInterface, int i) {
//                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                                        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
//                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                                        intent.setData(uri);
//                                        startActivity(intent);
//                                    }
//                                })
//                                .setNegativeButton(R.string.cancel, null)
//                                .show();
//                    } else {
//                        // request the permission
//                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                                PERMISSION_REQUEST_CODE);
//                    }
//                } else {
                    // Permission has already been granted
                    selectBackUpLocation();
//                }
                return true;
            }
        });

        Preference restore = findPreference(getString(R.string.restore));
        restore.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

//                //Check permission
//                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
//                        != PackageManager.PERMISSION_GRANTED) {
//
//                    // Permission is not granted
//                    if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
//                        new AlertDialog.Builder(context).setTitle(R.string.permission_required).setMessage(R.string.storage_permission_msg)
//                                .setPositiveButton(R.string.open_settings, new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialogInterface, int i) {
//                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                                        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
//                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                                        intent.setData(uri);
//                                        startActivity(intent);
//                                    }
//                                })
//                                .setNegativeButton(R.string.cancel, null)
//                                .show();
//                    } else {
//                        // request the permission
//                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//                                PERMISSION_REQUEST_CODE);
//                    }
//                } else {
                    // Permission has already been granted
                    selectRestoreLocation();
//                }
                return true;
            }
        });

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            Uri uri;
            if (intent != null) {
                uri = intent.getData();
                if (uri == null){
                    Toast.makeText(getActivity(),R.string.error_text,Toast.LENGTH_SHORT).show();
                    return;
                }

                switch (requestCode){
                    case BACK_UP_REQUEST_CODE:
                        try {
                            ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "w");
                            if (pfd == null) {
                                Toast.makeText(getActivity(),R.string.error_back_up,Toast.LENGTH_SHORT).show();
                                break;
                            }
                            FileOutputStream fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());

                            File dataPath = Environment.getDataDirectory();
                            String dbPath = "//data//"+context.getPackageName()+"//databases//"+DATABASE_NAME+"_db";
                            File db = new File(dataPath, dbPath);
                            FileInputStream fileInputStream = new FileInputStream(db);

                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = fileInputStream.read(buffer))>0){
                                fileOutputStream.write(buffer, 0, length);
                            }

                            fileOutputStream.flush();
                            fileInputStream.close();
                            fileOutputStream.close();
                            pfd.close();

                            Toast.makeText(getActivity(),R.string.backup_success,Toast.LENGTH_SHORT).show();

                        }catch (Exception e){
                            e.printStackTrace();
                            Toast.makeText(getActivity(),R.string.error_back_up,Toast.LENGTH_SHORT).show();
                        }
                        //TODO ads related
//                        ListFragment.isAllowedToUse = false;
                        break;

                    case RESTORE_REQUEST_CODE:
                        new RestoreAysnTask(getContext()).execute(uri);
                        //TODO ads related
//                        ListFragment.isAllowedToUse = false;
                        break;
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }

    void selectBackUpLocation(){
        //Define back up file name
        String backUpName = getString(R.string.back_up_name)+new SimpleDateFormat("yyMMddHHmmss", Locale.getDefault()).format(new Date())+"_db";

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

        // Filter to only show results that can be "opened", such as
        // a file (as opposed to a list of contacts or timezones).
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Create a file with the requested MIME type.
        intent.setType("application/octet-stream");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Intent.EXTRA_TITLE, backUpName);
        startActivityForResult(intent, BACK_UP_REQUEST_CODE);
    }

    void selectRestoreLocation(){
        //Define back up file name

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as
        // a file (as opposed to a list of contacts or timezones).
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Create a file with the requested MIME type.
        intent.setType("application/octet-stream");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent, RESTORE_REQUEST_CODE);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {

        if (key.equals(getString(R.string.font_size_widget))){
            // Update the 3 widget after widget font setting change
            updateNoteWidget(getContext());
            updateNoteListWidget(getContext());
            updateTaskListWidget(getContext());

        } else if (key.equals(getString(R.string.default_screen))){
            Preference defaultScreen = findPreference(getString(R.string.default_screen));
            if (prefs.getBoolean(getString(R.string.default_screen), false)){
                defaultScreen.setSummary(R.string.task_as_home_page);
            } else defaultScreen.setSummary(R.string.note_as_home_page);


        } else if (key.equals(getString(R.string.change_default_sorting))){
            Preference defaultSorting = findPreference(getString(R.string.change_default_sorting));
            if (prefs.getBoolean(getString(R.string.change_default_sorting), false)){
                defaultSorting.setSummary(R.string.default_sort_by_urgency);
                updateTaskListWidget(getContext());
            } else {
                defaultSorting.setSummary(R.string.default_sort_by_time);
                updateTaskListWidget(getContext());
            }

        } else if (key.equals(getString(R.string.notification_pin))){

            if (prefs.getBoolean(getString(R.string.notification_pin), false)){

                Intent toolBarIntent = new Intent(getContext(), NotificationHelper.class);
                toolBarIntent.setAction(ACTION_TOOL_BAR);
                PendingIntent toolbarPendingIntent = PendingIntent.getBroadcast(getContext(), 0, toolBarIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                try {
                    toolbarPendingIntent.send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }

            } else {
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null) {
                    notificationManager.cancel(TOOL_BAR_NOTIFICATION_ID);
                }
                AlarmHelper.cancelDailyUpdate(context, ACTION_TOOL_BAR, TOOL_BAR_REQUEST_CODE);
            }

        } else if  (key.equals(getString(R.string.daily_update))){
            if (prefs.getBoolean(getString(R.string.daily_update), false)){
                Intent intent = new Intent(getContext(), NotificationHelper.class);
                intent.setAction(ACTION_DAILY_UPDATE);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), DAILY_UPDATE_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                try {
                    pendingIntent.send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            } else {
                AlarmHelper.cancelDailyUpdate(context, ACTION_DAILY_UPDATE, DAILY_UPDATE_REQUEST_CODE);
            }
        }
    }


    static class RestoreAysnTask extends AsyncTask<Uri, Void, Boolean>{

        private WeakReference<Context> contextReference;

        RestoreAysnTask(Context context){
            contextReference = new WeakReference<>(context);
        }

        @Override
        protected Boolean doInBackground(Uri... uris) {
            return restoreWithResult(contextReference, uris[0]);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            resultToast(contextReference, aBoolean);
        }
    }


    private static boolean restoreWithResult(WeakReference<Context> contextWeakReference, Uri uri){
        File dataPath = Environment.getDataDirectory();
        Context context = contextWeakReference.get();
        if (context==null) return false;

        String verifyPath = "//data//"+context.getPackageName()+"//databases//verify_db";
        File verifydb = new File(dataPath, verifyPath);

        try {
            ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
            if (pfd == null) return false;
            FileInputStream fileInputStream = new FileInputStream(pfd.getFileDescriptor());

            // Verify integrity of  restoreWithResult file
            FileOutputStream verifyOutputStream = new FileOutputStream(verifydb);

            byte[] verifyBuffer = new byte[1024];
            int verifyLength;

            while ((verifyLength = fileInputStream.read(verifyBuffer))>0){
                verifyOutputStream.write(verifyBuffer, 0, verifyLength);
            }

            verifyOutputStream.flush();
            verifyOutputStream.close();
            fileInputStream.close();
            pfd.close();

            DatabaseHelper helper  = new DatabaseHelper(context.getApplicationContext(), "verify_db", null, CURRENT_DB_VER);
            SQLiteDatabase databaseToBeRestore = helper.getWritableDatabase();

            Cursor verifyCursor = databaseToBeRestore.rawQuery("SELECT * FROM '" + DATABASE_NAME + "' LIMIT 1", null);

            if (databaseToBeRestore.isDatabaseIntegrityOk() && Arrays.equals(verifyCursor.getColumnNames(), dbColumn)
                    && verifyCursor.moveToFirst() && verifyCursor.getType(0) == Cursor.FIELD_TYPE_INTEGER
                    && verifyCursor.getType(1) == Cursor.FIELD_TYPE_STRING && verifyCursor.getType(2) == Cursor.FIELD_TYPE_STRING
                    && verifyCursor.getType(3) == Cursor.FIELD_TYPE_INTEGER && verifyCursor.getType(4) == Cursor.FIELD_TYPE_INTEGER
                    && verifyCursor.getType(5) == Cursor.FIELD_TYPE_INTEGER && verifyCursor.getType(6) == Cursor.FIELD_TYPE_INTEGER
                    && verifyCursor.getType(7) == Cursor.FIELD_TYPE_INTEGER && verifyCursor.getType(8) == Cursor.FIELD_TYPE_INTEGER
                    && verifyCursor.getType(9) == Cursor.FIELD_TYPE_INTEGER &&
                    (verifyCursor.getType(10) == Cursor.FIELD_TYPE_STRING || verifyCursor.getType(10) == Cursor.FIELD_TYPE_NULL) &&
                    (verifyCursor.getType(11) == Cursor.FIELD_TYPE_STRING || verifyCursor.getType(11) == Cursor.FIELD_TYPE_NULL)){
                // Restore file verified. Begin restoring

                ContentValues values = new ContentValues();
                SQLiteDatabase currentDatabase = ((MyApplication)context.getApplicationContext()).database;

                // Create a copy of database column without the id
                String[] columnToBeCopied = new String[dbColumn.length-1];
                System.arraycopy(dbColumn, 1, columnToBeCopied, 0, columnToBeCopied.length);

                verifyCursor = databaseToBeRestore.query(DATABASE_NAME, columnToBeCopied, null, null, null
                        , null, null);
                if (verifyCursor!=null && verifyCursor.moveToFirst()) {
                    do {
                        values.put(columnToBeCopied[0], verifyCursor.getString(0));
                        values.put(columnToBeCopied[1], verifyCursor.getString(1));
                        values.put(columnToBeCopied[2], verifyCursor.getLong(2));
                        values.put(columnToBeCopied[3], verifyCursor.getInt(3));
                        values.put(columnToBeCopied[4], verifyCursor.getInt(4));
                        values.put(columnToBeCopied[5], verifyCursor.getInt(5));
                        values.put(columnToBeCopied[6], verifyCursor.getInt(6));
                        values.put(columnToBeCopied[7], verifyCursor.getLong(7));
                        values.put(columnToBeCopied[8], verifyCursor.getLong(8));
                        values.put(columnToBeCopied[9], verifyCursor.getString(9));
                        values.put(columnToBeCopied[10], verifyCursor.getString(10));
                        currentDatabase.insert(DATABASE_NAME, "", values);
                    } while (verifyCursor.moveToNext());
                }

                if (verifyCursor != null) {
                    verifyCursor.close();
                }
                helper.close();
                return true;
            } else {
                //verification fail. Give up restoreWithResult
                verifyCursor.close();
                helper.close();
                return false;
            }
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }finally {
            SQLiteDatabase.deleteDatabase(verifydb);
        }
    }

    private static void resultToast(WeakReference<Context> contextWeakReference, boolean result){
        Context context = contextWeakReference.get();
        if (context!=null) {
            if (result) {
                Toast.makeText(context, context.getString(R.string.restore_success), Toast.LENGTH_SHORT).show();
                NoteWidget.updateNoteWidget(context);
                TaskListWidget.updateTaskListWidget(context);
                NoteListWidget.updateNoteListWidget(context);
            } else {
                Toast.makeText(context, context.getString(R.string.error_restore), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
