package com.jkjk.quicknote;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.jkjk.quicknote.Fragment.NoteEditFragment;

import static com.jkjk.quicknote.Fragment.NoteEditFragment.hasNoteSave;
import static com.jkjk.quicknote.Fragment.NoteListFragment.EXTRA_NOTE_ID;


public class Note extends AppCompatActivity {

    final String fragmentTag = NoteEditFragment.DEFAULT_FRAGMENT_TAG;

    Long noteId = null;
    NoteEditFragment noteEditFragment;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        if (getIntent().hasExtra(EXTRA_NOTE_ID)) {
            noteId = getIntent().getLongExtra(EXTRA_NOTE_ID, 0L);
            noteEditFragment = NoteEditFragment.newNoteEditFragmentInstance(noteId);
        } else {
            noteEditFragment = NoteEditFragment.newNoteEditFragmentInstance();
        }
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, noteEditFragment, fragmentTag).commit();
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
                noteEditFragment.saveNote();
                noteEditFragment.sendResult();
                Toast.makeText(this,R.string.saved, Toast.LENGTH_SHORT).show();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
    //confirm to discard dialog
        new AlertDialog.Builder(this).setTitle(R.string.discard_title).setMessage(R.string.confirm_discard)
                .setPositiveButton(R.string.discard, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (hasNoteSave){
                            //if note has been saved during onPaused, send back intent to NoteList for an update of view
                            noteEditFragment.sendResult();
                            finish();
                        }else {
                            //Note no need to save
                            hasNoteSave = true;
                            Note.super.onBackPressed();
                        }
                    }
                })
                .setNeutralButton(R.string.save_instead, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        noteEditFragment.saveNote();
                        noteEditFragment.sendResult();
                        Toast.makeText(getBaseContext(),R.string.saved, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
        }


    @Override
    protected void onPause() {
        //when user quit the app without choosing save or discard, save the note
        if (!hasNoteSave){
            NoteEditFragment fragment = (NoteEditFragment) getSupportFragmentManager().findFragmentByTag(fragmentTag);
            fragment.saveNote();
            Toast.makeText(this,R.string.saved, Toast.LENGTH_SHORT).show();
        }
        super.onPause();
    }

}