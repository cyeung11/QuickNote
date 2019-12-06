package com.jkjk.quicknote.listscreen;


import com.jkjk.quicknote.R;
import com.jkjk.quicknote.noteeditscreen.NoteEdit;


public class NoteListFragment extends ItemListFragment {


    public NoteListFragment() {
        // Required empty public constructor
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_note_list;
    }

    @Override
    protected Class getEditActivityClass() {
        return NoteEdit.class;
    }

    @Override
    protected ItemListAdapter initAdapter() {
        return new NoteListAdapter(getContext(), this);
    }


    @Override
    protected void resetList() {
        if (((NoteListAdapter)itemListAdapter).showingStarred) {
            ((NoteListAdapter)itemListAdapter).updateCursorForStarred();
        } else {
            itemListAdapter.updateCursor();
        }
    }
}
