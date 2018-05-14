package com.jkjk.quicknote;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.jkjk.quicknote.Fragment.NoteEditFragment;

import static com.jkjk.quicknote.NoteList.EXTRA_NOTE_ID;
import static com.jkjk.quicknote.NoteList.hasResultSent;


public class Note extends AppCompatActivity {

    Long position = null;
    NoteEditFragment fragment;
    final String fragmentTag = NoteEditFragment.DEFAULT_FRAGMENT_TAG;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        if (getIntent().hasExtra(EXTRA_NOTE_ID)) {
            position = getIntent().getLongExtra(EXTRA_NOTE_ID, 0L);
            fragment = NoteEditFragment.newNoteEditFragmentInstance(position);
        } else {
            fragment = NoteEditFragment.newNoteEditFragmentInstance();
        }
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, fragment, fragmentTag).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }catch (Exception e){
            Toast.makeText(this, R.string.error_saving, Toast.LENGTH_SHORT).show();
            Log.e(this.getClass().getName(),"error",e);
        }
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //By pressing the done button on the action bar, call finish and send back edited id to note list activity.
        switch (item.getItemId()) {
            case R.id.done:
                NoteEditFragment fragment = (NoteEditFragment) getSupportFragmentManager().findFragmentByTag(fragmentTag);
                fragment.saveNote();
                fragment.sendResult();
                hasResultSent = true;
                Toast.makeText(this,R.string.saved, Toast.LENGTH_SHORT).show();
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
    //TODO add confirm to discard dialog
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        //when user quit the app with choosing save or discard, save the note
        if (!hasResultSent){
            NoteEditFragment fragment = (NoteEditFragment) getSupportFragmentManager().findFragmentByTag(fragmentTag);
            fragment.saveNote();
            Toast.makeText(this,R.string.saved, Toast.LENGTH_SHORT).show();
        }
        super.onPause();
    }

}
