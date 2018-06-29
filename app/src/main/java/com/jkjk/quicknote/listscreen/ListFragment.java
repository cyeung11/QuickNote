package com.jkjk.quicknote.listscreen;


import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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

import com.jkjk.quicknote.MyApplication;
import com.jkjk.quicknote.R;
import com.jkjk.quicknote.helper.SearchHelper;
import com.jkjk.quicknote.settings.Settings;

import java.util.ArrayList;

import static com.jkjk.quicknote.helper.DatabaseHelper.DATABASE_NAME;
import static com.jkjk.quicknote.widget.NoteListWidget.updateNoteListWidget;
import static com.jkjk.quicknote.widget.TaskListWidget.updateTaskListWidget;


public class ListFragment extends Fragment{

    /*TODO change when release
    debug id: ca-app-pub-3940256099942544/5224354917
    real id ca-app-pub-8833570917041672/9209453063
     */
//    public static final String REWARD_VIDEO_AD_ID = "ca-app-pub-8833570917041672/9209453063";
//    public static final String ADMOB_ID = "ca-app-pub-8833570917041672~2236425579";
    // 0 stands for note , 1 stands for task
    boolean defaultPageIsTask;
    private char currentPage;
    private boolean showingStarred = false, sortingBytime, byUrgencyByDefault;

    private MenuItem showStarred, search, settings, sortBy, showDone, switchTab;
    private NoteListFragment noteListFragment;
    private TaskListFragment taskListFragment;
    private ViewPager viewPager;


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
//        MobileAds.initialize(getActivity(),ADMOB_ID);

        android.support.v7.widget.Toolbar listMenu = getActivity().findViewById(R.id.list_menu);
        ((AppCompatActivity) getActivity()).setSupportActionBar(listMenu);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        defaultPageIsTask = sharedPref.getBoolean(getString(R.string.default_screen), false);
        byUrgencyByDefault = sharedPref.getBoolean(getString(R.string.change_default_sorting), false);
        sortingBytime = !byUrgencyByDefault;

        if (defaultPageIsTask) {
            currentPage = 'T';
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.task);
        } else {
            currentPage = 'N';
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.note);
        }

        ListPageAdapter listPageAdapter = new ListPageAdapter(getContext(), getChildFragmentManager());
        viewPager = getActivity().findViewById(R.id.pager);
        viewPager.setAdapter(listPageAdapter);


        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                NoteListAdapter noteListAdapter = noteListFragment.getNoteListAdapter();
                TaskListAdapter taskListAdapter = taskListFragment.getTaskListAdapter();
                if (noteListAdapter.actionMode !=null) {
                    noteListAdapter.actionMode.finish();
                }
                if (taskListAdapter.actionMode !=null) {
                    taskListAdapter.actionMode.finish();
                }

                // if default page is note, position 0 will be note, so current page is 0
                // if default page is task, position 0 will be task, so current page is 1
                if (position==0) {
                    currentPage = defaultPageIsTask ?'T' :'N';
                } else if (position==1){
                    currentPage = defaultPageIsTask ?'N' :'T';
                }
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(currentPage == 'N' ?R.string.note :R.string.task);
                setMenuItemForPage(currentPage == 'N');
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        FloatingActionButton addNote =  getActivity().findViewById(R.id.add_note);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            addNote.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.pencil));
        } else {
            addNote.setImageResource(R.drawable.pencil);
        }
        addNote.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.highlight)));
        addNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // button served as delete function
                if (noteListFragment.getNoteListAdapter().isInActionMode || taskListFragment.getTaskListAdapter().isInActionMode){
                    new AlertDialog.Builder(view.getContext()).setTitle(R.string.delete_title).setMessage(R.string.confirm_delete_list)
                            .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    if (currentPage == 'N') {
                                        NoteListAdapter noteListAdapter = noteListFragment.getNoteListAdapter();

                                        // delete note from  selectedItems
                                        ArrayList<Integer> mSelect = noteListAdapter.getSelected();
                                        Cursor noteCursor = noteListAdapter.getItemCursor();
                                        for (int removedPosition : mSelect) {
                                            noteCursor.moveToPosition(removedPosition);
                                            String removedId = noteCursor.getString(0);
                                            MyApplication.database.delete(DATABASE_NAME, "_id='" + removedId + "'", null);
                                        }

                                        noteListAdapter.updateCursor();
                                        for (int removedPosition : mSelect) {
                                            noteListAdapter.notifyItemRemoved(removedPosition);
                                        }

                                        Toast.makeText(getContext(), R.string.note_deleted_toast, Toast.LENGTH_SHORT).show();
                                        noteListAdapter.actionMode.finish();
                                        updateNoteListWidget(getContext());
                                        noteCursor.close();

                                    } else if (currentPage == 'T'){
                                        TaskListAdapter taskListAdapter = taskListFragment.getTaskListAdapter();

                                        // delete task from  selectedItems
                                        ArrayList<Integer> mSelect = taskListAdapter.getSelected();
                                        Cursor taskCursor = taskListAdapter.getItemCursor();
                                        for (int removedPosition : mSelect) {
                                            taskCursor.moveToPosition(removedPosition);
                                            String removedId = taskCursor.getString(0);
                                            MyApplication.database.delete(DATABASE_NAME, "_id='" + removedId + "'", null);
                                        }

                                        taskListAdapter.updateCursor();
                                        for (int removedPosition : mSelect) {
                                            taskListAdapter.notifyItemRemoved(removedPosition);
                                        }

                                        Toast.makeText(getContext(), R.string.task_deleted_toast, Toast.LENGTH_SHORT).show();
                                        taskListAdapter.actionMode.finish();
                                        updateTaskListWidget(getContext());
                                        taskCursor.close();
                                    }
                                }
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .show();

                }else if (currentPage=='N') {
                    noteListFragment.onNoteEdit();
                }else if (currentPage=='T'){
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
        sortBy = menu.findItem(R.id.sort_by);
        showDone = menu.findItem(R.id.show_done);
        switchTab = menu.findItem(R.id.switch_tab);

        setMenuItemForPage(currentPage=='N');

        settings.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Intent intent = new Intent(getContext(), Settings.class);
                startActivity(intent);
                getActivity().finish();
                return true;
            }
        });

        final SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
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
                    toggleNotResultView(false);
                    noteListAdapter.notifyDataSetChanged();
                    taskListAdapter.notifyDataSetChanged();

                } else if (!newText.equals("")) {
                    //If search result is empty and the search input is not empty, show result not found
                    toggleNotResultView(true);

                } else {
                    //after finish searching and user empty the search input, reset the view
                    noteListAdapter.updateCursor();
                    taskListAdapter.updateCursor();
                    toggleNotResultView(false);
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
                TaskListAdapter taskListAdapter = taskListFragment.getTaskListAdapter();

                showStarred.setVisible(false);
                settings.setVisible(false);
                sortBy.setVisible(false);
                showDone.setVisible(false);
                switchTab.setVisible(false);

                showingStarred = false;
                taskListAdapter.showingDone = false;

                showStarred.setTitle(R.string.show_starred);
                sortBy.setTitle(R.string.sort_by_urgency);
                showDone.setTitle(R.string.show_done);

                taskListAdapter.updateCursor();
                taskListAdapter.notifyDataSetChanged();
                noteListAdapter.updateCursor();
                noteListAdapter.notifyDataSetChanged();

                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                settings.setVisible(true);
                switchTab.setVisible(true);

                if (currentPage == 'N') {
                    showStarred.setVisible(true);
                } else if (currentPage == 'T'){
                    sortBy.setVisible(true);
                    showDone.setVisible(true);
                }

                return true;
            }
        });

//        showStarred.setIcon(R.drawable.sharp_star_border_24);
        showStarred.setTitle(R.string.show_starred);
        showStarred.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                NoteListAdapter noteListAdapter = noteListFragment.getNoteListAdapter();

                if (!showingStarred) {
                    // to show only starred notes
                    showingStarred = true;
                    showStarred.setTitle(R.string.show_all_note);

                    noteListAdapter.updateCursorForStarred();
                    noteListAdapter.notifyDataSetChanged();
                } else {
                    // to show all notes
                    showingStarred = false;
                    showStarred.setTitle(R.string.show_starred);

                    noteListAdapter.updateCursor();
                    noteListAdapter.notifyDataSetChanged();
                }
                return true;
            }
        });

        showDone.setTitle(R.string.show_done);
        showDone.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                if (byUrgencyByDefault) {
                    sortBy.setTitle(R.string.sort_by_time);
                    sortingBytime = false;
                } else {
                    sortBy.setTitle(R.string.sort_by_urgency);
                    sortingBytime = true;
                }

                TaskListAdapter taskListAdapter = taskListFragment.getTaskListAdapter();

                if(!taskListAdapter.showingDone){
                    taskListAdapter.showingDone = true;
                    showDone.setTitle(R.string.show_all_task);

                    taskListAdapter.updateCursorForDone();
                    taskListAdapter.notifyDataSetChanged();
                } else {
                    taskListAdapter.showingDone = false;
                    showDone.setTitle(R.string.show_done);

                    taskListAdapter.updateCursor();
                    taskListAdapter.notifyDataSetChanged();
                }
                return true;
            }
        });



        if (byUrgencyByDefault) {
            sortBy.setTitle(R.string.sort_by_time);
        } else sortBy.setTitle(R.string.sort_by_urgency);

        sortBy.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                TaskListAdapter taskListAdapter = taskListFragment.getTaskListAdapter();
                taskListAdapter.showingDone = false;
                showDone.setTitle(R.string.show_done);

                if (sortingBytime){
                    sortingBytime = false;
                    sortBy.setTitle(R.string.sort_by_time);
                    taskListAdapter.updateCursorByUrgency();
                } else {
                    sortingBytime = true;
                    sortBy.setTitle(R.string.sort_by_urgency);
                    taskListAdapter.updateCursorByTime();
                }
                taskListAdapter.notifyDataSetChanged();
                return true;
            }
        });

        switchTab.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                if (currentPage == 'N'){
                    viewPager.setCurrentItem(defaultPageIsTask ?0 :1, true);

                } else if (currentPage == 'T'){
                    if (!defaultPageIsTask) {
                        viewPager.setCurrentItem(0, true);
                    } else {
                        viewPager.setCurrentItem(1, true);
                    }
                }
                return true;
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        setMenuItemForPage(currentPage=='N');
    }

    @Override
    public void onStop() {
        // Clear all filter
        search.collapseActionView();
        showStarred.setTitle(R.string.show_starred);
        showingStarred = false;
        showDone.setTitle(R.string.show_done);
        taskListFragment.getTaskListAdapter().showingDone = false;
        if (byUrgencyByDefault) {
            sortBy.setTitle(R.string.sort_by_time);
            sortingBytime = false;
        } else {
            sortBy.setTitle(R.string.sort_by_urgency);
            sortingBytime = true;
        }
        super.onStop();
    }

    private void toggleNotResultView (boolean on){
        noteListFragment.recyclerView.setVisibility(on ?View.INVISIBLE :View.VISIBLE);
        noteListFragment.notFoundTextView.setVisibility(on ?View.VISIBLE :View.INVISIBLE);
        taskListFragment.recyclerView.setVisibility(on ?View.INVISIBLE :View.VISIBLE);
        taskListFragment.notFoundTextView.setVisibility(on ?View.VISIBLE :View.INVISIBLE);
    }

    private void setMenuItemForPage (boolean currentPageIsNote){
        if (showStarred!=null && showDone!=null && sortBy!=null && switchTab!=null) {
            showStarred.setVisible(currentPageIsNote);
            showDone.setVisible(!currentPageIsNote);
            sortBy.setVisible(!currentPageIsNote);
            switchTab.setTitle(currentPageIsNote ?R.string.switch_task :R.string.switch_note);
        }
    }

    void updateNoteListFragment(String tag) {
        noteListFragment = (NoteListFragment)getChildFragmentManager().findFragmentByTag(tag);
    }

    void updateTaskListFragment(String tag) {
        taskListFragment = (TaskListFragment) getChildFragmentManager().findFragmentByTag(tag);
    }
}
