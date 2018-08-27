package com.jkjk.quicknote.settings;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.jkjk.quicknote.R;
import com.jkjk.quicknote.listscreen.List;

import static com.jkjk.quicknote.settings.SettingsFragment.BACK_UP_REQUEST_CODE;
import static com.jkjk.quicknote.settings.SettingsFragment.RESTORE_REQUEST_CODE;

public class Settings extends AppCompatActivity {

    private SettingsFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar settingsMenu = findViewById(R.id.settings_menu);
        setSupportActionBar(settingsMenu);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle(R.string.settings);
        }

        final String fragmentTag = "settingsFragment";

        if (savedInstanceState == null) {
            settingsFragment = new SettingsFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.settings_container, settingsFragment,fragmentTag).commit();
        } else {
            settingsFragment = (SettingsFragment) getSupportFragmentManager().findFragmentByTag(fragmentTag);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                Intent intent = new Intent(this, List.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, List.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case BACK_UP_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    settingsFragment.selectBackUpLocation();
                } else {
                    new AlertDialog.Builder(this).setTitle(R.string.permission_required).setMessage(R.string.storage_permission_msg)
                            .setPositiveButton(R.string.open_settings, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intent.setData(uri);
                                    startActivity(intent);
                                }
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .show();
                }
                break;

            case RESTORE_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    settingsFragment.selectRestoreLocation();
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        new AlertDialog.Builder(this).setTitle(R.string.permission_required).setMessage(R.string.storage_permission_msg)
                                .setPositiveButton(R.string.open_settings, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        intent.setData(uri);
                                        startActivity(intent);
                                    }
                                })
                                .setNegativeButton(R.string.cancel, null)
                                .show();
                    }
                }
                break;
        }
    }
}
