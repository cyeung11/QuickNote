package com.jkjk.quicknote.listscreen;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jkjk.quicknote.R;
import com.jkjk.quicknote.noteeditscreen.NoteEditFragment;

public abstract class ItemListFragment extends Fragment {

    ItemListAdapter itemListAdapter;
    TextView notFoundTextView;
    RecyclerView recyclerView;

    protected abstract int getLayoutId();

    protected abstract Class getEditActivityClass();

    protected abstract ItemListAdapter initAdapter();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_note_list, container, false);
        notFoundTextView = view.findViewById(R.id.result_not_found);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        itemListAdapter = initAdapter();
        itemListAdapter.updateCursor();
        recyclerView.setAdapter(itemListAdapter);

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        itemListAdapter.updateCursor();
        itemListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        if (itemListAdapter.actionMode !=null) {
            itemListAdapter.actionMode.finish();
        }
        super.onStop();
    }

    void toggleNoResult(boolean noResult){
        if (recyclerView != null) {
            recyclerView.setVisibility(noResult ? View.INVISIBLE : View.VISIBLE);
        }
        if (notFoundTextView != null) {
            notFoundTextView.setVisibility(noResult ? View.VISIBLE : View.INVISIBLE);
        }
    }

    void onSearch(String query, String result){
        //If search result is not empty, update the cursor to show result
        if (!result.equals("")) {
            itemListAdapter.updateCursorForSearch(result);
            itemListAdapter.notifyDataSetChanged();
            toggleNoResult(false);

        } else if (!query.equals("")) {
            //If search result is empty and the search input is not empty, show result not found
            toggleNoResult(true);

        } else {
            //after finish searching and user empty the search input, reset the view
            resetList();
            itemListAdapter.notifyDataSetChanged();
            toggleNoResult(false);
        }
    }

    void closeActionMode(){
        if (itemListAdapter != null && itemListAdapter.actionMode != null) {
            itemListAdapter.actionMode.finish();
        }
    }

    protected abstract void resetList();

    public void onItemEdit(long itemId) {
        Intent editIntent = new Intent(getContext(), getEditActivityClass());
        editIntent.putExtra(NoteEditFragment.EXTRA_ITEM_ID, itemId);
        startActivity(editIntent);
    }

    public void onItemEdit() {
        Intent editIntent = new Intent(getContext(), getEditActivityClass());
        startActivity(editIntent);
    }

    public ItemListAdapter getItemListAdapter(){
        return itemListAdapter;
    }

}
