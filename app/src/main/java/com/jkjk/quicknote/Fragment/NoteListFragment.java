package com.jkjk.quicknote.Fragment;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jkjk.quicknote.MyApplication;
import com.jkjk.quicknote.NoteListAdapter;
import com.jkjk.quicknote.R;

import static com.jkjk.quicknote.DatabaseHelper.DATABASE_NAME;
import static com.jkjk.quicknote.NoteListAdapter.inActionMode;
import static com.jkjk.quicknote.NoteListAdapter.mActionMode;


/**
 * A simple {@link Fragment} subclass.
 */
public class NoteListFragment extends Fragment {

    public final static String ACTION_UPDATE_NOTE = "actionUpdateNote";
    public final static String EXTRA_NOTE_ID = "extraNoteId";
    RecyclerView recyclerView;
    NoteListAdapter noteListAdapter;
    FloatingActionButton addNote;

    public NoteListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        noteListAdapter = new NoteListAdapter(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        addNote = (FloatingActionButton)getActivity().findViewById(R.id.add_note);
        addNote.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_white));
        addNote.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ff33b5e5")));
        addNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inActionMode){
                    new AlertDialog.Builder(view.getContext()).setTitle(R.string.delete_title).setMessage(R.string.confirm_delete)
                            .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //TODO delete note  selectedItems
                                    int[] mSelect = noteListAdapter.getSelected();
                                    Cursor tempNote = MyApplication.getDatabase().query(DATABASE_NAME, new String[]{"_id", "time"}, null, null, null
                                            , null, "time DESC");
                                    for (int removedPosition : mSelect) {
                                        tempNote.moveToPosition(removedPosition);
                                        String removedId = tempNote.getString(0);
                                        MyApplication.getDatabase().delete(DATABASE_NAME, "_id='" + removedId+"'",null);
                                        NoteListAdapter.updateCursor();
                                        mActionMode.finish();
                                        noteListAdapter.notifyItemRemoved(removedPosition);
                                    }
                                    tempNote.close();
                                }
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .show();
                }else {
                    onNoteEdit();
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_note_list, container, false);
        recyclerView = (RecyclerView)view.findViewById(R.id.recycler_view);
        recyclerView.setAdapter(noteListAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //call its fragment notifyitemchange method to update list
        if (data!=null) {
                noteListAdapter.notifyDataSetChanged();
            }
    }

    public void onNoteEdit(long noteId) {
        Intent startNoteActivity = new Intent();
        startNoteActivity.setClassName("com.jkjk.quicknote", "com.jkjk.quicknote.Note");
        startNoteActivity.putExtra(EXTRA_NOTE_ID, noteId);
        startActivityForResult(startNoteActivity, 1);
    }


    public void onNoteEdit() {
        Intent startNoteActivity = new Intent();
        startNoteActivity.setClassName("com.jkjk.quicknote", "com.jkjk.quicknote.Note");
        startActivityForResult(startNoteActivity, 1);
    }


}
