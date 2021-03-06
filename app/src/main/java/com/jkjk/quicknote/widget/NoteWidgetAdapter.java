package com.jkjk.quicknote.widget;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.cardview.widget.CardView;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.jkjk.quicknote.R;
import com.jkjk.quicknote.listscreen.ItemListAdapter;
import com.jkjk.quicknote.noteeditscreen.Note;
import com.jkjk.quicknote.noteeditscreen.NoteEdit;

import java.util.ArrayList;
import java.util.Calendar;

import static android.app.Activity.RESULT_CANCELED;
import static android.content.Context.MODE_PRIVATE;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import static com.jkjk.quicknote.noteeditscreen.NoteEditFragment.EXTRA_ITEM_ID;

public class NoteWidgetAdapter extends ItemListAdapter {

    private Activity activity;
    private int mAppWidgetId, color;
    private String widgetSize;

    private boolean showingStarred = false;

    private ArrayList<Note> notes = new ArrayList<>();

    NoteWidgetAdapter(Activity activity, int mAppWidgetId){
        this.activity = activity;

        // Set the result to CANCELED.  This will cause the widget host to cancel out of the widget placement if the user presses the back button.
        activity.setResult(RESULT_CANCELED);

        this.mAppWidgetId = mAppWidgetId;

        // Obtain correspond value from preferences to show appropriate size for the card view
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        String cardViewSize = sharedPref.getString(activity.getString(R.string.font_size_main_screen),"m");
        widgetSize = sharedPref.getString(activity.getString(R.string.font_size_widget),"m");
        switch (cardViewSize) {
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
        color = Color.parseColor("#FFFFFF");
    }

    class ViewHolder extends ItemListAdapter.ViewHolder {
        TextView noteContent;
        Note note;

        private ViewHolder(CardView card) {
            super(card);
            noteContent = card.findViewById(R.id.note_content);
        }
    }

    @NonNull
    @Override
    public NoteWidgetAdapter.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        CardView v = (CardView) LayoutInflater.from(parent.getContext()).inflate(cardViewInt, parent, false);
        final ViewHolder holder = new ViewHolder(v);

        v.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                RemoteViews remoteViews = new RemoteViews(view.getContext().getPackageName(), R.layout.note_preview_widget);

                //Obtain correspond data according to the position the user click. As both the recyclerview and cursor are sorted chronically, position equals to cursor index
                if (holder.note.getId() == null) {
                    return;
                }
                remoteViews.setTextViewText(R.id.widget_title, holder.note.getTitle());
                remoteViews.setTextViewText(R.id.widget_content, holder.note.getContent());
                remoteViews.setInt(R.id.widget, "setBackgroundColor", color);

                switch (widgetSize){
                    case "s":
                        remoteViews.setTextViewTextSize(R.id.widget_title, TypedValue.COMPLEX_UNIT_SP,16);
                        remoteViews.setTextViewTextSize(R.id.widget_content, TypedValue.COMPLEX_UNIT_SP,14);
                        break;
                    case "m":
                        remoteViews.setTextViewTextSize(R.id.widget_title, TypedValue.COMPLEX_UNIT_SP,18);
                        remoteViews.setTextViewTextSize(R.id.widget_content, TypedValue.COMPLEX_UNIT_SP,16);
                        break;
                    case "l":
                        remoteViews.setTextViewTextSize(R.id.widget_title, TypedValue.COMPLEX_UNIT_SP,22);
                        remoteViews.setTextViewTextSize(R.id.widget_content, TypedValue.COMPLEX_UNIT_SP,20);
                        break;
                    case "xl":
                        remoteViews.setTextViewTextSize(R.id.widget_title, TypedValue.COMPLEX_UNIT_SP,26);
                        remoteViews.setTextViewTextSize(R.id.widget_content, TypedValue.COMPLEX_UNIT_SP,24);
                        break;
                    default:
                        remoteViews.setTextViewTextSize(R.id.widget_title, TypedValue.COMPLEX_UNIT_SP,18);
                        remoteViews.setTextViewTextSize(R.id.widget_content, TypedValue.COMPLEX_UNIT_SP,16);
                }

                Intent startAppIntent = new Intent(activity, NoteEdit.class);
                startAppIntent.putExtra(EXTRA_ITEM_ID, holder.note.getId())
                        .setFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(activity, holder.note.getId().intValue(), startAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                remoteViews.setOnClickPendingIntent(R.id.widget, pendingIntent);


                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(activity);
                appWidgetManager.updateAppWidget(mAppWidgetId, remoteViews);

                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                activity.setResult(Activity.RESULT_OK, resultValue);

                SharedPreferences idPref = activity.getSharedPreferences("widget_id", MODE_PRIVATE);
                SharedPreferences colorPref = activity.getSharedPreferences("widget_color", MODE_PRIVATE);
                idPref.edit().putLong(Integer.toString(mAppWidgetId), holder.note.getId().intValue()).apply();
                colorPref.edit().putInt(Integer.toString(mAppWidgetId), color).apply();
                activity.finish();
            }
        });
        return holder;
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    @Override
    public void onBindViewHolder(@NonNull ItemListAdapter.ViewHolder viewHolder, int position) {

        ViewHolder holder = (ViewHolder)viewHolder;

        holder.note = notes.get(position);

        holder.itemTitle.setText(holder.note.getTitle().trim());

        holder.noteContent.setText(holder.note.getContent().trim());

        //Time formatting
        long time = (holder.note.getEditTime().getTimeInMillis());
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
    public void updateCursor(){
        showingStarred = false;
        notes = Note.Companion.getAllNotes(activity);
    }

    @Override
    public void updateCursorForSearch(String result){
        notes = Note.Companion.getAllNotes(activity, showingStarred, result);
    }

    void updateCursorForStarred(){
        showingStarred = true;
        notes = Note.Companion.getAllNotes(activity, true);
    }

    void onColorSelected(View view) {
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
}
