package com.jkjk.quicknote.listscreen;


import com.jkjk.quicknote.R;
import com.jkjk.quicknote.taskeditscreen.TaskEdit;


public class TaskListFragment extends ItemListFragment {


    public TaskListFragment() {
        // Required empty public constructor
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_task_list;
    }

    @Override
    protected Class getEditActivityClass() {
        return TaskEdit.class;
    }

    @Override
    protected ItemListAdapter initAdapter() {
        return new TaskListAdapter(getContext(), this);
    }

    @Override
    protected void resetList() {
        if (((TaskListAdapter)itemListAdapter).showingDone) {
            ((TaskListAdapter)itemListAdapter).updateCursorForDone();
        } else {
            itemListAdapter.updateCursor();
        }
    }
}
