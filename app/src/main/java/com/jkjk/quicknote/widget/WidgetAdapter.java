package com.jkjk.quicknote.widget;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.jkjk.quicknote.MyApplication;
import com.jkjk.quicknote.R;

import java.util.Calendar;

import static android.app.Activity.RESULT_CANCELED;
import static android.content.Context.MODE_PRIVATE;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static com.jkjk.quicknote.editscreen.EditFragment.EXTRA_NOTE_ID;
import static com.jkjk.quicknote.helper.DatabaseHelper.DATABASE_NAME;
import static com.jkjk.quicknote.widget.AppWidgetService.IS_FROM_WIDGET;

public class WidgetAdapter extends RecyclerView.Adapter<WidgetAdapter.ViewHolder> {

    private Activity activity;
    private int mAppWidgetId;
    private int color = Color.parseColor("#FFFFFF");
    private Cursor cursorForWidget;


    WidgetAdapter(Activity activity, int mAppWidgetId){
        this.activity = activity;
        this.mAppWidgetId = mAppWidgetId;
        //There can only be one widget to be create for each adapter , so create an array with only one widget Id to send back to widget for update
    }

    public void onColorSelected(View view) {
        switch(view.getId()) {
            case R.id.color_red:
                color = Color.parseColor("#ffb1b1");
                break;
            case R.id.color_yellow:
                color = Color.parseColor("#fdd782");
                break;
            case R.id.color_green:
                color = Color.parseColor("#a9ddc7");
                break;
            case R.id.color_blue:
                color = Color.parseColor("#95d3ec");
                break;
            case R.id.color_grey:
                color = Color.parseColor("#bbbbbb");
                break;
            case R.id.color_white:
                color = Color.parseColor("#FFFFFF");
                break;
        }
        }


    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView noteTitle, noteTime, noteContent;
        long noteId;

        private ViewHolder(CardView card) {
            super(card);
            cardView = card;
            noteTitle = card.findViewById(R.id.note_title);
            noteTime = card.findViewById(R.id.note_date);
            noteContent = card.findViewById(R.id.note_content);
        }
    }

    @Override
    public WidgetAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        CardView v = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.card_note, parent, false);
        final ViewHolder holder = new ViewHolder(v);


        // Set the result to CANCELED.  This will cause the widget host to cancel out of the widget placement if the user presses the back button.
        activity.setResult(RESULT_CANCELED);

        v.setCardBackgroundColor(Color.WHITE);

        v.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                RemoteViews remoteViews = new RemoteViews("com.jkjk.quicknote", R.layout.note_preview);

                //Obtain correspond data according to the position the user click. As both the recyclerview and cursor are sorted chronically, position equals to cursor index
                cursorForWidget.moveToPosition(holder.getAdapterPosition());
                remoteViews.setTextViewText(R.id.widget_title, cursorForWidget.getString(1));
                remoteViews.setTextViewText(R.id.widget_content, cursorForWidget.getString(2));
                remoteViews.setInt(R.id.widget, "setBackgroundColor", color);

                Intent startAppIntent = new Intent();
                startAppIntent.setClassName("com.jkjk.quicknote", "com.jkjk.quicknote.editscreen.Edit")
                        .putExtra(EXTRA_NOTE_ID, cursorForWidget.getLong(0)).putExtra(IS_FROM_WIDGET, true)
                        .setFlags(FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(activity,(int)cursorForWidget.getLong(0),startAppIntent,PendingIntent.FLAG_UPDATE_CURRENT);
                remoteViews.setOnClickPendingIntent(R.id.widget, pendingIntent);


                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(activity);
                appWidgetManager.updateAppWidget(mAppWidgetId, remoteViews);

                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                activity.setResult(Activity.RESULT_OK, resultValue);

                SharedPreferences idPref = activity.getSharedPreferences("widget_id", MODE_PRIVATE);
                SharedPreferences colorPref = activity.getSharedPreferences("widget_color", MODE_PRIVATE);
                idPref.edit().putLong(Integer.toString(mAppWidgetId), cursorForWidget.getLong(0)).commit();
                colorPref.edit().putInt(Integer.toString(mAppWidgetId), color).commit();

                activity.finish();
            }
        });
        return holder;
    }



    @Override
    public int getItemCount() {
        //Obtain all data from database provided from Application class and get count
        return cursorForWidget.getCount();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.noteTitle.setTypeface(Typeface.SERIF);
        holder.noteTitle.setTextColor(Color.BLACK);
        holder.noteContent.setTextColor(Color.GRAY);

        //Extract data from cursor
        if (cursorForWidget != null) {
            try {
                cursorForWidget.moveToPosition(position);

                holder.noteId = cursorForWidget.getLong(0);

                holder.noteTitle.setText(cursorForWidget.getString(1).trim());

                holder.noteContent.setText(cursorForWidget.getString(2).trim());

                //Time formatting
                long time = (Long.parseLong(cursorForWidget.getString(3)));
                String shownTime;
                // Get current time from Calendar and check how long aga was the note edited
                long timeSpan = Calendar.getInstance().getTimeInMillis() - time;

                if (timeSpan < 300000L){
                    //less than 5 minutes
                    shownTime = holder.cardView.getResources().getString(R.string.just_now);

                } else if (timeSpan < 3600000L){
                    //less than an hour
                    shownTime = DateUtils.getRelativeTimeSpanString(time).toString();

                }else if (DateUtils.isToday(time)) {
                    shownTime = DateUtils.formatDateTime(activity, time, DateUtils.FORMAT_SHOW_TIME);

                } else if (isYesterday(time)){
                    shownTime = holder.cardView.getResources().getString(R.string.yesterday);

                } else {
                    shownTime = DateUtils.formatDateTime(activity, time, DateUtils.FORMAT_SHOW_DATE);
                }
                holder.noteTime.setText(shownTime);

                // Show starred note
                if (cursorForWidget.getInt(4) == 1) {
                    holder.noteTitle.setTypeface(Typeface.SERIF, Typeface.BOLD_ITALIC);
                    holder.noteTitle.setTextColor(Color.parseColor("#0099cc"));
                    holder.noteContent.setTextColor(Color.parseColor("#000000"));
                }

            } catch (Exception e) {
                Toast.makeText(activity, R.string.error_loading, Toast.LENGTH_SHORT).show();
                Log.e(this.getClass().getName(), "error", e);
            }
        }

    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        cursorForWidget.close();
        super.onDetachedFromRecyclerView(recyclerView);
    }


    public void updateCursorForWidget(){
        cursorForWidget = MyApplication.database.query(DATABASE_NAME, new String[]{"_id", "title", "content", "time","starred"}, null, null, null
                , null, "time DESC");
    }

    public void updateCursorForSearchForWidget(String result){
        cursorForWidget = MyApplication.database.query(DATABASE_NAME, new String[]{"_id", "title", "content", "time","starred"}, "_id in ("+result+")", null, null
                , null, "time DESC");
    }

    public void updateCursorForStarred(){
        cursorForWidget = MyApplication.database.query(DATABASE_NAME, new String[]{"_id", "title", "content", "time","starred"}, "starred = 1", null, null
                , null, "time DESC");
    }

    private boolean isYesterday(long time) {
        int currentDayOfYear, currentYear, setTimeDayOfYear, setTimeYear, setTimeMonth, setTimeDay;

        Calendar dateOfSetTime = Calendar.getInstance();
        Calendar currentTime = Calendar.getInstance();
        dateOfSetTime.setTimeInMillis(time);

        currentDayOfYear = currentTime.get(Calendar.DAY_OF_YEAR);
        currentYear = currentTime.get(Calendar.YEAR);
        setTimeDayOfYear = dateOfSetTime.get(Calendar.DAY_OF_YEAR);
        setTimeYear = dateOfSetTime.get(Calendar.YEAR);
        setTimeMonth = dateOfSetTime.get(Calendar.MONTH);
        setTimeDay = dateOfSetTime.get(Calendar.DAY_OF_MONTH);

        return (currentDayOfYear == 1 && setTimeYear == currentYear - 1 && setTimeMonth == 11 && setTimeDay == 31) || (setTimeDayOfYear == currentDayOfYear - 1 && setTimeYear == currentYear);
    }
}
