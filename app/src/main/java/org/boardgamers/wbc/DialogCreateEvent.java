package org.boardgamers.wbc;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Created by Kevin on 4/24/2015
 * Dialog shown for creating user events
 */
public class DialogCreateEvent extends DialogFragment {

  // final String TAG="WBC CreateEventDialog";
  private final int[] ceDayIDs=
      {R.id.ce_repeat_d0, R.id.ce_repeat_d1, R.id.ce_repeat_d2, R.id.ce_repeat_d3,
          R.id.ce_repeat_d4, R.id.ce_repeat_d5, R.id.ce_repeat_d6, R.id.ce_repeat_d7,
          R.id.ce_repeat_d8};

  protected TextView dialogTitle;
  protected Spinner hourSpinner;
  protected Spinner durationSpinner;
  protected Button add;
  protected EditText titleET;
  protected Button cancel;
  protected EditText locationET;

  protected TextView daysTV;
  protected LinearLayout daysLL;
  private CheckBox[] days;

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    Dialog dialog=super.onCreateDialog(savedInstanceState);
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

    return dialog;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view=inflater.inflate(R.layout.dialog_event, container);

    dialogTitle=(TextView) view.findViewById(R.id.ce_dialog_title);
    dialogTitle.setText(getResources().getString(R.string.create_event));

    daysTV=(TextView) view.findViewById(R.id.ce_days);
    daysTV.setVisibility(View.VISIBLE);
    daysLL=(LinearLayout) view.findViewById(R.id.ce_days_layout);
    daysLL.setVisibility(View.VISIBLE);

    hourSpinner=(Spinner) view.findViewById(R.id.ce_hour);

    SharedPreferences settings=getActivity()
        .getSharedPreferences(getResources().getString(R.string.sp_file_name),
            Context.MODE_PRIVATE);

    boolean hours24=settings.getBoolean("24_hour", true);
    int hoursID=(hours24 ? R.array.hours_24 : R.array.hours_12);

    ArrayAdapter<CharSequence> hourA=ArrayAdapter
        .createFromResource(getActivity(), hoursID, android.R.layout.simple_spinner_item);
    hourA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    hourSpinner.setAdapter(hourA);

    durationSpinner=(Spinner) view.findViewById(R.id.ce_duration);
    ArrayAdapter<CharSequence> durationA=ArrayAdapter
        .createFromResource(getActivity(), R.array.duration, android.R.layout.simple_spinner_item);
    durationA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    durationSpinner.setAdapter(durationA);

    add=(Button) view.findViewById(R.id.ce_add);
    add.setEnabled(false);
    add.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        createEvent();
        getDialog().dismiss();
      }
    });
    cancel=(Button) view.findViewById(R.id.ce_cancel);
    cancel.setEnabled(true);
    cancel.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        getDialog().dismiss();
      }
    });

    titleET=(EditText) view.findViewById(R.id.ce_title);
    titleET.addTextChangedListener(new TextWatcher() {

      @Override
      public void afterTextChanged(Editable arg0) {
        checkButton();

      }

      @Override
      public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
      }

      @Override
      public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
      }
    });

    locationET=(EditText) view.findViewById(R.id.ce_location);
    locationET.addTextChangedListener(new TextWatcher() {

      @Override
      public void afterTextChanged(Editable arg0) {
        checkButton();
      }

      @Override
      public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
      }

      @Override
      public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
      }
    });

    String[] dayStrings=getResources().getStringArray(R.array.days);
    days=new CheckBox[dayStrings.length];
    for (int i=0; i<dayStrings.length; i++) {
      days[i]=(CheckBox) view.findViewById(ceDayIDs[i]);
      days[i].setText(dayStrings[i]);
      days[i].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
          checkButton();
        }
      });
    }
    return view;
  }

  public void checkButton() {
    add.setEnabled(false);

    if (titleET.getText().toString().length()>0 && locationET.getText().toString().length()>0) {
      for (CheckBox checkBox : days) {
        if (checkBox.isChecked()) {
          add.setEnabled(true);
          break;
        }
      }
    }
  }

  public void createEvent() {
    String title=titleET.getText().toString();
    int hour=hourSpinner.getSelectedItemPosition()+7;
    int duration=durationSpinner.getSelectedItemPosition()+1;
    String location=locationET.getText().toString();

    WBCDataDbHelper dbHelper=new WBCDataDbHelper(getActivity());
    dbHelper.getWritableDatabase();

    String identifier;
    long id;
    for (int day=0; day<days.length; day++) {
      if (days[day].isChecked()) {
        identifier=String.valueOf(day*24+hour)+title;

        id=dbHelper
            .insertEvent(identifier, -1, day, hour, title, "", "", false, duration, false, duration,
                location, true);

        MainActivity.SELECTED_EVENT_ID=id;
      }
    }
    dbHelper.close();
  }
}
