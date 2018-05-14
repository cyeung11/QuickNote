package com.jkjk.quicknote;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.jkjk.quicknote.Fragment.NoteListFragment;
import com.jkjk.quicknote.Interface.OnNoteEdit;

public class NoteList extends AppCompatActivity implements OnNoteEdit{

    public final static String ACTION_UPDATE_NOTE = "actionUpdateNote";
    public final static String EXTRA_NOTE_ID = "extraNoteId";
    public static boolean hasResultSent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_select);
    }

    @Override
    public void onNoteEdit(long position) {
        Intent startNoteActivity = new Intent();
        startNoteActivity.setClassName("com.jkjk.quicknote", "com.jkjk.quicknote.Note");
        startNoteActivity.putExtra(EXTRA_NOTE_ID, position);
        startActivityForResult(startNoteActivity, 1);
    }

    @Override
    public void onNoteEdit() {
        Intent startNoteActivity = new Intent();
        startNoteActivity.setClassName("com.jkjk.quicknote", "com.jkjk.quicknote.Note");
        startActivityForResult(startNoteActivity, 1);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("life","getresult");
        //call its fragment notifyitemchange method to update list
        if (data!=null) {
            NoteListFragment fragment = (NoteListFragment) getSupportFragmentManager().findFragmentById(R.id.note_list);
            if (data.hasExtra("position")) {
                long result = data.getExtras().getLong("position");
                fragment.adapterItemChanged((int) result);
                Log.e("result","old" + Long.toString(result));
                // handle case if the note is newly created
            }else if (data.hasExtra("newNoteId")){
                long result = data.getExtras().getLong("newNoteId");
                //position = noteId - 1
                fragment.adapterItemInserted((int) result-1);
                Log.e("result","new" + Long.toString(result));
            }
        }
        hasResultSent = false;
    }

}
