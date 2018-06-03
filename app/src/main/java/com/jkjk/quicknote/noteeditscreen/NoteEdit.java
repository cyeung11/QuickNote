package com.jkjk.quicknote.noteeditscreen;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.jkjk.quicknote.R;

import static com.jkjk.quicknote.noteeditscreen.NoteEditFragment.EXTRA_NOTE_ID;


public class NoteEdit extends AppCompatActivity {

    private NoteEditFragment noteEditFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);
        long noteId;
        final String fragmentTag = "noteEditFragmentTag";

        if (savedInstanceState == null) {
            //Case when the activity is newly created

            if (getIntent().getAction()!= null && getIntent().getAction().equals(Intent.ACTION_SEND)) {
                //Case when activity is called by external intent
                noteEditFragment = NoteEditFragment.newEditFragmentInstance(getIntent());

            } else if (getIntent().hasExtra(EXTRA_NOTE_ID)) {
                //Case when activity is called by note list activity after user selected existing note
                noteId = getIntent().getLongExtra(EXTRA_NOTE_ID, 0L);
                noteEditFragment = NoteEditFragment.newEditFragmentInstance(noteId);

            } else {
                //Case when opening a new note
                noteEditFragment = NoteEditFragment.newEditFragmentInstance();
            }
            getSupportFragmentManager().beginTransaction().add(R.id.note_container, noteEditFragment,fragmentTag).commit();

        }else {
            //Case when restoring from saved instance, rotate etc.
            noteEditFragment = (NoteEditFragment) getSupportFragmentManager().findFragmentByTag(fragmentTag);
        }
    }


    @Override
    public void onBackPressed() {
        //confirm to discard dialog, and ask the note list activity to update its view
        if (noteEditFragment.hasModified) {
            new AlertDialog.Builder(this).setTitle(R.string.discard_title).setMessage(R.string.confirm_discard)
                    .setPositiveButton(R.string.discard, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (!noteEditFragment.hasNoteSave) {
                                //NoteEdit no need to save. so override hasNoteSave to true to avoid saving on onStop
                                noteEditFragment.hasNoteSave = true;
                            }
                            NoteEdit.super.onBackPressed();
                        }
                    })
                    .setNeutralButton(R.string.save_instead, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //Save instantly instead of onStop so that when the note list's onResume get called, the note is already update
                            noteEditFragment.saveNote();
                            Toast.makeText(getBaseContext(), R.string.saved, Toast.LENGTH_SHORT).show();
                            noteEditFragment.updateAllWidget();
                            noteEditFragment.hasNoteSave = true;
                            NoteEdit.super.onBackPressed();
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        } else {
            noteEditFragment.hasNoteSave = true;
            super.onBackPressed();
        }
    }

}