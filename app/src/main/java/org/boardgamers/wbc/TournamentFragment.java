package org.boardgamers.wbc;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class TournamentFragment extends Fragment {
  private static final String TAG = "Game Fragment";
  public static ArrayList<Event> tournamentEvents;
  private static ImageView starGame;
  private static TournamentListAdapter listAdapter;
  private final int[] finishIDs = {R.id.gf_finish_0, R.id.gf_finish_1,
      R.id.gf_finish_2, R.id.gf_finish_3, R.id.gf_finish_4,
      R.id.gf_finish_5, R.id.gf_finish_6};
  private boolean allStarred;
  private Tournament tournament;
  private boolean hasFormat;
  private boolean hasClass;
  private TextView gameGM;
  private TextView gameFormat;
  private TextView gameClass;
  private View gameFormatDivider;
  private View gameClassDivider;
  private TextView previewLink;
  private TextView reportLink;
  private ListView listView;
  private LinearLayout finishLayout;
  private RadioGroup finishGroup;
  private RadioButton[] finishButtons;

  public static void changeEventStar(String id, boolean starred,
                                     Activity context) {
    for (Event event : tournamentEvents) {
      if (event.identifier.equalsIgnoreCase(id)) {
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
  public void changeAllStarred() {
    allStarred = !allStarred;
    setGameStar();

    for (Event event : tournamentEvents) {
      if (event.starred ^ allStarred) {
        changeEventStar(event.identifier, allStarred, getActivity());
      }
    }
  }

  /**
   * set starGame image view
   */
  public void setGameStar() {
    starGame.setImageResource(allStarred ? R.drawable.star_on
        : R.drawable.star_off);
  }

  /**
   * Event star changed - check for change in allStarred boolean and set game star image view
   */
  public void setAllStared() {
    allStarred = true;
    for (Event tEvent : tournamentEvents) {
      if (!tEvent.starred) {
        allStarred = false;
        return;
      }
    }
    setGameStar();
  }


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.tournament_fragment, container,
        false);

    gameGM = (TextView) view.findViewById(R.id.gf_gm);
    listView = (ListView) view.findViewById(R.id.gf_events);
    starGame = (ImageView) view.findViewById(R.id.gf_star);

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
      tournament = new Tournament(id, formatStrings[id - 1000], "N/A", false, 0, "N/A");
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
          } else if (id >= 1000 && event.format.equalsIgnoreCase(formatStrings[id - 1000])) {
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

    listAdapter = new TournamentListAdapter();
    listView.setAdapter(listAdapter);
    listView.setDividerHeight(0);

    final String label = tournament.label;

    // get tournament variables from last event
    Event lastEvent = tournamentEvents.get(tournamentEvents.size() - 1);

    // check if last event is class (is tournament)
    if (lastEvent.eClass.length() > 0) {

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


      // if last event started, allow finish
      boolean started = lastEvent.day * 24 + lastEvent.hour <= MainActivity.currentDay * 24
          + MainActivity.currentHour;
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

      finishLayout.setVisibility(View.VISIBLE);
    } else {
      // not tournament
      finishLayout.setVisibility(View.GONE);
      previewLink.setVisibility(View.GONE);
      reportLink.setVisibility(View.GONE);
    }

    // set title
    getActivity().setTitle(tournament.title);

    // set GM
    gameGM.setText("GM: " + tournament.gm);

    // set game star
    starGame.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        changeAllStarred();
      }
    });

    setAllStared();
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


  public class TournamentListAdapter extends BaseAdapter {
    private final LayoutInflater inflater;

    public TournamentListAdapter() {
      inflater = (LayoutInflater)
          getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

      if (event.identifier
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

      star.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          changeEventStar(event.identifier, !event.starred, getActivity());
          setAllStared();
        }
      });

      view.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          selectEvent(event.identifier);
        }
      });
      return view;
    }

    public void selectEvent(String eID) {
      MainActivity.SELECTED_EVENT_ID = eID;
      notifyDataSetChanged();

      EventFragment fragment = (EventFragment) getFragmentManager()
          .findFragmentById(R.id.eventFragment);

      if (fragment != null && fragment.isInLayout()) {
        EventFragment.setEvent(getActivity());
      } else {
        Intent intent = new Intent(getActivity(), EventActivity.class);
        startActivity(intent);
      }
    }
  }
}
