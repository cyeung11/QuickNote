package com.jkjk.quicknote.taskeditscreen;


import android.app.DatePickerDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.text.format.DateUtils;
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
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.jkjk.quicknote.MyApplication;
import com.jkjk.quicknote.R;
import com.jkjk.quicknote.helper.AlarmHelper;
import com.jkjk.quicknote.helper.NotificationHelper;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static com.jkjk.quicknote.helper.AlarmHelper.EVENT_TIME;
import static com.jkjk.quicknote.helper.AlarmHelper.ITEM_TITLE;
import static com.jkjk.quicknote.helper.AlarmHelper.ITEM_TYPE;
import static com.jkjk.quicknote.helper.DatabaseHelper.DATABASE_NAME;
import static com.jkjk.quicknote.helper.NotificationHelper.ACTION_PIN_ITEM;
import static com.jkjk.quicknote.helper.NotificationHelper.ACTION_TOOL_BAR;
import static com.jkjk.quicknote.helper.NotificationHelper.PIN_ITEM_NOTIFICATION_ID;
import static com.jkjk.quicknote.noteeditscreen.NoteEditFragment.DEFAULT_NOTE_ID;
import static com.jkjk.quicknote.noteeditscreen.NoteEditFragment.EXTRA_NOTE_ID;
import static com.jkjk.quicknote.widget.TaskListWidget.updateTaskListWidget;

public class TaskEditFragment extends Fragment {

    private static final String TASK_ID = "taskId";
    public static final int TIME_NOT_SET_MILLISECOND_INDICATOR = 999;
    public static final int TIME_NOT_SET_MINUTE_SECOND_INDICATOR = 59;
    public static final int TIME_NOT_SET_HOUR_INDICATOR = 23;
    public static final long DATE_NOT_SET_INDICATOR = 9999999999999L;

    private SQLiteDatabase database;
    boolean hasTaskSave = false;
    private long taskId;
    private long initEventTime;
    private EditText titleInFragment, remarkInFragment;
    private TextView dateInFragment, timeInFragment;
    private Switch markAsDoneInFragment;
    private boolean newTask, isDone,  hasModified = false, urgencySelectByUser = false
            , reminderSelectByUser = false, hasShowRemoveDateHint = false, hasShowRemoveTimeHint = false, notificationToolEnable;
    private Calendar taskDate, reminderTime;
    private String title, remark;
    private Spinner urgencyInFragment, reminderInFragment;
    private int spinnerPresetSize;
    private ImageView timeIcon;
    private ArrayList<String> reminderArray;

    public TaskEditFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = ((MyApplication)getActivity().getApplication()).database;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Obtain correspond value from preferences to show appropriate size for the view
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String editViewSize = sharedPref.getString(getResources().getString(R.string.font_size_editing_screen),"m");
        notificationToolEnable = sharedPref.getBoolean(getString(R.string.notification_pin), false);
        int editViewInt;
        int spinnerDropDownInt;
        int spinnerItemInt;
        Cursor taskCursor = null;
        final DateFormat dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.SHORT);

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
        titleInFragment = view.findViewById(R.id.item_title);
        remarkInFragment = view.findViewById(R.id.task_remark);
        markAsDoneInFragment = view.findViewById(R.id.task_done);
        dateInFragment = view.findViewById(R.id.item_date);
        timeInFragment = view.findViewById(R.id.task_time);
        urgencyInFragment = view.findViewById(R.id.task_urgency);
        timeIcon = view.findViewById(R.id.task_time_icon);

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


            // Reading from database
        if (!newTask) {
            try {
                taskCursor = database.query(DATABASE_NAME, new String[]{"title", "content", "event_time","urgency","done", "reminder_time"}, "_id= " + taskId ,
                        null, null, null, null, null);
                taskCursor.moveToFirst();
            } catch (Exception e) {
                Toast.makeText(container.getContext(), R.string.error_loading, Toast.LENGTH_SHORT).show();
                Log.e(this.getClass().getName(), "loading task from cursor       ", e);
            }
        }

        taskDate = Calendar.getInstance();
        taskDate.setTimeInMillis(DATE_NOT_SET_INDICATOR);
        reminderTime = Calendar.getInstance();
        reminderTime.setTimeInMillis(0);

        if (taskCursor != null) {
            titleInFragment.setText(taskCursor.getString(0));
            remarkInFragment.setText(taskCursor.getString(1));

            // read time to date and time field
            taskDate.setTimeInMillis(taskCursor.getLong(2));
            if (taskDate.getTimeInMillis() != DATE_NOT_SET_INDICATOR) {
                DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
                dateInFragment.setText(dateFormat.format(taskDate.getTime()));
                timeInFragment.setVisibility(View.VISIBLE);
                timeIcon.setVisibility(View.VISIBLE);

                // to see if the save time is default or not. If so, time will be shown as not set
                if (!(taskDate.get(Calendar.MILLISECOND) == TIME_NOT_SET_MILLISECOND_INDICATOR
                        && taskDate.get(Calendar.SECOND) == TIME_NOT_SET_MINUTE_SECOND_INDICATOR
                        && taskDate.get(Calendar.MINUTE) == TIME_NOT_SET_MINUTE_SECOND_INDICATOR
                        && taskDate.get(Calendar.HOUR_OF_DAY) == TIME_NOT_SET_HOUR_INDICATOR)) {
                    DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
                    timeInFragment.setText(timeFormat.format(taskDate.getTime()));
                }
            }

            if (taskCursor.getInt(4)==1) {
                markAsDoneInFragment.setChecked(true);
            } else {
                markAsDoneInFragment.setChecked(false);
            }

            reminderTime.setTimeInMillis(taskCursor.getLong(5));

        }


        // Urgency
        ArrayAdapter<CharSequence> urgencyAdapter = ArrayAdapter.createFromResource(getContext(),R.array.urgency_list, spinnerItemInt);
        urgencyAdapter.setDropDownViewResource(spinnerDropDownInt);
        urgencyInFragment.setAdapter(urgencyAdapter);
        urgencyInFragment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (urgencySelectByUser){
                    hasModified = true;
                }
                urgencySelectByUser = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        if (taskCursor != null) {
            urgencySelectByUser = false;
            urgencyInFragment.setSelection(taskCursor.getInt(3));
        }


         // Reminder
        reminderArray = new ArrayList<>();
        reminderArray.add(getString(R.string.no_reminder_set));
        if (taskDate.getTimeInMillis()!=DATE_NOT_SET_INDICATOR) {
            reminderArray.add(getString(R.string.zero_min_before));
            if (!(taskDate.get(Calendar.MILLISECOND) == TIME_NOT_SET_MILLISECOND_INDICATOR
                    && taskDate.get(Calendar.SECOND) == TIME_NOT_SET_MINUTE_SECOND_INDICATOR
                    && taskDate.get(Calendar.MINUTE) == TIME_NOT_SET_MINUTE_SECOND_INDICATOR
                    && taskDate.get(Calendar.HOUR_OF_DAY) == TIME_NOT_SET_HOUR_INDICATOR)) {
                reminderArray.add(getString(R.string.ten_min_before));
                reminderArray.add(getString(R.string.thirty_min_before));
            }
            reminderArray.add(getString(R.string.a_day_beofre));
        }
        reminderArray.add(getString(R.string.custom));

        spinnerPresetSize = reminderArray.size();

        reminderInFragment = view.findViewById(R.id.task_reminder);
        final ArrayAdapter<String> reminderAdapter = new ArrayAdapter<>(getContext(), spinnerItemInt, reminderArray);
        reminderAdapter.setDropDownViewResource(spinnerDropDownInt);
        reminderInFragment.setAdapter(reminderAdapter);
        reminderInFragment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // only invoked by user interaction
                if (reminderSelectByUser){
                    hasModified = true;
                    // if select other, show date & time picker
                    if (reminderInFragment.getItemAtPosition(i).equals(getString(R.string.custom))){
                        int yr, mn, dy;
                        if (reminderTime.getTimeInMillis()==0){
                            yr = Calendar.getInstance().get(Calendar.YEAR);
                            mn = Calendar.getInstance().get(Calendar.MONTH);
                            dy = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
                        } else {
                            yr = reminderTime.get(Calendar.YEAR);
                            mn = reminderTime.get(Calendar.MONTH);
                            dy = reminderTime.get(Calendar.DAY_OF_MONTH);
                        }

                        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker,int year, int month, int dayOfMonth) {
                                int hr, min;
                                if (reminderTime.getTimeInMillis()==0){
                                    hr = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                                    min = Calendar.getInstance().get(Calendar.MINUTE);
                                } else {
                                    hr = reminderTime.get(Calendar.HOUR_OF_DAY);
                                    min = reminderTime.get(Calendar.MINUTE);
                                }
                                reminderTime.set(Calendar.YEAR, year);
                                reminderTime.set(Calendar.MONTH, month);
                                reminderTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute){
                                        reminderTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                        reminderTime.set(Calendar.MINUTE, minute);

                                        // If user is changing custom time, remove the previous custom time, which must be the first one
                                        if (reminderArray.size()> spinnerPresetSize) {
                                            reminderArray.remove(0);
                                        }

                                        //insert select time into spinner
                                        reminderArray.add(0, dateTimeFormat.format(reminderTime.getTime()));
                                        reminderAdapter.notifyDataSetChanged();
                                        reminderSelectByUser = false;
                                        reminderInFragment.setSelection(0);
                                    }
                                }, hr, min, false);

                                timePickerDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialogInterface) {
                                        reminderSelectByUser = false;
                                        reminderInFragment.setSelection(0);
                                        reminderTime.setTimeInMillis(0);
                                    }
                                });
                                timePickerDialog.show();
                            }

                        }, yr, mn, dy);

                        datePickerDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                reminderSelectByUser = false;
                                reminderInFragment.setSelection(0);
                            }
                        });
                        datePickerDialog.show();

                    } else if (reminderArray.size()> spinnerPresetSize){
                        reminderArray.remove(0);
                        reminderAdapter.notifyDataSetChanged();

                        // As we remove item and mess with the position before spinner show what is selected, we need to set the selection to correct position
                        reminderSelectByUser = false;
                        reminderInFragment.setSelection(i-1);
                    }
                }
                reminderSelectByUser = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        // Read the reminder time set by user and select the corresponding option in the spinner
        if (taskCursor!=null) {
            reminderSelectByUser = false;
            String customTime;
            if (reminderTime.getTimeInMillis()==0) {
                reminderInFragment.setSelection(0);
            } else if (!(taskDate.get(Calendar.MILLISECOND) == TIME_NOT_SET_MILLISECOND_INDICATOR
                    && taskDate.get(Calendar.SECOND) == TIME_NOT_SET_MINUTE_SECOND_INDICATOR
                    && taskDate.get(Calendar.MINUTE) == TIME_NOT_SET_MINUTE_SECOND_INDICATOR
                    && taskDate.get(Calendar.HOUR_OF_DAY) == TIME_NOT_SET_HOUR_INDICATOR)) {
                if (reminderTime.getTimeInMillis() == taskDate.getTimeInMillis()) {
                    reminderInFragment.setSelection(1);
                } else if (reminderTime.getTimeInMillis() == taskDate.getTimeInMillis() - 600000) {
                    reminderInFragment.setSelection(2);
                } else if (reminderTime.getTimeInMillis() == taskDate.getTimeInMillis() - 1800000) {
                    reminderInFragment.setSelection(3);
                } else if (reminderTime.getTimeInMillis() == taskDate.getTimeInMillis() - 86400000) {
                    reminderInFragment.setSelection(4);
                } else {
                    customTime = dateTimeFormat.format(new Date(reminderTime.getTimeInMillis()));
                    reminderArray.add(0, customTime);
                    reminderAdapter.notifyDataSetChanged();
                    reminderInFragment.setSelection(0);
                }
            } else if (taskDate.getTimeInMillis()!=0) {
                Calendar calendar = taskDate;
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                if (reminderTime.getTimeInMillis() == calendar.getTimeInMillis()) {
                    reminderInFragment.setSelection(1);
                } else if (reminderTime.getTimeInMillis() == calendar.getTimeInMillis() - 86400000) {
                    reminderInFragment.setSelection(2);
                } else {
                    customTime = dateTimeFormat.format(new Date(reminderTime.getTimeInMillis()));
                    reminderArray.add(0, customTime);
                    reminderAdapter.notifyDataSetChanged();
                    reminderInFragment.setSelection(0);
                }
            } else {
                customTime = dateTimeFormat.format(new Date(reminderTime.getTimeInMillis()));
                reminderArray.add(0, customTime);
                reminderAdapter.notifyDataSetChanged();
                reminderInFragment.setSelection(0);
            }
        }

        dateInFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Only show hint in the first time for every instance of the activity
                if (!hasShowRemoveDateHint) {
                    hasShowRemoveDateHint = true;
                    Toast.makeText(getContext(), R.string.remove_date_hint, Toast.LENGTH_SHORT).show();
                }

                int year, month, dayOfMonth;
                if (taskDate.getTimeInMillis()==DATE_NOT_SET_INDICATOR){
                    year = Calendar.getInstance().get(Calendar.YEAR);
                    month = Calendar.getInstance().get(Calendar.MONTH);
                    dayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
                } else {
                    year = taskDate.get(Calendar.YEAR);
                    month = taskDate.get(Calendar.MONTH);
                    dayOfMonth = taskDate.get(Calendar.DAY_OF_MONTH);
                }

                new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker,int year, int month, int dayOfMonth) {
                        if (taskDate.getTimeInMillis()==DATE_NOT_SET_INDICATOR){
                            taskDate.set(Calendar.HOUR_OF_DAY, TIME_NOT_SET_HOUR_INDICATOR);
                            taskDate.set(Calendar.MINUTE, TIME_NOT_SET_MINUTE_SECOND_INDICATOR);
                            taskDate.set(Calendar.SECOND, TIME_NOT_SET_MINUTE_SECOND_INDICATOR);
                            taskDate.set(Calendar.MILLISECOND, TIME_NOT_SET_MILLISECOND_INDICATOR);
                        }
                        taskDate.set(Calendar.YEAR, year);
                        taskDate.set(Calendar.MONTH, month);
                        taskDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
                        dateInFragment.setText(dateFormat.format(taskDate.getTime()));
                        timeInFragment.setVisibility(View.VISIBLE);
                        timeIcon.setVisibility(View.VISIBLE);
                        hasModified = true;
                        if (!reminderArray.contains(getString(R.string.zero_min_before))) {
                            // add two option after "No reminder" for reminder if user has specify event date;
                            int duePosition = reminderArray.indexOf(getString(R.string.no_reminder_set));
                            reminderArray.add(duePosition + 1, getString(R.string.zero_min_before));
                            reminderArray.add(duePosition + 2, getString(R.string.a_day_beofre));
                            spinnerPresetSize += 2;
                            reminderAdapter.notifyDataSetChanged();
                        }

                    }
                }, year, month, dayOfMonth).show();
            }
        });

        // Allow removing date when long click
        dateInFragment.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (taskDate.getTimeInMillis() != DATE_NOT_SET_INDICATOR) {
                    taskDate.setTimeInMillis(DATE_NOT_SET_INDICATOR);
                    dateInFragment.setText("");
                    timeInFragment.setVisibility(View.INVISIBLE);
                    timeIcon.setVisibility(View.INVISIBLE);
                    hasModified = true;

                    reminderArray.clear();
                    reminderArray.add(getString(R.string.no_reminder_set));
                    reminderArray.add(getString(R.string.custom));
                    reminderAdapter.notifyDataSetChanged();

                    if (reminderTime.getTimeInMillis() != 0){
                        reminderArray.add(0, dateTimeFormat.format(new Date(reminderTime.getTimeInMillis())));
                        reminderAdapter.notifyDataSetChanged();
                        reminderSelectByUser = false;
                        reminderInFragment.setSelection(0);
                    }
                    return true;
                } else return false;
            }
        });

        timeInFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Only show hint in the first time for every instance of the activity
                if (!hasShowRemoveTimeHint) {
                    hasShowRemoveTimeHint = true;
                    Toast.makeText(getContext(), R.string.remove_time_hint, Toast.LENGTH_SHORT).show();
                }
                int hourOfDay, minute;
                // Change hour and minute shown as current time or event time
                if (taskDate.get(Calendar.MILLISECOND)== TIME_NOT_SET_MILLISECOND_INDICATOR
                        && taskDate.get(Calendar.SECOND) == TIME_NOT_SET_MINUTE_SECOND_INDICATOR
                        && taskDate.get(Calendar.MINUTE) == TIME_NOT_SET_MINUTE_SECOND_INDICATOR
                        && taskDate.get(Calendar.HOUR_OF_DAY) == TIME_NOT_SET_HOUR_INDICATOR) {
                    hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                    minute = Calendar.getInstance().get(Calendar.MINUTE);
                } else {
                    hourOfDay = taskDate.get(Calendar.HOUR_OF_DAY);
                    minute = taskDate.get(Calendar.MINUTE);
                }

                new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute){
                        taskDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        taskDate.set(Calendar.MINUTE, minute);
                        DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
                        timeInFragment.setText(timeFormat.format(taskDate.getTime()));
                        hasModified = true;

                        if (!reminderArray.contains(getString(R.string.ten_min_before))) {
                            // add two option after "Due" for reminder if user has specify event time;
                            int duePosition = reminderArray.indexOf(getString(R.string.zero_min_before));
                            reminderArray.add(duePosition + 1, getString(R.string.ten_min_before));
                            reminderArray.add(duePosition + 2, getString(R.string.thirty_min_before));
                            spinnerPresetSize += 2;
                            reminderAdapter.notifyDataSetChanged();
                        }
                    }
                }, hourOfDay, minute, false).show();
            }
        });

        timeInFragment.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                taskDate.set(Calendar.HOUR_OF_DAY, TIME_NOT_SET_HOUR_INDICATOR);
                taskDate.set(Calendar.MINUTE, TIME_NOT_SET_MINUTE_SECOND_INDICATOR);
                taskDate.set(Calendar.SECOND, TIME_NOT_SET_MINUTE_SECOND_INDICATOR);
                taskDate.set(Calendar.MILLISECOND, TIME_NOT_SET_MILLISECOND_INDICATOR);

                hasModified = true;
                timeInFragment.setText("");

                reminderArray.clear();
                reminderArray.add(getString(R.string.no_reminder_set));
                reminderArray.add(getString(R.string.zero_min_before));
                reminderArray.add(getString(R.string.a_day_beofre));
                reminderArray.add(getString(R.string.custom));
                reminderAdapter.notifyDataSetChanged();

                if (reminderTime.getTimeInMillis() != 0 ) {
                    reminderArray.add(0, dateTimeFormat.format(new Date(reminderTime.getTimeInMillis())));
                    reminderAdapter.notifyDataSetChanged();
                    reminderSelectByUser = false;
                    reminderInFragment.setSelection(0);
                }
                return true;
            }
        });

        final ImageButton showDropMenu = view.findViewById(R.id.task_show_drop_menu);
        showDropMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu editDropMenu = new PopupMenu(view.getContext(),showDropMenu);
                editDropMenu.inflate(R.menu.task_edit_drop_menu);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    MenuItem pinToNotification = editDropMenu.getMenu().findItem(R.id.edit_drop_menu_pin);
                    if (!newTask && isItemAnActiveNotification()){
                        pinToNotification.setTitle(R.string.notification_unpin);
                    } else pinToNotification.setTitle(R.string.notification_pin);

                    pinToNotification.setVisible(true);
                }

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
                                                            database.delete(DATABASE_NAME, "_id='" + taskId + "'", null);
                                                            updateTaskListWidget(getContext());
                                                        }
                                                        // No need to do saving
                                                        hasTaskSave = true;
                                                        Toast.makeText(getContext(), R.string.note_deleted_toast, Toast.LENGTH_SHORT).show();
                                                        if (getActivity()!=null) {
                                                            getActivity().finish();
                                                        }
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
                                if (taskDate.getTimeInMillis()!=DATE_NOT_SET_INDICATOR){
                                    stringBuilder.append("\n").append(dateInFragment.getText());
                                }
                                if (!(taskDate.get(Calendar.MILLISECOND) == TIME_NOT_SET_MILLISECOND_INDICATOR
                                        && taskDate.get(Calendar.SECOND) == TIME_NOT_SET_MINUTE_SECOND_INDICATOR
                                        && taskDate.get(Calendar.MINUTE) == TIME_NOT_SET_MINUTE_SECOND_INDICATOR
                                        && taskDate.get(Calendar.HOUR_OF_DAY) == TIME_NOT_SET_HOUR_INDICATOR)){
                                    stringBuilder.append(" ").append(timeInFragment.getText().toString());
                                }
                                stringBuilder.append("\n").append(remarkInFragment.getText().toString());
                                shareIntent.putExtra(Intent.EXTRA_TEXT,stringBuilder.toString());
                                shareIntent.putExtra(Intent.EXTRA_TITLE, titleInFragment.getText());
                                startActivity(shareIntent);
                                return true;
                            case R.id.edit_drop_menu_pin:
                                // if it is a new note, save the note and then pin to notification
                                if (newTask) {
                                    saveTask();
                                    hasTaskSave = false;
                                }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isItemAnActiveNotification()) {
                                    pinTaskToNotification();
                                } else {
                                    NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                                    if (notificationManager!=null){
                                        // to distinguish reminder and pin item, id of pin item is the item id * PIN_ITEM_NOTIFICATION_ID
                                        notificationManager.cancel((int)taskId*PIN_ITEM_NOTIFICATION_ID);
                                    }
                                }
                                return true;

                            default:
                                return false;
                        }
                    }
                });
                editDropMenu.show();
            }
        });

        if (taskCursor != null) taskCursor.close();

        return view;
    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Define save function for the done button
        FloatingActionButton done = getActivity().findViewById(R.id.done_fab);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            done.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.sharp_done_24));
        } else {
            done.setImageResource(R.drawable.sharp_done_24);
        }
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
        initEventTime = taskDate.getTimeInMillis();
        hasTaskSave = false;
    }

    @Override
    public void onPause() {
        //when user quit the app without choosing save or discard, save the task
        if (checkModified() && !hasTaskSave) saveTask();
        super.onPause();
    }

    public void saveTask(){
        ContentValues values = new ContentValues();

        title = titleInFragment.getText().toString().trim();

        if (title.length()<1) title = getString(R.string.untitled_task);
        values.put("title", title);

        remark = remarkInFragment.getText().toString();
        values.put("content", remark);

        values.put("event_time", taskDate.getTimeInMillis());
        initEventTime = taskDate.getTimeInMillis();

        if (notificationToolEnable && DateUtils.isToday(taskDate.getTimeInMillis())){

            Intent toolBarIntent = new Intent(getContext(), NotificationHelper.class);
            toolBarIntent.setAction(ACTION_TOOL_BAR);
            PendingIntent toolbarPendingIntent = PendingIntent.getBroadcast(getContext(), 0, toolBarIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            try {
                toolbarPendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }

        values.put("type", 1);
        values.put("urgency", urgencyInFragment.getSelectedItemPosition());

        if (markAsDoneInFragment.isChecked()) {
            values.put("done", 1);
            isDone = true;
        } else {
            values.put("done", 0);
            isDone = false;
        }

//        long saveReminderTime;
        if (reminderInFragment.getSelectedItem().toString().equals(getString(R.string.no_reminder_set))){
            AlarmHelper.cancelReminder(getContext(),taskId);
            reminderTime.setTimeInMillis(0);

        } else {
            if (reminderInFragment.getSelectedItem().toString().equals(getString(R.string.zero_min_before))){
                if  (!(taskDate.get(Calendar.MILLISECOND) == TIME_NOT_SET_MILLISECOND_INDICATOR
                        && taskDate.get(Calendar.SECOND) == TIME_NOT_SET_MINUTE_SECOND_INDICATOR
                        && taskDate.get(Calendar.MINUTE) == TIME_NOT_SET_MINUTE_SECOND_INDICATOR
                        && taskDate.get(Calendar.HOUR_OF_DAY) == TIME_NOT_SET_HOUR_INDICATOR)) {
                    reminderTime.setTimeInMillis(taskDate.getTimeInMillis());
                } else {
                    reminderTime = taskDate;
                    reminderTime.set(Calendar.MILLISECOND, 0);
                    reminderTime.set(Calendar.SECOND, 0);
                    reminderTime.set(Calendar.MINUTE, 0);
                    reminderTime.set(Calendar.HOUR_OF_DAY, 0);
                }

            } else if (reminderInFragment.getSelectedItem().toString().equals(getString(R.string.ten_min_before))){
                reminderTime.setTimeInMillis(taskDate.getTimeInMillis()-600000);

            } else if (reminderInFragment.getSelectedItem().toString().equals(getString(R.string.thirty_min_before))){
                reminderTime.setTimeInMillis(taskDate.getTimeInMillis()-1800000);

            } else if (reminderInFragment.getSelectedItem().toString().equals(getString(R.string.a_day_beofre))){
                if (!(taskDate.get(Calendar.MILLISECOND) == TIME_NOT_SET_MILLISECOND_INDICATOR
                        && taskDate.get(Calendar.SECOND) == TIME_NOT_SET_MINUTE_SECOND_INDICATOR
                        && taskDate.get(Calendar.MINUTE) == TIME_NOT_SET_MINUTE_SECOND_INDICATOR
                        && taskDate.get(Calendar.HOUR_OF_DAY) == TIME_NOT_SET_HOUR_INDICATOR)) {
                    reminderTime.setTimeInMillis(taskDate.getTimeInMillis()-86400000);
                } else {
                    reminderTime = taskDate;
                    reminderTime.set(Calendar.MILLISECOND, 0);
                    reminderTime.set(Calendar.SECOND, 0);
                    reminderTime.set(Calendar.MINUTE, 0);
                    reminderTime.set(Calendar.HOUR_OF_DAY, 0);
                }
            }
            AlarmHelper.setReminder(getContext(), 'T', taskId, title, remark, taskDate.getTimeInMillis(), reminderTime.getTimeInMillis());
        }
        values.put("reminder_time", reminderTime.getTimeInMillis());

        if (!newTask) {
            database.update(DATABASE_NAME, values, "_id='" + taskId +"'", null);
            // to update pinned notification if there is any, api 23 up exclusive
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(isItemAnActiveNotification()){
                    pinTaskToNotification();
                }
            }
        }else {
            taskId = database.insert(DATABASE_NAME, "",values);
        }
        values.clear();
        hasTaskSave = true;
        newTask = false;
        updateTaskListWidget(getContext());
        Toast.makeText(getActivity(),R.string.saved_task, Toast.LENGTH_SHORT).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean isItemAnActiveNotification(){
        boolean result = false;
        NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            StatusBarNotification[] activeNotifications = notificationManager.getActiveNotifications();
            ArrayList<Integer> notificationId = new ArrayList<>();
            for (StatusBarNotification activeNotification : activeNotifications) {
                notificationId.add(activeNotification.getId());
            }
            // to distinguish reminder and pin item, id of pin item is the item id * PIN_ITEM_NOTIFICATION_ID
            if (notificationId.contains((int) taskId*PIN_ITEM_NOTIFICATION_ID)){
                result = true;
            }
        } else Toast.makeText(getContext(), R.string.error_text, Toast.LENGTH_SHORT).show();
        return result;
    }

    private void pinTaskToNotification(){
        Intent intent = new Intent(getContext(), NotificationHelper.class);
        intent.setAction(ACTION_PIN_ITEM);
        intent.putExtra(EXTRA_NOTE_ID, taskId);
        intent.putExtra(ITEM_TYPE, 'T');
        intent.putExtra(ITEM_TITLE, title);
        intent.putExtra(EVENT_TIME, initEventTime);
        PendingIntent pinNotificationPI = PendingIntent.getBroadcast(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        try {
            pinNotificationPI.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
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
