package com.jkjk.quicknote;

import android.content.Context;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.jkjk.quicknote.Fragment.NoteListFragment;

import java.util.ArrayList;
import java.util.Calendar;

import static com.jkjk.quicknote.DatabaseHelper.DATABASE_NAME;

public class NoteListAdapter extends RecyclerView.Adapter<NoteListAdapter.ViewHolder> {

    private int itemCount;
    NoteListFragment fragment;
    public static Boolean inActionMode = false;
    public static ActionMode mActionMode;
    private ArrayList<Integer> selectedItems;
    private static Cursor cursor;



    public NoteListAdapter(NoteListFragment fragment){
        this.fragment = fragment;
        selectedItems = new ArrayList<>();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView noteTitle, noteTime, noteContent;
        Long noteId;

        public ViewHolder(CardView card) {
            super(card);
            cardView = card;
            noteTitle = (TextView) card.findViewById(R.id.note_title);
            noteTime = (TextView) card.findViewById(R.id.note_date);
            noteContent = (TextView) card.findViewById(R.id.note_content);
        }
    }

    @Override
    public NoteListAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        CardView v = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.card_note, parent, false);
        final ViewHolder holder = new ViewHolder(v);

        v.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                int clickPosition = holder.getAdapterPosition();
                //Multi-select
                if (inActionMode) {
                    if (selectedItems.contains(clickPosition)) {
                        // Not all are selected anymore, so change title to select all
                        if (selectedItems.size()==itemCount){
                            MenuItem selectAll = mActionMode.getMenu().findItem(R.id.select_all);
                            selectAll.setTitle(fragment.getResources().getString(R.string.select_all));
                        }
                        // Item has already been selected, so deselect
                        selectedItems.remove(Integer.valueOf(clickPosition));
                        holder.cardView.setCardBackgroundColor(Color.WHITE);
                    } else {
                        // Item not been selected, so select
                        selectedItems.add(clickPosition);
                        holder.cardView.setCardBackgroundColor(Color.LTGRAY);

                        // if all have been select, change title to deselect all
                        if (selectedItems.size()==itemCount){
                            MenuItem selectAll = mActionMode.getMenu().findItem(R.id.select_all);
                            selectAll.setTitle(fragment.getResources().getString(R.string.deselect_all));
                        }
                    }
                }else {
                    fragment.onNoteEdit(holder.noteId);

                }
            }
        });

        v.setOnLongClickListener(new View.OnLongClickListener() {
            ActionMode actionMode;

            @Override
            public boolean onLongClick(final View view) {

                //change the FAB to delete
                final FloatingActionButton addNote = (FloatingActionButton)fragment.getActivity().findViewById(R.id.add_note);
                addNote.setImageDrawable(fragment.getResources().getDrawable(R.drawable.sharp_delete_white_24));
                addNote.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ffff4444")));


                final int clickPosition = holder.getAdapterPosition();
                if (actionMode != null) {
                    return false;
                } else {
                    //Create contextual menu
                    actionMode = fragment.getActivity().startActionMode(new ActionMode.Callback() {
                        @Override
                        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                            MenuInflater inflater = mode.getMenuInflater();
                            inflater.inflate(R.menu.edit_menu, menu);
                            inActionMode = true;
                            selectedItems.add(clickPosition);
                            holder.cardView.setCardBackgroundColor(Color.LTGRAY);
                            return true;
                        }

                        @Override
                        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                            return false;
                        }

                        @Override
                        public boolean onActionItemClicked(final ActionMode mode, MenuItem menuItem) {
                            switch (menuItem.getItemId()) {
                                case R.id.select_all:
                                    if (itemCount != selectedItems.size()){
                                        //select all, change title to deselect all
                                        selectedItems.clear();
                                        menuItem.setTitle(fragment.getResources().getString(R.string.deselect_all));
                                        for (int i = 0 ; i < itemCount ; i++) {
                                            selectedItems.add(i);

                                        }
                                        notifyDataSetChanged();
                                    } else {
                                        //deselect all, change title to select all
                                        selectedItems.clear();
                                        menuItem.setTitle(fragment.getResources().getString(R.string.select_all));
                                        notifyDataSetChanged();
                                    }
                                    return true;
                                default:
                                    return false;
                            }
                        }

                        @Override
                        public void onDestroyActionMode(ActionMode mode) {
                            notifyDataSetChanged();
                            actionMode = null;
                            inActionMode = false;
                            selectedItems.clear();
                            addNote.setImageDrawable(fragment.getResources().getDrawable(R.drawable.ic_add_white));
                            addNote.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ff33b5e5")));
                        }
                    });
                    view.setSelected(true);
                    mActionMode = actionMode;
                    return true;
                }
            }
        });

        return holder;
    }

    @Override
    public int getItemCount() {
        //Obtain all data from database provided from Application class and get count
        itemCount = updateCursor().getCount();
        return itemCount;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final Context context = holder.cardView.getContext();

        if (selectedItems.contains(holder.getAdapterPosition())) {
            holder.cardView.setCardBackgroundColor(Color.LTGRAY);
        } else {
            holder.cardView.setCardBackgroundColor(Color.WHITE);
        }


        //Extract data from cursor
        if (updateCursor() != null) {
            try {
                cursor.moveToPosition(position);

                holder.noteId = cursor.getLong(0);

                holder.noteTitle.setText(cursor.getString(1).trim());

                holder.noteContent.setText(cursor.getString(2).trim());

                //Time formatting
                Long time = (Long.parseLong(cursor.getString(3)));
                String shownTime;
                if (DateUtils.isToday(time)) {
                    shownTime = DateUtils.formatDateTime(context, time, DateUtils.FORMAT_SHOW_TIME);
                } else if (isYesterday(time)){
                    shownTime = holder.cardView.getResources().getString(R.string.yesterday);
                } else {
                    shownTime = DateUtils.formatDateTime(context, time, DateUtils.FORMAT_SHOW_DATE);
                }
                holder.noteTime.setText(shownTime);

            } catch (Exception e) {
                Toast.makeText(context, R.string.error_loading, Toast.LENGTH_SHORT).show();
                Log.e(this.getClass().getName(), "error", e);
            }
        }

    }

    private boolean isYesterday(Long setTime) {
        int currentDayOfYear, currentYear, setTimeDayOfYear, setTimeYear, setTimeMonth, setTimeDay;

        Calendar dateOfSetTime = Calendar.getInstance();
        Calendar currentTime = Calendar.getInstance();
        dateOfSetTime.setTimeInMillis(setTime);

        currentDayOfYear = currentTime.get(Calendar.DAY_OF_YEAR);
        currentYear = currentTime.get(Calendar.YEAR);
        setTimeDayOfYear = dateOfSetTime.get(Calendar.DAY_OF_YEAR);
        setTimeYear = dateOfSetTime.get(Calendar.YEAR);
        setTimeMonth = dateOfSetTime.get(Calendar.MONTH);
        setTimeDay = dateOfSetTime.get(Calendar.DAY_OF_MONTH);

        return (currentDayOfYear == 1 && setTimeYear == currentYear - 1 && setTimeMonth == 11 && setTimeDay == 31) || (setTimeDayOfYear == currentDayOfYear - 1 && setTimeYear == currentYear);
    }


    public int[] getSelected (){
        int[] selected = new int[selectedItems.size()];
        for (int i = 0; i < selectedItems.size(); i++){
            selected [i] = selectedItems.get(i);
        }
        return selected;
    }

    public static Cursor updateCursor(){
        cursor = MyApplication.getDatabase().query(DATABASE_NAME, new String[]{"_id", "title", "content", "time"}, null, null, null
                , null, "time DESC");
        return cursor;
    }
}

