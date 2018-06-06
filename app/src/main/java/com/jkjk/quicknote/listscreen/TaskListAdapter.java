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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jkjk.quicknote.MyApplication;
import com.jkjk.quicknote.R;

import java.util.ArrayList;
import java.util.Calendar;

import static android.graphics.Paint.STRIKE_THRU_TEXT_FLAG;
import static com.jkjk.quicknote.helper.DatabaseHelper.DATABASE_NAME;
import static com.jkjk.quicknote.taskeditscreen.TaskEditFragment.TIME_NOT_SET_HOUR_INDICATOR;
import static com.jkjk.quicknote.taskeditscreen.TaskEditFragment.TIME_NOT_SET_MILLISECOND_INDICATOR;
import static com.jkjk.quicknote.taskeditscreen.TaskEditFragment.TIME_NOT_SET_MINUTE_SECOND_INDICATOR;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.ViewHolder> {

    boolean showingDone = false;
    private TaskListFragment fragment;
    private Cursor taskCursor;
    ActionMode actionMode;
    Boolean isInTaskActionMode = false;
    private ArrayList<Integer> selectedItems = new ArrayList<>();
    private int itemCount, notDoneCount;
    private MenuItem markAsDone;

    TaskListAdapter(TaskListFragment fragment){
        this.fragment = fragment;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView taskTitle, taskTime, urgency;
        CheckBox taskDone;
        LinearLayout taskBody;
        long taskId;
        boolean isDone;

        private ViewHolder(CardView card) {
            super(card);
            cardView = card;
            taskTitle = card.findViewById(R.id.task_title);
            taskTime = card.findViewById(R.id.task_date);
            urgency = card.findViewById(R.id.task_urgency);
            taskDone = card.findViewById(R.id.task_done);
            taskBody = card.findViewById(R.id.task_body);
        }
    }


    @NonNull
    @Override
    public TaskListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // Obtain correspond value from preferences to show appropriate size for the card view
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(fragment.getContext());
        String cardViewSize = sharedPref.getString(fragment.getResources().getString(R.string.font_size_main_screen),"m");
        int cardViewInt;
        switch (cardViewSize){
            case ("s"):
                cardViewInt = R.layout.card_task_s;
                break;
            case ("m"):
                cardViewInt = R.layout.card_task_m;
                break;
            case ("l"):
                cardViewInt = R.layout.card_task_l;
                break;
            case ("xl"):
                cardViewInt = R.layout.card_task_xl;
                break;
            default:
                cardViewInt = R.layout.card_task_m;
        }

        CardView v = (CardView) LayoutInflater.from(parent.getContext()).inflate(cardViewInt, parent, false);
        final ViewHolder holder = new ViewHolder(v);

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isInTaskActionMode){
                    int clickPosition = holder.getAdapterPosition();
                    MenuItem selectAll = actionMode.getMenu().findItem(R.id.select_all);
                    if (selectedItems.contains(clickPosition)) {
                        // Not all task are selected, so change title to select all
                        if (selectedItems.size()==itemCount){
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
                            selectAll.setTitle(fragment.getResources().getString(R.string.deselect_all));
                        }
                    }

                } else fragment.onTaskEdit(holder.taskId);
            }
        });

        v.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View view) {

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
                            inflater.inflate(R.menu.task_action_mode, menu);

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                fragment.getActivity().getWindow().setStatusBarColor(Color.DKGRAY);
                            }

                            selectedItems.add(clickPosition);

                            markAsDone = menu.findItem(R.id.mark_as_done);

                            if (showingDone){
                                markAsDone.setTitle(fragment.getResources().getString(R.string.mark_as_pending));
                            } else markAsDone.setTitle(fragment.getResources().getString(R.string.mark_as_done));

                            if (itemCount == 1){
                                menu.findItem(R.id.select_all).setTitle(fragment.getResources().getString(R.string.deselect_all));
                            }

                            holder.cardView.setCardBackgroundColor(Color.LTGRAY);

                            //change the FAB to delete
                            addNote.setImageDrawable(ContextCompat.getDrawable(fragment.getContext(), R.drawable.sharp_delete_24));
                            addNote.setBackgroundTintList(ColorStateList.valueOf(fragment.getResources().getColor(R.color.alternative)));

                            // Get item count of pending tasks
                            Cursor checkPendingCursor = MyApplication.database.query(DATABASE_NAME, new String[]{"_id"}, "done=0 AND type=1", null, null
                                    , null, null);
                            notDoneCount = checkPendingCursor.getCount();
                            checkPendingCursor.close();

                            isInTaskActionMode = true;

                            return true;
                        }

                        @Override
                        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                            return false;
                        }

                        @Override
                        public boolean onActionItemClicked(ActionMode mode, MenuItem menuItem) {
                            switch (menuItem.getItemId()) {
                                case R.id.select_all:
                                    if (itemCount != selectedItems.size()) {
                                        //select all, change title to deselect all
                                        selectedItems.clear();
                                        menuItem.setTitle(fragment.getResources().getString(R.string.deselect_all));
                                        for (int i = 0; i < itemCount; i++) {
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

                                case R.id.mark_as_done:
                                    if (selectedItems.size() > 0) {

                                        ContentValues values = new ContentValues();
                                        //Convert selected items' position to note ID

                                        if (showingDone) {
                                            // When all selected notes are done, change them to pending
                                            values.put("done", 0);
                                            for (int pendingPosition : selectedItems) {
                                                taskCursor.moveToPosition(pendingPosition);
                                                String pendingId = taskCursor.getString(0);
                                                //Update
                                                MyApplication.database.update(DATABASE_NAME, values, "_id='" + pendingId + "'", null);
                                            }

                                            updateCursorForDone();
                                            for (int pendingPosition : selectedItems) {
                                                notifyItemRemoved(pendingPosition);
                                            }

                                            Toast.makeText(fragment.getContext(), R.string.pending_toast, Toast.LENGTH_SHORT).show();

                                        } else {
                                            // When selected tasks are pending, mark all of them done
                                            values.put("done", 1);
                                            for (int pendingPosition : selectedItems) {
                                                taskCursor.moveToPosition(pendingPosition);
                                                String doneId = taskCursor.getString(0);

                                                //Update
                                                MyApplication.database.update(DATABASE_NAME, values, "_id='" + doneId + "'", null);
                                            }

                                            updateCursor();
                                            for (int pendingPosition : selectedItems) {
                                                notifyItemRemoved(pendingPosition);
                                            }

                                            Toast.makeText(fragment.getContext(), R.string.done_toast, Toast.LENGTH_SHORT).show();
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
                            isInTaskActionMode = false;
                            selectedItems.clear();
                            addNote.setImageDrawable(ContextCompat.getDrawable(fragment.getContext(), R.drawable.pencil));
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


        holder.taskDone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {

                if (compoundButton.isPressed()) {
                    ContentValues values = new ContentValues();

                    if (checked) {
                        values.put("done", 1);
                        holder.isDone = true;

                    } else {
                        values.put("done", 0);
                        holder.isDone = false;
                    }
                    MyApplication.database.update(DATABASE_NAME, values, "_id='" + holder.taskId + "'", null);
                    if (showingDone) {
                        updateCursorForDone();
                    } else {
                        updateCursor();
                    }
                    notifyItemRemoved(holder.getAdapterPosition());
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final Context context = holder.cardView.getContext();

        holder.taskTitle.setTypeface(Typeface.SERIF);
        holder.taskTitle.setMaxLines(1);
        holder.taskDone.setChecked(false);
        holder.urgency.setVisibility(View.VISIBLE);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) holder.taskBody.getLayoutParams();
        layoutParams.removeRule(RelativeLayout.START_OF);

        // Reset card background color
        if (selectedItems.contains(holder.getAdapterPosition())) {
            holder.cardView.setCardBackgroundColor(Color.LTGRAY);
        } else {
            holder.cardView.setCardBackgroundColor(Color.WHITE);
        }

        // if mark as done
        if (showingDone) {
            holder.isDone = true;
            holder.urgency.setVisibility(View.GONE);
            layoutParams.addRule(RelativeLayout.START_OF, R.id.task_date);
            holder.taskDone.setChecked(true);
            holder.taskTitle.setPaintFlags(holder.taskTitle.getPaintFlags()|STRIKE_THRU_TEXT_FLAG);
            holder.taskTitle.setMaxLines(2);
        } else {
            holder.isDone = false;
            holder.urgency.setVisibility(View.VISIBLE);
            layoutParams.removeRule(RelativeLayout.START_OF);
            holder.taskDone.setChecked(false);
            holder.taskTitle.setPaintFlags(holder.taskTitle.getPaintFlags() & (~STRIKE_THRU_TEXT_FLAG));
            holder.taskTitle.setMaxLines(1);
        }


        if (taskCursor != null) {
            try {
                taskCursor.moveToPosition(position);
                holder.taskId = taskCursor.getLong(0);

                holder.taskTitle.setText(taskCursor.getString(1).trim());

                switch (taskCursor.getInt(2)){
                    case 2:
                        holder.urgency.setText(R.string.asap);
                        holder.urgency.setTextColor(fragment.getResources().getColor(R.color.highlight));
                        break;
                    case 1:
                        holder.urgency.setText(R.string.important);
                        holder.urgency.setTextColor(fragment.getResources().getColor(R.color.colorPrimaryDark));
                        break;
                    case 0:
                        holder.urgency.setVisibility(View.GONE);
                        layoutParams.addRule(RelativeLayout.START_OF, R.id.task_date);
                        holder.taskTitle.setMaxLines(2);
                        break;
                }

                long time = (Long.parseLong(taskCursor.getString(3)));

                if (time!=9999999999999L) {
                    if (DateUtils.isToday(time)) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(time);

                        //get the time to see if the time was set by user
                        if (calendar.get(Calendar.MILLISECOND) == TIME_NOT_SET_MILLISECOND_INDICATOR
                                && calendar.get(Calendar.SECOND) ==  TIME_NOT_SET_MINUTE_SECOND_INDICATOR
                                && calendar.get(Calendar.MINUTE) ==  TIME_NOT_SET_MINUTE_SECOND_INDICATOR
                                && calendar.get(Calendar.HOUR_OF_DAY) ==  TIME_NOT_SET_HOUR_INDICATOR) {

                            holder.taskTime.setText(R.string.today);

                        } else holder.taskTime.setText(DateUtils.formatDateTime(context, time, DateUtils.FORMAT_SHOW_TIME));

                    } else if (isTomorrow(time)) {
                        holder.taskTime.setText(R.string.tomorrow);
                    } else {
                        holder.taskTime.setText(DateUtils.formatDateTime(context, time, DateUtils.FORMAT_SHOW_DATE));
                    }
                }

            }catch (Exception e) {
                Toast.makeText(context, R.string.error_loading, Toast.LENGTH_SHORT).show();
                Log.e(this.getClass().getName(), "Loading task into card view", e);
            }
        }
    }

    @Override
    public int getItemCount() {
        itemCount = taskCursor.getCount();
        return itemCount;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        taskCursor.close();
    }

    private boolean isTomorrow(long time) {
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

    ArrayList<Integer> getSelected (){
        return selectedItems;
    }

    Cursor getTaskCursor(){
        return taskCursor;
    }

    public void updateCursor(){
        taskCursor = MyApplication.database.query(DATABASE_NAME, new String[]{"_id", "title", "urgency", "time","done"}, "type = 1 AND done = 0", null, null
                , null, "time ASC");
    }

    public void updateCursorForSearch(String result){
        taskCursor = MyApplication.database.query(DATABASE_NAME, new String[]{"_id", "title", "urgency", "time","done"}, "_id in ("+result+") AND type = 1", null, null
                , null, "time ASC");
    }

    public void updateCursorForDone(){
        taskCursor = MyApplication.database.query(DATABASE_NAME, new String[]{"_id", "title", "urgency", "time","done"}, "type=1 AND done=1", null, null
                , null, "time ASC");
    }

    public void updateCursorForSorting(){
        taskCursor = MyApplication.database.query(DATABASE_NAME, new String[]{"_id", "title", "urgency", "time","done"}, "type = 1 AND done = 0", null, null
                , null, "urgency DESC, time ASC");
    }
}
