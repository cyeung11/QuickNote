package com.jkjk.quicknote.listscreen;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.preference.PreferenceManager;

import com.jkjk.quicknote.R;


public class ListPageAdapter extends FragmentPagerAdapter {

    private Context context;
    private boolean defaultScreen;

    ListPageAdapter(Context context, FragmentManager fragmentManager){
        super(fragmentManager);
        this.context = context;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        defaultScreen = sharedPref.getBoolean(context.getString(R.string.default_screen), false);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                if (defaultScreen) return context.getString(R.string.task);
                else return context.getString(R.string.note);
            case 1:
                if (defaultScreen) return context.getString(R.string.note);
                else return context.getString(R.string.task);
            default:
                return null;
        }
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                if (defaultScreen) return new TaskListFragment();
                else return new NoteListFragment();
            case 1:
                if (defaultScreen) return new NoteListFragment();
                else return new TaskListFragment();
            default:
                return null;
        }
    }


    @Override
    public int getCount() {
        return 2;
    }

}
