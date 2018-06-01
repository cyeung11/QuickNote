package com.jkjk.quicknote.listscreen;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.jkjk.quicknote.MyApplication;
import com.jkjk.quicknote.R;

import java.util.Calendar;

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
        long taskId;
        boolean isStarred;

        private ViewHolder(CardView card) {
            super(card);
            cardView = card;
            taskTitle = card.findViewById(R.id.task_title);
            taskTime = card.findViewById(R.id.task_date);
            urgency = card.findViewById(R.id.task_urgency);
            taskDone = card.findViewById(R.id.task_done);
        }
    }


    @NonNull
    @Override
    public TaskListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CardView v = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.card_task_m, parent, false);
        final ViewHolder holder = new ViewHolder(v);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragment.onTaskEdit(holder.taskId);
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final Context context = holder.cardView.getContext();

        holder.taskTitle.setTypeface(Typeface.SERIF);
        holder.urgency.setTypeface(Typeface.DEFAULT);
        holder.taskTime.setTypeface(Typeface.DEFAULT);
        holder.taskTime.setTextColor(Color.DKGRAY);
        holder.taskDone.setChecked(false);

        if (taskCursor != null) {
            try {
                taskCursor.moveToPosition(position);
                holder.taskId = taskCursor.getLong(0);

                holder.taskTitle.setText(taskCursor.getString(1).trim());

                switch (taskCursor.getInt(2)){
                    case 0:
                        holder.urgency.setText(R.string.urgent_important);
                        holder.urgency.setTextColor(Color.RED);
                        break;
                    case 1:
                        holder.urgency.setText(R.string.urgent_trivial);
                        holder.urgency.setTextColor(Color.YELLOW);
                        break;
                    case 2:
                        holder.urgency.setText(R.string.delayable_important);
                        holder.urgency.setTextColor(Color.GREEN);
                        break;
                    case 3:
                        holder.urgency.setText(R.string.delayable_trivial);
                        holder.urgency.setTextColor(Color.DKGRAY);
                        break;
                }

                if (taskCursor.getInt(5)==1) {
                    holder.urgency.setText(R.string.done);
                    holder.urgency.setTextColor(Color.LTGRAY);
                    holder.taskDone.setChecked(true);
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
                    holder.taskTitle.setTypeface(Typeface.SERIF, Typeface.BOLD_ITALIC);
                    holder.urgency.setTypeface(Typeface.DEFAULT, Typeface.BOLD_ITALIC);
                    holder.taskTime.setTextColor(Color.BLACK);
                    holder.taskTime.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
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
}
