package com.jkjk.quicknote.settings;


import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.widget.Toast;

import com.jkjk.quicknote.R;
import com.jkjk.quicknote.helper.DatabaseHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import static android.app.Activity.RESULT_OK;
import static com.jkjk.quicknote.helper.DatabaseHelper.DATABASE_NAME;
import static com.jkjk.quicknote.helper.DatabaseHelper.dbColumn;

public class SettingsFragment extends PreferenceFragmentCompat  {


    static final  int BACK_UP_REQUEST_CODE = 5555;
    static final  int RESTORE_REQUEST_CODE = 5556;

    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);


        Preference backup = findPreference(getString(R.string.back_up));
        backup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Permission is not granted
                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        new AlertDialog.Builder(getActivity()).setTitle(R.string.permission_required).setMessage(R.string.permission)
                                .setPositiveButton(R.string.open_settings, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        intent.setData(uri);
                                        startActivity(intent);
                                    }
                                })
                                .setNegativeButton(R.string.cancel, null)
                                .show();
                    } else {
                        // request the permission
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                BACK_UP_REQUEST_CODE);
                    }
                } else {
                    // Permission has already been granted
                    selectBackUpLocation();
                }
                return true;
            }
        });


        Preference restore = findPreference(getString(R.string.restore));
        restore.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(getActivity()).setTitle(R.string.restore).setMessage(R.string.restore_confirm)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                //Check permission
                                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                                        != PackageManager.PERMISSION_GRANTED) {

                                    // Permission is not granted
                                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                        new AlertDialog.Builder(getActivity()).setTitle(R.string.permission_required).setMessage(R.string.permission)
                                                .setPositiveButton(R.string.open_settings, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                                        Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                        intent.setData(uri);
                                                        startActivity(intent);
                                                    }
                                                })
                                                .setNegativeButton(R.string.cancel, null)
                                                .show();
                                    } else {
                                        // request the permission
                                        ActivityCompat.requestPermissions(getActivity(),
                                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                                RESTORE_REQUEST_CODE);
                                    }
                                } else {
                                    // Permission has already been granted
                                    selectRestoreLocation();
                                }
                            }
                        }).setNegativeButton(R.string.cancel, null).show();
                return true;
            }
        });

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            Uri uri = null;
            if (intent != null) {
                uri = intent.getData();

                switch (requestCode){
                    case BACK_UP_REQUEST_CODE:
                        try {
                            ParcelFileDescriptor pfd = getActivity().getContentResolver().openFileDescriptor(uri, "w");
                            FileOutputStream fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());

                            File dataPath = Environment.getDataDirectory();
                            String dbPath = "//data//"+getActivity().getPackageName()+"//databases//"+DATABASE_NAME+"_db";
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

                        break;

                    case RESTORE_REQUEST_CODE:
                        File dataPath = Environment.getDataDirectory();

                        String verifyPath = "//data//"+getActivity().getPackageName()+"//databases//verify_db";
                        File verifydb = new File(dataPath, verifyPath);

                        try {
                            ParcelFileDescriptor pfd = getActivity().getContentResolver().openFileDescriptor(uri, "r");
                            FileInputStream fileInputStream = new FileInputStream(pfd.getFileDescriptor());


                            // Verify integrity of  restore file
                            FileOutputStream verifyOutputStream = new FileOutputStream(verifydb);

                            byte[] verifyBuffer = new byte[1024];
                            int verifyLength;

                            while ((verifyLength = fileInputStream.read(verifyBuffer))>0){
                                verifyOutputStream.write(verifyBuffer, 0, verifyLength);
                            }

                            verifyOutputStream.flush();
                            verifyOutputStream.close();

                            DatabaseHelper helper  = new DatabaseHelper(getActivity().getApplicationContext(), "verify_db", null, 2);
                            SQLiteDatabase database = helper.getWritableDatabase();

                            Cursor verifyCursor = database.query(DATABASE_NAME, null, null, null, null
                                    , null, null);


                            if (database.isDatabaseIntegrityOk() && verifyCursor.moveToLast() && Arrays.equals(verifyCursor.getColumnNames(), dbColumn)){

                                // Restore file verified. Begin restoring
                                String dbPath = "//data//"+getActivity().getPackageName()+"//databases//note_db";
                                File db = new File(dataPath, dbPath);
                                FileOutputStream fileOutputStream = new FileOutputStream(db);

                                fileInputStream = new FileInputStream(verifydb);

                                byte[] buffer = new byte[1024];
                                int length;

                                while ((length = fileInputStream.read(buffer))>0){
                                    fileOutputStream.write(buffer, 0, length);
                                }

                                fileOutputStream.flush();
                                fileInputStream.close();
                                fileOutputStream.close();
                                pfd.close();
                                SQLiteDatabase.deleteDatabase(verifydb);
                                verifyCursor.close();
                                helper.close();

                                Toast.makeText(getActivity(),R.string.restore_success, Toast.LENGTH_SHORT).show();

                            } else {
                                //verification fail. Give up restore
                                fileInputStream.close();
                                pfd.close();
                                SQLiteDatabase.deleteDatabase(verifydb);
                                verifyCursor.close();
                                helper.close();
                                Toast.makeText(getActivity(),R.string.error_restore,Toast.LENGTH_SHORT).show();
                            }

                        }catch (Exception e){
                            e.printStackTrace();
                            try {
                                SQLiteDatabase.deleteDatabase(verifydb);
                            } catch (Exception exception){
                                exception.printStackTrace();
                            }
                            Toast.makeText(getActivity(),R.string.error_restore,Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        }
    }

    void selectBackUpLocation(){
        //Define back up file name
        String backUpName = getString(R.string.back_up_name)+new SimpleDateFormat("yyMMddHHmmss").format(new Date())+"_db";

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


}
