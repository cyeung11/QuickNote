package com.jkjk.quicknote.listscreen;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jkjk.quicknote.R;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.ViewHolder> {

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView noteTitle, noteTime, noteContent;
        long noteId;
        boolean isStarred;

        private ViewHolder(CardView card) {
            super(card);
            cardView = card;
            noteTitle = card.findViewById(R.id.note_title);
            noteTime = card.findViewById(R.id.note_date);
            noteContent = card.findViewById(R.id.note_content);
        }
    }


    @NonNull
    @Override
    public TaskListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull TaskListAdapter.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }
}
