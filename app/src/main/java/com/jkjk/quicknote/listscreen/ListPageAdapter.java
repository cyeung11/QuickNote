package com.jkjk.quicknote.listscreen;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

public class ListPageAdapter extends FragmentPagerAdapter {

    NoteListFragment noteListFragment;
    TaskListFragment taskListFragment;

    ListPageAdapter(FragmentManager fragmentManager){
        super(fragmentManager);

        noteListFragment = new NoteListFragment();
        taskListFragment = new TaskListFragment();
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return noteListFragment;
            case 1:
                return taskListFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        return super.instantiateItem(container, position);
    }

    public NoteListFragment getNoteListFragment() {
        return noteListFragment;
    }

    public TaskListFragment getTaskListFragment() {
        return taskListFragment;
    }
}
