package com.jkjk.quicknote.listscreen;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.preference.PreferenceManager;

import com.jkjk.quicknote.R;


public class ListPageAdapter extends FragmentPagerAdapter {

    private Context context;
    private boolean defaultScreenIsTask;

    TaskListFragment taskListFragment;
    NoteListFragment noteListFragment;

    ListPageAdapter(Context context, FragmentManager fragmentManager){
        super(fragmentManager);
        this.context = context;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        defaultScreenIsTask = sharedPref.getBoolean(context.getString(R.string.default_screen), false);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return context.getString(defaultScreenIsTask ?R.string.task :R.string.note);
            case 1:
                return context.getString(defaultScreenIsTask ?R.string.note :R.string.task);
            default:
                return null;
        }
    }

    @Override
    public Fragment getItem(int position) {
        return getItemListFragment(position);
    }

    public ItemListFragment getItemListFragment(int position) {
        if (position == 0) {
            return defaultScreenIsTask ? getTaskListFragment() : getNoteListFragment();
        } else {
            return defaultScreenIsTask ? getNoteListFragment() : getTaskListFragment();
        }
    }

    private TaskListFragment getTaskListFragment(){
        if (taskListFragment == null) {
            taskListFragment = new TaskListFragment();
        }
        return taskListFragment;
    }

    private NoteListFragment getNoteListFragment(){
        if (noteListFragment == null) {
            noteListFragment = new NoteListFragment();
        }
        return noteListFragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

}
