package com.jkjk.quicknote.Adapter;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
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
import static com.jkjk.quicknote.Adapter.NoteListAdapter.isYesterday;
import static com.jkjk.quicknote.DatabaseHelper.DATABASE_NAME;

public class WidgetAdapter extends RecyclerView.Adapter<WidgetAdapter.ViewHolder> {

    private Activity activity;
    private int mAppWidgetId;
    private static Cursor cursorForWidget;


    public WidgetAdapter(Activity activity, int mAppWidgetId){
        this.activity = activity;
        this.mAppWidgetId = mAppWidgetId;
        //There can only be one widget to be create for each adapter , so create an array with only one widget Id to send back to widget for update
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView noteTitle, noteTime, noteContent;
        long noteId;

        private ViewHolder(CardView card) {
            super(card);
            cardView = card;
            noteTitle = (TextView) card.findViewById(R.id.note_title);
            noteTime = (TextView) card.findViewById(R.id.note_date);
            noteContent = (TextView) card.findViewById(R.id.note_content);
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
                final Context context = activity;

                RemoteViews remoteViews = new RemoteViews("com.jkjk.quicknote", R.layout.note_preview);

                //Obtain correspond data according to the position the user click. As both the recyclerview and cursor are sorted chronically, position equals to cursor index
                cursorForWidget.moveToPosition(holder.getAdapterPosition());
                remoteViews.setTextViewText(R.id.widget_title, cursorForWidget.getString(1));
                remoteViews.setTextViewText(R.id.widget_content, cursorForWidget.getString(2));

                Intent startAppIntent = new Intent();
                startAppIntent.setClassName("com.jkjk.quicknote", "com.jkjk.quicknote.NoteList");
                PendingIntent pendingIntent = PendingIntent.getActivity(context,0,startAppIntent,PendingIntent.FLAG_UPDATE_CURRENT);
                remoteViews.setOnClickPendingIntent(R.id.widget, pendingIntent);

                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                appWidgetManager.updateAppWidget(mAppWidgetId, remoteViews);

                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                activity.setResult(Activity.RESULT_OK, resultValue);

                SharedPreferences pref = context.getSharedPreferences("widget", MODE_PRIVATE);
                pref.edit().putLong(Integer.toString(mAppWidgetId), cursorForWidget.getLong(0)).commit();

                activity.finish();
            }
        });
        return holder;
    }



    @Override
    public int getItemCount() {
        //Obtain all data from database provided from Application class and get count
        return updateCursorForWidget().getCount();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final Context context = holder.cardView.getContext();

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

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        cursorForWidget.close();
        super.onDetachedFromRecyclerView(recyclerView);
    }


    private static Cursor updateCursorForWidget(){
        cursorForWidget = MyApplication.getDatabase().query(DATABASE_NAME, new String[]{"_id", "title", "content", "time"}, null, null, null
                , null, "time DESC");
        return cursorForWidget;
    }
}
