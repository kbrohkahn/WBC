package org.boardgamers.wbc;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.List;

public class DialogPreferenceSchedulePicker extends DialogPreference {
  private RadioGroup group;
  private List<User> users;
  private int value;

  public DialogPreferenceSchedulePicker(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected View onCreateDialogView() {
    LayoutInflater inflater=
        (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View view=inflater.inflate(R.layout.dialog_radio, null);

    group=(RadioGroup) view.findViewById(R.id.dialog_radio_group);

    WBCDataDbHelper dbHelper=new WBCDataDbHelper(getContext());
    dbHelper.getReadableDatabase();
    users=dbHelper.getUsers(null);
    dbHelper.close();

    RadioButton button;
    for (int i=0; i<users.size(); i++) {
      button=new RadioButton(getContext());
      button.setId(users.get(i).id);
      button.setText(users.get(i).name);
      group.addView(button);
    }

    value=getPersistedInt(SettingsActivity.currentUserId);
    group.check(value);

    return view;
  }

  @Override
  protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
    if (restorePersistedValue) {
      value=getPersistedInt(SettingsActivity.currentUserId);
    } else {
      value=(Integer) defaultValue;
      if (persistInt(value)) {
        Log.d("", "Success");
      }
      ;
    }
  }

  @Override
  protected Object onGetDefaultValue(TypedArray a, int index) {
    return a.getInteger(index, MainActivity.PRIMARY_USER_ID);
  }

  @Override
  protected void onDialogClosed(boolean positiveResult) {
    if (positiveResult) {
      value=group.getCheckedRadioButtonId();
      persistInt(value);

      SettingsActivity.currentUserId=value;
      SettingsActivity.SettingsFragment.updatePreferences();
    }
  }
}
