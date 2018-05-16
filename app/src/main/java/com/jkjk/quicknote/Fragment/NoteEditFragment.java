package com.jkjk.quicknote.Fragment;


import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.jkjk.quicknote.MyApplication;
import com.jkjk.quicknote.NoteListAdapter;
import com.jkjk.quicknote.R;

import java.util.Calendar;

import static com.jkjk.quicknote.DatabaseHelper.DATABASE_NAME;
import static com.jkjk.quicknote.Fragment.NoteListFragment.EXTRA_NOTE_ID;


/**
 * A simple {@link Fragment} subclass.
 */
public class NoteEditFragment extends Fragment {

    public static final String NOTE_ID = "noteId";
    public static final String DEFAULT_FRAGMENT_TAG = "NoteEditFragment";

    public static boolean hasNoteSave;

    private ContentValues values = null;
    long noteId, newNoteId ;
    private EditText titleEditFragment, noteEditFragment;
    boolean newNote;


    public NoteEditFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_note_edit, container, false);
        titleEditFragment = (EditText) view.findViewById(R.id.title_edit_fragment);
        noteEditFragment = (EditText) view.findViewById(R.id.content_edit_fragment);
        hasNoteSave = false;

        if (savedInstanceState !=null) {
            noteId = savedInstanceState.getLong(NOTE_ID, 0L);
        }else if (getArguments() != null) {
            noteId = getArguments().getLong(EXTRA_NOTE_ID, 0L);
        } else {newNote = true;}

        //read data from database and attach them into the fragment
        if (!newNote) {
            try {
//                    cursor.moveToPosition((int)position);
                Cursor tempNote = MyApplication.getDatabase().query(DATABASE_NAME, new String[]{"_id","title", "content", "time"}, "_id='" + noteId +"'",
                      null, null, null, null, null);
                Log.d("cursor index: ",Long.toString(noteId));
                if (tempNote.moveToFirst()){
                    Log.d("cursor info", Integer.toString(tempNote.getColumnCount()));
                }else {Log.d("no cursor","error");}
                titleEditFragment.setText(tempNote.getString(1));
                noteEditFragment.setText(tempNote.getString(2));
                tempNote.close();
            } catch (Exception e) {
                Toast.makeText(container.getContext(), R.string.error_loading, Toast.LENGTH_SHORT).show();
                Log.e(this.getClass().getName(), "error", e);
            }
        }
        return view;
    }



    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(NOTE_ID, noteId);
    }


    public void sendResult(){
        getActivity().setResult(Activity.RESULT_OK, new Intent());
    }

    public void saveNote(){
        values = new ContentValues();
        String noteTitle = titleEditFragment.getText().toString().trim();
        if (noteTitle.length()<1){
            noteTitle = getActivity().getResources().getString(R.string.untitled);
        }
        values.put("title", noteTitle);
        values.put("content", noteEditFragment.getText().toString());
        values.put("time", Long.toString(Calendar.getInstance().getTimeInMillis()));
        if (!newNote) {
            MyApplication.getDatabase().update(DATABASE_NAME, values, "_id='" + noteId +"'", null);
        }else {
            newNoteId = MyApplication.getDatabase().insert(DATABASE_NAME, "",values);
        }
        values.clear();
        NoteListAdapter.updateCursor();
        hasNoteSave = true;
    }


    public static NoteEditFragment newNoteEditFragmentInstance(long noteId){
        NoteEditFragment fragment = new NoteEditFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(EXTRA_NOTE_ID, noteId);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static NoteEditFragment newNoteEditFragmentInstance(){
        return new NoteEditFragment();
    }

}
