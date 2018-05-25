package com.jkjk.quicknote;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import static com.jkjk.quicknote.DatabaseHelper.DATABASE_NAME;
import static com.jkjk.quicknote.Fragment.NoteListFragment.RESTORE_REQUEST_CODE;

public class Setting extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
    }

    public void restoreNote(View view) {

        //Check permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            } else {
                // request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        RESTORE_REQUEST_CODE);
            }
        } else {
            // Permission has already been granted
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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case RESTORE_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

                    // Filter to only show results that can be "opened", such as
                    // a file (as opposed to a list of contacts or timezones).
                    intent.addCategory(Intent.CATEGORY_OPENABLE);

                    // Create a file with the requested MIME type.
                    intent.setType("application/octet-stream");
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivityForResult(intent, RESTORE_REQUEST_CODE);
                } else {
                    Toast.makeText(this, R.string.permission_required, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            Uri uri = null;
            if (intent != null) {
                uri = intent.getData();

                try {
                    ParcelFileDescriptor pfd = this.getContentResolver().openFileDescriptor(uri, "r");
                    FileInputStream fileInputStream = new FileInputStream(pfd.getFileDescriptor());

                    File dataPath = Environment.getDataDirectory();
                    String dbPath = "//data//" + this.getPackageName() + "//databases//" + DATABASE_NAME + "_db";
                    File db = new File(dataPath, dbPath);
                    FileOutputStream fileOutputStream = new FileOutputStream(db);

                    byte[] buffer = new byte[1024];
                    int length;

                    while ((length = fileInputStream.read(buffer)) > 0) {
                        fileOutputStream.write(buffer, 0, length);
                    }

                    fileOutputStream.flush();
                    fileInputStream.close();
                    fileOutputStream.close();
                    pfd.close();

                    //Restart app
                    Intent restart = new Intent();
                    restart.setClassName(this.getPackageName(),"com.jkjk.quicknote.NoteList");
                    restart.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(restart);

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, R.string.error_back_up, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}