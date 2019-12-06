package com.jkjk.quicknote.taskeditscreen;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import com.jkjk.quicknote.R;

public class NumberPickerDialog extends AlertDialog implements DialogInterface.OnClickListener, NumberPicker.OnValueChangeListener {

    private NumberPicker.OnValueChangeListener mCallback;
    private NumberPicker numberPicker;
    private int oldValue, newValue, minValue, maxValue;

    private static final String MIN_VALUE_FOR_NUM_PICKER = "minimum value";
    private static final String MAX_VALUE_FOR_NUM_PICKER = "maximum value";
    private static final String CURRENT_VALUE_FOR_NUM_PICKER = "current value";

    // A dialog class that include a single number picker within itself with positive button. pressing the button will trigger the listener which return the value selected
    NumberPickerDialog(Context context, NumberPicker.OnValueChangeListener onValueChangeListener, int minValue, int maxValue){
        super(context);

        setTitle(R.string.repeat_interval);
        setButton(BUTTON_POSITIVE, context.getText(R.string.done), this);

        mCallback = onValueChangeListener;
        if (minValue < 1){
            this.minValue = 1;
        } else {
            this.minValue = minValue;
        }
        this.maxValue = maxValue;

        LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.number_picker_dialog, null);
        setView(view);

        numberPicker = view.findViewById(R.id.number_picker);
        numberPicker.setMaxValue(maxValue);
        numberPicker.setMinValue(minValue);
        numberPicker.setValue(minValue);
        newValue = minValue;
        numberPicker.setOnValueChangedListener(this);

    }

    @NonNull
    @Override
    public Bundle onSaveInstanceState() {
        Bundle bundle = super.onSaveInstanceState();
        bundle.putInt(MIN_VALUE_FOR_NUM_PICKER, minValue);
        bundle.putInt(MAX_VALUE_FOR_NUM_PICKER, maxValue);
        bundle.putInt(CURRENT_VALUE_FOR_NUM_PICKER, numberPicker.getValue());
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        numberPicker.setMinValue(savedInstanceState.getInt(MIN_VALUE_FOR_NUM_PICKER, 1));
        numberPicker.setMaxValue(savedInstanceState.getInt(MAX_VALUE_FOR_NUM_PICKER, 999));
        numberPicker.setValue(savedInstanceState.getInt(CURRENT_VALUE_FOR_NUM_PICKER, 1));
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        if (i == BUTTON_POSITIVE){
            mCallback.onValueChange(numberPicker, oldValue, newValue);
        }
    }

    @Override
    public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
}
