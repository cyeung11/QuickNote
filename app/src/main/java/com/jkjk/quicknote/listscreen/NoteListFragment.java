package com.jkjk.quicknote.listscreen;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.jkjk.quicknote.R;
import com.jkjk.quicknote.noteeditscreen.NoteEdit;
import com.jkjk.quicknote.noteeditscreen.NoteEditFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class NoteListFragment extends Fragment {

    NoteListAdapter noteListAdapter;
    TextView notFoundTextView;

    RecyclerView recyclerView;

    public NoteListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Crashlytics.log(getClass().getName());
        super.onCreate(savedInstanceState);
        noteListAdapter = new NoteListAdapter(this);
        noteListAdapter.updateCursor();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (noteListAdapter.actionMode !=null) {
            noteListAdapter.actionMode.finish();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_note_list, container, false);
        notFoundTextView = view.findViewById(R.id.result_not_found);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setAdapter(noteListAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        ListFragment hostFragment = (ListFragment)getParentFragment();

        if(hostFragment != null) {
            hostFragment.updateNoteListFragment(getTag());
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        noteListAdapter.updateCursor();
        noteListAdapter.notifyDataSetChanged();
    }


    public void onNoteEdit(long noteId) {
        Intent startNoteActivity = new Intent(getContext(), NoteEdit.class);
        startNoteActivity.putExtra(NoteEditFragment.EXTRA_ITEM_ID, noteId);
        startActivity(startNoteActivity);
    }

    public void onNoteEdit() {
        Intent startNoteActivity = new Intent(getContext(), NoteEdit.class);
        startActivity(startNoteActivity);
    }

    public NoteListAdapter getNoteListAdapter(){
        return noteListAdapter;
    }

}
