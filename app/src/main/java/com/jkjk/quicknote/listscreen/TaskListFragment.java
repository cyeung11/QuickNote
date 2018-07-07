package com.jkjk.quicknote.listscreen;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jkjk.quicknote.R;
import com.jkjk.quicknote.taskeditscreen.TaskEdit;

import static com.jkjk.quicknote.noteeditscreen.NoteEditFragment.EXTRA_ITEM_ID;

/**
 * A simple {@link Fragment} subclass.
 */
public class TaskListFragment extends Fragment {

    TaskListAdapter taskListAdapter;
    TextView notFoundTextView;
    boolean showingStarred = false;
    RecyclerView recyclerView;

    public TaskListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        taskListAdapter = new TaskListAdapter(this);
        taskListAdapter.updateCursor();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_task_list, container, false);
        notFoundTextView = view.findViewById(R.id.result_not_found);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setAdapter(taskListAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        ListFragment hostFragment = (ListFragment)getParentFragment();

        if(hostFragment != null) {
            hostFragment.updateTaskListFragment(getTag());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        taskListAdapter.updateCursor();
        taskListAdapter.notifyDataSetChanged();
    }


    public void onTaskEdit(long taskId) {
        Intent startTaskActivity = new Intent(getContext(), TaskEdit.class);
        startTaskActivity.putExtra(EXTRA_ITEM_ID, taskId);
        startActivity(startTaskActivity);
    }

    public void onTaskEdit() {
        Intent startTaskActivity = new Intent(getContext(), TaskEdit.class);
        startActivity(startTaskActivity);
    }

    public TaskListAdapter getTaskListAdapter(){
        return taskListAdapter;
    }

}
