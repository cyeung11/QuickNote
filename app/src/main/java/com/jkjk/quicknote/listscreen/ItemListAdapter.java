package com.jkjk.quicknote.listscreen;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.widget.ImageView;
import android.widget.TextView;

import com.jkjk.quicknote.R;

import java.util.ArrayList;
import java.util.Calendar;

public abstract class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.ViewHolder> {
//    protected Cursor itemCursor;
    boolean isInActionMode = false;
    ActionMode actionMode;
    protected int cardViewInt;
    ArrayList<Integer> selectedItems;

    protected abstract class ViewHolder extends RecyclerView.ViewHolder{
        public CardView cardView;
        public TextView itemTitle, itemTime;
        public ImageView flagIcon;

        protected ViewHolder(CardView card){
            super(card);
            cardView = card;
            itemTitle = card.findViewById(R.id.item_title);
            itemTime = card.findViewById(R.id.item_date);
            flagIcon = card.findViewById(R.id.flag);
        }
    }

    public static boolean isTomorrow(long time) {
        int currentDayOfYear, currentDay, currentYear, currentMonth, setTimeDayOfYear, setTimeYear;

        Calendar setTime = Calendar.getInstance();
        setTime.setTimeInMillis(time);

        Calendar currentTime = Calendar.getInstance();

        currentDay = currentTime.get(Calendar.DAY_OF_MONTH);
        currentDayOfYear = currentTime.get(Calendar.DAY_OF_YEAR);
        currentMonth = currentTime.get(Calendar.MONTH);
        currentYear = currentTime.get(Calendar.YEAR);
        setTimeDayOfYear = setTime.get(Calendar.DAY_OF_YEAR);
        setTimeYear = setTime.get(Calendar.YEAR);

        return (setTimeDayOfYear == currentDayOfYear + 1 && setTimeYear == currentYear) || (setTimeDayOfYear == 1 && setTimeYear == currentYear + 1 && currentMonth == 11 && currentDay == 31);
    }

    public static boolean isYesterday(long time) {
        int currentDayOfYear, currentYear, setTimeDayOfYear, setTimeYear, setTimeMonth, setTimeDay;

        Calendar setTime = Calendar.getInstance();
        Calendar currentTime = Calendar.getInstance();
        setTime.setTimeInMillis(time);

        currentDayOfYear = currentTime.get(Calendar.DAY_OF_YEAR);
        currentYear = currentTime.get(Calendar.YEAR);
        setTimeDayOfYear = setTime.get(Calendar.DAY_OF_YEAR);
        setTimeYear = setTime.get(Calendar.YEAR);
        setTimeMonth = setTime.get(Calendar.MONTH);
        setTimeDay = setTime.get(Calendar.DAY_OF_MONTH);

        return (setTimeDayOfYear == currentDayOfYear - 1 && setTimeYear == currentYear) || (currentDayOfYear == 1 && setTimeYear == currentYear - 1 && setTimeMonth == 11 && setTimeDay == 31) ;
    }

    ArrayList<Integer> getSelected (){
        return selectedItems;
    }

    public abstract void updateCursor();

    public abstract void updateCursorForSearch(String result);

}
