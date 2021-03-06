package com.jkjk.quicknote.taskeditscreen;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import java.util.concurrent.TimeUnit;

import static android.content.Context.MODE_PRIVATE;
import static com.jkjk.quicknote.MyApplication.PINNED_NOTIFICATION_IDS;
import static com.jkjk.quicknote.helper.NotificationHelper.ACTION_PIN_ITEM;
import static com.jkjk.quicknote.helper.NotificationHelper.ACTION_TOOL_BAR;
import static com.jkjk.quicknote.helper.NotificationHelper.PIN_ITEM_NOTIFICATION_ID;
import static com.jkjk.quicknote.noteeditscreen.NoteEditFragment.DEFAULT_NOTE_ID;
import static com.jkjk.quicknote.noteeditscreen.NoteEditFragment.EXTRA_ITEM_ID;
import static com.jkjk.quicknote.taskeditscreen.LocationAct.EXTRA_LOCATION_LAT_LNG;
import static com.jkjk.quicknote.widget.TaskListWidget.updateTaskListWidget;

public class TaskEditFragment extends Fragment implements View.OnClickListener {

    private static final String TASK_ID = "taskId";
    public static final int TIME_NOT_SET_MILLISECOND_INDICATOR = 999;
    public static final int TIME_NOT_SET_MINUTE_SECOND_INDICATOR = 59;
    public static final int TIME_NOT_SET_HOUR_INDICATOR = 23;
    public static final long DATE_NOT_SET_INDICATOR = 9999999999999L;
    private static final int LOCATION_PICKER_REQUEST_CODE = 1;

    boolean hasTaskSave = false;

    private Task task = new Task();

    private long taskId, initEventTime;
    private EditText titleInFragment, remarkInFragment;
    private TextView dateInFragment, timeInFragment, locationInFragment;
    private Switch markAsDoneInFragment;
    private boolean newTask,
            hasModified = false,
            urgencySelectByUser = false,
            repeatSelectByUser = false,
            reminderSelectByUser = false,
            hasShowRemoveDateHint = false,
            hasShowRemoveTimeHint = false,
            notificationToolEnable;

    private Spinner urgencyInFragment, reminderInFragment, repeatInFragment;
    private int reminderPresetSize, repeatPresetSize;
    private LinearLayout timeRow, repeatRow;
    private Context context;
    private float saveButtonYPosition;
    private ArrayAdapter<String> reminderAdapter;
    private ImageButton showDropMenu;
    private FloatingActionButton saveButton;

    private ArrayList<String> reminderArray, repeatArray;

    public TaskEditFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Obtain correspond value from preferences to show appropriate size for the view
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String editViewSize = sharedPref.getString(getResources().getString(R.string.font_size_editing_screen), "m");
        notificationToolEnable = sharedPref.getBoolean(getString(R.string.notification_pin), false);
        int editViewInt;
        int spinnerDropDownInt;
        int spinnerItemInt;
        final DateFormat dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);

        switch (editViewSize) {
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

        if (savedInstanceState != null) {
            // case when restoring from saved instance
            taskId = savedInstanceState.getLong(TASK_ID, 0L);
            newTask = false;

        } else if (getArguments() != null) {
            // case when argument has data, either note ID from the note list activity or text from external intent
            taskId = getArguments().getLong(EXTRA_ITEM_ID, DEFAULT_NOTE_ID);
            newTask = (taskId == DEFAULT_NOTE_ID);

        } else {
            newTask = true;
        }


        // Reading from database
        if (!newTask) {
            Task loadedTask = Task.Companion.getTask(context, taskId);
            if (loadedTask != null) {
                task = loadedTask;
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null) {
                    notificationManager.cancel((int)taskId);
                }
            } else {
                Toast.makeText(context, R.string.error_loading, Toast.LENGTH_SHORT).show();
                hasTaskSave = true;
                if (getActivity() != null) {
                    getActivity().finish();
                }
                return view;
            }

            titleInFragment.setText(task.getTitle());
            remarkInFragment.setText(task.getContent());

        }

        if (task.isDateSet()) {
            DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
            dateInFragment.setText(dateFormat.format(task.getEventTime().getTime()));
            timeRow.setVisibility(View.VISIBLE);
            repeatRow.setVisibility(View.VISIBLE);

            // to see if the save time is default or not. If so, time will be shown as not set
            if (task.isTimeSet()) {
                DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
                timeInFragment.setText(timeFormat.format(task.getEventTime().getTime()));
            }
        }

        markAsDoneInFragment.setChecked(task.isDone());


        // Urgency
        ArrayAdapter<CharSequence> urgencyAdapter = ArrayAdapter.createFromResource(context, R.array.urgency_list, spinnerItemInt);
        urgencyAdapter.setDropDownViewResource(spinnerDropDownInt);
        urgencyInFragment.setAdapter(urgencyAdapter);
        urgencyInFragment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //To prevent initialization trigger the method when the fragment first startup. After that, allow all interaction
                if (urgencySelectByUser) {
                    hasModified = true;
                }
                urgencySelectByUser = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        urgencySelectByUser = false;
        urgencyInFragment.setSelection(task.getUrgency());

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
                if (repeatSelectByUser) {
                    hasModified = true;

//                    if select the last item, i.e. other, show the number picker
                    if (i == repeatArray.size() - 1) {
                        NumberPickerDialog numberPickerDialog = new NumberPickerDialog(context, new NumberPicker.OnValueChangeListener() {
                            @Override
                            public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {
                                task.setRepeatTime(TimeUnit.DAYS.toMillis(newValue));
                                if (repeatArray.size() > repeatPresetSize) {
                                    repeatArray.remove(0);
                                }
                                repeatArray.add(0, newValue + " " + getString(R.string.day));
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
                        switch (i) {
                            case 1:
                                task.setRepeatTime(0L);
                                break;
                            case 2:
                                task.setRepeatTime(TimeUnit.DAYS.toMillis(1));
                                break;
                            case 3:
                                task.setRepeatTime(TimeUnit.DAYS.toMillis(7));
                                break;
                            case 4:
                                task.setRepeatTime(TimeUnit.DAYS.toMillis(30));
                                break;
                            case 5:
                                task.setRepeatTime(TimeUnit.DAYS.toMillis(365));
                                break;
                        }
                        // As we remove item and mess with the position before spinner show what is selected, we need to set the selection to correct position
                        repeatSelectByUser = false;
                        repeatInFragment.setSelection(i - 1);
                    } else {
                        switch (i) {
                            case 0:
                                task.setRepeatTime(0L);
                                break;
                            case 1:
                                task.setRepeatTime(TimeUnit.DAYS.toMillis(1));
                                break;
                            case 2:
                                task.setRepeatTime(TimeUnit.DAYS.toMillis(7));
                                break;
                            case 3:
                                task.setRepeatTime(TimeUnit.DAYS.toMillis(30));
                                break;
                            case 4:
                                task.setRepeatTime(TimeUnit.DAYS.toMillis(365));
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
        if (task.getRepeatTime() == 0) {
            repeatInFragment.setSelection(0);
        } else if (task.getRepeatTime() == TimeUnit.DAYS.toMillis(1)) {
            repeatInFragment.setSelection(1);
        } else if (task.getRepeatTime() == TimeUnit.DAYS.toMillis(7)) {
            repeatInFragment.setSelection(2);
        } else if (task.getRepeatTime() == TimeUnit.DAYS.toMillis(30)) {
            repeatInFragment.setSelection(3);
        } else if (task.getRepeatTime() == TimeUnit.DAYS.toMillis(365)) {
            repeatInFragment.setSelection(4);
        } else {
            repeatArray.add(0, task.getRepeatTime() / TimeUnit.DAYS.toMillis(1) + " " + getString(R.string.day));
            repeatAdapter.notifyDataSetChanged();
            repeatInFragment.setSelection(0);
        }

        // Reminder
        reminderArray = new ArrayList<>();
        reminderArray.add(getString(R.string.no_reminder_set));
        if (task.isDateSet()) {
            reminderArray.add(getString(R.string.zero_min_before));
            if (task.isTimeSet()) {
                reminderArray.add(getString(R.string.ten_min_before));
                reminderArray.add(getString(R.string.thirty_min_before));
            }
            reminderArray.add(getString(R.string.a_day_beofre));
        }
        reminderArray.add(getString(R.string.custom));

        reminderPresetSize = reminderArray.size();

        reminderAdapter = new ArrayAdapter<>(context, spinnerItemInt, reminderArray);
        reminderAdapter.setDropDownViewResource(spinnerDropDownInt);
        reminderInFragment.setAdapter(reminderAdapter);
        reminderInFragment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // only invoked by user interaction
                if (reminderSelectByUser) {
                    hasModified = true;
                    // if select other, i.e the last item, show date & time picker
                    if (i == reminderArray.size() - 1) {
                        int yr, mn, dy;
                        if (!task.isReminderSet()) {
                            yr = Calendar.getInstance().get(Calendar.YEAR);
                            mn = Calendar.getInstance().get(Calendar.MONTH);
                            dy = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
                        } else {
                            yr = task.getReminderTime().get(Calendar.YEAR);
                            mn = task.getReminderTime().get(Calendar.MONTH);
                            dy = task.getReminderTime().get(Calendar.DAY_OF_MONTH);
                        }

                        DatePickerDialog datePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                                int hr, min;
                                if (!task.isReminderSet()) {
                                    hr = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                                    min = Calendar.getInstance().get(Calendar.MINUTE);
                                } else {
                                    hr = task.getReminderTime().get(Calendar.HOUR_OF_DAY);
                                    min = task.getReminderTime().get(Calendar.MINUTE);
                                }

                                task.getReminderTime().set(Calendar.YEAR, year);
                                task.getReminderTime().set(Calendar.MONTH, month);
                                task.getReminderTime().set(Calendar.DAY_OF_MONTH, dayOfMonth);

                                TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                                        task.getReminderTime().set(Calendar.HOUR_OF_DAY, hourOfDay);
                                        task.getReminderTime().set(Calendar.MINUTE, minute);

                                        // If user is changing custom time, remove the previous custom time, which must be the first one
                                        if (reminderArray.size() > reminderPresetSize) {
                                            reminderArray.remove(0);
                                        }

                                        //insert select time into spinner
                                        reminderArray.add(0, dateTimeFormat.format(task.getReminderTime().getTime()));
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
                                        task.getReminderTime().setTimeInMillis(0);
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

                    } else if (reminderArray.size() > reminderPresetSize) {
                        reminderArray.remove(0);
                        reminderAdapter.notifyDataSetChanged();

                        // As we remove item and mess with the position before spinner show what is selected, we need to set the selection to correct position
                        reminderSelectByUser = false;
                        reminderInFragment.setSelection(i - 1);
                    }
                }
                reminderSelectByUser = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        // Read the reminder time set by user and select the corresponding option in the spinner

        reminderSelectByUser = false;
        String customTime;
        if (!task.isReminderSet()) {
            reminderInFragment.setSelection(0);
        } else if (task.isTimeSet()) {
            if (task.getReminderTime().getTimeInMillis() == task.getEventTime().getTimeInMillis()) {
                reminderInFragment.setSelection(1);
            } else if (task.getReminderTime().getTimeInMillis() == task.getEventTime().getTimeInMillis() - 600000L) {
                reminderInFragment.setSelection(2);
            } else if (task.getReminderTime().getTimeInMillis() == task.getEventTime().getTimeInMillis() - 1800000L) {
                reminderInFragment.setSelection(3);
            } else if (task.getReminderTime().getTimeInMillis() == task.getEventTime().getTimeInMillis() - 86400000L) {
                reminderInFragment.setSelection(4);
            } else {
                customTime = dateTimeFormat.format(new Date(task.getReminderTime().getTimeInMillis()));
                reminderArray.add(0, customTime);
                reminderAdapter.notifyDataSetChanged();
                reminderInFragment.setSelection(0);
            }
        } else if (task.getEventTime().getTimeInMillis() != 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(task.getEventTime().getTimeInMillis());
            calendar.set(Calendar.MILLISECOND, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            if (task.getReminderTime().getTimeInMillis() == calendar.getTimeInMillis()) {
                reminderInFragment.setSelection(1);
            } else if (task.getReminderTime().getTimeInMillis() == calendar.getTimeInMillis() - 86400000L) {
                reminderInFragment.setSelection(2);
            } else {
                customTime = dateTimeFormat.format(new Date(task.getReminderTime().getTimeInMillis()));
                reminderArray.add(0, customTime);
                reminderAdapter.notifyDataSetChanged();
                reminderInFragment.setSelection(0);
            }
        } else {
            customTime = dateTimeFormat.format(new Date(task.getReminderTime().getTimeInMillis()));
            reminderArray.add(0, customTime);
            reminderAdapter.notifyDataSetChanged();
            reminderInFragment.setSelection(0);
        }


        dateInFragment.setOnClickListener(this);

        // Allow removing date when long click
        dateInFragment.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (task.isDateSet()) {
                    task.removeDate();
                    dateInFragment.setText("");
                    timeRow.setVisibility(View.GONE);
                    task.setRepeatTime(0L);
                    repeatRow.setVisibility(View.GONE);
                    if (repeatArray.size() > repeatPresetSize) {
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

                    if (task.isReminderSet() && !currentReminderSelection.equals(getString(R.string.no_reminder_set))) {
                        reminderArray.add(0, dateTimeFormat.format(new Date(task.getReminderTime().getTimeInMillis())));
                        reminderAdapter.notifyDataSetChanged();
                    }
                    reminderSelectByUser = false;
                    reminderInFragment.setSelection(0);
                    return true;
                } else return false;
            }
        });

        timeInFragment.setOnClickListener(this);

        timeInFragment.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                task.removeTime();

                hasModified = true;
                timeInFragment.setText("");

                reminderArray.clear();
                reminderArray.add(getString(R.string.no_reminder_set));
                reminderArray.add(getString(R.string.zero_min_before));
                reminderArray.add(getString(R.string.a_day_beofre));
                reminderArray.add(getString(R.string.custom));
                reminderAdapter.notifyDataSetChanged();

                if (task.isReminderSet()) {
                    reminderArray.add(0, dateTimeFormat.format(new Date(task.getReminderTime().getTimeInMillis())));
                    reminderAdapter.notifyDataSetChanged();
                    reminderSelectByUser = false;
                    reminderInFragment.setSelection(0);
                }
                return true;
            }
        });

        showDropMenu = view.findViewById(R.id.task_show_drop_menu);
        showDropMenu.setOnClickListener(this);

//        if (latLng != null){
//            getLocationText(latLng);
//        }
        if (task.getPlaceName() != null) locationInFragment.setText(task.getPlaceName());
        locationInFragment.setOnClickListener(this);
        locationInFragment.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                locationInFragment.setText(null);
                task.setPlaceName(null);
                task.setLatLng(null);
                hasModified = true;
                return true;
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOCATION_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            LatLng resultLatLng = data.getParcelableExtra(LocationAct.EXTRA_LOCATION_LAT_LNG);
            String name = data.getStringExtra(LocationAct.EXTRA_LOCATION_NAME);
            task.setLatLng(resultLatLng);

            // Check if the name is coordinate. If so, check if geo coder can return a readable name
            if (name.contains("\"") && name.contains("\'") && name.contains("°") && name.contains(".")) {
                task.setPlaceName(getLocationText(resultLatLng));
            } else {
                task.setPlaceName(name);
            }
            locationInFragment.setText(task.getPlaceName());
            hasModified = true;
//            getLocationText(latLng);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Define save function for the done button
        if (getActivity() != null) {
            saveButton = getActivity().findViewById(R.id.save_fab);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                saveButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.sharp_save_24));
            } else {
                saveButton.setImageResource(R.drawable.sharp_save_24);
            }
            saveButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.highlight)));
            saveButton.setOnClickListener(this);

            saveButtonYPosition = saveButton.getY();
            saveButton.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    float y = saveButton.getY();
                    if (y < saveButtonYPosition) {
                        if (remarkInFragment.hasFocus()) {
                            saveButton.setVisibility(View.INVISIBLE);

                        }
                    } else {
                        saveButton.setVisibility(View.VISIBLE);
                    }
                    saveButtonYPosition = y;

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
        task.setTitle(titleInFragment.getText().toString());
        task.setContent(remarkInFragment.getText().toString());
        task.setDone(markAsDoneInFragment.isChecked());
        initEventTime = task.getEventTime().getTimeInMillis();
        hasTaskSave = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        //when user quit the app without choosing save or discard, save the task
        if (!hasTaskSave && checkModified()) {
            if (getActivity() == null || getActivity().isFinishing()) saveTask();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (!hasTaskSave && checkModified()) saveTask();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.item_date:
                // Only show hint in the first time for every instance of the activity
                if (!hasShowRemoveDateHint) {
                    hasShowRemoveDateHint = true;
                    Toast.makeText(context, R.string.remove_date_hint, Toast.LENGTH_SHORT).show();
                }

                int year, month, dayOfMonth;
                if (!task.isDateSet()) {
                    year = Calendar.getInstance().get(Calendar.YEAR);
                    month = Calendar.getInstance().get(Calendar.MONTH);
                    dayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
                } else {
                    year = task.getEventTime().get(Calendar.YEAR);
                    month = task.getEventTime().get(Calendar.MONTH);
                    dayOfMonth = task.getEventTime().get(Calendar.DAY_OF_MONTH);
                }

                new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        if (!task.isDateSet()) {
                            task.removeTime();

                            if (!task.isReminderSet()) {
                                reminderSelectByUser = false;
                                reminderInFragment.setSelection(1);
                            }
                        }
                        task.getEventTime().set(Calendar.YEAR, year);
                        task.getEventTime().set(Calendar.MONTH, month);
                        task.getEventTime().set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
                        dateInFragment.setText(dateFormat.format(task.getEventTime().getTime()));

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
                break;

            case R.id.task_time:
                // Only show hint in the first time for every instance of the activity
                if (!hasShowRemoveTimeHint) {
                    hasShowRemoveTimeHint = true;
                    Toast.makeText(context, R.string.remove_time_hint, Toast.LENGTH_SHORT).show();
                }
                int hourOfDay, minute;
                // Change hour and minute shown as current time or event time
                if (!task.isTimeSet()) {
                    hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                    minute = Calendar.getInstance().get(Calendar.MINUTE);
                } else {
                    hourOfDay = task.getEventTime().get(Calendar.HOUR_OF_DAY);
                    minute = task.getEventTime().get(Calendar.MINUTE);
                }

                new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                        task.getEventTime().set(Calendar.HOUR_OF_DAY, hourOfDay);
                        task.getEventTime().set(Calendar.MINUTE, minute);
                        DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
                        timeInFragment.setText(timeFormat.format(task.getEventTime().getTime()));
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
                break;

            case R.id.task_show_drop_menu:
                PopupMenu editDropMenu = new PopupMenu(context, showDropMenu);
                editDropMenu.inflate(R.menu.task_edit_drop_menu);

                SharedPreferences idPref = context.getSharedPreferences(PINNED_NOTIFICATION_IDS, MODE_PRIVATE);

                MenuItem pinToNotification = editDropMenu.getMenu().findItem(R.id.edit_drop_menu_pin);
                if (!newTask && idPref.getLong(Long.toString(taskId), 999999L) != 999999L) {
                    pinToNotification.setTitle(R.string.notification_unpin);
                } else {
                    pinToNotification.setTitle(R.string.notification_pin);
                }

                pinToNotification.setVisible(true);

                editDropMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.edit_drop_menu_delete:
                                //Delete task
                                new AlertDialog.Builder(context).setTitle(R.string.delete_title).setMessage(R.string.confirm_delete_edit)
                                        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        if (!newTask) {
                                                            Task.Companion.delete(context, taskId);
                                                            updateTaskListWidget(context);
                                                            AlarmHelper.cancelReminder(context.getApplicationContext(), taskId);

                                                            SharedPreferences idPref = context.getSharedPreferences(PINNED_NOTIFICATION_IDS, MODE_PRIVATE);
                                                            if (idPref.getLong(Long.toString(taskId), 999999L) != 999999L) {
                                                                idPref.edit().remove(Long.toString(taskId)).apply();
                                                                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                                                if (notificationManager != null) {
                                                                    notificationManager.cancel((int) taskId * PIN_ITEM_NOTIFICATION_ID);
                                                                }
                                                            }
                                                            updateToolbar();
                                                        }
                                                        // No need to do saving
                                                        hasTaskSave = true;
                                                        Toast.makeText(context, R.string.task_deleted_toast, Toast.LENGTH_SHORT).show();
                                                        if (getActivity() != null) {
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
                                if (markAsDoneInFragment.isChecked()) {
                                    stringBuilder.append("[").append(getString(R.string.done)).append("] ");
                                } else if (urgencyInFragment.getSelectedItemPosition() == 1) {
                                    stringBuilder.append("[").append(getString(R.string.important)).append("] ");
                                } else if (urgencyInFragment.getSelectedItemPosition() == 2) {
                                    stringBuilder.append("[").append(getString(R.string.asap)).append("] ");
                                }
                                stringBuilder.append(titleInFragment.getText().toString());
                                if (task.isDateSet()) {
                                    stringBuilder.append("\n").append(dateInFragment.getText());
                                }
                                if (task.isTimeSet()) {
                                    stringBuilder.append(" ").append(timeInFragment.getText().toString());
                                }
                                stringBuilder.append("\n").append(remarkInFragment.getText().toString());
                                shareIntent.putExtra(Intent.EXTRA_TEXT, stringBuilder.toString());
                                shareIntent.putExtra(Intent.EXTRA_TITLE, titleInFragment.getText());
                                Intent chooser = Intent.createChooser(shareIntent, getString(R.string.share));
                                startActivity(chooser);
                                return true;
                            case R.id.edit_drop_menu_pin:
                                // if it is a new note, save the note and then pin to notification
                                if (newTask) {
                                    saveTask();
                                    hasTaskSave = false;
                                }

                                SharedPreferences idPref = context.getSharedPreferences(PINNED_NOTIFICATION_IDS, MODE_PRIVATE);

                                if (idPref.getLong(Long.toString(taskId), 999999L) == 999999L) {
                                    pinTaskToNotification();
                                } else {
                                    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                    if (notificationManager != null) {
                                        // to distinguish reminder and pin item, id of pin item is the item id * PIN_ITEM_NOTIFICATION_ID
                                        notificationManager.cancel((int) taskId * PIN_ITEM_NOTIFICATION_ID);
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
                break;

            case R.id.item_location:
//                LatLng latLng = task.getLatLng();
//                LatLngBounds.Builder latLngBoundsBuilder = LatLngBounds.builder();
//                if (latLng != null) {
//                    latLngBoundsBuilder.include(latLng);
//                    //Make sure the zoom scale is appropriate
//                    latLngBoundsBuilder.include(SphericalUtil.computeOffset(latLng, LOCATION_PICKER_ZOOM_OUT_METER, 0));
//                    latLngBoundsBuilder.include(SphericalUtil.computeOffset(latLng, LOCATION_PICKER_ZOOM_OUT_METER, 90));
//                    latLngBoundsBuilder.include(SphericalUtil.computeOffset(latLng, LOCATION_PICKER_ZOOM_OUT_METER, 180));
//                    latLngBoundsBuilder.include(SphericalUtil.computeOffset(latLng, LOCATION_PICKER_ZOOM_OUT_METER, 270));
//                } else {
//                    latLngBoundsBuilder.include(new LatLng(0.0, 0.0));
//                }

                startActivityForResult(new Intent(context, LocationAct.class)
                        .putExtra(EXTRA_LOCATION_LAT_LNG, task.getLatLng())
                        , LOCATION_PICKER_REQUEST_CODE);
                break;
            case R.id.save_fab:
                saveTask();
                hasTaskSave = true;
                if (getActivity() != null) getActivity().finish();
                break;
        }
    }

    public void saveTask() {

        String title = titleInFragment.getText().toString().trim();
        if (title.length() < 1) {
            title = getString(R.string.untitled_task);
        }
        task.setTitle(title);
        task.setContent(remarkInFragment.getText().toString());

        initEventTime = task.getEventTime().getTimeInMillis();
        if (task.getRepeatTime() <= 0 || initEventTime == DATE_NOT_SET_INDICATOR) {
            task.setRepeatTime(0L);
        }

        task.setUrgency(urgencyInFragment.getSelectedItemPosition());

        task.setDone(markAsDoneInFragment.isChecked() && task.getRepeatTime() == 0);

        if (markAsDoneInFragment.isChecked() && task.getRepeatTime() > 0) {
            long eT = task.getEventTime().getTimeInMillis();
            eT += task.getRepeatTime();
            task.getEventTime().setTimeInMillis(eT);
        }

        if (reminderInFragment.getSelectedItem().toString().equals(getString(R.string.no_reminder_set))) {
            AlarmHelper.cancelReminder(context.getApplicationContext(), taskId);
            task.getReminderTime().setTimeInMillis(0L);

        } else {
            if (reminderInFragment.getSelectedItem().toString().equals(getString(R.string.zero_min_before))) {
                if (task.isTimeSet()) {
                    task.getReminderTime().setTimeInMillis(task.getEventTime().getTimeInMillis());
                } else {
                    task.getReminderTime().setTimeInMillis(task.getEventTime().getTimeInMillis());
                    task.getReminderTime().set(Calendar.MILLISECOND, 0);
                    task.getReminderTime().set(Calendar.SECOND, 0);
                    task.getReminderTime().set(Calendar.MINUTE, 0);
                    task.getReminderTime().set(Calendar.HOUR_OF_DAY, 0);
                }

            } else if (reminderInFragment.getSelectedItem().toString().equals(getString(R.string.ten_min_before))) {
                task.getReminderTime().setTimeInMillis(task.getEventTime().getTimeInMillis() - 600000L);
            } else if (reminderInFragment.getSelectedItem().toString().equals(getString(R.string.thirty_min_before))) {
                task.getReminderTime().setTimeInMillis(task.getEventTime().getTimeInMillis() - 1800000L);
            } else if (reminderInFragment.getSelectedItem().toString().equals(getString(R.string.a_day_beofre))) {
                if (task.isTimeSet()) {
                    task.getReminderTime().setTimeInMillis(task.getEventTime().getTimeInMillis() - 86400000L);
                } else {
                    task.getReminderTime().setTimeInMillis(task.getEventTime().getTimeInMillis());
                    task.getReminderTime().set(Calendar.MILLISECOND, 0);
                    task.getReminderTime().set(Calendar.SECOND, 0);
                    task.getReminderTime().set(Calendar.MINUTE, 0);
                    task.getReminderTime().set(Calendar.HOUR_OF_DAY, 0);
                    task.getReminderTime().setTimeInMillis(task.getReminderTime().getTimeInMillis() - 86400000L);
                }
            }
            if (markAsDoneInFragment.isChecked() && task.getRepeatTime() > 0) {
                task.getReminderTime().setTimeInMillis(task.getReminderTime().getTimeInMillis() + task.getRepeatTime());
            }
        }

        if (!newTask) {
            task.save(context, taskId);
            SharedPreferences idPref = context.getApplicationContext().getSharedPreferences(PINNED_NOTIFICATION_IDS, MODE_PRIVATE);
            if (idPref.getLong(Long.toString(taskId), 999999L) != 999999L) {
                pinTaskToNotification();
            }
////            to update pinned notification if there is any, api 23 up exclusive
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                if(isItemAnActiveNotification()){
//                    pinTaskToNotification();
//                }
//            }
        } else {
            Long newId = task.saveAsNew(context);
            if (newId != null) {
                taskId = newId;
            }
        }
        if (task.getReminderTime().getTimeInMillis() > 0) {
            AlarmHelper.setReminder(context.getApplicationContext(), taskId, task.getReminderTime().getTimeInMillis());
        }
        hasTaskSave = true;
        newTask = false;
        updateTaskListWidget(context);
        updateToolbar();
        Toast.makeText(getActivity(), R.string.saved_task, Toast.LENGTH_SHORT).show();

    }

    private String getLocationText(LatLng latLng) {
        try {
            Geocoder geocoder;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                geocoder = new Geocoder(context, context.getResources().getConfiguration().locale);
            } else
                geocoder = new Geocoder(context, context.getResources().getConfiguration().getLocales().get(0));

            List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addressList.size() == 0) {
                DecimalFormat decimalFormat = new DecimalFormat("#.00000");
                return getString(R.string.lat_lng, decimalFormat.format(latLng.latitude), decimalFormat.format(latLng.longitude));
            } else {
                Address address = addressList.get(0);
                String featureName = address.getFeatureName();
                if (featureName != null && !featureName.isEmpty() && !isNumericString(featureName)) {
                    return featureName;
                } else {
                    StringBuilder stringBuilder = new StringBuilder();
                    String addressLine = address.getAddressLine(0);
                    for (int i = 1; addressLine != null; i++) {
                        stringBuilder.append(addressLine).append(" ");
                        addressLine = address.getAddressLine(i);
                    }
                    return stringBuilder.toString();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            DecimalFormat decimalFormat = new DecimalFormat("#.00000");
            return getString(R.string.lat_lng, decimalFormat.format(latLng.latitude), decimalFormat.format(latLng.longitude));
        }
    }

    private void pinTaskToNotification() {
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
            return hasModified
                    || markAsDoneInFragment.isChecked()
                    || !titleInFragment.getText().toString().isEmpty()
                    || !remarkInFragment.getText().toString().isEmpty();
        } else {
            return hasModified
                    || markAsDoneInFragment.isChecked() != task.isDone()
                    || !titleInFragment.getText().toString().equals(task.getTitle())
                    || !remarkInFragment.getText().toString().equals(task.getContent());
        }
    }

    private void updateToolbar() {
        if (notificationToolEnable && DateUtils.isToday(task.getEventTime().getTimeInMillis())) {

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

    public static TaskEditFragment newEditFragmentInstance(long taskId) {
        TaskEditFragment fragment = new TaskEditFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(EXTRA_ITEM_ID, taskId);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static TaskEditFragment newEditFragmentInstance() {
        return new TaskEditFragment();
    }

    private boolean isNumericString(String string) {
        try {
            int result = Integer.parseInt(string.trim());
            Log.i(getClass().getName(), "Confirm " + string + "is number" + Integer.toString(result));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
