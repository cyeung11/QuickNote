package com.jkjk.quicknote.listscreen;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.jkjk.quicknote.R;
import com.jkjk.quicknote.noteeditscreen.Note;

import java.util.ArrayList;
import java.util.Calendar;

public class NoteListAdapter extends ItemListAdapter {

    private NoteListFragment fragment;
    private int selectedNotStarred, notStarredCount;
    boolean showingStarred = false;

    private Context context;

    ArrayList<Note> notes = new ArrayList<>();

    NoteListAdapter(Context context, NoteListFragment fragment){
        this.fragment = fragment;
        this.context = context;
        selectedItems = new ArrayList<>();
        // Obtain correspond value from preferences to show appropriate size for the card view
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String cardViewSize = sharedPref.getString(context.getString(R.string.font_size_main_screen),"m");
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


    class ViewHolder extends ItemListAdapter.ViewHolder {
        TextView noteContent;
        Note note;

        private ViewHolder(CardView card) {
            super(card);
            noteContent = card.findViewById(R.id.note_content);
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        context = recyclerView.getContext();
    }

    @NonNull
    @Override
    public NoteListAdapter.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {

        CardView v = (CardView) LayoutInflater.from(parent.getContext()).inflate(cardViewInt, parent, false);
        final ViewHolder holder = new ViewHolder(v);

        v.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                //Multi-select
                if (isInActionMode) {
                    int clickPosition = holder.getAdapterPosition();
                    MenuItem selectAll = actionMode.getMenu().findItem(R.id.select_all);
                    MenuItem starred = actionMode.getMenu().findItem(R.id.starred);
                    if (selectedItems.contains(clickPosition)) {
                        // Not all notes are selected, so set title to select all
                        if (selectedItems.size() == notes.size()) {
                            selectAll.setTitle(context.getString(R.string.select_all));
                        }
                        // Item has already been selected, so deselect
                        selectedItems.remove(Integer.valueOf(clickPosition));

                        holder.cardView.setCardBackgroundColor(Color.WHITE);

                        if (!holder.note.isStarred()){
                            selectedNotStarred -= 1;
                        }
                        // if all selected item are starred, set title of menu item to unstar
                        if (selectedNotStarred == 0){
                            starred.setTitle(context.getString(R.string.unstarred));
                            starred.setIcon(R.drawable.sharp_outlined_flag_24);
                        }

                    } else {
                        // Item not been selected, so select
                        selectedItems.add(clickPosition);
                        holder.cardView.setCardBackgroundColor(Color.LTGRAY);

                        if (!holder.note.isStarred()){
                            selectedNotStarred += 1;
                            starred.setTitle(context.getString(R.string.starred));
                            starred.setIcon(R.drawable.sharp_flag_24);
                        }

                        // if all have been select, change title to deselect all
                        if (selectedItems.size() == notes.size()) {
                            selectAll.setTitle(context.getString(R.string.deselect_all));
                        }
                    }

                }else if (holder.note.getId() != null){ fragment.onItemEdit(holder.note.getId());}
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
                            isInActionMode = true;

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                fragment.getActivity().getWindow().setStatusBarColor(Color.DKGRAY);
                            }

                            selectedItems.add(clickPosition);

                            MenuItem starred = menu.findItem(R.id.starred);
                            if (holder.note.isStarred()){
                                selectedNotStarred = 0;
                                starred.setTitle(context.getString(R.string.unstarred));
                                starred.setIcon(R.drawable.sharp_outlined_flag_24);
                            }else {
                                selectedNotStarred = 1;
                                starred.setTitle(context.getString(R.string.starred));
                                starred.setIcon(R.drawable.sharp_flag_24);
                            }

                            if (notes.size() == 1){
                                menu.findItem(R.id.select_all).setTitle(context.getString(R.string.deselect_all));
                            }
                            holder.cardView.setCardBackgroundColor(Color.LTGRAY);
                            //change the FAB to delete
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                                addNote.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.sharp_delete_24));
                            } else {
                                addNote.setImageResource(R.drawable.sharp_delete_24);
                            }
                            addNote.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.alternative)));

                            // Get item count of not starred note
                            notStarredCount = Note.Companion.getAllNotes(context, false).size();
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
                                    if (notes.size() != selectedItems.size()){
                                        //select all, change title to deselect all
                                        selectedItems.clear();
                                        menuItem.setTitle(context.getString(R.string.deselect_all));
                                        for (int i = 0 ; i < notes.size() ; i++) {
                                            selectedItems.add(i);
                                        }

                                        selectedNotStarred = notStarredCount;
                                        notifyDataSetChanged();
                                    } else {
                                        //deselect all, change title to select all
                                        selectedItems.clear();
                                        menuItem.setTitle(context.getString(R.string.select_all));
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
                                                Note toUnstar = notes.get(unstarredPosition);
                                                toUnstar.setStarred(false);
                                                toUnstar.save(context, toUnstar.getId(), false);
                                            }

                                            updateCursor();
                                            for (int unstarredPosition : selectedItems) {
                                                notifyItemChanged(unstarredPosition);
                                            }

                                            Toast.makeText(context,R.string.unstarred_toast,Toast.LENGTH_SHORT).show();

                                        } else if (selectedNotStarred >0){
                                            // When some of the selected notes are not starred, starred them all again
                                            values.put("starred", 1);

                                            for (int starredPosition : selectedItems) {
                                                Note toStar = notes.get(starredPosition);
                                                toStar.setStarred(true);
                                                toStar.save(context, toStar.getId(), false);
                                            }

                                            updateCursor();
                                            for (int starredPosition : selectedItems) {
                                                notifyItemChanged(starredPosition);
                                            }

                                            Toast.makeText(context,R.string.starred_toast,Toast.LENGTH_SHORT).show();
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
                            isInActionMode = false;
                            selectedItems.clear();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                                addNote.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.pencil));
                            } else {
                                addNote.setImageResource(R.drawable.pencil);
                            }
                            addNote.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.highlight)));
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                fragment.getActivity().getWindow().setStatusBarColor(context.getResources().getColor(R.color.colorPrimaryDark));
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
    public void onBindViewHolder(@NonNull ItemListAdapter.ViewHolder viewHolder, int position) {

        ViewHolder holder = (ViewHolder)viewHolder;
        final Context context = holder.cardView.getContext();

        // Reset card background color
        if (selectedItems.contains(holder.getAdapterPosition())) {
            holder.cardView.setCardBackgroundColor(Color.LTGRAY);
        } else {
            holder.cardView.setCardBackgroundColor(Color.WHITE);
        }

        holder.note = notes.get(position);

        holder.itemTitle.setText(holder.note.getTitle());

        holder.noteContent.setText(holder.note.getContent());

        //Time formatting
        long time = holder.note.getEditTime().getTimeInMillis();
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
        holder.itemTime.setText(shownTime);

        // Show starred note
        if (holder.note.isStarred()) {
            holder.itemTitle.setTypeface(Typeface.SERIF, Typeface.BOLD);
            holder.flagIcon.setVisibility(View.VISIBLE);
        }else {
            holder.itemTitle.setTypeface(Typeface.SERIF);
            holder.flagIcon.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    @Override
    public void updateCursor(){
        showingStarred = false;
        notes = Note.Companion.getAllNotes(context);
    }

    @Override
    public void updateCursorForSearch(String result){
        notes = Note.Companion.getAllNotes(context, showingStarred ? true :null, result);
    }

    void updateCursorForStarred(){
        showingStarred = true;
        notes = Note.Companion.getAllNotes(context, true);
    }


}

