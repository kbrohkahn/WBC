package org.boardgamers.wbc;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;

public class DialogPreferenceNumberPicker extends DialogPreference {

    public static final int MAX_VALUE = 60;
    public static final int MIN_VALUE = 0;
    public static final int DEFAULT_VALUE = 5;

    private NumberPicker picker;

    int value;

    public DialogPreferenceNumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View onCreateDialogView() {
        View view = View.inflate(getContext(), R.layout.dialog_notify_time, null);

        picker = (NumberPicker) view.findViewById(R.id.dialog_notify_time_picker);
        picker.setMaxValue(MAX_VALUE);
        picker.setMinValue(MIN_VALUE);
        picker.setValue(getPersistedInt(DEFAULT_VALUE));
        picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        picker.setWrapSelectorWheel(false);

        return view;
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            value = getPersistedInt(DEFAULT_VALUE);
        } else {
            value = (Integer) defaultValue;
            persistInt(value);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInteger(index, DEFAULT_VALUE);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            SettingsActivity.notifyChanged = true;
            persistInt(picker.getValue());
            setSummary(String.valueOf(picker.getValue()));
        }
    }
}
