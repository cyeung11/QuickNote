package com.jkjk.quicknote.listscreen;


import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.google.android.gms.ads.MobileAds;


import com.jkjk.quicknote.MyApplication;
import com.jkjk.quicknote.R;
import com.jkjk.quicknote.helper.SearchHelper;
import com.jkjk.quicknote.settings.Settings;

import java.util.ArrayList;

import static com.jkjk.quicknote.helper.DatabaseHelper.DATABASE_NAME;



public class ListFragment extends Fragment{

    /*TODO change when release
    debug id: ca-app-pub-3940256099942544/5224354917
    real id ca-app-pub-8833570917041672/9209453063
     */
    public static final String REWARD_VIDEO_AD_ID = "ca-app-pub-3940256099942544/5224354917";
    public static final String ADMOB_ID = "ca-app-pub-8833570917041672~2236425579";
    public static boolean isAllowedToUse = false;
    // 0 stands for note , 1 stands for task
    private int defaultPage, currentPage;

    private MenuItem showStarred, search, settings;
    private NoteListFragment noteListFragment;
    private TaskListFragment taskListFragment;


    public ListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MobileAds.initialize(getActivity(),ADMOB_ID);

        android.support.v7.widget.Toolbar listMenu = getActivity().findViewById(R.id.list_menu);
        ((AppCompatActivity) getActivity()).setSupportActionBar(listMenu);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        defaultPage =  Integer.valueOf(sharedPref.getString(getString(R.string.default_screen), "0"));
        if (defaultPage==0) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.note);
        } else ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.task);

        ListPageAdapter listPageAdapter = new ListPageAdapter(getContext(), getChildFragmentManager());
        ViewPager viewPager = getActivity().findViewById(R.id.pager);
        viewPager.setAdapter(listPageAdapter);


        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                // if default page is note, position 0 will be note, so current page is 0
                if (defaultPage==0) {
                    currentPage = position;
                    if (position==0) {
                        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.note);
                    } else ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.task);
                    // if default page is task, position 0 will be task, so current page is 1
                } else if (position==0){
                    currentPage = 1;
                    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.task);
                } else if (position==1){
                    currentPage = 0;
                    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.note);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        FloatingActionButton addNote =  getActivity().findViewById(R.id.add_note);
        addNote.setImageDrawable(getResources().getDrawable(R.drawable.pencil));
        addNote.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.highlight)));
        addNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // button served as delete function
                if (noteListFragment.getNoteListAdapter().inActionMode){
                    new AlertDialog.Builder(view.getContext()).setTitle(R.string.delete_title).setMessage(R.string.confirm_delete_list)
                            .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    NoteListAdapter noteListAdapter = noteListFragment.getNoteListAdapter();

                                    // delete note from  selectedItems
                                    ArrayList<Integer> mSelect = noteListAdapter.getSelected();
                                    Cursor tempNote = noteListAdapter.getNoteCursor();
                                    for (int removedPosition : mSelect) {
                                        tempNote.moveToPosition(removedPosition);
                                        String removedId = tempNote.getString(0);
                                        MyApplication.database.delete(DATABASE_NAME, "_id='" + removedId+"'",null);
                                        noteListAdapter.notifyItemRemoved(removedPosition);
                                    }
                                    Toast.makeText(getContext(),R.string.deleted_toast,Toast.LENGTH_SHORT).show();

                                    noteListAdapter.updateCursor();
                                    noteListAdapter.mActionMode.finish();
                                    tempNote.close();
                                }
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .show();
                }else if (currentPage==0) {
                    noteListFragment.onNoteEdit();
                }else if (currentPage==1){
                    taskListFragment.onTaskEdit();
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false);
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
                Intent intent = new Intent(getContext(), Settings.class);
                startActivity(intent);
                getActivity().finish();
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
                NoteListAdapter noteListAdapter = noteListFragment.getNoteListAdapter();
                TaskListAdapter taskListAdapter = taskListFragment.getTaskListAdapter();

                //If search result is not empty, update the cursor to show result
                if (!result.equals("")) {
                    noteListAdapter.updateCursorForSearch(result);
                    taskListAdapter.updateCursorForSearch(result);
                    noteListFragment.recyclerView.setVisibility(View.VISIBLE);
                    noteListFragment.notFoundTextView.setVisibility(View.INVISIBLE);
                    taskListFragment.recyclerView.setVisibility(View.VISIBLE);
                    taskListFragment.notFoundTextView.setVisibility(View.INVISIBLE);
                    noteListAdapter.notifyDataSetChanged();
                    taskListAdapter.notifyDataSetChanged();

                } else if (!newText.equals("")) {
                    //If search result is empty and the search input is not empty, show result not found
                    noteListFragment.recyclerView.setVisibility(View.INVISIBLE);
                    noteListFragment.notFoundTextView.setVisibility(View.VISIBLE);
                    taskListFragment.recyclerView.setVisibility(View.INVISIBLE);
                    taskListFragment.notFoundTextView.setVisibility(View.VISIBLE);

                } else {
                    //after finish searching and user empty the search input, reset the view
                    noteListAdapter.updateCursor();
                    taskListAdapter.updateCursor();
                    noteListFragment.recyclerView.setVisibility(View.VISIBLE);
                    noteListFragment.notFoundTextView.setVisibility(View.INVISIBLE);
                    taskListFragment.recyclerView.setVisibility(View.VISIBLE);
                    taskListFragment.notFoundTextView.setVisibility(View.INVISIBLE);
                    noteListAdapter.notifyDataSetChanged();
                    taskListAdapter.notifyDataSetChanged();
                }
                return true;
            }
        });

        search.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {

            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                NoteListAdapter noteListAdapter = noteListFragment.getNoteListAdapter();

                showStarred.setVisible(false);
                settings.setVisible(false);

                noteListFragment.showingStarred = false;
                showStarred.setIcon(R.drawable.sharp_star_border_24);
                showStarred.setTitle(getResources().getString(R.string.show_starred));
                noteListAdapter.updateCursor();
                noteListAdapter.notifyDataSetChanged();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                showStarred.setVisible(true);
                settings.setVisible(true);
                return true;
            }
        });

        showStarred.setIcon(R.drawable.sharp_star_border_24);
        showStarred.setTitle(getResources().getString(R.string.show_starred));
        showStarred.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                NoteListAdapter noteListAdapter = noteListFragment.getNoteListAdapter();

                if (!noteListFragment.showingStarred) {
                    // to show only starred notes
                    noteListFragment.showingStarred = true;
                    showStarred.setIcon(R.drawable.baseline_star_24);
                    showStarred.setTitle(getResources().getString(R.string.show_all));

                    noteListAdapter.updateCursorForStarred();
                    noteListAdapter.notifyDataSetChanged();
                } else {
                    // to show all notes
                    noteListFragment.showingStarred = false;
                    showStarred.setIcon(R.drawable.sharp_star_border_24);
                    showStarred.setTitle(getResources().getString(R.string.show_starred));

                    noteListAdapter.updateCursor();
                    noteListAdapter.notifyDataSetChanged();
                }
                return true;
            }
        });

    }




    @Override
    public void onStop() {
        // Clear all filter
        search.collapseActionView();
        showStarred.setIcon(R.drawable.sharp_star_border_24);
        showStarred.setTitle(getResources().getString(R.string.show_starred));
        super.onStop();
    }


    void updateNoteListFragment(String tag) {
        noteListFragment = (NoteListFragment)getChildFragmentManager().findFragmentByTag(tag);
    }

    void updateTaskListFragment(String tag) {
        taskListFragment = (TaskListFragment) getChildFragmentManager().findFragmentByTag(tag);
    }
}
