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
import com.jkjk.quicknote.R;

import java.util.Calendar;

import static com.jkjk.quicknote.DatabaseHelper.DATABASE_NAME;
import static com.jkjk.quicknote.NoteList.EXTRA_NOTE_ID;


/**
 * A simple {@link Fragment} subclass.
 */
public class NoteEditFragment extends Fragment {

    Cursor cursor = null;
    ContentValues values = null;
    long position, newNoteId ;
    public static final String NOTE_ID = "noteId";
    public static final String DEFAULT_FRAGMENT_TAG = "NoteEditFragment";
    EditText titleEditFragment, noteEditFragment;
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

        if (savedInstanceState !=null) {
            position = savedInstanceState.getLong(NOTE_ID, 0L);
        }else if (getArguments() != null) {
            position = getArguments().getLong(EXTRA_NOTE_ID, 0L);
        } else {newNote = true;}

        //read data from database and attach them into the fragment
        if (!newNote) {
            cursor = MyApplication.getDatabase().query(DATABASE_NAME, new String[]{"title", "content", "time"}, "_id" + "=?",
                    new String[]{String.valueOf(position + 1)}, null, null, null, null);
            if (cursor != null) {
                try {
                    cursor.moveToFirst();
                    titleEditFragment.setText(cursor.getString(0));
                    noteEditFragment.setText(cursor.getString(1));
                } catch (Exception e) {
                    Toast.makeText(container.getContext(), R.string.error_loading, Toast.LENGTH_SHORT).show();
                    Log.e(this.getClass().getName(), "error", e);
                }
            }
        }
        return view;
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(NOTE_ID, position);
    }


    public void sendResult(){
        Intent intent = new Intent();
        if (!newNote) {
            intent.putExtra("position", position);
        }else {
            intent.putExtra("newNoteId", newNoteId);
        }
        getActivity().setResult(Activity.RESULT_OK, intent);
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
            MyApplication.getDatabase().update(DATABASE_NAME, values, "_id='" + (position + 1) + "'", null);
        }else {
            newNoteId = MyApplication.getDatabase().insert(DATABASE_NAME, "",values);
        }
        values.clear();
    }


    public static NoteEditFragment newNoteEditFragmentInstance(long position){
        NoteEditFragment fragment = new NoteEditFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(EXTRA_NOTE_ID, position);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static NoteEditFragment newNoteEditFragmentInstance(){
        return new NoteEditFragment();
    }

}
