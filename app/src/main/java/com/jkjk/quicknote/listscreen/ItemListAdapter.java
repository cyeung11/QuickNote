package com.jkjk.quicknote.listscreen;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jkjk.quicknote.R;

import java.util.ArrayList;
import java.util.Calendar;

public abstract class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.ViewHolder> {
    Cursor itemCursor;
    boolean isInActionMode = false;
    ActionMode actionMode;
    int cardViewInt;
    ArrayList<Integer> selectedItems;
    int itemCount;

    abstract class ViewHolder extends RecyclerView.ViewHolder{
        CardView cardView;
        long itemId;
        TextView itemTitle, itemTime;
        ImageView flagIcon;

        ViewHolder(CardView card){
            super(card);
            cardView = card;
            itemTitle = card.findViewById(R.id.item_title);
            itemTime = card.findViewById(R.id.item_date);
            flagIcon = card.findViewById(R.id.flag);
        }
    }

    @NonNull
    @Override
    public abstract ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType);

    @Override
    public abstract void onBindViewHolder(@NonNull ViewHolder holder, int position);

    @Override
    public int getItemCount() {
        //Obtain all data from database provided from Application class and get count
        itemCount = itemCursor.getCount();
        return itemCount;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        itemCursor.close();
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

    Cursor getItemCursor() {
        return itemCursor;
    }

    public abstract void updateCursor();

    public abstract void updateCursorForSearch(String result);

}
