package com.jkjk.quicknote.listscreen;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
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

import com.jkjk.quicknote.MyApplication;
import com.jkjk.quicknote.R;

import java.util.ArrayList;
import java.util.Calendar;

import static com.jkjk.quicknote.helper.DatabaseHelper.DATABASE_NAME;

public class NoteListAdapter extends RecyclerView.Adapter<NoteListAdapter.ViewHolder> {

    private NoteListFragment fragment;
    Boolean isInNoteActionMode = false;
    ActionMode actionMode;
    private ArrayList<Integer> selectedItems = new ArrayList<>();
    private Cursor noteCursor;
    private int itemCount, selectedNotStarred, notStarredCount, cardViewInt;


    NoteListAdapter(NoteListFragment fragment){
        this.fragment = fragment;
        // Obtain correspond value from preferences to show appropriate size for the card view
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(fragment.getContext());
        String cardViewSize = sharedPref.getString(fragment.getResources().getString(R.string.font_size_main_screen),"m");
        switch (cardViewSize){
            case ("s"):
                cardViewInt = R.layout.card_note_s;
                break;
            case ("m"):
                cardViewInt = R.layout.card_note_m;
                break;
            case ("l"):
                cardViewInt = R.layout.card_note_l;
                break;
            case ("xl"):
                cardViewInt = R.layout.card_note_xl;
                break;
            default:
                cardViewInt = R.layout.card_note_m;
        }
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView noteTitle, noteTime, noteContent;
        long noteId;
        boolean isStarred;

        private ViewHolder(CardView card) {
            super(card);
            cardView = card;
            noteTitle = card.findViewById(R.id.note_title);
            noteTime = card.findViewById(R.id.note_date);
            noteContent = card.findViewById(R.id.note_content);
        }
    }

    @Override
    public NoteListAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {

        CardView v = (CardView) LayoutInflater.from(parent.getContext()).inflate(cardViewInt, parent, false);
        final ViewHolder holder = new ViewHolder(v);

        v.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                //Multi-select
                if (isInNoteActionMode) {
                    int clickPosition = holder.getAdapterPosition();
                    MenuItem selectAll = actionMode.getMenu().findItem(R.id.select_all);
                    MenuItem starred = actionMode.getMenu().findItem(R.id.starred);
                    if (selectedItems.contains(clickPosition)) {
                        // Not all notes are selected, so set title to select all
                        if (selectedItems.size()==itemCount){
                            selectAll.setTitle(fragment.getResources().getString(R.string.select_all));
                        }
                        // Item has already been selected, so deselect
                        selectedItems.remove(Integer.valueOf(clickPosition));

                        holder.cardView.setCardBackgroundColor(Color.WHITE);

                        if (!holder.isStarred){
                            selectedNotStarred -= 1;
                        }
                        // if all selected item are starred, set title of menu item to unstar
                        if (selectedNotStarred == 0){
                            starred.setTitle(fragment.getResources().getString(R.string.unstarred));
                        }

                    } else {
                        // Item not been selected, so select
                        selectedItems.add(clickPosition);
                        holder.cardView.setCardBackgroundColor(Color.LTGRAY);

                        if (!holder.isStarred){
                            selectedNotStarred += 1;
                            starred.setTitle(fragment.getResources().getString(R.string.starred));
                        }

                        // if all have been select, change title to deselect all
                        if (selectedItems.size()==itemCount){
                            selectAll.setTitle(fragment.getResources().getString(R.string.deselect_all));
                        }
                    }

                }else fragment.onNoteEdit(holder.noteId);

            }
        });

        v.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(final View view) {

                final FloatingActionButton addNote = fragment.getActivity().findViewById(R.id.add_note);
                final int clickPosition = holder.getAdapterPosition();

                if (actionMode != null) {
                    return false;
                } else {
                    //Create contextual menu
                    actionMode = fragment.getActivity().startActionMode(new ActionMode.Callback() {
                        @Override
                        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                            MenuInflater inflater = mode.getMenuInflater();
                            inflater.inflate(R.menu.note_action_mode, menu);
                            isInNoteActionMode = true;

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                fragment.getActivity().getWindow().setStatusBarColor(Color.DKGRAY);
                            }

                            selectedItems.add(clickPosition);

                            MenuItem starred = menu.findItem(R.id.starred);
                            if (holder.isStarred){
                                selectedNotStarred = 0;
                                starred.setTitle(fragment.getResources().getString(R.string.unstarred));
                            }else {
                                selectedNotStarred = 1;
                                starred.setTitle(fragment.getResources().getString(R.string.starred));
                            }

                            if (itemCount == 1){
                                menu.findItem(R.id.select_all).setTitle(fragment.getResources().getString(R.string.deselect_all));
                            }
                            holder.cardView.setCardBackgroundColor(Color.LTGRAY);
                            //change the FAB to delete
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                                addNote.setImageDrawable(ContextCompat.getDrawable(fragment.getContext(), R.drawable.sharp_delete_24));
                            } else {
                                addNote.setImageResource(R.drawable.sharp_delete_24);
                            }
                            addNote.setBackgroundTintList(ColorStateList.valueOf(fragment.getResources().getColor(R.color.alternative)));

                            // Get item count of not starred note
                            Cursor checkStarredCursor = MyApplication.database.query(DATABASE_NAME, new String[]{"_id"}, "starred=0 AND type=0", null, null
                                    , null, null);
                            notStarredCount = checkStarredCursor.getCount();
                            checkStarredCursor.close();

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

                                        selectedNotStarred = notStarredCount;
                                        notifyDataSetChanged();
                                    } else {
                                        //deselect all, change title to select all
                                        selectedItems.clear();
                                        menuItem.setTitle(fragment.getResources().getString(R.string.select_all));
                                        notifyDataSetChanged();
                                        selectedNotStarred = 0;
                                    }
                                    return true;


                                case  R.id.starred:
                                    // handle starred message
                                    if (selectedItems.size()>0){

                                        ContentValues values = new ContentValues();
                                        //Convert selected items' position to note ID

                                        if (selectedNotStarred ==0){
                                            // When all selected notes are starred, un-starred them
                                            values.put("starred", 0);
                                            for (int unstarredPosition : selectedItems) {
                                                noteCursor.moveToPosition(unstarredPosition);
                                                String unstarredId = noteCursor.getString(0);

                                                //Update
                                                MyApplication.database.update(DATABASE_NAME, values, "_id='" + unstarredId + "'", null);
                                            }

                                            updateCursor();
                                            for (int unstarredPosition : selectedItems) {
                                                notifyItemChanged(unstarredPosition);
                                            }

                                            Toast.makeText(fragment.getContext(),R.string.unstarred_toast,Toast.LENGTH_SHORT).show();

                                        } else if (selectedNotStarred >0){
                                            // When some of the selected notes are not starred, starred them all again
                                            values.put("starred", 1);

                                            for (int starredPosition : selectedItems) {
                                                noteCursor.moveToPosition(starredPosition);
                                                String starredId = noteCursor.getString(0);

                                                //Update
                                                MyApplication.database.update(DATABASE_NAME, values, "_id='" + starredId + "'", null);
                                            }

                                            updateCursor();
                                            for (int starredPosition : selectedItems) {
                                                notifyItemChanged(starredPosition);
                                            }

                                            Toast.makeText(fragment.getContext(),R.string.starred_toast,Toast.LENGTH_SHORT).show();
                                        }
                                        actionMode.finish();
                                        return true;

                                    } else return false;

                                default:
                                    return false;
                            }
                        }

                        @Override
                        public void onDestroyActionMode(ActionMode mode) {
                            notifyDataSetChanged();
                            actionMode = null;
                            isInNoteActionMode = false;
                            selectedItems.clear();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                                addNote.setImageDrawable(ContextCompat.getDrawable(fragment.getContext(), R.drawable.pencil));
                            } else {
                                addNote.setImageResource(R.drawable.pencil);
                            }
                            addNote.setBackgroundTintList(ColorStateList.valueOf(fragment.getResources().getColor(R.color.highlight)));
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                fragment.getActivity().getWindow().setStatusBarColor(fragment.getResources().getColor(R.color.colorPrimaryDark));
                            }
                        }
                    });
                    return true;
                }
            }
        });

        return holder;
    }

    @Override
    public int getItemCount() {
        //Obtain all data from database provided from Application class and get count
        itemCount = noteCursor.getCount();
        return itemCount;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final Context context = holder.cardView.getContext();

        holder.noteTitle.setTypeface(Typeface.SERIF);

        // Reset card background color
        if (selectedItems.contains(holder.getAdapterPosition())) {
            holder.cardView.setCardBackgroundColor(Color.LTGRAY);
        } else {
            holder.cardView.setCardBackgroundColor(Color.WHITE);
        }

        //Extract data from noteCursor
        if (noteCursor != null) {
            try {
                noteCursor.moveToPosition(position);

                holder.noteId = noteCursor.getLong(0);

                holder.noteTitle.setText(noteCursor.getString(1).trim());

                holder.noteContent.setText(noteCursor.getString(2).trim());

                //Time formatting
                long time = noteCursor.getLong(3);
                String shownTime;
                // Get current time from Calendar and check how long ago was the note edited
                long timeSpan = Calendar.getInstance().getTimeInMillis() - time;

                if (timeSpan < 300000L){
                    //less than 5 minutes
                    shownTime = holder.cardView.getResources().getString(R.string.just_now);

                } else if (timeSpan < 3600000L){
                    //less than an hour
                    shownTime = DateUtils.getRelativeTimeSpanString(time).toString();

                }else if (DateUtils.isToday(time)) {
                    shownTime = DateUtils.formatDateTime(context, time, DateUtils.FORMAT_SHOW_TIME);

                } else if (isYesterday(time)){
                    shownTime = holder.cardView.getResources().getString(R.string.yesterday);

                } else {
                    shownTime = DateUtils.formatDateTime(context, time, DateUtils.FORMAT_SHOW_DATE);
                }
                holder.noteTime.setText(shownTime);

                // Show starred note
                if (noteCursor.getInt(4) == 1) {
                    holder.isStarred = true;
                    holder.noteTitle.setTypeface(Typeface.SERIF, Typeface.BOLD);
                }else {
                    holder.isStarred = false;
                }

            } catch (Exception e) {
                Toast.makeText(context, R.string.error_loading, Toast.LENGTH_SHORT).show();
                Log.e(this.getClass().getName(), "Loading note into card view", e);
            }
        }

    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        noteCursor.close();
    }

    private boolean isYesterday(long time) {
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

    public void updateCursor(){
        noteCursor = MyApplication.database.query(DATABASE_NAME, new String[]{"_id", "title", "content", "event_time","starred"},  "type = 0", null, null
                , null, "event_time DESC");
    }

    public void updateCursorForSearch(String result){
        noteCursor = MyApplication.database.query(DATABASE_NAME, new String[]{"_id", "title", "content", "event_time","starred"}, "_id in ("+result+") AND type = 0", null, null
                , null, "event_time DESC");
    }

    public void updateCursorForStarred(){
        noteCursor = MyApplication.database.query(DATABASE_NAME, new String[]{"_id", "title", "content", "event_time","starred"}, "starred = 1 AND type = 0", null, null
                , null, "event_time DESC");
    }

    Cursor getNoteCursor(){
        return noteCursor;
    }


}

