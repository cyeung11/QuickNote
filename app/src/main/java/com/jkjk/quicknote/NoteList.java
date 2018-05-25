package com.jkjk.quicknote;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.jkjk.quicknote.Fragment.NoteListFragment;

import static com.jkjk.quicknote.Fragment.NoteListFragment.BACK_UP_REQUEST_CODE;
import static com.jkjk.quicknote.Fragment.NoteListFragment.RESTORE_REQUEST_CODE;


public class NoteList extends AppCompatActivity  {
    NoteListFragment noteListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);
        noteListFragment = (NoteListFragment) getSupportFragmentManager().findFragmentById(R.id.note_list);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case BACK_UP_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    noteListFragment.selectBackUpLocation();
                } else {
                    Toast.makeText(this,R.string.permission_required,Toast.LENGTH_SHORT).show();
                }
                break;

            case RESTORE_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    noteListFragment.selectRestoreLocation();
                } else {
                    Toast.makeText(this,R.string.permission_required,Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
