package org.boardgamers.wbc;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment containing user's WBC data, including tournament finishes, help notes, and created events
 */
public class UserDataFragment extends Fragment {
  private static ArrayList<Event> userEvents;
  private static ImageView userEventsStarIV;
  private static boolean allStarred;

  private static LinearLayout userEventsLayout;
  private static int selectedEvent;
  private static LayoutInflater userEventLayoutInflater;
  private final String TAG = "My WBC Data Activity";

  public static void changeEventStar(String id, boolean starred,
                                     Activity context) {
    for (Event event : userEvents) {
      if (event.identifier.equalsIgnoreCase(id)) {
        event.starred = starred;

        // will be null if calling from user event
        updateUserEventList(MainActivity.activity);

        // update in schedule activity
        ArrayList<Event> eventList = MainActivity.dayList.get(event.day).get(
            event.hour - 6);
        for (Event tempE : eventList) {
          if (tempE.identifier.equalsIgnoreCase(event.identifier)) {
            tempE.starred = starred;
            break;
          }
        }
        if (starred)
          MainActivity.addStarredEvent(event);
        else
          MainActivity.removeStarredEvent(event.identifier, event.day);

        // update in event fragment
        EventFragment fragment = (EventFragment) context.getFragmentManager()
            .findFragmentById(R.id.eventFragment);
        if (fragment != null && fragment.isInLayout() && MainActivity.SELECTED_EVENT_ID.equalsIgnoreCase(id)) {
          EventFragment.star.setImageResource(starred ? R.drawable.star_on
              : R.drawable.star_off);
        }

        return;
      }
    }
  }

  /**
   * Game star button clicked - change allStarred boolean and update events
   */
  public static void changeAllStarred(Activity a) {
    allStarred = !allStarred;
    setGameStar();

    for (Event event : userEvents) {
      if (event.starred ^ allStarred) {
        changeEventStar(event.identifier, allStarred, a);
      }
    }
  }

  /**
   * set userEventsStarIV image resource
   */
  public static void setGameStar() {
    userEventsStarIV.setImageResource(allStarred ? R.drawable.star_on
        : R.drawable.star_off);
  }

  /**
   * Event star changed - check for change in allStarred boolean and set game star image view.
   * Call setGameStar before return
   */
  public static void setAllStared() {
    allStarred = true;
    for (Event tEvent : userEvents) {
      if (!tEvent.starred) {
        allStarred = false;
        return;
      }
    }
    setGameStar();
  }

  private static void saveUserEvents(Context context) {
    final Resources resources = context.getResources();

    SharedPreferences.Editor editor = context.getSharedPreferences(
        resources.getString(R.string.sp_file_name),
        Context.MODE_PRIVATE).edit();
    String userEventPrefString = resources.getString(R.string.sp_user_event);
    String starPrefString = resources.getString(R.string.sp_event_starred);

    String saveString;
    Event event;
    String breakCharacter = "~";

    int i = 0;
    for (; i < userEvents.size(); i++) {
      event = userEvents.get(i);

      saveString = String.valueOf(event.day) + breakCharacter
          + String.valueOf(event.hour) + breakCharacter + event.title
          + breakCharacter + String.valueOf(event.duration)
          + breakCharacter + event.location;
      editor.putString(userEventPrefString + String.valueOf(i), saveString);
      editor.putBoolean(
          starPrefString + String.valueOf(MainActivity.NUM_EVENTS + i),
          event.starred);

    }
    editor.putString(userEventPrefString + String.valueOf(i), "");
    editor.apply();
  }

  public static void updateUserEventList(final Context context) {
    userEventsLayout.removeAllViews();
    for (int i = 0; i < userEvents.size(); i++) {
      final Event event = userEvents.get(i);

      LinearLayout layout = (LinearLayout) userEventLayoutInflater.inflate(R.layout.tournament_item, null);

      boolean started = event.day * 24 + event.hour <= MainActivity.currentDay * 24 + MainActivity.currentHour;
      boolean ended = event.day * 24 + event.hour + event.totalDuration <= MainActivity.currentDay
          * 24 + MainActivity.currentHour;
      boolean happening = started && !ended;

      if (event.identifier
          .equalsIgnoreCase(MainActivity.SELECTED_EVENT_ID))
        layout.setBackgroundResource(R.drawable.selected);
      else if (i % 2 == 0) {
        if (ended)
          layout.setBackgroundResource(R.drawable.ended_light);
        else if (happening)
          layout.setBackgroundResource(R.drawable.current_light);
        else
          layout.setBackgroundResource(R.drawable.future_light);
      } else {
        if (ended)
          layout.setBackgroundResource(R.drawable.ended_dark);
        else if (happening)
          layout.setBackgroundResource(R.drawable.current_dark);
        else
          layout.setBackgroundResource(R.drawable.future_dark);
      }

      int tColor = MainActivity.getTextColor(event);
      int tType = MainActivity.getTextStyle(event);

      TextView title = (TextView) layout.findViewById(R.id.gi_name);
      title.setTypeface(null, tType);
      title.setTextColor(tColor);
      title.setText(event.title);

      TextView day = (TextView) layout.findViewById(R.id.gi_date);
      day.setTypeface(null, tType);
      day.setTextColor(tColor);
      day.setText(MainActivity.dayStrings[event.day]);

      TextView time = (TextView) layout.findViewById(R.id.gi_time);
      time.setTypeface(null, tType);
      time.setTextColor(tColor);
      time.setText(String.valueOf(event.hour));

      TextView duration = (TextView) layout.findViewById(R.id.gi_duration);
      duration.setTypeface(null, tType);
      duration.setTextColor(tColor);
      duration.setText(String.valueOf(event.duration));

      if (event.continuous) {
        duration.setCompoundDrawablesWithIntrinsicBounds(0, 0,
            R.drawable.continuous_icon, 0);
      }

      TextView location = (TextView) layout.findViewById(R.id.gi_location);
      location.setTypeface(null, tType);
      location.setTextColor(tColor);

      SpannableString locationText = new SpannableString(event.location);
      locationText.setSpan(new UnderlineSpan(), 0, locationText.length(),
          0);
      location.setText(locationText);

      location.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View v) {
          MainActivity.openMap(event.location);
        }
      });

      ImageView star = (ImageView) layout.findViewById(R.id.gi_star);
      if (event.starred)
        star.setImageResource(R.drawable.star_on);
      else
        star.setImageResource(R.drawable.star_off);

      star.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          changeEventStar(event.identifier, !event.starred, MainActivity.activity);
          setAllStared();


        }
      });

      layout.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          selectEvent(MainActivity.activity, event.identifier);
        }
      });

      final int position = i;
      layout.setOnLongClickListener(new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
          AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.activity);

          builder.setTitle("Choose an action");
          builder.setMessage("What do you want to do to " + event.title);

          builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              deleteEvents(position);
            }
          });

          builder.setNeutralButton(R.string.edit, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              editEvent(position);
            }
          });

          builder.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              dialog.dismiss();
            }
          });

          builder.create().show();

          return false;
        }
      });
      userEventsLayout.addView(layout);
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    setGameStar();
  }

  private static void selectEvent(Activity activity, String eID) {
    MainActivity.SELECTED_EVENT_ID = eID;

    EventFragment fragment = (EventFragment) activity.getFragmentManager()
        .findFragmentById(R.id.eventFragment);

    if (fragment != null && fragment.isInLayout()) {
      EventFragment.setEvent(activity);
    } else {
      Intent intent = new Intent(activity, EventActivity.class);
      activity.startActivity(intent);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.my_wbc_data, container, false);

    /** ADD FINISHES **/
    LinearLayout finishesLayout = (LinearLayout) view.findViewById(R.id.my_wbc_data_finishes);
    finishesLayout.removeAllViews();

    int padding = (int) getResources().getDimension(R.dimen.text_margin_small);

    TextView textView;
    String finishString;

    for (Tournament tournament : MainActivity.allTournaments) {
      if (tournament.finish > 0) {
        Log.d(TAG, String.valueOf(tournament.title) + " finish is "
            + String.valueOf(tournament.finish));

        switch (tournament.finish) {
          case 1:
            finishString = getResources().getString(R.string.first);
            break;
          case 2:
            finishString = getResources().getString(R.string.second);
            break;
          case 3:
            finishString = getResources().getString(R.string.third);
            break;
          case 4:
            finishString = getResources().getString(R.string.fourth);
            break;
          case 5:
            finishString = getResources().getString(R.string.fifth);
            break;
          case 6:
            finishString = getResources().getString(R.string.sixth);
            break;
          default:
            finishString = "No finish";
            break;
        }

        finishString += " in " + tournament.title;

        textView = new TextView(MainActivity.activity);
        textView.setText(finishString);
        textView.setTextAppearance(MainActivity.activity, R.style.medium_text);
        textView.setGravity(Gravity.START);
        textView.setPadding(padding, padding, padding, padding);

        if (tournament.finish <= tournament.prize)
          textView.setTypeface(null, Typeface.BOLD);
        else
          textView.setTypeface(null, Typeface.ITALIC);

        finishesLayout.addView(textView);
      }
    }

    /** ADD NOTES **/

    LinearLayout notesLayout = (LinearLayout) view
        .findViewById(R.id.my_wbc_data_notes);
    notesLayout.removeAllViews();

    SharedPreferences sp = MainActivity.activity.getSharedPreferences(
        getResources().getString(R.string.sp_file_name),
        Context.MODE_PRIVATE);
    String notePrefString = getResources().getString(R.string.sp_event_note);

    String noteString;

    for (ArrayList<ArrayList<Event>> searchDayList : MainActivity.dayList) {
      for (ArrayList<Event> searchList : searchDayList) {
        for (Event event : searchList) {
          noteString = sp
              .getString(
                  notePrefString
                      + String.valueOf(event.identifier),
                  "");
          if (noteString.length() > 0) {

            textView = new TextView(MainActivity.activity);
            textView.setText(event.title + ": " + noteString);
            textView.setTextAppearance(MainActivity.activity,
                R.style.medium_text);
            textView.setGravity(Gravity.START);
            textView.setPadding(padding, padding, padding,
                padding);

            notesLayout.addView(textView);
          }
        }
      }
    }

    /** ADD USER EVENTS **/
    userEventLayoutInflater = inflater;
    userEventsLayout = (LinearLayout) view.findViewById(R.id.my_wbc_data_events);

    String identifier, row, eventTitle, location;
    String[] rowData;
    int index, day, hour;
    double duration;

    String userEventPrefString = getResources().getString(R.string.sp_user_event);
    String starPrefString = getResources().getString(R.string.sp_event_starred);

    userEvents = new ArrayList<Event>();
    for (index = 0; ; index++) {
      row = sp.getString(userEventPrefString + String.valueOf(index), "");
      if (row.equalsIgnoreCase(""))
        break;

      rowData = row.split("~");

      day = Integer.valueOf(rowData[0]);
      hour = Integer.valueOf(rowData[1]);
      eventTitle = rowData[2];
      duration = Double.valueOf(rowData[3]);
      location = rowData[4];

      identifier = String.valueOf(day * 24 + hour) + eventTitle;
      Event event = new Event(identifier, 0, day, hour, eventTitle, "", "",
          false, duration, false, duration, location);
      event.starred = sp.getBoolean(starPrefString + identifier, false);

      userEvents.add(event);
    }

    updateUserEventList(MainActivity.activity);

    Button addEvent = (Button) view.findViewById(R.id.add_event);
    addEvent.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        showCreateDialog();
      }
    });

    Button deleteAll = (Button) view.findViewById(R.id.delete_all);
    deleteAll.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        deleteEvents(-1);
      }
    });

    userEventsStarIV = (ImageView) view.findViewById(R.id.gf_star);
    userEventsStarIV.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        changeAllStarred(MainActivity.activity);
      }
    });

    setAllStared();

    return view;
  }

  private void showCreateDialog() {
    DialogCreateEvent editNameDialog = new DialogCreateEvent();
    editNameDialog.show(MainActivity.activity.getFragmentManager(),
        "create_event_dialog");
  }

  private static void editEvent(int index) {
    selectedEvent = index;
    DialogEditEvent editNameDialog = new DialogEditEvent();
    editNameDialog.show(MainActivity.activity.getFragmentManager(), "edit_event_dialog");
  }

  private static void deleteEvents(int index) {
    selectedEvent = index;
    DialogDelete deleteDialog = new DialogDelete();
    deleteDialog.show(MainActivity.activity.getFragmentManager(), "delete_dialog");
  }

  public static class DialogDelete extends DialogFragment {
    // private final String TAG="DeleteDialog";

    private boolean ALL_EVENTS;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      Dialog dialog = super.onCreateDialog(savedInstanceState);
      dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
      return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
      View view = inflater
          .inflate(R.layout.dialog_delete, container, false);

      ALL_EVENTS = selectedEvent == -1;

      String string;
      if (ALL_EVENTS)
        string = "all your events?";
      else {
        Event event = userEvents.get(selectedEvent);
        String day = getResources().getStringArray(R.array.days)[event.day];

        SharedPreferences settings = MainActivity.activity.getSharedPreferences(
            getResources().getString(R.string.sp_file_name),
            Context.MODE_PRIVATE);

        boolean hours24 = settings.getBoolean("24_hour", true);
        int hoursID = (hours24 ? R.array.hours_24 : R.array.hours_12);
        String[] hours = getResources().getStringArray(hoursID);

        String time = hours[event.hour - 7];
        string = event.title + " on " + day + " at " + time + "?";

      }

      TextView text = (TextView) view.findViewById(R.id.dd_text);
      text.setText(string);

      Button delete = (Button) view.findViewById(R.id.dd_delete);
      delete.setEnabled(true);
      delete.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          deleteItem();
          getDialog().dismiss();
        }
      });

      Button cancel = (Button) view.findViewById(R.id.dd_cancel);
      cancel.setEnabled(true);
      cancel.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          getDialog().dismiss();
        }
      });

      return view;

    }

    public void deleteItem() {
      Event event;

      int index;
      if (ALL_EVENTS)
        index = 0;
      else
        index = selectedEvent;
      while (index < userEvents.size()) {
        event = userEvents.remove(index);

        // delete from dayList (currentHour)
        List<Event> scheduleEvents = MainActivity.dayList.get(
            event.day).get(event.hour - 6);

        for (int j = 0; j < scheduleEvents.size(); j++) {
          if (scheduleEvents.get(j).identifier
              .equalsIgnoreCase(event.identifier)) {
            MainActivity.dayList.get(event.day).get(
                event.hour - 6).remove(j);
            break;
          }
        }
        // delete from dayList (starred)
        if (event.starred) {
          scheduleEvents = MainActivity.dayList.get(event.day).get(
              0);
          for (int j = 0; j < scheduleEvents.size(); j++) {
            if (scheduleEvents.get(j).identifier
                .equalsIgnoreCase(event.identifier)) {
              scheduleEvents.remove(j);
              break;
            }
          }
        }

        // eventsDB.deleteEvent(help.ID);

        if (!ALL_EVENTS)
          index = userEvents.size();

      }

      updateUserEventList(MainActivity.activity);
      saveUserEvents(MainActivity.activity);
    }

  }

  public static class DialogCreateEvent extends DialogFragment {

    // final String TAG="WBC CreateEventDialog";
    private final int[] ceDayIDs = {R.id.ce_repeat_d0, R.id.ce_repeat_d1,
        R.id.ce_repeat_d2, R.id.ce_repeat_d3, R.id.ce_repeat_d4,
        R.id.ce_repeat_d5, R.id.ce_repeat_d6, R.id.ce_repeat_d7,
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
      Dialog dialog = super.onCreateDialog(savedInstanceState);
      dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
      return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.dialog_event, container);

      dialogTitle = (TextView) view.findViewById(R.id.ce_dialog_title);
      dialogTitle
          .setText(getResources().getString(R.string.create_event));

      daysTV = (TextView) view.findViewById(R.id.ce_days);
      daysTV.setVisibility(View.VISIBLE);
      daysLL = (LinearLayout) view.findViewById(R.id.ce_days_layout);
      daysLL.setVisibility(View.VISIBLE);

      hourSpinner = (Spinner) view.findViewById(R.id.ce_hour);

      SharedPreferences settings = MainActivity.activity.getSharedPreferences(
          getResources().getString(R.string.sp_file_name),
          Context.MODE_PRIVATE);

      boolean hours24 = settings.getBoolean("24_hour", true);
      int hoursID = (hours24 ? R.array.hours_24 : R.array.hours_12);

      ArrayAdapter<CharSequence> hourA = ArrayAdapter.createFromResource(
          MainActivity.activity, hoursID, android.R.layout.simple_spinner_item);
      hourA
          .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      hourSpinner.setAdapter(hourA);

      durationSpinner = (Spinner) view.findViewById(R.id.ce_duration);
      ArrayAdapter<CharSequence> durationA = ArrayAdapter
          .createFromResource(MainActivity.activity, R.array.duration,
              android.R.layout.simple_spinner_item);
      durationA
          .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      durationSpinner.setAdapter(durationA);

      add = (Button) view.findViewById(R.id.ce_add);
      add.setEnabled(false);
      add.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          createEvent();
          getDialog().dismiss();
        }
      });
      cancel = (Button) view.findViewById(R.id.ce_cancel);
      cancel.setEnabled(true);
      cancel.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View v) {
          getDialog().dismiss();
        }
      });

      titleET = (EditText) view.findViewById(R.id.ce_title);
      titleET.addTextChangedListener(new TextWatcher() {

        @Override
        public void afterTextChanged(Editable arg0) {
          checkButton();

        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1,
                                      int arg2, int arg3) {
        }

        @Override
        public void onTextChanged(CharSequence arg0, int arg1,
                                  int arg2, int arg3) {
        }
      });

      locationET = (EditText) view.findViewById(R.id.ce_location);
      locationET.addTextChangedListener(new TextWatcher() {

        @Override
        public void afterTextChanged(Editable arg0) {
          checkButton();
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1,
                                      int arg2, int arg3) {
        }

        @Override
        public void onTextChanged(CharSequence arg0, int arg1,
                                  int arg2, int arg3) {
        }
      });

      days = new CheckBox[MainActivity.dayStrings.length];
      for (int i = 0; i < MainActivity.dayStrings.length; i++) {
        days[i] = (CheckBox) view.findViewById(ceDayIDs[i]);
        days[i].setText(MainActivity.dayStrings[i]);
        days[i].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

          @Override
          public void onCheckedChanged(CompoundButton buttonView,
                                       boolean isChecked) {
            checkButton();
          }
        });
      }
      return view;
    }

    public void checkButton() {
      add.setEnabled(false);

      if (titleET.getText().toString().length() > 0
          && locationET.getText().toString().length() > 0) {
        for (CheckBox checkBox : days) {
          if (checkBox.isChecked()) {
            add.setEnabled(true);
            break;
          }
        }
      }
    }

    public void createEvent() {
      String title = titleET.getText().toString();
      int hour = hourSpinner.getSelectedItemPosition() + 7;
      int duration = durationSpinner.getSelectedItemPosition() + 1;
      String location = locationET.getText().toString();

      Event newEvent, tempEvent;
      String identifier;
      for (int day = 0; day < days.length; day++) {
        if (days[day].isChecked()) {
          identifier = String.valueOf(day * 24 + hour) + title;

          newEvent = new Event(identifier, -1, day, hour, title, "", "",
              false, duration, false, duration, location);

          int index = 0;
          for (; index < userEvents.size(); index++) {
            tempEvent = userEvents.get(index);
            if ((tempEvent.day * 24 + tempEvent.hour > newEvent.day * 24
                + newEvent.hour)
                || (tempEvent.day * 24 + tempEvent.hour == newEvent.day
                * 24 + newEvent.hour && tempEvent.title
                .compareToIgnoreCase(title) == 1))
              break;
          }

          userEvents.add(index, newEvent);
          changeEventStar(newEvent.identifier, true, MainActivity.activity);

          MainActivity.SELECTED_EVENT_ID = identifier;

          MainActivity.dayList.get(day).get(hour - 6).add(0,
              newEvent);

        }
      }

      saveUserEvents(MainActivity.activity);

      EventFragment fragment = (EventFragment) MainActivity.activity
          .getFragmentManager().findFragmentById(R.id.eventFragment);

      if (fragment != null && fragment.isInLayout()) {
        EventFragment.setEvent(MainActivity.activity);
      }

      updateUserEventList(MainActivity.activity);

    }
  }

  public static class DialogEditEvent extends DialogCreateEvent {
    // private final String TAG="WBC EditEventDialog";

    private int oldTime;
    private Event event;

    @Override
    public void onStart() {
      super.onStart();

      dialogTitle.setText(getResources().getString(R.string.edit_event));
      daysTV.setVisibility(View.GONE);
      daysLL.setVisibility(View.GONE);

      event = null;
      for (int i = 0; i < userEvents.size(); i++) {
        event = userEvents.get(i);
        if (event.identifier.equalsIgnoreCase(MainActivity.SELECTED_EVENT_ID))
          break;
      }

      oldTime = event.hour;

      hourSpinner.setSelection(event.hour - 7);
      durationSpinner.setSelection((int) event.duration - 1);
      titleET.setText(event.title);
      locationET.setText(event.location);

      add.setText(getResources().getString(R.string.save));
      add.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View v) {
          saveEvent();
          getDialog().dismiss();

        }
      });
    }

    @Override
    public void checkButton() {
      add.setEnabled(titleET.getText().toString().length() > 0
          && locationET.getText().toString().length() > 0);

    }

    public void saveEvent() {
      String title = titleET.getText().toString();
      int hour = hourSpinner.getSelectedItemPosition() + 7;
      int duration = durationSpinner.getSelectedItemPosition() + 1;
      String location = locationET.getText().toString();

      // remove help from global list and edit

      int index = 0;
      for (; index < userEvents.size(); index++) {
        if (userEvents.get(index).identifier
            .equalsIgnoreCase(MainActivity.SELECTED_EVENT_ID))
          break;
      }

      Event editedEvent = userEvents.remove(index);
      editedEvent.hour = hour;
      editedEvent.title = title;
      editedEvent.duration = duration;
      editedEvent.location = location;

      // add edited help to tournament list
      Event tempEvent;
      index = 0;
      for (; index < userEvents.size(); index++) {
        tempEvent = userEvents.get(index);
        if ((tempEvent.day * 24 + tempEvent.hour > editedEvent.day * 24
            + editedEvent.hour)
            || (tempEvent.day * 24 + tempEvent.hour == editedEvent.day * 24
            + editedEvent.hour && tempEvent.title
            .compareToIgnoreCase(title) == 1))
          break;

      }
      userEvents.add(index, editedEvent);

      // remove old help from schedule
      List<Event> events = MainActivity.dayList.get(event.day).get(
          oldTime - 6);
      for (int i = 0; i < events.size(); i++) {
        if (events.get(i).identifier.equalsIgnoreCase(event.identifier)) {
          events.remove(i);
          break;
        }
      }

      // add new help to schedule
      MainActivity.dayList.get(event.day).get(hour - 6).add(0,
          editedEvent);

      // check for starred help
      if (event.starred) {
        MainActivity
            .removeStarredEvent(event.identifier, event.day);
        MainActivity.addStarredEvent(editedEvent);
      }

      MainActivity.SELECTED_EVENT_ID = event.identifier;

      updateUserEventList(MainActivity.activity);

      EventFragment fragment = (EventFragment) MainActivity.activity
          .getFragmentManager().findFragmentById(R.id.eventFragment);

      if (fragment != null && fragment.isInLayout()) {
        EventFragment.setEvent(MainActivity.activity);
      }
      saveUserEvents(MainActivity.activity);
    }
  }
}
