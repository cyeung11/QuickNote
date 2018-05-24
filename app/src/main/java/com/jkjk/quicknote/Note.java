package com.jkjk.quicknote;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.jkjk.quicknote.Fragment.NoteEditFragment;

import static com.jkjk.quicknote.Fragment.NoteEditFragment.EXTRA_NOTE_ID;
import static com.jkjk.quicknote.Fragment.NoteEditFragment.hasNoteSave;


public class Note extends AppCompatActivity {

    final String fragmentTag = NoteEditFragment.DEFAULT_FRAGMENT_TAG;

    long noteId;
    NoteEditFragment noteEditFragment;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        if (savedInstanceState == null) {
            //Case when the activity is newly created

            if (getIntent().getAction()!= null && getIntent().getAction().equals(Intent.ACTION_SEND)) {
                //Case when activity is called by external intent
                noteEditFragment = NoteEditFragment.newNoteEditFragmentInstance(getIntent());

            } else if (getIntent().hasExtra(EXTRA_NOTE_ID)) {
                //Case when activity is called by note list activity after user selected existing note
                noteId = getIntent().getLongExtra(EXTRA_NOTE_ID, 0L);
                noteEditFragment = NoteEditFragment.newNoteEditFragmentInstance(noteId);

            } else {
                //Case when opening a new note
                noteEditFragment = NoteEditFragment.newNoteEditFragmentInstance();
            }
            getSupportFragmentManager().beginTransaction().add(R.id.container, noteEditFragment, fragmentTag).commit();

        }else {
            //Case when restoring from saved instance, rotate etc.
            noteEditFragment = (NoteEditFragment) getSupportFragmentManager().findFragmentByTag(fragmentTag);
        }
    }


    @Override
    public void onBackPressed() {
    //confirm to discard dialog, and ask the note list activity to update its view
        new AlertDialog.Builder(this).setTitle(R.string.discard_title).setMessage(R.string.confirm_discard)
                .setPositiveButton(R.string.discard, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (!hasNoteSave){
                            //Note no need to save. so override hasNoteSave to true to avoid saving on onStop
                            hasNoteSave = true;
                        }
                        Note.super.onBackPressed();
                    }
                })
                .setNeutralButton(R.string.save_instead, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Save instantly instead of onStop so that when the note list's onResume get called, the note is already update
                        noteEditFragment.saveNote();
                        Toast.makeText(getBaseContext(),R.string.saved, Toast.LENGTH_SHORT).show();
                        noteEditFragment.updateAllWidget();
                        hasNoteSave = true;
                        finish();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
        }


    @Override
    protected void onStop() {
        //when user quit the app without choosing save or discard, save the note
        if (!hasNoteSave){
            noteEditFragment.saveNote();
            noteEditFragment.updateAllWidget();
            Toast.makeText(this,R.string.saved, Toast.LENGTH_SHORT).show();
        }
        // then reset it to not saved for the case when user come back
        hasNoteSave = false;
        super.onStop();
    }

}