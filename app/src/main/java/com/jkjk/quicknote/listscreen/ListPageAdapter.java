package com.jkjk.quicknote.listscreen;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.preference.PreferenceManager;

import com.crashlytics.android.Crashlytics;
import com.jkjk.quicknote.R;


public class ListPageAdapter extends FragmentPagerAdapter {

    private Context context;
    private boolean defaultScreenIsTask;

    ListPageAdapter(Context context, FragmentManager fragmentManager){
        super(fragmentManager);
        this.context = context;
        Crashlytics.log(getClass().getName());

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
        switch (position){
            case 0:
                return defaultScreenIsTask ?new TaskListFragment() :new NoteListFragment();
            case 1:
                return defaultScreenIsTask ?new NoteListFragment() :new TaskListFragment();
            default:
                return null;
        }
    }


    @Override
    public int getCount() {
        return 2;
    }

}
