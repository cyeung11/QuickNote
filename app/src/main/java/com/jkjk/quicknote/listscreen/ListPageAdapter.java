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
    private int defaultScreen;

    ListPageAdapter(Context context, FragmentManager fragmentManager){
        super(fragmentManager);
        this.context = context;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        defaultScreen = Integer.valueOf(sharedPref.getString(context.getResources().getString(R.string.default_screen), "0"));
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                if (defaultScreen ==0) return context.getResources().getString(R.string.note);
                else return context.getResources().getString(R.string.task);
            case 1:
                if (defaultScreen ==0) return context.getResources().getString(R.string.task);
                else return context.getResources().getString(R.string.note);
            default:
                return null;
        }
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                if (defaultScreen ==0) return new NoteListFragment();
                else return new TaskListFragment();
            case 1:
                if (defaultScreen ==0) return new TaskListFragment();
                else return new NoteListFragment();
            default:
                return null;
        }
    }


    @Override
    public int getCount() {
        return 2;
    }

}
