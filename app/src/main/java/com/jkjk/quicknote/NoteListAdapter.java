package com.jkjk.quicknote;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.jkjk.quicknote.Interface.OnNoteEdit;

import java.util.Calendar;

import static com.jkjk.quicknote.DatabaseHelper.DATABASE_NAME;
import static com.jkjk.quicknote.DatabaseHelper.QUERY_STRING;


public class NoteListAdapter extends RecyclerView.Adapter<NoteListAdapter.ViewHolder> {

    Cursor cursor = null;


    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView noteTitle, noteTime, noteContent;

        public ViewHolder(CardView card) {
            super(card);
            cardView = card;
            noteTitle = (TextView) card.findViewById(R.id.note_title);
            noteTime = (TextView) card.findViewById(R.id.note_date);
            noteContent = (TextView) card.findViewById(R.id.note_content);
        }
    }

    @Override
    public NoteListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView v = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.card_note, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public int getItemCount() {
        //Obtain all data from database provided from Application class and get count
        cursor = MyApplication.getDatabase().rawQuery(QUERY_STRING, null);
        return cursor.getCount();
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        //Retreat data from specific row from database provided from Application class to the cursor
        final Context context = holder.cardView.getContext();

        Calendar calendar = Calendar.getInstance();

        cursor = MyApplication.getDatabase().query(DATABASE_NAME, new String[]{"title", "content", "time"}, "_id" + "=?",
                new String[]{String.valueOf(holder.getAdapterPosition() + 1)}, null, null, null, null);

        //Extract data from cursor
        if (cursor != null) {
            try {
                cursor.moveToFirst();
                holder.noteTitle.setText(cursor.getString(0));

                //Time formatting
                Long time = (Long.parseLong(cursor.getString(2)));
                Long yesterday = calendar.getTimeInMillis() - DateUtils.DAY_IN_MILLIS;
                String shownTime;
                if (DateUtils.isToday(time)) {
                    shownTime = DateUtils.formatDateTime(context, time, DateUtils.FORMAT_SHOW_TIME);
                } else if (isYesterday(time)){
                    shownTime = holder.cardView.getResources().getString(R.string.yesterday);
                } else {
                    shownTime = DateUtils.formatDateTime(context, time, DateUtils.FORMAT_SHOW_DATE);
                }
                holder.noteTime.setText(shownTime);

                holder.noteContent.setText(cursor.getString(1));
            } catch (Exception e) {
                Toast.makeText(context, R.string.error_loading, Toast.LENGTH_SHORT).show();
                Log.e(this.getClass().getName(), "error", e);
            }
        }
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((OnNoteEdit) context).onNoteEdit(holder.getAdapterPosition());
            }
        });
    }

    private boolean isYesterday(Long setTime) {
        boolean result;
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
}

