package com.jkjk.quicknote.editscreen;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.jkjk.quicknote.R;

import static com.jkjk.quicknote.editscreen.EditFragment.EXTRA_NOTE_ID;


public class Edit extends AppCompatActivity {

    private EditFragment editFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        long noteId;
        final String fragmentTag = "editFragmentTag";

        if (savedInstanceState == null) {
            //Case when the activity is newly created

            if (getIntent().getAction()!= null && getIntent().getAction().equals(Intent.ACTION_SEND)) {
                //Case when activity is called by external intent
                editFragment = EditFragment.newEditFragmentInstance(getIntent());

            } else if (getIntent().hasExtra(EXTRA_NOTE_ID)) {
                //Case when activity is called by note list activity after user selected existing note
                noteId = getIntent().getLongExtra(EXTRA_NOTE_ID, 0L);
                editFragment = EditFragment.newEditFragmentInstance(noteId);

            } else {
                //Case when opening a new note
                editFragment = EditFragment.newEditFragmentInstance();
            }
            getSupportFragmentManager().beginTransaction().add(R.id.container, editFragment,fragmentTag).commit();

        }else {
            //Case when restoring from saved instance, rotate etc.
            editFragment = (EditFragment) getSupportFragmentManager().findFragmentByTag(fragmentTag);
        }
    }


    @Override
    public void onBackPressed() {
    //confirm to discard dialog, and ask the note list activity to update its view
        new AlertDialog.Builder(this).setTitle(R.string.discard_title).setMessage(R.string.confirm_discard)
                .setPositiveButton(R.string.discard, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (!editFragment.hasNoteSave){
                            //Edit no need to save. so override hasNoteSave to true to avoid saving on onStop
                            editFragment.hasNoteSave = true;
                        }
                        Edit.super.onBackPressed();
                    }
                })
                .setNeutralButton(R.string.save_instead, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Save instantly instead of onStop so that when the note list's onResume get called, the note is already update
                        editFragment.saveNote();
                        Toast.makeText(getBaseContext(),R.string.saved, Toast.LENGTH_SHORT).show();
                        editFragment.updateAllWidget();
                        editFragment.hasNoteSave = true;
                        finish();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
        }

}