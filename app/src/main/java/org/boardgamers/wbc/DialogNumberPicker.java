package org.boardgamers.wbc;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;

/**
 * A {@link android.preference.Preference} that displays a number picker as a dialog.
 */
public class DialogNumberPicker extends DialogPreference {

  public static final int MAX_VALUE=60;
  public static final int MIN_VALUE=0;
  public static final int DEFAULT_VALUE=5;

  private NumberPicker picker;
  private int value;

  public DialogNumberPicker(Context context, AttributeSet attrs) {
    super(context, attrs);
    setDialogLayoutResource(R.layout.dialog_notify_time);


    setPositiveButtonText(android.R.string.ok);
    setNegativeButtonText(android.R.string.cancel);

    setDialogIcon(null);

  }

  @Override
  protected View onCreateDialogView() {
//    NumberPicker picker=(NumberPicker) getDialog().findViewById(R.id.dialog_notify_time_picker);
//    picker.setMinValue(MIN_VALUE);
//    picker. setMaxValue(MAX_VALUE);
    return super.onCreateDialogView();
  }

  @Override
  protected void onDialogClosed(boolean positiveResult) {
    if (positiveResult) {
      setValue(value);
    }
  }

  @Override
  protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
    if (restorePersistedValue) {
      // Restore existing state
      value=getPersistedInt(DEFAULT_VALUE);
    } else {
      // Set default state from the XML attribute
      value=(Integer) defaultValue;
      persistInt(value);
    }
  }

  public void setValue(int v) {
    value=v;
  }

  @Override
  protected Object onGetDefaultValue(TypedArray a, int index) {
    return a.getInteger(index, DEFAULT_VALUE);
  }

}
