package com.jkjk.quicknote.Fragment;


import android.app.SearchManager;
import android.content.Context;
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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jkjk.quicknote.Adapter.NoteListAdapter;
import com.jkjk.quicknote.MyApplication;
import com.jkjk.quicknote.R;
import com.jkjk.quicknote.SearchHelper;

import java.util.ArrayList;

import static com.jkjk.quicknote.Adapter.NoteListAdapter.getCursor;
import static com.jkjk.quicknote.Adapter.NoteListAdapter.inActionMode;
import static com.jkjk.quicknote.Adapter.NoteListAdapter.mActionMode;
import static com.jkjk.quicknote.Adapter.NoteListAdapter.updateCursor;
import static com.jkjk.quicknote.Adapter.NoteListAdapter.updateCursorForStarred;
import static com.jkjk.quicknote.DatabaseHelper.DATABASE_NAME;


/**
 * A simple {@link Fragment} subclass.
 */
public class NoteListFragment extends Fragment {

    RecyclerView recyclerView;
    TextView notFoundTextView;
    SearchView searchView;
    NoteListAdapter noteListAdapter;
    FloatingActionButton addNote;
    android.support.v7.widget.Toolbar noteListMenu;
    public static boolean showingStarred = false;
    public static boolean inSearchView = false;
    static MenuItem showStarred;
    MenuItem search;

    public NoteListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        noteListAdapter = new NoteListAdapter(this);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!inActionMode) {
            noteListMenu = (android.support.v7.widget.Toolbar) getActivity().findViewById(R.id.note_list_menu);
            ((AppCompatActivity) getActivity()).setSupportActionBar(noteListMenu);
        }

        addNote = (FloatingActionButton)getActivity().findViewById(R.id.add_note);
        addNote.setImageDrawable(getResources().getDrawable(R.drawable.sharp_add_24));
        addNote.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ff33b5e5")));
        addNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inActionMode){
                    new AlertDialog.Builder(view.getContext()).setTitle(R.string.delete_title).setMessage(R.string.confirm_delete_list)
                            .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // delete note from  selectedItems
                                    ArrayList<Integer> mSelect = noteListAdapter.getSelected();
                                    Cursor tempNote = getCursor();
                                    for (int removedPosition : mSelect) {
                                        tempNote.moveToPosition(removedPosition);
                                        String removedId = tempNote.getString(0);
                                        MyApplication.database.delete(DATABASE_NAME, "_id='" + removedId+"'",null);
                                        noteListAdapter.notifyItemRemoved(removedPosition);
                                    }
                                    NoteListAdapter.updateCursor();
                                    mActionMode.finish();
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
        notFoundTextView = (TextView)view.findViewById(R.id.result_not_found);
        recyclerView = (RecyclerView)view.findViewById(R.id.recycler_view);
        recyclerView.setAdapter(noteListAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        NoteListAdapter.updateCursor();
        return view;
    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.note_list_menu, menu);

        showStarred = (MenuItem) menu.findItem(R.id.show_starred);
        search = (MenuItem) menu.findItem(R.id.search);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView)menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setQueryHint(getResources().getString(R.string.search_hint));
        searchView.setSubmitButtonEnabled(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            SearchHelper searchHelper = new SearchHelper();
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                String result = searchHelper.searchResult(newText);

                //If search result is not empty, update the cursor to show result
                if (!result.equals("")) {
                    recyclerView.setVisibility(View.VISIBLE);
                    notFoundTextView.setVisibility(View.INVISIBLE);
                    NoteListAdapter.updateCursorForSearch(result);
                    noteListAdapter.notifyDataSetChanged();
                }else if (!newText.equals("")){
                    //If search result is empty and the search input is not empty, show result not found
                    recyclerView.setVisibility(View.INVISIBLE);
                    notFoundTextView.setVisibility(View.VISIBLE);
                } else {
                    //after finish searching and user empty the search input, reset the view
                    recyclerView.setVisibility(View.VISIBLE);
                    notFoundTextView.setVisibility(View.INVISIBLE);
                    updateCursor();
                    noteListAdapter.notifyDataSetChanged();
                }
                return true;
            }
        });

        search.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                inSearchView = true;
                showStarred.setVisible(false);
                showingStarred = false;
                showStarred.setIcon(R.drawable.sharp_star_border_24);
                updateCursor();
                noteListAdapter.notifyDataSetChanged();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                inSearchView = false;
                showStarred.setVisible(true);
                return true;
            }
        });

        showStarred.setIcon(R.drawable.sharp_star_border_24);
        showStarred.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (!showingStarred){
                    // to show only starred notes
                    showingStarred = true;
                    showStarred.setIcon(R.drawable.baseline_star_24);
                    updateCursorForStarred();
                    noteListAdapter.notifyDataSetChanged();
                } else {
                    // to show all notes
                    showingStarred = false;
                    showStarred.setIcon(R.drawable.sharp_star_border_24);
                    updateCursor();
                    noteListAdapter.notifyDataSetChanged();
                }
                return true;
            }
        });
       }


    @Override
    public void onResume() {
        super.onResume();
        //Reset the recyclerview
        updateCursor();
        noteListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        if (mActionMode!=null) {
            mActionMode.finish();
        }

        // Clear all filter
        search.collapseActionView();
        showingStarred = false;
        showStarred.setIcon(R.drawable.sharp_star_border_24);
        super.onStop();
    }

    public void onNoteEdit(long noteId) {
        Intent startNoteActivity = new Intent();
        startNoteActivity.setClassName("com.jkjk.quicknote", "com.jkjk.quicknote.Note");
        startNoteActivity.putExtra(NoteEditFragment.EXTRA_NOTE_ID, noteId);
        startActivity(startNoteActivity);
    }


    public void onNoteEdit() {
        Intent startNoteActivity = new Intent();
        startNoteActivity.setClassName("com.jkjk.quicknote", "com.jkjk.quicknote.Note");
        startActivity(startNoteActivity);
    }


}
