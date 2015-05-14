package org.boardgamers.wbc;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kevin on 4/24/2015
 * Dialog shown for creating user events
 */
public class DialogCreateEvent extends DialogFragment {
  private final String TAG="WBC CreateEventDialog";

  private final int[] ceDayIDs=
      {R.id.ce_repeat_d0, R.id.ce_repeat_d1, R.id.ce_repeat_d2, R.id.ce_repeat_d3,
          R.id.ce_repeat_d4, R.id.ce_repeat_d5, R.id.ce_repeat_d6, R.id.ce_repeat_d7,
          R.id.ce_repeat_d8};

  protected Button add;
  protected Spinner hourSpinner;
  protected Spinner durationSpinner;
  protected EditText titleET;
  protected EditText locationET;

  protected TextView daysTV;
  protected LinearLayout daysLL;
  private CheckBox[] days;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view=inflater.inflate(R.layout.dialog_event, container);

    getDialog().setTitle(getResources().getString(R.string.create_event));

    titleET=(EditText) view.findViewById(R.id.ce_title);
    locationET=(EditText) view.findViewById(R.id.ce_location);

    daysTV=(TextView) view.findViewById(R.id.ce_days);
    daysTV.setVisibility(View.VISIBLE);
    daysLL=(LinearLayout) view.findViewById(R.id.ce_days_layout);
    daysLL.setVisibility(View.VISIBLE);

    hourSpinner=(Spinner) view.findViewById(R.id.ce_hour);

    ArrayAdapter<CharSequence> hourA=ArrayAdapter
        .createFromResource(getActivity(), R.array.hours_24, android.R.layout.simple_spinner_item);
    hourA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    hourSpinner.setAdapter(hourA);

    durationSpinner=(Spinner) view.findViewById(R.id.ce_duration);
    ArrayAdapter<CharSequence> durationA=ArrayAdapter
        .createFromResource(getActivity(), R.array.duration, android.R.layout.simple_spinner_item);
    durationA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    durationSpinner.setAdapter(durationA);

    add=(Button) view.findViewById(R.id.ce_add);
    add.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        createEvents();
      }
    });

    Button cancel=(Button) view.findViewById(R.id.ce_cancel);
    cancel.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dismiss();
      }
    });

    String[] dayStrings=getResources().getStringArray(R.array.days);
    days=new CheckBox[dayStrings.length];
    for (int i=0; i<dayStrings.length; i++) {
      days[i]=(CheckBox) view.findViewById(ceDayIDs[i]);
      days[i].setText(dayStrings[i]);
    }
    return view;
  }

  public void createEvents() {
    String title=titleET.getText().toString();
    int hour=hourSpinner.getSelectedItemPosition()+7;
    int duration=durationSpinner.getSelectedItemPosition()+1;
    String location=locationET.getText().toString();

    WBCDataDbHelper dbHelper=new WBCDataDbHelper(getActivity());
    dbHelper.getWritableDatabase();

    int id=MainActivity.USER_EVENT_ID+dbHelper.getNumUserEvents();
    List<Event> newEvents=new ArrayList<>();
    Event event;
    for (int day=0; day<days.length; day++) {
      if (days[day].isChecked()) {
        dbHelper.insertUserEvent(id, MainActivity.userId, title, day, hour, duration, location);
        event=
            new Event(id, MainActivity.USER_EVENT_ID+MainActivity.userId, day, hour, title, "", "",
                false, duration, false, duration, location);
        newEvents.add(event);
        id++;
      }
    }

    dbHelper.close();

    MainActivity.changeEvents(getActivity(), newEvents.toArray(new Event[newEvents.size()]), 3);

    dismiss();
  }
}
