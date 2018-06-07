package com.jkjk.quicknote.taskeditscreen;


import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.jkjk.quicknote.MyApplication;
import com.jkjk.quicknote.R;
import com.jkjk.quicknote.helper.AlarmHelper;

import java.sql.Array;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static com.jkjk.quicknote.helper.DatabaseHelper.DATABASE_NAME;
import static com.jkjk.quicknote.noteeditscreen.NoteEditFragment.DEFAULT_NOTE_ID;
import static com.jkjk.quicknote.noteeditscreen.NoteEditFragment.EXTRA_NOTE_ID;

public class TaskEditFragment extends Fragment {

    private static final String TASK_ID = "taskId";
    public static final int TIME_NOT_SET_MILLISECOND_INDICATOR = 999;
    public static final int TIME_NOT_SET_MINUTE_SECOND_INDICATOR = 59;
    public static final int TIME_NOT_SET_HOUR_INDICATOR = 23;
    public static final long DATE_NOT_SET_INDICATOR = 9999999999999L;

    boolean hasTaskSave = false;
    private long taskId;
    private EditText titleInFragment, remarkInFragment;
    private TextView dateInFragment, timeInFragment;
    private Switch markAsDoneInFragment;
    private boolean newTask, isDone, dateChange = false, timeChanged = false, hasModified = false;
    private Calendar taskDate;
    private String title, remark;
    private Spinner urgencyInFragment, reminderInFragment;

    public TaskEditFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Obtain correspond value from preferences to show appropriate size for the view
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String editViewSize = sharedPref.getString(getResources().getString(R.string.font_size_editing_screen),"m");
        int editViewInt;
        int spinnerDropDownInt;
        int spinnerItemInt;
        switch (editViewSize){
            case ("s"):
                editViewInt = R.layout.fragment_task_edit_s;
                spinnerDropDownInt = R.layout.spinner_drop_down_s;
                spinnerItemInt = R.layout.spinner_item_s;
                break;
            case ("m"):
                editViewInt = R.layout.fragment_task_edit_m;
                spinnerDropDownInt = android.R.layout.simple_spinner_dropdown_item;
                spinnerItemInt = android.R.layout.simple_spinner_item;
                break;
            case ("l"):
                editViewInt = R.layout.fragment_task_edit_l;
                spinnerDropDownInt = R.layout.spinner_drop_down_l;
                spinnerItemInt = R.layout.spinner_item_l;
                break;
            case ("xl"):
                editViewInt = R.layout.fragment_task_edit_xl;
                spinnerDropDownInt = R.layout.spinner_drop_down_xl;
                spinnerItemInt = R.layout.spinner_item_xl;
                break;
            default:
                editViewInt = R.layout.fragment_task_edit_m;
                spinnerDropDownInt = android.R.layout.simple_spinner_dropdown_item;
                spinnerItemInt = android.R.layout.simple_spinner_item;
        }


        // Inflate the layout for this fragment
        View view = inflater.inflate(editViewInt, container, false);
        titleInFragment = view.findViewById(R.id.task_title);
        remarkInFragment = view.findViewById(R.id.task_remark);
        markAsDoneInFragment = view.findViewById(R.id.task_done);
        dateInFragment = view.findViewById(R.id.task_date);
        timeInFragment = view.findViewById(R.id.task_time);

        if (savedInstanceState !=null) {
            // case when restoring from saved instance
            taskId = savedInstanceState.getLong(TASK_ID, 0L);
            newTask = false;

        }else if (getArguments() != null) {

            // case when argument has data, either note ID from the note list activity or text from external intent
            taskId = getArguments().getLong(EXTRA_NOTE_ID, DEFAULT_NOTE_ID);
            newTask = (taskId == DEFAULT_NOTE_ID);

        } else {
            newTask = true;}

        taskDate = Calendar.getInstance();


        urgencyInFragment = view.findViewById(R.id.task_urgency);
        ArrayAdapter<CharSequence> urgencyAdapter = ArrayAdapter.createFromResource(getContext(),R.array.urgency_list, spinnerItemInt);
        urgencyAdapter.setDropDownViewResource(spinnerDropDownInt);
        urgencyInFragment.setAdapter(urgencyAdapter);
        urgencyInFragment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (view.isPressed()){
                    hasModified = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        List<String> reminderArray = Arrays.asList(getResources().getStringArray(R.array.reminder_list));

        reminderInFragment = view.findViewById(R.id.task_reminder);
        ArrayAdapter<String> reminderAdapter = new ArrayAdapter<>(getContext(), spinnerItemInt, reminderArray);
        reminderAdapter.setDropDownViewResource(spinnerDropDownInt);
        reminderInFragment.setAdapter(reminderAdapter);
        reminderInFragment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (view.isPressed()){
                    hasModified = true;
                }
                // if select other, show date & time picker
                if (i == 5){
                    final Calendar reminderTime = Calendar.getInstance();
                    new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker datePicker,int year, int month, int dayOfMonth) {
                            reminderTime.set(Calendar.YEAR, year);
                            reminderTime.set(Calendar.MONTH, month);
                            reminderTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                            new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute){
                                    reminderTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                    reminderTime.set(Calendar.MINUTE, minute);
                                    
                                    //TODO insert select time into spinner
                                }
                            }, reminderTime.get(Calendar.HOUR_OF_DAY), reminderTime.get(Calendar.MINUTE), false).show();
                        }

                    }, reminderTime.get(Calendar.YEAR), reminderTime.get(Calendar.MONTH), reminderTime.get(Calendar.DAY_OF_MONTH)).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if (!newTask) {
            try {
                Cursor taskCursor = MyApplication.database.query(DATABASE_NAME, new String[]{"title", "content", "time","urgency","done"}, "_id='" + taskId +"'",
                        null, null, null, null, null);
                taskCursor.moveToFirst();

                titleInFragment.setText(taskCursor.getString(0));
                remarkInFragment.setText(taskCursor.getString(1));

                // read time to date and time field
                if (!taskCursor.getString(2).equals(Long.toString(DATE_NOT_SET_INDICATOR))) {
                    taskDate.setTimeInMillis(Long.valueOf(taskCursor.getString(2)));
                    DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
                    dateInFragment.setText(dateFormat.format(taskDate.getTime()));
                    dateChange = true;
                    timeInFragment.setVisibility(View.VISIBLE);

                    // to see if the save time is default or not. If default (999), time will be shown as not set
                    if (taskDate.get(Calendar.MILLISECOND)!= TIME_NOT_SET_MILLISECOND_INDICATOR
                            && taskDate.get(Calendar.SECOND) != TIME_NOT_SET_MINUTE_SECOND_INDICATOR
                            && taskDate.get(Calendar.MINUTE) != TIME_NOT_SET_MINUTE_SECOND_INDICATOR
                            && taskDate.get(Calendar.HOUR_OF_DAY) != TIME_NOT_SET_HOUR_INDICATOR) {
                        DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
                        timeInFragment.setText(timeFormat.format(taskDate.getTime()));
                        timeChanged = true;
                    }
                }

                urgencyInFragment.setSelection(taskCursor.getInt(3));

                if (taskCursor.getInt(4)==1) {
                    markAsDoneInFragment.setChecked(true);
                } else {
                    markAsDoneInFragment.setChecked(false);
                }

                taskCursor.close();

            } catch (Exception e) {
                Toast.makeText(container.getContext(), R.string.error_loading, Toast.LENGTH_SHORT).show();
                Log.e(this.getClass().getName(), "loading task from cursor       ", e);
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
                        timeInFragment.setVisibility(View.VISIBLE);
                        dateChange = true;
                        hasModified = true;
                    }
                }, taskDate.get(Calendar.YEAR), taskDate.get(Calendar.MONTH), taskDate.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        timeInFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute){
                        taskDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        taskDate.set(Calendar.MINUTE, minute);
                        DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
                        timeInFragment.setText(timeFormat.format(taskDate.getTime()));
                        timeChanged = true;
                        hasModified = true;
                    }
                }, taskDate.get(Calendar.HOUR_OF_DAY), taskDate.get(Calendar.MINUTE), false).show();
            }
        });

        final ImageButton showDropMenu = view.findViewById(R.id.task_show_drop_menu);
        showDropMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu editDropMenu = new PopupMenu(view.getContext(),showDropMenu);
                editDropMenu.inflate(R.menu.task_edit_drop_menu);
                editDropMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()){
                            case R.id.edit_drop_menu_delete:
                                //Delete task
                                new AlertDialog.Builder(getContext()).setTitle(R.string.delete_title).setMessage(R.string.confirm_delete_edit)
                                        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        if (!newTask) {
                                                            MyApplication.database.delete(DATABASE_NAME, "_id='" + taskId + "'", null);
                                                        }
                                                        // No need to do saving
                                                        hasTaskSave = true;
                                                        Toast.makeText(getContext(), R.string.note_deleted_toast, Toast.LENGTH_SHORT).show();
                                                        getActivity().finish();
                                                    }
                                                }
                                        )
                                        .setNegativeButton(R.string.cancel, null)
                                        .show();
                                return true;
                            case R.id.edit_drop_menu_share:
                                Intent shareIntent = new Intent();
                                shareIntent.setAction(Intent.ACTION_SEND);
                                shareIntent.setType("text/plain");
                                StringBuilder stringBuilder = new StringBuilder();
                                if (markAsDoneInFragment.isChecked()){
                                    stringBuilder.append("[").append(getString(R.string.done)).append("] ");
                                } else if (urgencyInFragment.getSelectedItemPosition()==1){
                                    stringBuilder.append("[").append(getString(R.string.important)).append("] ");
                                } else if (urgencyInFragment.getSelectedItemPosition()==2){
                                    stringBuilder.append("[").append(getString(R.string.asap)).append("] ");
                                }
                                stringBuilder.append(titleInFragment.getText().toString());
                                if (dateChange){
                                    stringBuilder.append("\n").append(dateInFragment.getText());
                                }
                                if (timeChanged){
                                    stringBuilder.append(" ").append(timeInFragment.getText().toString());
                                }
                                stringBuilder.append("\n").append(remarkInFragment.getText().toString());
                                shareIntent.putExtra(Intent.EXTRA_TEXT,stringBuilder.toString());
                                shareIntent.putExtra(Intent.EXTRA_TITLE, titleInFragment.getText());
                                startActivity(shareIntent);
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                editDropMenu.show();
            }
        });


        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Define save function for the done button
        FloatingActionButton done = getActivity().findViewById(R.id.done_fab);
        done.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.sharp_done_24));
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
    public void onResume() {
        super.onResume();
        title = titleInFragment.getText().toString();
        remark = remarkInFragment.getText().toString();
        isDone = markAsDoneInFragment.isChecked();
        hasTaskSave = false;
    }

    @Override
    public void onPause() {
        //when user quit the app without choosing save or discard, save the task
        if (checkModified() && !hasTaskSave){
            saveTask();
        }
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
            values.put("time", Long.toString(DATE_NOT_SET_INDICATOR));
        } else {
            if(!timeChanged){
                // if time is not set by user, set the millisecond to 0 to indicate so that we can determine if we should show the time in time field when user comeback
                taskDate.set(Calendar.MILLISECOND, TIME_NOT_SET_MILLISECOND_INDICATOR);
                taskDate.set(Calendar.SECOND, TIME_NOT_SET_MINUTE_SECOND_INDICATOR);
                taskDate.set(Calendar.MINUTE, TIME_NOT_SET_MINUTE_SECOND_INDICATOR);
                taskDate.set(Calendar.HOUR_OF_DAY, TIME_NOT_SET_HOUR_INDICATOR);
            }
            values.put("time", Long.toString(taskDate.getTimeInMillis()));
        }

        values.put("starred", 0);
        values.put("type", 1);
        values.put("urgency", urgencyInFragment.getSelectedItemPosition());

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

    boolean checkModified() {
        if (newTask) {
            return !titleInFragment.getText().toString().trim().equals("")
                    || hasModified
                    || !remarkInFragment.getText().toString().trim().equals("")
                    || markAsDoneInFragment.isChecked();
        } else {
            return !titleInFragment.getText().toString().equals(title)
                    || markAsDoneInFragment.isChecked() != isDone
                    || hasModified
                    || !remarkInFragment.getText().toString().equals(remark);
        }
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
