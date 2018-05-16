package com.jkjk.quicknote.Adapter;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

import com.jkjk.quicknote.MyApplication;
import com.jkjk.quicknote.R;
import com.jkjk.quicknote.Service.AppWidgetService;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static com.jkjk.quicknote.Adapter.NoteListAdapter.isYesterday;
import static com.jkjk.quicknote.DatabaseHelper.DATABASE_NAME;
import static com.jkjk.quicknote.Widget.NoteWidget.SELECT_NOTE_ID;

public class WidgetAdapter extends RecyclerView.Adapter<WidgetAdapter.ViewHolder> {

    private int itemCount;
    Activity activity;
    int mAppWidgetId;
    private static Cursor cursorForWidget;
    public static final String NEW_WIDGET = "isNewWidget";


    public WidgetAdapter(Activity activity, int mAppWidgetId){
        this.activity = activity;
        this.mAppWidgetId = mAppWidgetId;
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
    public WidgetAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView v = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.card_note, parent, false);
        final ViewHolder holder = new ViewHolder(v);

        v.setCardBackgroundColor(Color.WHITE);

        v.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                final Context context = activity;

                cursorForWidget.moveToPosition(holder.getAdapterPosition());
                long id = cursorForWidget.getLong(0);

                Intent intent = new Intent(context,AppWidgetService.class);
                intent.putExtra(EXTRA_APPWIDGET_ID, mAppWidgetId);
                intent.putExtra(SELECT_NOTE_ID, id);
                intent.putExtra(NEW_WIDGET, true);
                PendingIntent pendingIntent = PendingIntent.getService(context,12546345,intent,PendingIntent.FLAG_UPDATE_CURRENT);
                try { pendingIntent.send();
                } catch (Exception e){
                    Toast.makeText(context, R.string.error_text, Toast.LENGTH_SHORT).show();
                    Log.e(this.getClass().getName(),"error",e);
                }

                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                activity.setResult(Activity.RESULT_OK, resultValue);

                activity.finish();
            }
        });
        return holder;
    }



    @Override
    public int getItemCount() {
        //Obtain all data from database provided from Application class and get count
        itemCount = updateCursorForWidget().getCount();
        return itemCount;
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
                Long time = (Long.parseLong(cursorForWidget.getString(3)));
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

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        cursorForWidget.close();
        super.onDetachedFromRecyclerView(recyclerView);
    }


    public static Cursor updateCursorForWidget(){
        cursorForWidget = MyApplication.getDatabase().query(DATABASE_NAME, new String[]{"_id", "title", "content", "time"}, null, null, null
                , null, "time DESC");
        return cursorForWidget;
    }
}
