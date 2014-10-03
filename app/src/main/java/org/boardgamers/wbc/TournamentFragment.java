package org.boardgamers.wbc;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class TournamentFragment extends Fragment {
  private static final String TAG = "Game Fragment";
  public static ArrayList<Event> tournamentEvents;
  private static Activity activity;
  private static ImageView starGame;
  private static EventListAdapter listAdapter;
  private static boolean allStarred;
  private final int[] finishIDs = {R.id.gf_finish_0, R.id.gf_finish_1,
      R.id.gf_finish_2, R.id.gf_finish_3, R.id.gf_finish_4,
      R.id.gf_finish_5, R.id.gf_finish_6};
  private Tournament tournament;
  private boolean hasFormat;
  private boolean hasClass;
  private TextView gameGM;
  private TextView gameFormat;
  private TextView gameClass;
  private ImageButton editGame;
  private ImageButton deleteGame;
  private View gameFormatDivider;
  private View gameClassDivider;
  private TextView previewLink;
  private TextView reportLink;
  private ListView listView;
  private LinearLayout finishLayout;
  private RadioGroup finishGroup;
  private RadioButton[] finishButtons;

  public static void changeEventStar(String id, boolean starred,
                                     Activity context, boolean checkAllStar) {

    Event event = null;
    for (int i = 0; i < tournamentEvents.size(); i++) {
      event = tournamentEvents.get(i);
      if (event.identifier.equalsIgnoreCase(id))
        break;
    }

    event.starred = starred;

    listAdapter.notifyDataSetChanged();

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
    EventFragment fragment = (EventFragment) activity.getFragmentManager()
        .findFragmentById(R.id.eventFragment);
    if (fragment != null && fragment.isInLayout() && MainActivity.SELECTED_EVENT_ID.equalsIgnoreCase(id)) {
      EventFragment.star.setImageResource(starred ? R.drawable.star_on
          : R.drawable.star_off);
    }

    if (checkAllStar)
      checkAllStar();
  }

  public static void checkAllStar() {
    allStarred = true;
    for (Event tEvent : tournamentEvents) {
      if (!tEvent.starred) {
        allStarred = false;
        break;
      }
    }
    setGameStar();
  }

  public static void setGameStar() {
    starGame.setImageResource(allStarred ? R.drawable.star_on
        : R.drawable.star_off);
  }

  public static void saveUserEvents(Context context) {
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
    for (; i < tournamentEvents.size(); i++) {
      event = tournamentEvents.get(i);

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

  public static void showCreateDialog() {
    DialogCreateEvent editNameDialog = new DialogCreateEvent();
    editNameDialog.show(activity.getFragmentManager(),
        "create_event_dialog");

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.tournament_fragment, container,
        false);
    activity = getActivity();

    final Resources resources = getResources();

    gameGM = (TextView) view.findViewById(R.id.gf_gm);
    listView = (ListView) view.findViewById(R.id.gf_events);
    starGame = (ImageView) view.findViewById(R.id.gf_star);
    deleteGame = (ImageButton) view.findViewById(R.id.gf_delete);
    editGame = (ImageButton) view.findViewById(R.id.gf_edit);

    gameFormatDivider = view.findViewById(R.id.gf_format_divider);
    gameClassDivider = view.findViewById(R.id.gf_class_divider);
    gameFormat = (TextView) view.findViewById(R.id.gf_format);
    gameClass = (TextView) view.findViewById(R.id.gf_class);

    previewLink = (TextView) view.findViewById(R.id.gf_preview_link);
    reportLink = (TextView) view.findViewById(R.id.gf_report_link);

    finishGroup = (RadioGroup) view.findViewById(R.id.gf_finish);
    finishLayout = (LinearLayout) view.findViewById(R.id.gf_finish_layout);
    finishButtons = new RadioButton[finishIDs.length];
    for (int i = 0; i < finishIDs.length; i++)
      finishButtons[i] = (RadioButton) view.findViewById(finishIDs[i]);

    setGame();

    return view;
  }

  public void setGame() {
    int id = MainActivity.SELECTED_GAME_ID;
    String[] formatStrings = getResources().getStringArray(R.array.search_formats);
    if (id < 1000)
      tournament = MainActivity.allTournaments.get(id);
    else {
      tournament = new Tournament(id, formatStrings[id-1000], "N/A", false, 0, "N/A");
    }
    // get events
    tournamentEvents = new ArrayList<Event>();

    hasFormat = false;
    hasClass = false;

    Event event;
    for (int i = 0; i < MainActivity.dayList.size(); i++) {
      for (int j = 1; j < MainActivity.dayList.get(i).size(); j++) {
        for (int k = 0; k < MainActivity.dayList.get(i).get(j)
            .size(); k++) {
          event = MainActivity.dayList.get(i).get(j).get(k);
          if (id < 1000 && event.tournamentID == tournament.ID) {
            tournamentEvents.add(event);

            if (event.format.length() > 0)
              hasFormat = true;
            if (event.eClass.length() > 0)
              hasClass = true;
          } else if (id >= 1000 && event.format.equalsIgnoreCase(formatStrings[id-1000])) {
            tournamentEvents.add(event);
          }
        }
      }
    }

    Log.d(TAG, hasClass ? "Has class" : "No class");
    gameFormatDivider.setVisibility(hasFormat ? View.VISIBLE : View.GONE);
    gameFormat.setVisibility(hasFormat ? View.VISIBLE : View.GONE);
    gameClassDivider.setVisibility(hasClass ? View.VISIBLE : View.GONE);
    gameClass.setVisibility(hasClass ? View.VISIBLE : View.GONE);

    listAdapter = new EventListAdapter();
    listView.setAdapter(listAdapter);
    listView.setDividerHeight(0);

    boolean userEvents = tournament.ID == 0;
    boolean started = false;
    if (!userEvents) {
      final String label = tournament.label;

      // get tournament variables from last event
      Event lastEvent = tournamentEvents.get(tournamentEvents.size() - 1);

      // check for finish if event is tournament
      if (lastEvent.eClass.length() > 0) {

        // if last event started, allow finish
        started = lastEvent.day * 24 + lastEvent.hour <= MainActivity.currentDay * 24
            + MainActivity.currentHour;

        // links available for tournament
        previewLink.setVisibility(View.VISIBLE);
        previewLink.setOnClickListener(new View.OnClickListener() {

          @Override
          public void onClick(View v) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri
                .parse("http://boardgamers.org/yearbkex/" + label
                    + "pge.htm")));
          }
        });

        reportLink.setVisibility(View.VISIBLE);
        reportLink.setOnClickListener(new View.OnClickListener() {

          @Override
          public void onClick(View v) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri
                .parse("http://boardgamers.org/yearbook13/"
                    + label + "pge.htm")));

          }
        });
      } else {
        // not user event and not tournament
        finishLayout.setVisibility(View.GONE);
        previewLink.setVisibility(View.GONE);
        reportLink.setVisibility(View.GONE);
      }

      editGame.setVisibility(View.GONE);
      deleteGame.setVisibility(View.GONE);

    } else {
      // user event buttons

      editGame.setVisibility(View.INVISIBLE);
      deleteGame.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View v) {
          deleteEvents("");

        }
      });
      finishLayout.setVisibility(View.GONE);
      previewLink.setVisibility(View.GONE);
      reportLink.setVisibility(View.GONE);

    }

    // set title
    activity.setTitle(tournament.title);

    // set GM
    gameGM.setText("GM: " + tournament.gm);

    // check for finish layout
    if (tournament.prize > 0) {
      finishLayout.setVisibility(View.VISIBLE);

      for (int i = 0; i <= 6; i++) {
        finishButtons[i].setClickable(started);
        finishButtons[i].setTextColor(started ? Color.BLACK
            : Color.GRAY);
        finishButtons[i].setVisibility(View.VISIBLE);

        if (i > 0 && i <= tournament.prize)
          finishButtons[i].setTypeface(null, Typeface.BOLD);
        else
          finishButtons[i].setTypeface(null, Typeface.ITALIC);
      }

      finishGroup.check(finishIDs[tournament.finish]);

    } else
      finishLayout.setVisibility(View.GONE);

    // set game star
    starGame.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        starAll();
      }
    });

    checkAllStar();
    setGameStar();

  }

  @Override
  public void onPause() {
    for (int i = 0; i < finishButtons.length; i++) {
      if (finishButtons[i].isChecked()) {
        SharedPreferences.Editor editor = getActivity()
            .getSharedPreferences(
                getResources().getString(R.string.sp_file_name),
                Context.MODE_PRIVATE).edit();
        editor.putInt("fin_" + tournament.title, i);
        editor.apply();

        MainActivity.allTournaments.get(tournament.ID).finish = i;
        break;
      }
    }

    super.onPause();
  }

  public void selectEvent(String eID) {
    MainActivity.SELECTED_EVENT_ID = eID;
    listAdapter.notifyDataSetChanged();

    EventFragment fragment = (EventFragment) getFragmentManager()
        .findFragmentById(R.id.eventFragment);

    if (fragment != null && fragment.isInLayout()) {
      EventFragment.setEvent(getActivity());
    } else {
      Intent intent = new Intent(activity, EventActivity.class);
      startActivity(intent);
    }

  }

  public void starAll() {
    allStarred = !allStarred;

    setGameStar();

    for (Event event : tournamentEvents) {
      if (event.starred ^ allStarred) {
        changeEventStar(event.identifier, allStarred, activity, false);
      }
    }

  }

  public void editEvent(String identifier) {
    MainActivity.SELECTED_EVENT_ID = identifier;

    DialogEditEvent editNameDialog = new DialogEditEvent();
    editNameDialog.show(activity.getFragmentManager(), "edit_event_dialog");
  }

  public void deleteEvents(String identifier) {
    MainActivity.SELECTED_EVENT_ID = identifier;

    DialogDelete deleteDialog = new DialogDelete();
    deleteDialog.show(activity.getFragmentManager(), "delete_dialog");
  }

  public static class DialogDelete extends DialogFragment {
    // private final String TAG="DeleteDialog";

    private int EVENT_INDEX;
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

      EVENT_INDEX = 0;
      for (; EVENT_INDEX < tournamentEvents.size(); EVENT_INDEX++) {
        if (tournamentEvents.get(EVENT_INDEX).identifier
            .equalsIgnoreCase(MainActivity.SELECTED_EVENT_ID))
          break;
      }

      ALL_EVENTS = EVENT_INDEX == tournamentEvents.size();

      String string;
      if (ALL_EVENTS)
        string = "all your events?";
      else {
        Event event = tournamentEvents.get(EVENT_INDEX);
        String day = getResources().getStringArray(R.array.days)[event.day];

        SharedPreferences settings = getActivity().getSharedPreferences(
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
          closeDialog();
        }
      });

      Button cancel = (Button) view.findViewById(R.id.dd_cancel);
      cancel.setEnabled(true);
      cancel.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View v) {
          closeDialog();
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
        index = EVENT_INDEX;
      while (index < tournamentEvents.size()) {
        event = tournamentEvents.remove(index);

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

        // eventsDB.deleteEvent(event.ID);

        if (!ALL_EVENTS)
          index = tournamentEvents.size();

      }

      if (tournamentEvents.size() == 0) {
        activity.finish();
      } else {

        if (EVENT_INDEX == 0)
          EVENT_INDEX++;

        MainActivity.SELECTED_EVENT_ID = tournamentEvents.get(EVENT_INDEX - 1).identifier;
        listAdapter.notifyDataSetChanged();

        EventFragment fragment = (EventFragment) activity
            .getFragmentManager().findFragmentById(
                R.id.eventFragment);

        if (fragment != null && fragment.isInLayout()) {
          EventFragment.setEvent(activity);
        }
      }
      saveUserEvents(activity);
    }

    public void closeDialog() {
      this.dismiss();
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

      SharedPreferences settings = getActivity().getSharedPreferences(
          getResources().getString(R.string.sp_file_name),
          Context.MODE_PRIVATE);

      boolean hours24 = settings.getBoolean("24_hour", true);
      int hoursID = (hours24 ? R.array.hours_24 : R.array.hours_12);

      ArrayAdapter<CharSequence> hourA = ArrayAdapter.createFromResource(
          activity, hoursID, android.R.layout.simple_spinner_item);
      hourA
          .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      hourSpinner.setAdapter(hourA);

      durationSpinner = (Spinner) view.findViewById(R.id.ce_duration);
      ArrayAdapter<CharSequence> durationA = ArrayAdapter
          .createFromResource(activity, R.array.duration,
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
          closeDialog();
        }
      });
      cancel = (Button) view.findViewById(R.id.ce_cancel);
      cancel.setEnabled(true);
      cancel.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View v) {
          closeDialog();
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
        days[i].setOnCheckedChangeListener(new OnCheckedChangeListener() {

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
        for (int i = 0; i < days.length; i++) {
          if (days[i].isChecked()) {
            add.setEnabled(true);
            break;
          }
        }
      }
    }

    public void closeDialog() {
      this.dismiss();
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

          newEvent = new Event(identifier, 0, day, hour, title, "", "",
              false, duration, false, duration, location);

          int index = 0;
          for (; index < tournamentEvents.size(); index++) {
            tempEvent = tournamentEvents.get(index);
            if ((tempEvent.day * 24 + tempEvent.hour > newEvent.day * 24
                + newEvent.hour)
                || (tempEvent.day * 24 + tempEvent.hour == newEvent.day
                * 24 + newEvent.hour && tempEvent.title
                .compareToIgnoreCase(title) == 1))
              break;
          }

          tournamentEvents.add(index, newEvent);

          changeEventStar(newEvent.identifier, true, activity, false);

          MainActivity.SELECTED_EVENT_ID = identifier;

          MainActivity.dayList.get(day).get(hour - 6).add(0,
              newEvent);

        }

      }

      saveUserEvents(getActivity());

      EventFragment fragment = (EventFragment) activity
          .getFragmentManager().findFragmentById(R.id.eventFragment);

      if (fragment != null && fragment.isInLayout()) {
        EventFragment.setEvent(activity);
      }

      listAdapter.notifyDataSetChanged();
      checkAllStar();

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
      for (int i = 0; i < tournamentEvents.size(); i++) {
        event = tournamentEvents.get(i);
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
          closeDialog();

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

      // remove event from global list and edit

      int index = 0;
      for (; index < tournamentEvents.size(); index++) {
        if (tournamentEvents.get(index).identifier
            .equalsIgnoreCase(MainActivity.SELECTED_EVENT_ID))
          break;
      }

      Event editedEvent = tournamentEvents.remove(index);
      editedEvent.hour = hour;
      editedEvent.title = title;
      editedEvent.duration = duration;
      editedEvent.location = location;

      // add edited event to tournament list
      Event tempEvent;
      index = 0;
      for (; index < tournamentEvents.size(); index++) {
        tempEvent = tournamentEvents.get(index);
        if ((tempEvent.day * 24 + tempEvent.hour > editedEvent.day * 24
            + editedEvent.hour)
            || (tempEvent.day * 24 + tempEvent.hour == editedEvent.day * 24
            + editedEvent.hour && tempEvent.title
            .compareToIgnoreCase(title) == 1))
          break;

      }
      tournamentEvents.add(index, editedEvent);

      // remove old event from schedule
      List<Event> events = MainActivity.dayList.get(event.day).get(
          oldTime - 6);
      for (int i = 0; i < events.size(); i++) {
        if (events.get(i).identifier.equalsIgnoreCase(event.identifier)) {
          events.remove(i);
          break;
        }
      }

      // add new event to schedule
      MainActivity.dayList.get(event.day).get(hour - 6).add(0,
          editedEvent);

      // check for starred event
      if (event.starred) {
        MainActivity
            .removeStarredEvent(event.identifier, event.day);
        MainActivity.addStarredEvent(editedEvent);
      }

      MainActivity.SELECTED_EVENT_ID = event.identifier;

      listAdapter.notifyDataSetChanged();

      EventFragment fragment = (EventFragment) activity
          .getFragmentManager().findFragmentById(R.id.eventFragment);

      if (fragment != null && fragment.isInLayout()) {
        EventFragment.setEvent(activity);
      }
      saveUserEvents(activity);
    }
  }

  public class EventListAdapter extends BaseAdapter {
    private final LayoutInflater inflater;

    public EventListAdapter() {
      inflater = (LayoutInflater) activity
          .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
      return tournamentEvents.size();
    }

    @Override
    public Object getItem(int position) {
      return position;
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
      if (view == null)
        view = inflater.inflate(R.layout.tournament_item, null);

      final Event event = tournamentEvents.get(position);

      boolean started = event.day * 24 + event.hour <= MainActivity.currentDay * 24 + MainActivity.currentHour;
      boolean ended = event.day * 24 + event.hour + event.totalDuration <= MainActivity.currentDay
          * 24 + MainActivity.currentHour;
      boolean happening = started && !ended;

      EventFragment fragment = (EventFragment) getFragmentManager()
          .findFragmentById(R.id.eventFragment);
      if (fragment != null
          && fragment.isInLayout()
          && event.identifier
          .equalsIgnoreCase(MainActivity.SELECTED_EVENT_ID))
        view.setBackgroundResource(R.drawable.selected);
      else if (position % 2 == 0) {
        if (ended)
          view.setBackgroundResource(R.drawable.ended_light);
        else if (happening)
          view.setBackgroundResource(R.drawable.current_light);
        else
          view.setBackgroundResource(R.drawable.future_light);
      } else {
        if (ended)
          view.setBackgroundResource(R.drawable.ended_dark);
        else if (happening)
          view.setBackgroundResource(R.drawable.current_dark);
        else
          view.setBackgroundResource(R.drawable.future_dark);
      }

      int tColor = MainActivity.getTextColor(event);
      int tType = MainActivity.getTextStyle(event);

      TextView title = (TextView) view.findViewById(R.id.gi_name);
      title.setTypeface(null, tType);
      title.setTextColor(tColor);
      title.setText(event.title);

      TextView day = (TextView) view.findViewById(R.id.gi_date);
      day.setTypeface(null, tType);
      day.setTextColor(tColor);
      day.setText(MainActivity.dayStrings[event.day]);

      TextView time = (TextView) view.findViewById(R.id.gi_time);
      time.setTypeface(null, tType);
      time.setTextColor(tColor);
      time.setText(String.valueOf(event.hour));

      LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
          LinearLayout.LayoutParams.MATCH_PARENT, 0);

      TextView eClass = (TextView) view.findViewById(R.id.gi_class);
      if (hasClass) {
        eClass.setVisibility(View.VISIBLE);
        eClass.setTypeface(null, tType);
        eClass.setTextColor(tColor);
        eClass.setText(event.eClass);
      } else
        eClass.setLayoutParams(lp);

      TextView format = (TextView) view.findViewById(R.id.gi_format);
      if (hasFormat) {
        format.setVisibility(View.VISIBLE);
        format.setTypeface(null, tType);
        format.setTextColor(tColor);
        format.setText(event.format);
      } else
        format.setLayoutParams(lp);

      TextView duration = (TextView) view.findViewById(R.id.gi_duration);
      duration.setTypeface(null, tType);
      duration.setTextColor(tColor);
      duration.setText(String.valueOf(event.duration));

      if (event.continuous) {
        duration.setCompoundDrawablesWithIntrinsicBounds(0, 0,
            R.drawable.continuous_icon, 0);
      }

      TextView location = (TextView) view.findViewById(R.id.gi_location);
      location.setTypeface(null, tType);
      location.setTextColor(tColor);

      SpannableString locationText = new SpannableString(event.location);
      locationText.setSpan(new UnderlineSpan(), 0, locationText.length(),
          0);
      location.setText(locationText);

      location.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View v) {
          MainActivity.openMap(getActivity(), event.location);
        }
      });

      ImageView star = (ImageView) view.findViewById(R.id.gi_star);
      if (event.starred)
        star.setImageResource(R.drawable.star_on);
      else
        star.setImageResource(R.drawable.star_off);

      star.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View view) {
          changeEventStar(event.identifier, !event.starred, activity,
              true);

        }
      });

      ImageButton delete = (ImageButton) view.findViewById(R.id.gi_delete);
      ImageButton edit = (ImageButton) view.findViewById(R.id.gi_edit);
      if (tournament.ID == 0) {

        edit.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            editEvent(event.identifier);

          }
        });

        delete.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            deleteEvents(event.identifier);

          }
        });
        format.setVisibility(View.GONE);
        eClass.setVisibility(View.GONE);

      } else {
        edit.setOnClickListener(null);
        edit.setVisibility(View.GONE);

        delete.setOnClickListener(null);
        delete.setVisibility(View.GONE);

        format.setVisibility(View.VISIBLE);
        eClass.setVisibility(View.VISIBLE);

      }

      view.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          selectEvent(event.identifier);
        }
      });
      return view;
    }
  }
}
