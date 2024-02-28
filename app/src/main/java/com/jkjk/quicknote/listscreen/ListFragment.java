package com.jkjk.quicknote.listscreen;


import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jkjk.quicknote.MyApplication;
import com.jkjk.quicknote.R;
import com.jkjk.quicknote.helper.AlarmHelper;
import com.jkjk.quicknote.helper.NotificationHelper;
import com.jkjk.quicknote.helper.SearchHelper;
import com.jkjk.quicknote.noteeditscreen.Note;
import com.jkjk.quicknote.settings.Settings;
import com.jkjk.quicknote.taskeditscreen.Task;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;
import static com.jkjk.quicknote.MyApplication.PINNED_NOTIFICATION_IDS;
import static com.jkjk.quicknote.helper.AlarmHelper.ITEM_TYPE;
import static com.jkjk.quicknote.helper.NotificationHelper.ACTION_TOOL_BAR;
import static com.jkjk.quicknote.helper.NotificationHelper.PIN_ITEM_NOTIFICATION_ID;
import static com.jkjk.quicknote.widget.NoteListWidget.updateNoteListWidget;
import static com.jkjk.quicknote.widget.TaskListWidget.updateTaskListWidget;


public class ListFragment extends Fragment implements MenuItem.OnMenuItemClickListener, ViewPager.OnPageChangeListener{

    /*TODO change when release
    debug id: ca-app-pub-3940256099942544/5224354917
    real id ca-app-pub-8833570917041672/9209453063
     */
//    public static final String REWARD_VIDEO_AD_ID = "ca-app-pub-8833570917041672/9209453063";
//    public static final String ADMOB_ID = "ca-app-pub-8833570917041672~2236425579";
    // 0 stands for note , 1 stands for task
    private char currentPage;
    private boolean defaultPageIsTask, sortingBytime, byUrgencyByDefault, isNotificationToolbarEnable;

    private MenuItem showStarred, search, settings, sortBy, showDone, switchTab;
    private ViewPager viewPager;
    private SQLiteDatabase database;
    private Context context;

    private ListPageAdapter listPageAdapter;


    public ListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        listPageAdapter = new ListPageAdapter(context, getChildFragmentManager());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        database = ((MyApplication)context.getApplicationContext()).database;
    }

    @Override
    public void onDetach() {
        context = null;
        database = null;
        super.onDetach();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        isNotificationToolbarEnable = sharedPref.getBoolean(getString(R.string.notification_pin), false);
        defaultPageIsTask = sharedPref.getBoolean(getString(R.string.default_screen), false);
        byUrgencyByDefault = sharedPref.getBoolean(getString(R.string.change_default_sorting), false);
        sortingBytime = !byUrgencyByDefault;

        if (defaultPageIsTask) {
            currentPage = 'T';
        } else {
            currentPage = 'N';
        }

        final Activity activity = getActivity();

        Toolbar listMenu = view.findViewById(R.id.list_menu);
        ((AppCompatActivity) activity).setSupportActionBar(listMenu);

        ((AppCompatActivity) activity).getSupportActionBar().setTitle(defaultPageIsTask ? R.string.task : R.string.note);

        viewPager = view.findViewById(R.id.pager);
        viewPager.setAdapter(listPageAdapter);

        viewPager.addOnPageChangeListener(this);

        Intent intent = activity.getIntent();
        if (intent != null && intent.hasExtra(ITEM_TYPE)){
            char itemPageToBeOpened = intent.getCharExtra(ITEM_TYPE, 'N');
            if (itemPageToBeOpened == 'N') {
                viewPager.setCurrentItem(defaultPageIsTask ?1 :0, true);
            } else {
                viewPager.setCurrentItem(defaultPageIsTask ?0 :1, true);
            }
        }

        FloatingActionButton addNote =  view.findViewById(R.id.add_note);
        addNote.setImageResource(R.drawable.pencil);
        addNote.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.highlight)));

        addNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final ItemListFragment itemListFragment = listPageAdapter.getItemListFragment(viewPager.getCurrentItem());

                // button served as delete function
                if (itemListFragment.getItemListAdapter().isInActionMode) {
                    new AlertDialog.Builder(view.getContext()).setTitle(R.string.delete_title).setMessage(R.string.confirm_delete_list)
                            .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    deleteSelected(itemListFragment);
                                }
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .show();
                }else {
                    itemListFragment.onItemEdit();
                }
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.list_menu, menu);

        showStarred =  menu.findItem(R.id.show_starred);
        search =  menu.findItem(R.id.search);
        settings =  menu.findItem(R.id.settings);
        sortBy = menu.findItem(R.id.sort_by);
        showDone = menu.findItem(R.id.show_done);
        switchTab = menu.findItem(R.id.switch_tab);

        setMenuItemForPage(currentPage=='N');

        settings.setOnMenuItemClickListener(this);

        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setQueryHint(getResources().getString(R.string.search));
        searchView.setSubmitButtonEnabled(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {


            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String result = SearchHelper.searchResult(database, newText);

                if (listPageAdapter.taskListFragment != null){
                    listPageAdapter.taskListFragment.onSearch(newText, result);
                }
                if (listPageAdapter.noteListFragment != null){
                    listPageAdapter.noteListFragment.onSearch(newText, result);
                }
                return true;
            }
        });

        search.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {

            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                showStarred.setVisible(false);
                settings.setVisible(false);
                sortBy.setVisible(false);
                showDone.setVisible(false);
                switchTab.setVisible(false);
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
        showStarred.setOnMenuItemClickListener(this);

        showDone.setTitle(R.string.show_done);
        showDone.setOnMenuItemClickListener(this);

        sortBy.setTitle(byUrgencyByDefault ? R.string.sort_by_time : R.string.sort_by_urgency);

        sortBy.setOnMenuItemClickListener(this);
        switchTab.setOnMenuItemClickListener(this);
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.settings:
                Intent intent = new Intent(context, Settings.class);
                startActivity(intent);
                if (getActivity() != null) getActivity().finish();
                return true;

            case R.id.show_starred:
                if (listPageAdapter.noteListFragment != null) {

                    NoteListAdapter noteListAdapter = (NoteListAdapter) listPageAdapter.noteListFragment.itemListAdapter;

                    if (!noteListAdapter.showingStarred) {
                        // to show only starred notes
                        showStarred.setTitle(R.string.show_all_note);
                        noteListAdapter.updateCursorForStarred();

                    } else {
                        // to show all notes
                        showStarred.setTitle(R.string.show_starred);
                        noteListAdapter.updateCursor();
                    }
                    noteListAdapter.notifyDataSetChanged();
                }
                return true;

            case R.id.show_done:
                // Done items are sorted by time as urgency is not available for done item. Sorting and filtering done item cannot be toggled on at the same time. So here we reset the sort by button
                sortingBytime = !byUrgencyByDefault;
                sortBy.setTitle(byUrgencyByDefault ? R.string.sort_by_time : R.string.sort_by_urgency);

                if (listPageAdapter.taskListFragment != null) {

                    TaskListAdapter taskListAdapter = (TaskListAdapter) listPageAdapter.taskListFragment.itemListAdapter;

                    if (!taskListAdapter.showingDone) {
                        showDone.setTitle(R.string.show_all_task);
                        taskListAdapter.updateCursorForDone();
                    } else {
                        showDone.setTitle(R.string.show_done);
                        taskListAdapter.updateCursor();
                    }
                    taskListAdapter.notifyDataSetChanged();
                }
                return true;

            case R.id.sort_by:
                showDone.setTitle(R.string.show_done);
                sortingBytime = !sortingBytime;
                sortBy.setTitle(sortingBytime ? R.string.sort_by_time : R.string.sort_by_urgency);

                if (listPageAdapter.taskListFragment != null) {
                    TaskListAdapter taskListAdapter = (TaskListAdapter) listPageAdapter.taskListFragment.itemListAdapter;
                    if (sortingBytime){
                        taskListAdapter.updateCursorByUrgency();
                    } else {
                        taskListAdapter.updateCursorByTime();
                    }
                    taskListAdapter.notifyDataSetChanged();
                }
                return true;
            case R.id.switch_tab:
                if (currentPage == 'N'){
                    viewPager.setCurrentItem(defaultPageIsTask ?0 :1, true);

                } else if (currentPage == 'T'){
                    viewPager.setCurrentItem(defaultPageIsTask ?1 :0, true);
                }
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        if (listPageAdapter.noteListFragment != null) {
            listPageAdapter.noteListFragment.closeActionMode();
        }
        if (listPageAdapter.taskListFragment != null) {
            listPageAdapter.taskListFragment.closeActionMode();
        }

        // if default page is note, position 0 will be note, so current page is 0
        // if default page is task, position 0 will be task, so current page is 1
        if (position == 0) {
            currentPage = defaultPageIsTask ?'T' :'N';
        } else if (position == 1){
            currentPage = defaultPageIsTask ?'N' :'T';
        }

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(currentPage == 'N' ?R.string.note :R.string.task);
        }
        setMenuItemForPage(currentPage == 'N');
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onResume() {
        super.onResume();
        setMenuItemForPage(currentPage=='N');
    }

    @Override
    public void onStop() {
        // Clear all filter
        if (search != null) {
            search.collapseActionView();
        }
        if (showStarred != null) {
            showStarred.setTitle(R.string.show_starred);
        }
        if (showDone != null) {
            showDone.setTitle(R.string.show_done);
        }
        if (sortBy != null) {
            sortBy.setTitle(byUrgencyByDefault ?R.string.sort_by_time :R.string.sort_by_urgency);
        }
        sortingBytime = !byUrgencyByDefault;
        super.onStop();
    }

    private void setMenuItemForPage (boolean currentPageIsNote){
        if (showStarred != null && showDone != null && sortBy != null && switchTab != null) {
            showStarred.setVisible(currentPageIsNote);
            showDone.setVisible(!currentPageIsNote);
            sortBy.setVisible(!currentPageIsNote);
            switchTab.setTitle(currentPageIsNote ? R.string.switch_task : R.string.switch_note);
        }
    }

    private void deleteSelected(ItemListFragment itemListFragment){

        ItemListAdapter itemListAdapter = itemListFragment.itemListAdapter;

        boolean isNote = itemListFragment instanceof NoteListFragment;

        // delete note from  selectedItems
        ArrayList<Integer> mSelect = itemListAdapter.getSelected();

        SharedPreferences idPref = context.getSharedPreferences(PINNED_NOTIFICATION_IDS, MODE_PRIVATE);
        boolean isItemToday = false;


        for (int removedPosition : mSelect) {
            String removedId;
            if (isNote) {
                Note noteToRemove = ((NoteListAdapter) itemListAdapter).notes.get(removedPosition);
                if (noteToRemove.getId() != null) {
                    Note.Companion.delete(context, noteToRemove.getId());
                }
                removedId = Long.toString(noteToRemove.getId());

            } else  {
                Task taskToRemove = ((TaskListAdapter) itemListAdapter).tasks.get(removedPosition);
                if (taskToRemove.getId() != null) {
                    Task.Companion.delete(context, taskToRemove.getId());
                }
                removedId = Long.toString(taskToRemove.getId());

                // To check if the deleted item is due today. If so (which will only check if other deleted items are not today) and if notification toolbar is on, update the notification below
                if (isNotificationToolbarEnable && DateUtils.isToday(taskToRemove.getEventTime().getTimeInMillis())){
                    isItemToday = true;
                }
            }
            AlarmHelper.cancelReminder(context.getApplicationContext(), Long.valueOf(removedId));

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager != null) {
                notificationManager.cancel(Integer.valueOf(removedId));
                idPref.edit().remove(removedId).apply();
                notificationManager.cancel(Integer.valueOf(removedId)*PIN_ITEM_NOTIFICATION_ID);
            }

        }

        itemListAdapter.updateCursor();
        for (int removedPosition : mSelect) {
            itemListAdapter.notifyItemRemoved(removedPosition);
        }

        if (isItemToday){
            Intent toolBarIntent = new Intent(context, NotificationHelper.class);
            toolBarIntent.setAction(ACTION_TOOL_BAR);
            PendingIntent toolbarPendingIntent = PendingIntent.getBroadcast(context, 0, toolBarIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
            try {
                toolbarPendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }

        Toast.makeText(context, isNote ?R.string.note_deleted_toast :R.string.task_deleted_toast, Toast.LENGTH_SHORT).show();
        itemListAdapter.actionMode.finish();
        if (isNote) {
            updateNoteListWidget(context);
        } else {
            updateTaskListWidget(context);
        }
    }
}
