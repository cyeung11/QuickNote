package com.jkjk.quicknote.noteeditscreen;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.actions.NoteIntents;
import com.jkjk.quicknote.R;

import static com.jkjk.quicknote.noteeditscreen.NoteEditFragment.EXTRA_ITEM_ID;
import static com.jkjk.quicknote.widget.NoteListWidget.updateNoteListWidget;
import static com.jkjk.quicknote.widget.NoteWidget.updateNoteWidget;


public class NoteEdit extends AppCompatActivity {

    private static final String fragmentTag = "noteEditFragmentTag";
    private NoteEditFragment noteEditFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_note_edit);

        if (savedInstanceState == null) {
            //Case when the activity is newly created
            handleIntent(getIntent());
        }else {
            //Case when restoring from saved instance, rotate etc.
            noteEditFragment = (NoteEditFragment) getSupportFragmentManager().findFragmentByTag(fragmentTag);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
       handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent.getAction() != null && (intent.getAction().equals(Intent.ACTION_SEND) || intent.getAction().equals(NoteIntents.ACTION_CREATE_NOTE))) {
            //Case when activity is called by external intent
            noteEditFragment = NoteEditFragment.newEditFragmentInstance(intent);

        } else if (intent.hasExtra(EXTRA_ITEM_ID)) {
            //Case when activity is called by note list activity after user selected existing note
            long noteId = intent.getLongExtra(EXTRA_ITEM_ID, -1L);
            noteEditFragment = NoteEditFragment.newEditFragmentInstance(noteId);

        } else {
            //Case when opening a new note
            noteEditFragment = NoteEditFragment.newEditFragmentInstance();
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.note_container, noteEditFragment,fragmentTag).commit();
    }

    @Override
    public void onBackPressed() {
        //confirm to discard dialog, and ask the note list activity to update its view
        if (noteEditFragment.checkModified()) {
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
                            updateNoteWidget(NoteEdit.this);
                            updateNoteListWidget(NoteEdit.this);
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