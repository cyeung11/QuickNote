package com.jkjk.quicknote.listscreen;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jkjk.quicknote.MyApplication;
import com.jkjk.quicknote.R;
import com.jkjk.quicknote.helper.NotificationHelper;
import com.jkjk.quicknote.helper.SearchHelper;
import com.jkjk.quicknote.settings.Settings;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;
import static com.jkjk.quicknote.MyApplication.PINNED_NOTIFICATION_IDS;
import static com.jkjk.quicknote.helper.AlarmHelper.ITEM_TYPE;
import static com.jkjk.quicknote.helper.DatabaseHelper.DATABASE_NAME;
import static com.jkjk.quicknote.helper.NotificationHelper.ACTION_TOOL_BAR;
import static com.jkjk.quicknote.helper.NotificationHelper.PIN_ITEM_NOTIFICATION_ID;
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
    private boolean showingStarred = false, sortingBytime, byUrgencyByDefault, isNotificationToolbarEnable;

    private MenuItem showStarred, search, settings, sortBy, showDone, switchTab;
    private NoteListFragment noteListFragment;
    private TaskListFragment taskListFragment;
    private ViewPager viewPager;
    private NoteListAdapter noteListAdapter;
    private TaskListAdapter taskListAdapter;
    private SQLiteDatabase database;


    public ListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        database = ((MyApplication)getActivity().getApplication()).database;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Toolbar listMenu = getActivity().findViewById(R.id.list_menu);
        ((AppCompatActivity) getActivity()).setSupportActionBar(listMenu);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        isNotificationToolbarEnable = sharedPref.getBoolean(getString(R.string.notification_pin), false);
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
                if (noteListFragment != null) {
                    noteListAdapter = noteListFragment.getNoteListAdapter();
                    if (noteListAdapter.actionMode != null) {
                        noteListAdapter.actionMode.finish();
                    }
                }
                if (taskListFragment != null) {
                    taskListAdapter = taskListFragment.getTaskListAdapter();
                    if (taskListAdapter.actionMode != null) {
                        taskListAdapter.actionMode.finish();
                    }
                }

                // if default page is note, position 0 will be note, so current page is 0
                // if default page is task, position 0 will be task, so current page is 1
                if (position == 0) {
                    currentPage = defaultPageIsTask ?'T' :'N';
                } else if (position == 1){
                    currentPage = defaultPageIsTask ?'N' :'T';
                }
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(currentPage == 'N' ?R.string.note :R.string.task);
                setMenuItemForPage(currentPage == 'N');
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(ITEM_TYPE)){
            char itemPageToBeOpened = intent.getCharExtra(ITEM_TYPE, 'N');
            if (itemPageToBeOpened == 'N') {
                viewPager.setCurrentItem(defaultPageIsTask ?1 :0, true);
            } else {
                viewPager.setCurrentItem(defaultPageIsTask ?0 :1, true);
            }
        }

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

                final boolean currentPageIsNote = (currentPage == 'N');
                // button served as delete function
                if (noteListFragment.getNoteListAdapter().isInActionMode || taskListFragment.getTaskListAdapter().isInActionMode){
                    new AlertDialog.Builder(view.getContext()).setTitle(R.string.delete_title).setMessage(R.string.confirm_delete_list)
                            .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    ItemListAdapter itemListAdapter;
                                    if (currentPageIsNote) {
                                        itemListAdapter = noteListFragment.getNoteListAdapter();
                                    } else {
                                        itemListAdapter = taskListFragment.getTaskListAdapter();
                                    }

                                    // delete note from  selectedItems
                                    ArrayList<Integer> mSelect = itemListAdapter.getSelected();
                                    Cursor itemCursor = itemListAdapter.getItemCursor();
                                    SharedPreferences idPref = getContext().getSharedPreferences(PINNED_NOTIFICATION_IDS, MODE_PRIVATE);
                                    boolean isItemToday = false;

                                    for (int removedPosition : mSelect) {
                                        itemCursor.moveToPosition(removedPosition);
                                        String removedId = itemCursor.getString(0);
                                        database.delete(DATABASE_NAME, "_id='" + removedId + "'", null);

                                        // To check if the deleted item is due today. If so (which will only check if other deleted items are not today) and if notification toolbar is on, update the notification below
                                        if (!currentPageIsNote && isNotificationToolbarEnable && !isItemToday && DateUtils.isToday(itemCursor.getLong(3))){
                                            isItemToday = true;
                                        }

                                        if (idPref.getLong(removedId, 999999L)!=999999L) {
                                            idPref.edit().remove(removedId).apply();
                                            NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                                            if (notificationManager != null) {
                                                notificationManager.cancel(Integer.valueOf(removedId)*PIN_ITEM_NOTIFICATION_ID);
                                            }
                                        }
                                    }

                                    itemListAdapter.updateCursor();
                                    for (int removedPosition : mSelect) {
                                        itemListAdapter.notifyItemRemoved(removedPosition);
                                    }

                                    if (isItemToday){
                                        Intent toolBarIntent = new Intent(getContext(), NotificationHelper.class);
                                        toolBarIntent.setAction(ACTION_TOOL_BAR);
                                        PendingIntent toolbarPendingIntent = PendingIntent.getBroadcast(getContext(), 0, toolBarIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                                        try {
                                            toolbarPendingIntent.send();
                                        } catch (PendingIntent.CanceledException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    Toast.makeText(getContext(), currentPageIsNote ?R.string.note_deleted_toast :R.string.task_deleted_toast, Toast.LENGTH_SHORT).show();
                                    itemListAdapter.actionMode.finish();
                                    if (currentPageIsNote) {
                                        updateNoteListWidget(getContext());
                                    } else {
                                        updateTaskListWidget(getContext());
                                    }
                                    itemCursor.close();
                                }
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .show();

                }else if (currentPageIsNote) {
                    noteListFragment.onNoteEdit();
                }else {
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
        searchView.setQueryHint(getResources().getString(R.string.search));
        searchView.setSubmitButtonEnabled(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            SearchHelper searchHelper = new SearchHelper();

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                String result = searchHelper.searchResult(database, newText);
                noteListAdapter = noteListFragment.getNoteListAdapter();
                taskListAdapter = taskListFragment.getTaskListAdapter();

                //If search result is not empty, update the cursor to show result
                if (!result.equals("")) {
                    noteListAdapter.updateCursorForSearch(result);
                    taskListAdapter.updateCursorForSearch(result);
                    noteListAdapter.notifyDataSetChanged();
                    taskListAdapter.notifyDataSetChanged();
                    toggleNotResultView(false);

                } else if (!newText.equals("")) {
                    //If search result is empty and the search input is not empty, show result not found
                    toggleNotResultView(true);

                } else {
                    //after finish searching and user empty the search input, reset the view
                    noteListAdapter.updateCursor();
                    taskListAdapter.updateCursor();
                    noteListAdapter.notifyDataSetChanged();
                    taskListAdapter.notifyDataSetChanged();
                    toggleNotResultView(false);
                }
                return true;
            }
        });

        search.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {

            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                noteListAdapter = noteListFragment.getNoteListAdapter();
                taskListAdapter = taskListFragment.getTaskListAdapter();

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
                setMenuItemForPage(currentPage == 'N');
                return true;
            }
        });

        showStarred.setTitle(R.string.show_starred);
        showStarred.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                noteListAdapter = noteListFragment.getNoteListAdapter();

                if (!showingStarred) {
                    // to show only starred notes
                    showingStarred = true;
                    showStarred.setTitle(R.string.show_all_note);
                    noteListAdapter.updateCursorForStarred();

                } else {
                    // to show all notes
                    showingStarred = false;
                    showStarred.setTitle(R.string.show_starred);
                    noteListAdapter.updateCursor();
                }
                noteListAdapter.notifyDataSetChanged();
                return true;
            }
        });

        showDone.setTitle(R.string.show_done);
        showDone.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                // Done items are sorted by time as urgency is not available for done item. Sorting and filtering done item cannot be toggled on at the same time. So here we reset the sort by button
                if (byUrgencyByDefault) {
                    sortBy.setTitle(R.string.sort_by_time);
                    sortingBytime = false;
                } else {
                    sortBy.setTitle(R.string.sort_by_urgency);
                    sortingBytime = true;
                }

                taskListAdapter = taskListFragment.getTaskListAdapter();

                if(!taskListAdapter.showingDone){
                    taskListAdapter.showingDone = true;
                    showDone.setTitle(R.string.show_all_task);
                    taskListAdapter.updateCursorForDone();

                } else {
                    taskListAdapter.showingDone = false;
                    showDone.setTitle(R.string.show_done);
                    taskListAdapter.updateCursor();
                }
                taskListAdapter.notifyDataSetChanged();
                return true;
            }
        });

        if (byUrgencyByDefault) {
            sortBy.setTitle(R.string.sort_by_time);
        } else sortBy.setTitle(R.string.sort_by_urgency);

        sortBy.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                taskListAdapter = taskListFragment.getTaskListAdapter();
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
                    viewPager.setCurrentItem(defaultPageIsTask ?1 :0, true);
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
        taskListAdapter = taskListFragment.getTaskListAdapter();
        noteListAdapter = noteListFragment.getNoteListAdapter();

        search.collapseActionView();
        showStarred.setTitle(R.string.show_starred);
        showingStarred = false;
        showDone.setTitle(R.string.show_done);
        taskListAdapter.showingDone = false;
        sortBy.setTitle(byUrgencyByDefault ?R.string.sort_by_time :R.string.sort_by_urgency);
        sortingBytime = !byUrgencyByDefault;
        if (taskListAdapter.actionMode !=null) {
            taskListAdapter.actionMode.finish();
        }
        if (noteListAdapter.actionMode !=null) {
            noteListAdapter.actionMode.finish();
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
