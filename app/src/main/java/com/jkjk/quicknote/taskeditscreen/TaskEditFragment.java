package com.jkjk.quicknote.taskeditscreen;


import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;
import com.jkjk.quicknote.MyApplication;
import com.jkjk.quicknote.R;
import com.jkjk.quicknote.helper.AlarmHelper;
import com.jkjk.quicknote.helper.NotificationHelper;

import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.jkjk.quicknote.MyApplication.PINNED_NOTIFICATION_IDS;
import static com.jkjk.quicknote.helper.DatabaseHelper.DATABASE_NAME;
import static com.jkjk.quicknote.helper.NotificationHelper.ACTION_PIN_ITEM;
import static com.jkjk.quicknote.helper.NotificationHelper.ACTION_TOOL_BAR;
import static com.jkjk.quicknote.helper.NotificationHelper.PIN_ITEM_NOTIFICATION_ID;
import static com.jkjk.quicknote.noteeditscreen.NoteEditFragment.DEFAULT_NOTE_ID;
import static com.jkjk.quicknote.noteeditscreen.NoteEditFragment.EXTRA_ITEM_ID;
import static com.jkjk.quicknote.widget.TaskListWidget.updateTaskListWidget;

public class TaskEditFragment extends Fragment {

    private static final String TASK_ID = "taskId";
    public static final int TIME_NOT_SET_MILLISECOND_INDICATOR = 999;
    public static final int TIME_NOT_SET_MINUTE_SECOND_INDICATOR = 59;
    public static final int TIME_NOT_SET_HOUR_INDICATOR = 23;
    public static final long DATE_NOT_SET_INDICATOR = 9999999999999L;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 0;
    private static final int LOCATION_PICKER_REQUEST_CODE = 1;
    private static final int LOCATION_PICKER_ZOOM_OUT_METER = 500;

    private SQLiteDatabase database;
    boolean hasTaskSave = false;
    private long taskId, initEventTime, repeatTime = 0;
    private EditText titleInFragment, remarkInFragment;
    private TextView dateInFragment, timeInFragment, locationInFragment;
    private Switch markAsDoneInFragment;
    private boolean newTask, isDone,  hasModified = false, urgencySelectByUser = false, repeatSelectByUser = false
            , reminderSelectByUser = false, hasShowRemoveDateHint = false, hasShowRemoveTimeHint = false, notificationToolEnable;
    private Calendar taskDate, reminderTime;
    private String title, remark;
    private Spinner urgencyInFragment, reminderInFragment, repeatInFragment;
    private int reminderPresetSize, repeatPresetSize;
    private LinearLayout timeRow, repeatRow;
    private Context context;
    private LatLng latLng;
    private float doneButtonYPosition;

    private ArrayList<String> reminderArray, repeatArray;

    public TaskEditFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        database = ((MyApplication)context.getApplicationContext()).database;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Obtain correspond value from preferences to show appropriate size for the view
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
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
        timeRow = view.findViewById(R.id.task_time_grid);
        repeatRow = view.findViewById(R.id.task_repeat_column);
        reminderInFragment = view.findViewById(R.id.task_reminder);
        repeatInFragment = view.findViewById(R.id.task_repeat);
        locationInFragment = view.findViewById(R.id.item_location);

        if (savedInstanceState !=null) {
            // case when restoring from saved instance
            taskId = savedInstanceState.getLong(TASK_ID, 0L);
            newTask = false;

        }else if (getArguments() != null) {
            // case when argument has data, either note ID from the note list activity or text from external intent
            taskId = getArguments().getLong(EXTRA_ITEM_ID, DEFAULT_NOTE_ID);
            newTask = (taskId == DEFAULT_NOTE_ID);

        } else {
            newTask = true;}


            // Reading from database
        if (!newTask) {
            try {
                taskCursor = database.query(DATABASE_NAME, new String[]{"title", "content", "event_time","urgency","done", "reminder_time", "repeat_interval", "lat_lng"}, "_id= " + taskId ,
                        null, null, null, null, null);
                taskCursor.moveToFirst();
                titleInFragment.setText(taskCursor.getString(0));
                remarkInFragment.setText(taskCursor.getString(1));
            } catch (Exception e) {
                Toast.makeText(context, R.string.error_loading, Toast.LENGTH_SHORT).show();
                Log.e(this.getClass().getName(), "loading task from cursor       ", e);
                hasTaskSave = true;
                if (getActivity() != null) {
                    getActivity().finish();
                }
                return view;
            }
        }

        taskDate = Calendar.getInstance();
        taskDate.setTimeInMillis(DATE_NOT_SET_INDICATOR);
        reminderTime = Calendar.getInstance();
        reminderTime.setTimeInMillis(0);

        if (taskCursor != null && taskCursor.getColumnCount() > 0) {
            // read time to date and time field
            taskDate.setTimeInMillis(taskCursor.getLong(2));
            if (taskDate.getTimeInMillis() != DATE_NOT_SET_INDICATOR) {
                DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
                dateInFragment.setText(dateFormat.format(taskDate.getTime()));
                timeRow.setVisibility(View.VISIBLE);
                repeatRow.setVisibility(View.VISIBLE);

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

            repeatTime = taskCursor.getLong(6);

            String latLngString = taskCursor.getString(7);
            if (latLngString != null){
                String[] latLngValue = latLngString.split(",");
                if (latLngValue.length == 2){
                    latLng = new LatLng(Double.valueOf(latLngValue[0]), Double.valueOf(latLngValue[1]));
                }
            }
        }

        // Urgency
        ArrayAdapter<CharSequence> urgencyAdapter = ArrayAdapter.createFromResource(context,R.array.urgency_list, spinnerItemInt);
        urgencyAdapter.setDropDownViewResource(spinnerDropDownInt);
        urgencyInFragment.setAdapter(urgencyAdapter);
        urgencyInFragment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //To prevent initialization trigger the method when the fragment first startup. After that, allow all interaction
                if (urgencySelectByUser){
                    hasModified = true;
                }
                urgencySelectByUser = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        if (taskCursor != null && taskCursor.getColumnCount() > 0) {
            urgencySelectByUser = false;
            urgencyInFragment.setSelection(taskCursor.getInt(3));
        }

        // Repeat
        repeatArray = new ArrayList<>(Arrays.asList(context.getResources().getStringArray(R.array.repeat_list)));
        repeatPresetSize = repeatArray.size();
        final ArrayAdapter<String> repeatAdapter = new ArrayAdapter<>(context, spinnerItemInt, repeatArray);
        repeatAdapter.setDropDownViewResource(spinnerDropDownInt);
        repeatInFragment.setAdapter(repeatAdapter);
        repeatInFragment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //To prevent initialization trigger the method when the fragment first startup. After that, allow all interaction
                if (repeatSelectByUser){
                    hasModified = true;

//                    if select the last item, i.e. other, show the number picker
                    if (i == repeatArray.size()-1){
                        NumberPickerDialog numberPickerDialog = new NumberPickerDialog(context, new NumberPicker.OnValueChangeListener() {
                            @Override
                            public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {
                                repeatTime = newValue * 86400000L;
                                if (repeatArray.size() > repeatPresetSize){
                                     repeatArray.remove(0);
                                }
                                repeatArray.add(0, Integer.toString(newValue)+" "+getString(R.string.day));
                                repeatAdapter.notifyDataSetChanged();
                                repeatSelectByUser = false;
                                repeatInFragment.setSelection(0);
                            }
                        }, 1, 365);
                        numberPickerDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                repeatSelectByUser = false;
                                repeatInFragment.setSelection(0);
                            }
                        });
                        numberPickerDialog.show();
                    } else if (repeatArray.size() > repeatPresetSize) {
                        repeatArray.remove(0);
                        switch (i){
                            case 1:
                                repeatTime = 0L;
                                break;
                            case 2:
                                repeatTime = 86400000L;
                                break;
                            case 3:
                                repeatTime = 604800000L;
                                break;
                            case 4:
                                repeatTime = 2592000000L;
                                break;
                            case 5:
                                repeatTime = 31104000000L;
                                break;
                        }
                        // As we remove item and mess with the position before spinner show what is selected, we need to set the selection to correct position
                        repeatSelectByUser = false;
                        repeatInFragment.setSelection(i-1);
                    } else {
                        switch (i){
                            case 0:
                                repeatTime = 0L;
                                break;
                            case 1:
                                repeatTime = 86400000L;
                                break;
                            case 2:
                                repeatTime = 604800000L;
                                break;
                            case 3:
                                repeatTime = 2592000000L;
                                break;
                            case 4:
                                repeatTime = 31104000000L;
                                break;
                        }
                    }
                }
                repeatSelectByUser = true;
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        repeatSelectByUser = false;
        if (repeatTime == 0){
            repeatInFragment.setSelection(0);
        } else if (repeatTime == 86400000){
            repeatInFragment.setSelection(1);
        } else if (repeatTime == 604800000){
            repeatInFragment.setSelection(2);
        }else if (repeatTime == 2592000000L){
            repeatInFragment.setSelection(3);
        }else if (repeatTime == 31104000000L){
            repeatInFragment.setSelection(4);
        } else {
            repeatArray.add(0, Long.toString(repeatTime/86400000L)+" "+getString(R.string.day));
            repeatAdapter.notifyDataSetChanged();
            repeatInFragment.setSelection(0);
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

        reminderPresetSize = reminderArray.size();

        final ArrayAdapter<String> reminderAdapter = new ArrayAdapter<>(context, spinnerItemInt, reminderArray);
        reminderAdapter.setDropDownViewResource(spinnerDropDownInt);
        reminderInFragment.setAdapter(reminderAdapter);
        reminderInFragment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // only invoked by user interaction
                if (reminderSelectByUser){
                    hasModified = true;
                    // if select other, i.e the last item, show date & time picker
                    if (i == reminderArray.size()-1){
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

                        DatePickerDialog datePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
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

                                TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute){
                                        reminderTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                        reminderTime.set(Calendar.MINUTE, minute);

                                        // If user is changing custom time, remove the previous custom time, which must be the first one
                                        if (reminderArray.size()> reminderPresetSize) {
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

                    } else if (reminderArray.size()> reminderPresetSize){
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
                    Toast.makeText(context, R.string.remove_date_hint, Toast.LENGTH_SHORT).show();
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

                new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker,int year, int month, int dayOfMonth) {
                        if (taskDate.getTimeInMillis()==DATE_NOT_SET_INDICATOR){
                            taskDate.set(Calendar.HOUR_OF_DAY, TIME_NOT_SET_HOUR_INDICATOR);
                            taskDate.set(Calendar.MINUTE, TIME_NOT_SET_MINUTE_SECOND_INDICATOR);
                            taskDate.set(Calendar.SECOND, TIME_NOT_SET_MINUTE_SECOND_INDICATOR);
                            taskDate.set(Calendar.MILLISECOND, TIME_NOT_SET_MILLISECOND_INDICATOR);

                            if (reminderTime.getTimeInMillis() == 0L) {
                                reminderSelectByUser = false;
                                reminderInFragment.setSelection(1);
                            }
                        }
                        taskDate.set(Calendar.YEAR, year);
                        taskDate.set(Calendar.MONTH, month);
                        taskDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
                        dateInFragment.setText(dateFormat.format(taskDate.getTime()));

                        timeRow.setVisibility(View.VISIBLE);
                        repeatRow.setVisibility(View.VISIBLE);
                        hasModified = true;
                        if (!reminderArray.contains(getString(R.string.zero_min_before))) {
                            // add two option after "No reminder" for reminder if user has specify event date;
                            int duePosition = reminderArray.indexOf(getString(R.string.no_reminder_set));
                            reminderArray.add(duePosition + 1, getString(R.string.zero_min_before));
                            reminderArray.add(duePosition + 2, getString(R.string.a_day_beofre));
                            reminderPresetSize += 2;
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
                    timeRow.setVisibility(View.GONE);
                    repeatTime = 0L;
                    repeatRow.setVisibility(View.GONE);
                    if (repeatArray.size()>repeatPresetSize){
                        repeatArray.remove(0);
                    }
                    repeatAdapter.notifyDataSetChanged();
                    repeatSelectByUser = false;
                    repeatInFragment.setSelection(0);
                    hasModified = true;

                    String currentReminderSelection = (String) reminderInFragment.getSelectedItem();

                    reminderArray.clear();
                    reminderArray.add(getString(R.string.no_reminder_set));
                    reminderArray.add(getString(R.string.custom));
                    reminderAdapter.notifyDataSetChanged();

                    if (reminderTime.getTimeInMillis() != 0 && !currentReminderSelection.equals(getString(R.string.no_reminder_set))){
                        reminderArray.add(0, dateTimeFormat.format(new Date(reminderTime.getTimeInMillis())));
                        reminderAdapter.notifyDataSetChanged();
                    }
                    reminderSelectByUser = false;
                    reminderInFragment.setSelection(0);
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
                    Toast.makeText(context, R.string.remove_time_hint, Toast.LENGTH_SHORT).show();
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

                new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
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
                            reminderPresetSize += 2;
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

                SharedPreferences idPref = context.getSharedPreferences(PINNED_NOTIFICATION_IDS, MODE_PRIVATE);

                MenuItem pinToNotification = editDropMenu.getMenu().findItem(R.id.edit_drop_menu_pin);
                if (!newTask && idPref.getLong(Long.toString(taskId), 999999L)!=999999L){
                    pinToNotification.setTitle(R.string.notification_unpin);
                } else pinToNotification.setTitle(R.string.notification_pin);

                pinToNotification.setVisible(true);

                editDropMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()){
                            case R.id.edit_drop_menu_delete:
                                //Delete task
                                new AlertDialog.Builder(context).setTitle(R.string.delete_title).setMessage(R.string.confirm_delete_edit)
                                        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        if (!newTask) {
                                                            database.delete(DATABASE_NAME, "_id='" + taskId + "'", null);
                                                            updateTaskListWidget(context);
                                                            AlarmHelper.cancelReminder(context.getApplicationContext(), taskId);

                                                            SharedPreferences idPref = context.getSharedPreferences(PINNED_NOTIFICATION_IDS, MODE_PRIVATE);
                                                            if (idPref.getLong(Long.toString(taskId), 999999L)!=999999L) {
                                                                idPref.edit().remove(Long.toString(taskId)).apply();
                                                                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                                                if (notificationManager != null) {
                                                                    notificationManager.cancel((int)taskId*PIN_ITEM_NOTIFICATION_ID);
                                                                }
                                                            }
                                                           updateToolbar();
                                                        }
                                                        // No need to do saving
                                                        hasTaskSave = true;
                                                        Toast.makeText(context, R.string.task_deleted_toast, Toast.LENGTH_SHORT).show();
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

                                SharedPreferences idPref = context.getSharedPreferences(PINNED_NOTIFICATION_IDS, MODE_PRIVATE);

                                if (idPref.getLong(Long.toString(taskId), 999999L)==999999L) {
                                    pinTaskToNotification();
                                } else {
                                    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                    if (notificationManager!=null){
                                        // to distinguish reminder and pin item, id of pin item is the item id * PIN_ITEM_NOTIFICATION_ID
                                        notificationManager.cancel((int)taskId*PIN_ITEM_NOTIFICATION_ID);
                                        idPref.edit().remove(Long.toString(taskId)).apply();
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

        if (latLng != null){
            setLocationText(latLng);
        }

        locationInFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)){
                        showPermissionDialog();
                    } else {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                    }
                } else {
                    FragmentActivity fragmentActivity = getActivity();
                    if (fragmentActivity != null) {
                        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                        if (latLng != null){
                            LatLngBounds.Builder latLngBoundsBuilder = LatLngBounds.builder().include(latLng);
                            //Make sure the zoom scale is appropriate
                            latLngBoundsBuilder.include(SphericalUtil.computeOffset(latLng, LOCATION_PICKER_ZOOM_OUT_METER, 0));
                            latLngBoundsBuilder.include(SphericalUtil.computeOffset(latLng, LOCATION_PICKER_ZOOM_OUT_METER, 90));
                            latLngBoundsBuilder.include(SphericalUtil.computeOffset(latLng, LOCATION_PICKER_ZOOM_OUT_METER, 180));
                            latLngBoundsBuilder.include(SphericalUtil.computeOffset(latLng, LOCATION_PICKER_ZOOM_OUT_METER, 270));
                            builder.setLatLngBounds(latLngBoundsBuilder.build());
                        }
                        try {
                            startActivityForResult(builder.build(fragmentActivity), LOCATION_PICKER_REQUEST_CODE);
                        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                            Toast.makeText(context, R.string.google_play_service_fail_toast, Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        if (taskCursor != null) taskCursor.close();
        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                FragmentActivity fragmentActivity = getActivity();
                if (fragmentActivity != null) {
                    PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                    if (latLng != null){
                        builder.setLatLngBounds(LatLngBounds.builder().include(latLng).build());
                    }
                    try {
                        startActivityForResult(builder.build(fragmentActivity), LOCATION_PICKER_REQUEST_CODE);
                    } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                        Toast.makeText(context, R.string.google_play_service_fail_toast, Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            } else showPermissionDialog();
        } else super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOCATION_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            latLng = PlacePicker.getPlace(context, data).getLatLng();
            setLocationText(latLng);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Define save function for the done button
        if (getActivity() != null) {
            final FloatingActionButton done = getActivity().findViewById(R.id.done_fab);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                done.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.sharp_done_24));
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

            doneButtonYPosition = done.getY();
            done.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    float y = done.getY();
                    if (y < doneButtonYPosition) {
                        if (remarkInFragment.hasFocus()) {
                            done.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        if (done.getVisibility() != View.VISIBLE) done.setVisibility(View.VISIBLE);
                    }
                    doneButtonYPosition = y;

                }
            });
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(TASK_ID, taskId);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        context = null;
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

//        Repeat can only work if date is set
        values.put("repeat_interval", (repeatTime > 0)&&(initEventTime!=DATE_NOT_SET_INDICATOR) ? repeatTime : 0);

        values.put("type", 1);
        values.put("urgency", urgencyInFragment.getSelectedItemPosition());

        if (markAsDoneInFragment.isChecked() && repeatTime == 0) {
            values.put("done", 1);
            isDone = true;
        } else {
            values.put("done", 0);
            isDone = false;
        }

        if (markAsDoneInFragment.isChecked() && repeatTime > 0){
            values.put("event_time", values.getAsLong("event_time") + repeatTime);
        }

        if (reminderInFragment.getSelectedItem().toString().equals(getString(R.string.no_reminder_set))){
            AlarmHelper.cancelReminder(context.getApplicationContext(),taskId);
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
                    reminderTime.setTimeInMillis(reminderTime.getTimeInMillis()-86400000);
                }
            }
            if (markAsDoneInFragment.isChecked() && repeatTime > 0){
                reminderTime.setTimeInMillis(reminderTime.getTimeInMillis()+repeatTime);
            }
        }
        values.put("reminder_time", reminderTime.getTimeInMillis());

        if (latLng != null) values.put("lat_lng", Double.toString(latLng.latitude) + "," + Double.toString(latLng.longitude));

        if (!newTask) {
            database.update(DATABASE_NAME, values, "_id='" + taskId +"'", null);

            SharedPreferences idPref = context.getApplicationContext().getSharedPreferences(PINNED_NOTIFICATION_IDS, MODE_PRIVATE);
            if (idPref.getLong(Long.toString(taskId), 999999L)!=999999L){
                pinTaskToNotification();
            }
////            to update pinned notification if there is any, api 23 up exclusive
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                if(isItemAnActiveNotification()){
//                    pinTaskToNotification();
//                }
//            }
        }else {
            taskId = database.insert(DATABASE_NAME, "",values);
        }
        if (reminderTime.getTimeInMillis() > 0) {
            AlarmHelper.setReminder(context.getApplicationContext(), taskId, reminderTime.getTimeInMillis());
        }
        values.clear();
        hasTaskSave = true;
        newTask = false;
        updateTaskListWidget(context);
        updateToolbar();
        Toast.makeText(getActivity(),R.string.saved_task, Toast.LENGTH_SHORT).show();
    }

//    @RequiresApi(api = Build.VERSION_CODES.M)
//    private boolean isItemAnActiveNotification(){
//        NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
//
//        if (notificationManager != null) {
//            StatusBarNotification[] activeNotifications = notificationManager.getActiveNotifications();
//            ArrayList<Integer> notificationId = new ArrayList<>();
//            for (StatusBarNotification activeNotification : activeNotifications) {
//                notificationId.add(activeNotification.getId());
//            }
//            // to distinguish reminder and pin item, id of pin item is the item id * PIN_ITEM_NOTIFICATION_ID
//            return notificationId.contains((int) taskId * PIN_ITEM_NOTIFICATION_ID);
//        } else Toast.makeText(getContext(), R.string.error_text, Toast.LENGTH_SHORT).show();
//        return false;
//    }

    private void setLocationText(LatLng latLng){
        try {
            Geocoder geocoder;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                geocoder = new Geocoder(context, context.getResources().getConfiguration().locale);
            } else
                geocoder = new Geocoder(context, context.getResources().getConfiguration().getLocales().get(0));

            List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addressList.size() == 0) {
                DecimalFormat decimalFormat = new DecimalFormat("#.00000");
                locationInFragment.setText(getString(R.string.lat_lng, decimalFormat.format(latLng.latitude), decimalFormat.format(latLng.longitude)));
            } else {
                Address address = addressList.get(0);
                String featureName = address.getFeatureName();
                if (featureName != null && !featureName.isEmpty() && !isNumericString(featureName)) {
                    locationInFragment.setText(featureName);
                } else {
                    StringBuilder stringBuilder = new StringBuilder();
                    String addressLine = address.getAddressLine(0);
                    for (int i = 1; addressLine != null; i++) {
                        stringBuilder.append(addressLine).append(" ");
                        addressLine = address.getAddressLine(i);
                    }
                    locationInFragment.setText(stringBuilder.toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            DecimalFormat decimalFormat = new DecimalFormat("#.00000");
            locationInFragment.setText(getString(R.string.lat_lng, decimalFormat.format(latLng.latitude), decimalFormat.format(latLng.longitude)));
        }
    }

    private void showPermissionDialog(){
        new AlertDialog.Builder(context).setTitle(R.string.permission_required).setMessage(R.string.location_permission_msg).
                setPositiveButton(R.string.open_settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                }).setNegativeButton(R.string.cancel, null).show();
    }

    private void pinTaskToNotification(){
        Intent intent = new Intent(context, NotificationHelper.class);
        intent.setAction(ACTION_PIN_ITEM);
        intent.putExtra(EXTRA_ITEM_ID, taskId);
        PendingIntent pinNotificationPI = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
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

    private void updateToolbar(){
        if (notificationToolEnable && DateUtils.isToday(taskDate.getTimeInMillis())){

            Intent toolBarIntent = new Intent(context, NotificationHelper.class);
            toolBarIntent.setAction(ACTION_TOOL_BAR);
            PendingIntent toolbarPendingIntent = PendingIntent.getBroadcast(context, 0, toolBarIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            try {
                toolbarPendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
    }

    public static TaskEditFragment newEditFragmentInstance(long taskId){
        TaskEditFragment fragment = new TaskEditFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(EXTRA_ITEM_ID, taskId);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static TaskEditFragment newEditFragmentInstance(){
        return new TaskEditFragment();
    }

    private boolean isNumericString(String string){
        try{
            Integer result = Integer.parseInt(string.trim());
            Log.d(getClass().getName(),"Confirm "+ string + "is number");
            return true;
        } catch (Exception e){
            return false;
        }
    }
}
