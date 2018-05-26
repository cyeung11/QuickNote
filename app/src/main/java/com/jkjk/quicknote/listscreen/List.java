package com.jkjk.quicknote.listscreen;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.jkjk.quicknote.R;

import static com.jkjk.quicknote.listscreen.ListFragment.BACK_UP_REQUEST_CODE;
import static com.jkjk.quicknote.listscreen.ListFragment.RESTORE_REQUEST_CODE;


public class List extends AppCompatActivity  {
    private ListFragment listFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        listFragment = (ListFragment) getSupportFragmentManager().findFragmentById(R.id.list);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case BACK_UP_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    listFragment.selectBackUpLocation();
                } else {
                    Toast.makeText(this,R.string.permission_required,Toast.LENGTH_SHORT).show();
                }
                break;

            case RESTORE_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    listFragment.selectRestoreLocation();
                } else {
                    Toast.makeText(this,R.string.permission_required,Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
