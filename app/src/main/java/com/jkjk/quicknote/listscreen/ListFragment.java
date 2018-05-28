package com.jkjk.quicknote.listscreen;


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
import android.widget.Toast;

import com.jkjk.quicknote.MyApplication;
import com.jkjk.quicknote.R;
import com.jkjk.quicknote.editscreen.EditFragment;
import com.jkjk.quicknote.helper.SearchHelper;

import java.util.ArrayList;

import static com.jkjk.quicknote.helper.DatabaseHelper.DATABASE_NAME;


/**
 * A simple {@link Fragment} subclass.
 */
public class ListFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView notFoundTextView;
    ListAdapter listAdapter;
    private boolean showingStarred = false;
    private MenuItem showStarred, search, settings;


    public ListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        listAdapter = new ListAdapter(this);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        android.support.v7.widget.Toolbar listMenu;
        if (!listAdapter.inActionMode) {
            listMenu = getActivity().findViewById(R.id.list_menu);
            ((AppCompatActivity) getActivity()).setSupportActionBar(listMenu);
        }

        FloatingActionButton addNote =  getActivity().findViewById(R.id.add_note);
        addNote.setImageDrawable(getResources().getDrawable(R.drawable.sharp_add_24));
        addNote.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ff33b5e5")));
        addNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listAdapter.inActionMode){
                    new AlertDialog.Builder(view.getContext()).setTitle(R.string.delete_title).setMessage(R.string.confirm_delete_list)
                            .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // delete note from  selectedItems
                                    ArrayList<Integer> mSelect = listAdapter.getSelected();
                                    Cursor tempNote = listAdapter.getCursor();
                                    for (int removedPosition : mSelect) {
                                        tempNote.moveToPosition(removedPosition);
                                        String removedId = tempNote.getString(0);
                                        MyApplication.database.delete(DATABASE_NAME, "_id='" + removedId+"'",null);
                                        listAdapter.notifyItemRemoved(removedPosition);
                                    }
                                    Toast.makeText(getContext(),R.string.deleted_toast,Toast.LENGTH_SHORT).show();
                                    listAdapter.updateCursor();
                                    listAdapter.mActionMode.finish();
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
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        notFoundTextView = view.findViewById(R.id.result_not_found);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setAdapter(listAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return view;
    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.list_menu, menu);

        showStarred =  menu.findItem(R.id.show_starred);
        search =  menu.findItem(R.id.search);
        settings =  menu.findItem(R.id.settings);

        settings.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Intent intent = new Intent();
                intent.setClassName("com.jkjk.quicknote", "com.jkjk.quicknote.settings.Settings");
                startActivity(intent);
//                getActivity().finish();
                return true;
            }
        });

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
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
                    listAdapter.updateCursorForSearch(result);
                    listAdapter.notifyDataSetChanged();
                } else if (!newText.equals("")) {
                    //If search result is empty and the search input is not empty, show result not found
                    recyclerView.setVisibility(View.INVISIBLE);
                    notFoundTextView.setVisibility(View.VISIBLE);
                } else {
                    //after finish searching and user empty the search input, reset the view
                    recyclerView.setVisibility(View.VISIBLE);
                    notFoundTextView.setVisibility(View.INVISIBLE);
                    listAdapter.updateCursor();
                    listAdapter.notifyDataSetChanged();
                }
                return true;
            }
        });

        search.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                showStarred.setVisible(false);
                settings.setVisible(false);

                showingStarred = false;
                showStarred.setIcon(R.drawable.sharp_star_border_24);
                showStarred.setTitle(getResources().getString(R.string.show_starred));
                listAdapter.updateCursor();
                listAdapter.notifyDataSetChanged();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                showStarred.setVisible(true);
                settings.setVisible(true);
                search.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItem.SHOW_AS_ACTION_ALWAYS);
                showStarred.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                return true;
            }
        });

        showStarred.setIcon(R.drawable.sharp_star_border_24);
        showStarred.setTitle(getResources().getString(R.string.show_starred));
        showStarred.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (!showingStarred) {
                    // to show only starred notes
                    showingStarred = true;
                    showStarred.setIcon(R.drawable.baseline_star_24);
                    showStarred.setTitle(getResources().getString(R.string.show_all));

                    listAdapter.updateCursorForStarred();
                    listAdapter.notifyDataSetChanged();
                } else {
                    // to show all notes
                    showingStarred = false;
                    showStarred.setIcon(R.drawable.sharp_star_border_24);
                    showStarred.setTitle(getResources().getString(R.string.show_starred));
                    listAdapter.updateCursor();
                    listAdapter.notifyDataSetChanged();
                }
                return true;
            }
        });

    }


    @Override
    public void onResume() {
        listAdapter.updateCursor();
        listAdapter.notifyDataSetChanged();
        super.onResume();
    }

    @Override
    public void onStop() {
        if (listAdapter.mActionMode!=null) {
            listAdapter.mActionMode.finish();
        }

        // Clear all filter
        search.collapseActionView();
        showingStarred = false;
        showStarred.setIcon(R.drawable.sharp_star_border_24);
        showStarred.setTitle(getResources().getString(R.string.show_starred));
        super.onStop();
    }

    public void onNoteEdit(long noteId) {
        Intent startNoteActivity = new Intent();
        startNoteActivity.setClassName("com.jkjk.quicknote", "com.jkjk.quicknote.editscreen.Edit");
        startNoteActivity.putExtra(EditFragment.EXTRA_NOTE_ID, noteId);
        startActivity(startNoteActivity);
    }


    public void onNoteEdit() {
        Intent startNoteActivity = new Intent();
        startNoteActivity.setClassName("com.jkjk.quicknote", "com.jkjk.quicknote.editscreen.Edit");
        startActivity(startNoteActivity);
    }

}
