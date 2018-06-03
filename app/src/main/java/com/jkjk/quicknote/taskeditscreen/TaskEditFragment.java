package com.jkjk.quicknote.taskeditscreen;


import android.content.ContentValues;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.jkjk.quicknote.MyApplication;
import com.jkjk.quicknote.R;

import java.util.Calendar;

import static com.jkjk.quicknote.helper.DatabaseHelper.DATABASE_NAME;
import static com.jkjk.quicknote.noteeditscreen.NoteEditFragment.EXTRA_NOTE_ID;

/**
 * A simple {@link Fragment} subclass.
 */
public class TaskEditFragment extends Fragment {

    private static final String TASK_ID = "taskId";
    boolean hasTaskSave = false;
    private long taskId;
    private EditText titleInFragment, remarkInFragment, dateInFragment, timeInFragment;
    private Switch markAsDoneInFragment;
    private Spinner urgencyInFragment;
    private boolean newTask;
    // 0 stands for not starred, 1 starred
    private int selectUrgency = 0;

    public TaskEditFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_task_edit, container, false);
        titleInFragment = view.findViewById(R.id.task_title);
        remarkInFragment = view.findViewById(R.id.task_remark);
        dateInFragment = view.findViewById(R.id.task_date);
        timeInFragment = view.findViewById(R.id.task_time);
        markAsDoneInFragment = view.findViewById(R.id.task_done);
        urgencyInFragment = view.findViewById(R.id.task_urgency);

        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(getContext(),R.array.urgency_list, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        urgencyInFragment.setAdapter(arrayAdapter);
        urgencyInFragment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectUrgency = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selectUrgency = 0;
            }
        });

        if (savedInstanceState !=null) {
            // case when restoring from saved instance
            taskId = savedInstanceState.getLong(TASK_ID, 0L);
            newTask = false;

        }else if (getArguments() != null) {

            // case when argument has data, either note ID from the note list activity or text from external intent
            taskId = getArguments().getLong(EXTRA_NOTE_ID, 999999999L);
            newTask = (taskId == 999999999L);

        } else {
            newTask = true;}

        if (!newTask) {
            try {
                Cursor tempNote = MyApplication.database.query(DATABASE_NAME, new String[]{"title", "content", "time","urgency","done"}, "_id='" + taskId +"'",
                        null, null, null, null, null);
                tempNote.moveToFirst();
                titleInFragment.setText(tempNote.getString(0));
                remarkInFragment.setText(tempNote.getString(1));
                //TODO time
                urgencyInFragment.setSelection(tempNote.getInt(3));
                if (tempNote.getInt(4)==1) {
                    markAsDoneInFragment.setChecked(true);
                } else markAsDoneInFragment.setChecked(false);

                tempNote.close();

            } catch (Exception e) {
                Toast.makeText(container.getContext(), R.string.error_loading, Toast.LENGTH_SHORT).show();
                Log.e(this.getClass().getName(), "error", e);
            }
        }


        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Define save function for the done button
        FloatingActionButton done = getActivity().findViewById(R.id.done_fab);
        done.setImageDrawable(getResources().getDrawable(R.drawable.sharp_done_24));
        done.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.highlight)));
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveTask();
                Toast.makeText(getContext(),R.string.saved, Toast.LENGTH_SHORT).show();
                hasTaskSave = true;
                getActivity().finish();
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(TASK_ID, taskId);
    }

    @Override
    public void onPause() {
        //when user quit the app without choosing save or discard, save the task
        if (!hasTaskSave){
            saveTask();
            Toast.makeText(getActivity(),R.string.saved, Toast.LENGTH_SHORT).show();
        }
        // then reset it to not saved for the case when user come back
        hasTaskSave = false;
        super.onPause();
    }

    public void saveTask(){
        ContentValues values = new ContentValues();

        String noteTitle = titleInFragment.getText().toString().trim();
        if (noteTitle.length()<1){
            noteTitle = getString(R.string.untitled_task);
        }
        values.put("title", noteTitle);

        values.put("content", remarkInFragment.getText().toString());

        Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.DAY_OF_MONTH, );
        values.put("time", Long.toString(Calendar.getInstance().getTimeInMillis()));


        values.put("starred", 0);
        values.put("type", 1);
        values.put("urgency", selectUrgency);

        if (markAsDoneInFragment.isChecked()) {
            values.put("done", 1);
        } else values.put("done", 0);

        if (!newTask) {
            MyApplication.database.update(DATABASE_NAME, values, "_id='" + taskId +"'", null);
        }else {
            taskId = MyApplication.database.insert(DATABASE_NAME, "",values);
        }
        values.clear();
        hasTaskSave = true;
        newTask = false;
    }

    public static TaskEditFragment newEditFragmentInstance(long taskId){
        TaskEditFragment fragment = new TaskEditFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(EXTRA_NOTE_ID, taskId);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static TaskEditFragment newEditFragmentInstance(){
        return new TaskEditFragment();
    }
}
