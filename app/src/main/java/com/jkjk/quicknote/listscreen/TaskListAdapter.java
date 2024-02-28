package com.jkjk.quicknote.listscreen;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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

import com.jkjk.quicknote.R;
import com.jkjk.quicknote.helper.AlarmHelper;
import com.jkjk.quicknote.helper.NotificationHelper;
import com.jkjk.quicknote.taskeditscreen.Task;

import java.util.ArrayList;
import java.util.Calendar;

import static android.content.Context.MODE_PRIVATE;
import static android.graphics.Paint.STRIKE_THRU_TEXT_FLAG;
import static com.jkjk.quicknote.MyApplication.PINNED_NOTIFICATION_IDS;
import static com.jkjk.quicknote.helper.NotificationHelper.ACTION_PIN_ITEM;
import static com.jkjk.quicknote.helper.NotificationHelper.ACTION_TOOL_BAR;
import static com.jkjk.quicknote.noteeditscreen.NoteEditFragment.EXTRA_ITEM_ID;
import static com.jkjk.quicknote.taskeditscreen.TaskEditFragment.DATE_NOT_SET_INDICATOR;
import static com.jkjk.quicknote.taskeditscreen.TaskEditFragment.TIME_NOT_SET_HOUR_INDICATOR;
import static com.jkjk.quicknote.taskeditscreen.TaskEditFragment.TIME_NOT_SET_MILLISECOND_INDICATOR;
import static com.jkjk.quicknote.taskeditscreen.TaskEditFragment.TIME_NOT_SET_MINUTE_SECOND_INDICATOR;
import static com.jkjk.quicknote.widget.TaskListWidget.updateTaskListWidget;

public class TaskListAdapter extends ItemListAdapter {

    boolean showingDone = false;
    private MenuItem markAsDone;
    private boolean byUrgencyByDefault, isNotificationToolbarEnable;

    private Context context;
    private TaskListFragment fragment;

    ArrayList<Task> tasks;

    TaskListAdapter(Context context, TaskListFragment fragment){
        this.context = context;
        this.fragment = fragment;
        selectedItems = new ArrayList<>();
        // Obtain correspond value from preferences to show appropriate size for the card view
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String cardViewSize = sharedPref.getString(context.getString(R.string.font_size_main_screen),"m");
        byUrgencyByDefault = sharedPref.getBoolean(context.getString(R.string.change_default_sorting), false);
        isNotificationToolbarEnable = sharedPref.getBoolean(context.getString(R.string.notification_pin), false);

        tasks = Task.Companion.getAllTask(context, byUrgencyByDefault);

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
    }

    class ViewHolder extends ItemListAdapter.ViewHolder {
        TextView urgency;
        CheckBox taskDone;
        LinearLayout taskBody;
        Task task;

        private ViewHolder(CardView card) {
            super(card);
            urgency = card.findViewById(R.id.task_urgency);
            taskDone = card.findViewById(R.id.task_done);
            taskBody = card.findViewById(R.id.task_body);
        }
    }


    @NonNull
    @Override
    public TaskListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        CardView v = (CardView) LayoutInflater.from(context).inflate(cardViewInt, parent, false);
        final ViewHolder holder = new ViewHolder(v);

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isInActionMode){
                    int clickPosition = holder.getAdapterPosition();
                    MenuItem selectAll = actionMode.getMenu().findItem(R.id.select_all);
                    if (selectedItems.contains(clickPosition)) {
                        // Not all task are selected, so change title to select all
                        if (selectedItems.size() == tasks.size()) {
                            selectAll.setTitle(context.getResources().getString(R.string.select_all));
                        }
                        // Item has already been selected, so deselect
                        selectedItems.remove(Integer.valueOf(clickPosition));

                        holder.cardView.setCardBackgroundColor(Color.WHITE);

                    } else {
                        // Item not been selected, so select
                        selectedItems.add(clickPosition);
                        holder.cardView.setCardBackgroundColor(Color.LTGRAY);

                        // if all have been select, change title to deselect all
                        if (selectedItems.size() == tasks.size()) {
                            selectAll.setTitle(context.getResources().getString(R.string.deselect_all));
                        }
                    }

                } else fragment.onItemEdit(holder.task.getId());
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
                                markAsDone.setTitle(context.getResources().getString(R.string.mark_as_pending));
                            } else markAsDone.setTitle(context.getResources().getString(R.string.mark_as_done));

                            if (tasks.size() == 1){
                                menu.findItem(R.id.select_all).setTitle(context.getResources().getString(R.string.deselect_all));
                            }

                            holder.cardView.setCardBackgroundColor(Color.LTGRAY);

                            //change the FAB to delete
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                                addNote.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.sharp_delete_24));
                            } else {
                                addNote.setImageResource(R.drawable.sharp_delete_24);
                            }
                            addNote.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.alternative)));

                            isInActionMode = true;

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
                                    if (tasks.size() != selectedItems.size()) {
                                        //select all, change title to deselect all
                                        selectedItems.clear();
                                        menuItem.setTitle(context.getResources().getString(R.string.deselect_all));
                                        for (int i = 0; i < tasks.size(); i++) {
                                            selectedItems.add(i);
                                        }

                                        notifyDataSetChanged();
                                    } else {
                                        //deselect all, change title to select all
                                        selectedItems.clear();
                                        menuItem.setTitle(context.getResources().getString(R.string.select_all));
                                        notifyDataSetChanged();
                                    }
                                    return true;

                                case R.id.mark_as_done:
                                    if (selectedItems.size() > 0) {
                                        //Convert selected items' position to note ID
                                        if (showingDone) {
                                            // When all selected notes are done, change them to pending
                                            for (int pendingPosition : selectedItems) {
                                                Task selectedTask = tasks.get(pendingPosition);
                                                selectedTask.setDone(false);
                                                //Update to pending
                                                selectedTask.save(context, selectedTask.getId());
                                            }

                                            updateTaskListWidget(context);
                                            updateCursorForDone();
                                            for (int pendingPosition : selectedItems) {
                                                notifyItemRemoved(pendingPosition);
                                            }

                                            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.notification_pin), false)){
                                                Intent toolBarIntent = new Intent(context, NotificationHelper.class);
                                                toolBarIntent.setAction(ACTION_TOOL_BAR);
                                                PendingIntent toolbarPendingIntent = PendingIntent.getBroadcast(context, 0, toolBarIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
                                                try {
                                                    toolbarPendingIntent.send();
                                                } catch (PendingIntent.CanceledException e) {
                                                    e.printStackTrace();
                                                }
                                            }

                                            Toast.makeText(context, R.string.pending_toast, Toast.LENGTH_SHORT).show();

                                        } else {
                                            // When selected tasks are pending, mark all of them done
                                            SharedPreferences idPref = context.getSharedPreferences(PINNED_NOTIFICATION_IDS, MODE_PRIVATE);
                                            for (int pendingPosition : selectedItems) {
                                                Task selectedTask = tasks.get(pendingPosition);
                                                if (selectedTask.getRepeatTime() == 0L) {
                                                    // update the task to done
                                                    selectedTask.setDone(true);
                                                } else {
                                                    long newEventTime = holder.task.getEventTime().getTimeInMillis() + holder.task.getRepeatTime();
                                                    long newReminderTime = holder.task.getReminderTime().getTimeInMillis() + holder.task.getRepeatTime();
                                                    selectedTask.getEventTime().setTimeInMillis(newEventTime);
                                                    selectedTask.getReminderTime().setTimeInMillis(newReminderTime);
                                                    AlarmHelper.setReminder(context.getApplicationContext(), holder.task.getId(), newReminderTime);
                                                }

                                                //Update to done
                                                selectedTask.save(context, selectedTask.getId());

                                                if (selectedTask.getRepeatTime() != 0) {
                                                    if (idPref.getLong(selectedTask.getId().toString(), 999999L) != 999999L) {
                                                        Intent intent = new Intent(context, NotificationHelper.class);
                                                        intent.setAction(ACTION_PIN_ITEM);
                                                        intent.putExtra(EXTRA_ITEM_ID, selectedTask.getId());
                                                        PendingIntent pinNotificationPI = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
                                                        try {
                                                            pinNotificationPI.send();
                                                        } catch (PendingIntent.CanceledException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }
                                            }

                                            updateTaskListWidget(context);
                                            updateCursor();
                                            notifyDataSetChanged();

                                            if (isNotificationToolbarEnable){
                                                Intent toolBarIntent = new Intent(context, NotificationHelper.class);
                                                toolBarIntent.setAction(ACTION_TOOL_BAR);
                                                PendingIntent toolbarPendingIntent = PendingIntent.getBroadcast(context, 0, toolBarIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
                                                try {
                                                    toolbarPendingIntent.send();
                                                } catch (PendingIntent.CanceledException e) {
                                                    e.printStackTrace();
                                                }
                                            }

                                            Toast.makeText(context, R.string.done_toast, Toast.LENGTH_SHORT).show();
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


        holder.taskDone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {

                if (compoundButton.isPressed()) {

                    if (context!=null) {
                        if (checked) {
                            // Check if the item has repeat property, if yes, delay the event time instead

                            if (holder.task != null) {

                                if (holder.task.getRepeatTime() == 0L) {
                                    // update the task to done
                                    holder.task.setDone(true);
                                } else {
                                    long newEventTime = holder.task.getEventTime().getTimeInMillis() + holder.task.getRepeatTime();
                                    long newReminderTime = holder.task.getReminderTime().getTimeInMillis() + holder.task.getRepeatTime();
                                    holder.task.getEventTime().setTimeInMillis(newEventTime);
                                    holder.task.getReminderTime().setTimeInMillis(newReminderTime);
                                    AlarmHelper.setReminder(context.getApplicationContext(), holder.task.getId(), newReminderTime);
                                }
                                holder.task.save(context, holder.task.getId());
                                if (holder.task.getRepeatTime() > 0) {
                                    SharedPreferences idPref = context.getSharedPreferences(PINNED_NOTIFICATION_IDS, MODE_PRIVATE);
                                    if (idPref.getLong(Long.toString(holder.task.getId()), 999999L) != 999999L) {
                                        Intent intent = new Intent(context, NotificationHelper.class);
                                        intent.setAction(ACTION_PIN_ITEM);
                                        intent.putExtra(EXTRA_ITEM_ID, holder.task.getId());
                                        PendingIntent pinNotificationPI = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
                                        try {
                                            pinNotificationPI.send();
                                        } catch (PendingIntent.CanceledException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                                Toast.makeText(context, R.string.done_toast, Toast.LENGTH_SHORT).show();
                                updateTaskListWidget(context);
                            } else {
                                Toast.makeText(context, R.string.error_text, Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            // update the task to pending
                            holder.task.setDone(false);
                            holder.task.save(context, holder.task.getId());
                            updateTaskListWidget(context);
                            Toast.makeText(context, R.string.pending_toast, Toast.LENGTH_SHORT).show();
                        }

                        if (isNotificationToolbarEnable){
                            Intent toolBarIntent = new Intent(context, NotificationHelper.class);
                            toolBarIntent.setAction(ACTION_TOOL_BAR);
                            PendingIntent toolbarPendingIntent = PendingIntent.getBroadcast(context, 0, toolBarIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
                            try {
                                toolbarPendingIntent.send();
                            } catch (PendingIntent.CanceledException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (showingDone) {
                        updateCursorForDone();
                    } else {
                        updateCursor();
                    }
                    if (holder.task.getRepeatTime() == 0L) {
                        notifyItemRemoved(holder.getAdapterPosition());
                    } else {
                        notifyDataSetChanged();
                    }
                }
            }
        });
        return holder;
    }


    @Override
    public void onBindViewHolder(@NonNull ItemListAdapter.ViewHolder viewHolder, int position) {

        ViewHolder holder = (ViewHolder)viewHolder;

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) holder.taskBody.getLayoutParams();

        // Reset card background color
        if (selectedItems.contains(holder.getAdapterPosition())) {
            holder.cardView.setCardBackgroundColor(Color.LTGRAY);
        } else {
            holder.cardView.setCardBackgroundColor(Color.WHITE);
        }

        // if showing done item
        if (showingDone) {
            layoutParams.addRule(RelativeLayout.START_OF, R.id.item_date);
            holder.taskDone.setChecked(true);
            holder.itemTitle.setPaintFlags(holder.itemTitle.getPaintFlags()|STRIKE_THRU_TEXT_FLAG);
            holder.itemTitle.setMaxLines(2);
        } else {
            layoutParams.removeRule(RelativeLayout.START_OF);
            holder.taskDone.setChecked(false);
            holder.itemTitle.setPaintFlags(holder.itemTitle.getPaintFlags() & (~STRIKE_THRU_TEXT_FLAG));
            holder.itemTitle.setMaxLines(1);
        }

        holder.urgency.setVisibility(View.GONE);

        if (position < tasks.size() ) {
            holder.task = tasks.get(position);
            if (holder.task != null) {
                try {
                    holder.itemTitle.setText(holder.task.getTitle().trim());

                    switch (holder.task.getUrgency()) {
                        case 2:
                            if (!holder.task.isDone()) {
                                holder.urgency.setVisibility(View.VISIBLE);
                            }
                            holder.urgency.setText(R.string.asap);
                            holder.urgency.setTextColor(context.getResources().getColor(R.color.colorPrimary));
                            holder.urgency.setTypeface(Typeface.DEFAULT_BOLD);
                            holder.flagIcon.setVisibility(showingDone ? View.GONE : View.VISIBLE);
                            break;
                        case 1:
                            if (!holder.task.isDone()) {
                                holder.urgency.setVisibility(View.VISIBLE);
                            }
                            holder.urgency.setText(R.string.important);
                            holder.urgency.setTextColor(context.getResources().getColor(R.color.darkGrey));
                            holder.urgency.setTypeface(Typeface.DEFAULT);
                            holder.flagIcon.setVisibility(showingDone ? View.GONE : View.VISIBLE);
                            break;
                        case 0:
                            layoutParams.addRule(RelativeLayout.START_OF, R.id.item_date);
                            holder.itemTitle.setMaxLines(2);
                            holder.flagIcon.setVisibility(View.GONE);
                            break;
                    }

                    long time = holder.task.getEventTime().getTimeInMillis();

                    if (time != DATE_NOT_SET_INDICATOR) {

                        // Make the time red if the task is expired
                        Calendar calendar = Calendar.getInstance();
                        if (!showingDone && calendar.getTimeInMillis() > time) {
                            holder.itemTime.setTextColor(context.getResources().getColor(R.color.alternative));
                        } else
                            holder.itemTime.setTextColor(context.getResources().getColor(R.color.darkGrey));

                        if (DateUtils.isToday(time)) {
                            calendar.setTimeInMillis(time);

                            //get the time to see if the time was set by user
                            if (calendar.get(Calendar.MILLISECOND) == TIME_NOT_SET_MILLISECOND_INDICATOR
                                    && calendar.get(Calendar.SECOND) == TIME_NOT_SET_MINUTE_SECOND_INDICATOR
                                    && calendar.get(Calendar.MINUTE) == TIME_NOT_SET_MINUTE_SECOND_INDICATOR
                                    && calendar.get(Calendar.HOUR_OF_DAY) == TIME_NOT_SET_HOUR_INDICATOR) {

                                holder.itemTime.setText(R.string.today);

                            } else
                                holder.itemTime.setText(DateUtils.formatDateTime(context, time, DateUtils.FORMAT_SHOW_TIME));

                        } else if (isTomorrow(time)) {
                            holder.itemTime.setText(R.string.tomorrow);
                        } else {
                            holder.itemTime.setText(DateUtils.formatDateTime(context, time, DateUtils.FORMAT_SHOW_DATE));
                        }
                    } else {
                        holder.itemTime.setText("");
                    }

                } catch (Exception e) {
                    Toast.makeText(context, R.string.error_loading, Toast.LENGTH_SHORT).show();
                    Log.e(this.getClass().getName(), "Loading task into card view", e);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public void updateCursor(){
        showingDone = false;
        tasks = Task.Companion.getAllTask(context, byUrgencyByDefault, false);
    }

    @Override
    public void updateCursorForSearch(String result){
        tasks = Task.Companion.getAllTask(context, byUrgencyByDefault, showingDone, result);
    }

    void updateCursorForDone(){
        showingDone = true;
        tasks = Task.Companion.getAllTask(context, byUrgencyByDefault, true);
    }

    void updateCursorByUrgency(){
        showingDone = false;
        tasks = Task.Companion.getAllTask(context, true, false);
    }

    void updateCursorByTime(){
        showingDone = false;
        tasks = Task.Companion.getAllTask(context, false, false);
    }
}
