package com.jkjk.quicknote.taskeditscreen;


import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.jkjk.quicknote.MyApplication;
import com.jkjk.quicknote.R;

import org.w3c.dom.Text;

import java.text.DateFormat;
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
    private EditText titleInFragment, remarkInFragment;
    private TextView dateInFragment, timeInFragment;
    private Switch markAsDoneInFragment;
    private boolean newTask;
    // 0 stands for not starred, 1 starred
    private int selectUrgency = 0;
    private Calendar taskDate;
    private boolean dateChange = false, timeChanged = false;

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
        markAsDoneInFragment = view.findViewById(R.id.task_done);
        dateInFragment = view.findViewById(R.id.task_date);
        timeInFragment = view.findViewById(R.id.task_time);

        Spinner urgencyInFragment = view.findViewById(R.id.task_urgency);
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

        taskDate = Calendar.getInstance();

        if (!newTask) {
            try {
                Cursor taskCursor = MyApplication.database.query(DATABASE_NAME, new String[]{"title", "content", "time","urgency","done"}, "_id='" + taskId +"'",
                        null, null, null, null, null);
                taskCursor.moveToFirst();
                titleInFragment.setText(taskCursor.getString(0));
                remarkInFragment.setText(taskCursor.getString(1));

                // read time to date and time field
                if (taskCursor.getLong(2) != 0L) {
                    taskDate.setTimeInMillis(taskCursor.getLong(2));
                    DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
                    dateInFragment.setText(dateFormat.format(taskDate.getTime()));

                    // to see if the save time is default or not. If default (999), time will not be shown
                    if (taskDate.get(Calendar.MILLISECOND)!=999) {
                        DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
                        timeInFragment.setText(timeFormat.format(taskDate.getTime()));
                        setTimePicker(timeInFragment, taskDate);
                    }
                }

                urgencyInFragment.setSelection(taskCursor.getInt(3));
                if (taskCursor.getInt(4)==1) {
                    markAsDoneInFragment.setChecked(true);
                } else markAsDoneInFragment.setChecked(false);

                taskCursor.close();

            } catch (Exception e) {
                Toast.makeText(container.getContext(), R.string.error_loading, Toast.LENGTH_SHORT).show();
                Log.e(this.getClass().getName(), "error", e);
            }
        }

        dateInFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker,int year, int month, int dayOfMonth) {
                        taskDate.set(Calendar.YEAR, year);
                        taskDate.set(Calendar.MONTH, month);
                        taskDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
                        dateInFragment.setText(dateFormat.format(taskDate.getTime()));
                        setTimePicker(timeInFragment, taskDate);
                        dateChange = true;
                    }
                }, taskDate.get(Calendar.YEAR), taskDate.get(Calendar.MONTH), taskDate.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        return view;
    }

    private void setTimePicker(final TextView textView, final Calendar calendar) {
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute){
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
                        textView.setText(timeFormat.format(calendar.getTime()));
                        timeChanged = true;
                }
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
            }
        });
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

        // if date is not set, save the time as 0
        if (!dateChange) {
            values.put("time", 0L);
            Log.e("Date        ", "0");
        } else {
            if(!timeChanged){
                // if time is not set by user, set the millisecond to 999 to indicate so that we can determine if we should show the time in time field when user comeback
                taskDate.set(Calendar.MILLISECOND, 999);

                Log.e("time        ", "999");
            }
            values.put("time", Long.toString(taskDate.getTimeInMillis()));
            Log.e("time        ", ((String)values.get("time")));
        }

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
        Toast.makeText(getActivity(),R.string.saved_task, Toast.LENGTH_SHORT).show();
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
