package com.jkjk.quicknote.listscreen;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
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

import java.util.Calendar;

import static android.graphics.Paint.STRIKE_THRU_TEXT_FLAG;
import static com.jkjk.quicknote.helper.DatabaseHelper.DATABASE_NAME;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.ViewHolder> {

    private TaskListFragment fragment;
    private Cursor taskCursor;
    private int itemCount;

    TaskListAdapter(TaskListFragment fragment){
        this.fragment = fragment;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView taskTitle, taskTime, urgency;
        CheckBox taskDone;
        LinearLayout taskBody;
        long taskId;
        boolean isStarred;

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
                fragment.onTaskEdit(holder.taskId);
            }
        });

        holder.taskDone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ContentValues values = new ContentValues();
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) holder.taskBody.getLayoutParams();
                if (b) {
                    values.put("done", 1);
                    holder.urgency.setVisibility(View.GONE);
                    holder.taskTitle.setMaxLines(2);
                    layoutParams.addRule(RelativeLayout.START_OF, R.id.task_date);
                    holder.taskTitle.setPaintFlags(holder.taskTitle.getPaintFlags()|STRIKE_THRU_TEXT_FLAG);
                } else {
                    values.put("done", 0);
                    holder.taskTitle.setPaintFlags(holder.taskTitle.getPaintFlags() & (~STRIKE_THRU_TEXT_FLAG));
                    if (taskCursor!=null) {
                        taskCursor.moveToPosition(holder.getAdapterPosition());
                        switch (taskCursor.getInt(2)) {
                            case 2:
                                layoutParams.removeRule(RelativeLayout.START_OF);
                                holder.urgency.setVisibility(View.VISIBLE);
                                holder.taskTitle.setMaxLines(1);
                                holder.urgency.setText(R.string.asap);
                                holder.urgency.setTextColor(fragment.getResources().getColor(R.color.highlight));
                                break;
                            case 1:
                                layoutParams.removeRule(RelativeLayout.START_OF);
                                holder.urgency.setVisibility(View.VISIBLE);
                                holder.taskTitle.setMaxLines(1);
                                holder.urgency.setText(R.string.important);
                                holder.urgency.setTextColor(fragment.getResources().getColor(R.color.colorPrimaryDark));
                                break;
                            case 0:
                                holder.urgency.setVisibility(View.GONE);
                                holder.taskTitle.setMaxLines(2);
                                layoutParams.addRule(RelativeLayout.START_OF, R.id.task_date);
                                break;
                        }
                    }
                }
                MyApplication.database.update(DATABASE_NAME, values, "_id='" + holder.taskId +"'", null);
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

                // if mark as done
                if (taskCursor.getInt(5)==1) {
                    holder.urgency.setVisibility(View.GONE);
                    layoutParams.addRule(RelativeLayout.START_OF, R.id.task_date);
                    holder.taskDone.setChecked(true);
                    holder.taskTitle.setPaintFlags(holder.taskTitle.getPaintFlags()|STRIKE_THRU_TEXT_FLAG);
                    holder.taskTitle.setMaxLines(2);
                }

                long time = (Long.parseLong(taskCursor.getString(3)));

                if (DateUtils.isToday(time)){
                    holder.taskTime.setText(R.string.today);
                }else if (isTomorrow(time)){
                    holder.taskTime.setText(R.string.tomorrow);
                } else {
                    holder.taskTime.setText(DateUtils.formatDateTime(context, time, DateUtils.FORMAT_SHOW_DATE));
                }

                if (taskCursor.getInt(4) == 1) {
                    holder.isStarred = true;
                    holder.taskTitle.setTypeface(Typeface.SERIF, Typeface.BOLD);
                }else {
                    holder.isStarred = false;
                }

            }catch (Exception e) {
                Toast.makeText(context, R.string.error_loading, Toast.LENGTH_SHORT).show();
                Log.e(this.getClass().getName(), "Loading task into card view", e);
            }
        }
    }

    //TODO
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

        Calendar dateOfSetTime = Calendar.getInstance();
        Calendar currentTime = Calendar.getInstance();
        dateOfSetTime.setTimeInMillis(time);

        currentDay = currentTime.get(Calendar.DAY_OF_MONTH);
        currentDayOfYear = currentTime.get(Calendar.DAY_OF_YEAR);
        currentMonth = currentTime.get(Calendar.MONTH);
        currentYear = currentTime.get(Calendar.YEAR);
        setTimeDayOfYear = dateOfSetTime.get(Calendar.DAY_OF_YEAR);
        setTimeYear = dateOfSetTime.get(Calendar.YEAR);

        return (setTimeDayOfYear == 1 && setTimeYear == currentYear + 1 && currentMonth == 11 && currentDay == 31) || (setTimeDayOfYear == currentDayOfYear - 1 && setTimeYear == currentYear);
    }

    public void updateCursor(){
        taskCursor = MyApplication.database.query(DATABASE_NAME, new String[]{"_id", "title", "urgency", "time","starred","done"}, "type = 1", null, null
                , null, "time DESC");
    }

    public void updateCursorForSearch(String result){
        taskCursor = MyApplication.database.query(DATABASE_NAME, new String[]{"_id", "title", "urgency", "time","starred","done"}, "_id in ("+result+") AND type = 1", null, null
                , null, "time DESC");
    }
}
