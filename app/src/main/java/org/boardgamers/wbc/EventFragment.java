package org.boardgamers.wbc;

import android.app.Activity;
import android.app.Fragment;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class EventFragment extends Fragment {
  private final static String TAG = "Event Fragment";

  private static Event event;

  private static RelativeLayout timeLayout;

  private static TextView eFormat;
  private static TextView eClass;
  private static TextView location;
  private static TextView time;

  public static ImageView star;
  private static EditText noteET;
  private static String note;


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.event_fragment, container, false);

    timeLayout = (RelativeLayout) view.findViewById(R.id.ef_time_layout);
    eFormat = (TextView) view.findViewById(R.id.ef_format);
    eClass = (TextView) view.findViewById(R.id.ef_class);
    location = (TextView) view.findViewById(R.id.ef_location);
    time = (TextView) view.findViewById(R.id.ef_time);

    star = (ImageView) view.findViewById(R.id.ef_star);
    star.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        changeEventStar(!event.starred);

      }
    });

    Button share = (Button) view.findViewById(R.id.ef_share);
    share.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        share();

      }
    });

    Button clear = (Button) view.findViewById(R.id.ef_clear);
    clear.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        noteET.getText().clear();

      }
    });

    noteET = (EditText) view.findViewById(R.id.ef_note);

    final Activity activity = getActivity();
    if (!MyApp.SELECTED_EVENT_ID.equalsIgnoreCase(""))
      setEvent(activity);

    return view;

  }

  public void changeEventStar(boolean starred) {
    event.starred = starred;
    star.setImageResource(starred ? R.drawable.star_on
        : R.drawable.star_off);

    TournamentFragment.changeEventStar(event.identifier, starred,
        getActivity(), true);

  }

  public static void setEvent(final Activity activity) {
    event = null;
    for (int i = 0; i < TournamentFragment.tournamentEvents.size(); i++) {
      event = TournamentFragment.tournamentEvents.get(i);
      if (event.identifier.equalsIgnoreCase(MyApp.SELECTED_EVENT_ID))
        break;
    }

    int tColor = MyApp.getTextColor(event);
    int tType = MyApp.getTextStyle(event);

    TournamentFragment fragment = (TournamentFragment) activity
        .getFragmentManager().findFragmentById(R.id.gameFragment);

    if (fragment == null || !fragment.isInLayout()) {

      activity.setTitle(event.title);
    }

    eFormat.setText("Format: " + event.format);
    eFormat.setTypeface(null, tType);
    eFormat.setTextColor(tColor);

    eClass.setText("Class: " + event.eClass);
    eClass.setTypeface(null, tType);
    eClass.setTextColor(tColor);

    SpannableString locationText = new SpannableString(event.location);
    locationText.setSpan(new UnderlineSpan(), 0, locationText.length(), 0);
    location.setText(locationText);
    location.setTypeface(null, tType);
    location.setTextColor(tColor);

    location.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        showMap(activity, event.location);

      }
    });

    String minute;
    if (event.duration < 1) {
      minute = String.valueOf((int) (event.duration * 60 + .5));
    } else
      minute = "00";

    String[] days = activity.getResources().getStringArray(R.array.days);

    time.setText(days[event.day] + ", " + String.valueOf(event.hour) + "00 to "
        + String.valueOf((event.hour + (int) event.duration) + minute));
    time.setTypeface(null, tType);
    time.setTextColor(tColor);

    if (event.continuous) {
      time.setCompoundDrawablesWithIntrinsicBounds(0, 0,
          R.drawable.continuous_icon, 0);
    }

    star.setImageResource(event.starred ? R.drawable.star_on
        : R.drawable.star_off);

    SharedPreferences sp = activity.getSharedPreferences(activity
            .getResources().getString(R.string.sp_file_name),
        Context.MODE_PRIVATE);
    note = sp.getString(
        activity.getResources().getString(R.string.sp_event_note)
            + event.identifier, "");
    noteET.setText(note);

    boolean started = event.day * 24 + event.hour <= MyApp.day * 24 + MyApp.hour;
    boolean ended = event.day * 24 + event.hour + event.totalDuration <= MyApp.day * 24
        + MyApp.hour;
    boolean happening = started && !ended;

    if (ended)
      timeLayout.setBackgroundResource(R.drawable.ended_light);
    else if (happening)
      timeLayout.setBackgroundResource(R.drawable.current_light);
    else
      timeLayout.setBackgroundResource(R.drawable.future_light);

  }

  public static void showMap(Activity a, String room) {
    Intent intent = new Intent(a, Map.class);
    intent.putExtra("room", room);
    a.startActivity(intent);

  }

  @Override
  public void onPause() {
    // save note on pause

    note = noteET.getText().toString();

    SharedPreferences.Editor editor = getActivity().getSharedPreferences(
        getResources().getString(R.string.sp_file_name),
        Context.MODE_PRIVATE).edit();

    editor.putString(getResources().getString(R.string.sp_event_note)
        + event.identifier, note);
    editor.apply();

    // TODO desel editText
    InputMethodManager imm = (InputMethodManager) getActivity()
        .getSystemService(Service.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(noteET.getWindowToken(), 0);

    super.onPause();
  }

  public void share() {
    String s = event.title + ": " + note;

    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
    sharingIntent.setType("text/plain");

    String subjectString = event.title;
    sharingIntent.putExtra(Intent.EXTRA_SUBJECT, subjectString);
    sharingIntent.putExtra(Intent.EXTRA_TITLE, subjectString);

    String contentString = s + " #WBC";
    sharingIntent.putExtra(Intent.EXTRA_TEXT, contentString);

    startActivity(Intent.createChooser(sharingIntent, "Share via"));

  }
}