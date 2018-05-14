package com.jkjk.quicknote.Fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jkjk.quicknote.Interface.OnNoteEdit;
import com.jkjk.quicknote.NoteListAdapter;
import com.jkjk.quicknote.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class NoteListFragment extends Fragment {

RecyclerView recyclerView;
NoteListAdapter noteListAdapter;
FloatingActionButton addNote;

    public NoteListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        noteListAdapter = new NoteListAdapter();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        addNote = (FloatingActionButton)getActivity().findViewById(R.id.add_note);
        addNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((OnNoteEdit) getActivity()).onNoteEdit();
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

    public void adapterItemChanged(int position){
        noteListAdapter.notifyItemChanged(position);
    }

    public void adapterItemInserted(int position){
        noteListAdapter.notifyItemInserted(position);
    }

}
