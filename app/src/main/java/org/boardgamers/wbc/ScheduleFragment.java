package org.boardgamers.wbc;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ScheduleFragment extends Fragment {
  private static final String TAG = "Schedule Fragment";

  private ScheduleListAdapter listAdapter;
  private ExpandableListView listView;
  private int dayID;

  public static ScheduleFragment newInstance(int d) {
    Log.d(TAG, "New instance " + String.valueOf(d));
    ScheduleFragment f = new ScheduleFragment();

    // Supply num input as an argument.
    Bundle args = new Bundle();
    args.putInt("current_day", d);
    f.setArguments(args);

    return f;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    dayID = getArguments().getInt("current_day");
    Log.d(TAG, "Create " + String.valueOf(dayID));

    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater
        .inflate(R.layout.schedule_fragment, container, false);

    listAdapter = new ScheduleListAdapter(MainActivity.activity);
    listView = (ExpandableListView) view.findViewById(R.id.sf_schedule);
    listView.setAdapter(listAdapter);
    listView.setDividerHeight(0);

    expandAll(0);

    if (dayID == MainActivity.currentDay)
      listView.setSelectedGroup(MainActivity.currentHour - 5);

    return view;
  }

  @Override
  public void onResume() {
    listAdapter.notifyDataSetChanged();
    super.onResume();
  }

  private void expandAll(int start) {
    for (int i = start; i < listAdapter.getGroupCount(); i++)
      listView.expandGroup(i);
  }

  private void collapseAll(int start) {
    for (int i = start; i < listAdapter.getGroupCount(); i++)
      listView.collapseGroup(i);
  }

  private void changeEventStar(Event event, boolean starred, int groupPosition) {
    // update help's star
    if (groupPosition == 0) {
      // if in first group, need to find original help
      List<Event> events = MainActivity.dayList.get(event.day).get(
          event.hour - 6);
      for (Event temp : events) {
        if (temp.identifier.equalsIgnoreCase(event.identifier)) {
          temp.starred = starred;
          break;
        }
      }
    } else
      event.starred = starred;
    if (starred)
      MainActivity.addStarredEvent(event);
    else
      MainActivity.removeStarredEvent(event.identifier, event.day);

    listAdapter.notifyDataSetChanged();
  }

  class ScheduleListAdapter extends BaseExpandableListAdapter {
    private final LayoutInflater inflater;

    public ScheduleListAdapter(Context c) {
      inflater = (LayoutInflater)
          c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
      return MainActivity.dayList.get(dayID).get(groupPosition).get(childPosition);
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View view, ViewGroup parent) {
      final Event event = (Event) getChild(groupPosition, childPosition);

      int tColor = MainActivity.getTextColor(event);
      int tType = MainActivity.getTextStyle(event);

      if (event.starred
          || (event.tournamentID > -1 && MainActivity.allTournaments.get(event.tournamentID).visible))
        view = inflater.inflate(R.layout.schedule_item, parent, false);
      else
        return inflater.inflate(R.layout.schedule_gone, parent, false);

      view.setVisibility(View.VISIBLE);

      TextView title = (TextView) view.findViewById(R.id.si_name);
      if (groupPosition == 0)
        title.setText(String.valueOf(event.hour) + " - " + event.title);
      else
        title.setText(event.title);
      title.setTypeface(null, tType);
      title.setTextColor(tColor);

      if (event.title.contains("Junior")) {
        title.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.junior_icon, 0, 0, 0);
      }

      TextView duration = (TextView) view.findViewById(R.id.si_duration);
      duration.setText(String.valueOf(event.duration));
      duration.setTypeface(null, tType);
      duration.setTextColor(tColor);

      if (event.continuous) {
        duration.setCompoundDrawablesWithIntrinsicBounds(0, 0,
            R.drawable.continuous_icon, 0);
      }

      TextView location = (TextView) view.findViewById(R.id.si_location);
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

      final int group = groupPosition;
      ImageView starIV = (ImageView) view.findViewById(R.id.si_star);
      starIV.setImageResource(event.starred ? R.drawable.star_on
          : R.drawable.star_off);
      starIV.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          changeEventStar(event, !event.starred, group);
        }
      });

      boolean started = event.day * 24 + event.hour <= MainActivity.currentDay * 24 + MainActivity.currentHour;
      boolean ended = event.day * 24 + event.hour + event.totalDuration <= MainActivity.currentDay
          * 24 + MainActivity.currentHour;
      boolean happening = started && !ended;

      if (childPosition % 2 == 0) {
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

      final int tID = event.tournamentID;
      final String eID = event.identifier;
      view.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          MainActivity.selectGame(MainActivity.activity, tID, eID);
        }
      });

      return view;
    }

    @Override
    public Object getGroup(int groupPosition) {
      return MainActivity.dayList.get(dayID).get(groupPosition);
    }

    @Override
    public View getGroupView(final int groupPosition,
                             final boolean isExpanded, View view, ViewGroup parent) {
      if (view == null)
        view = inflater.inflate(R.layout.schedule_group, parent, false);

      TextView name = (TextView) view.findViewById(R.id.sg_name);

      SharedPreferences settings = MainActivity.activity.getSharedPreferences(
          getResources().getString(R.string.sp_file_name),
          Context.MODE_PRIVATE);

      boolean hours24 = settings.getBoolean("24_hour", true);
      int hoursID = (hours24 ? R.array.hours_24 : R.array.hours_12);
      String[] hours = getResources().getStringArray(hoursID);

      String groupTitle;
      if (groupPosition == 0)
        groupTitle = "My Events: ";
      else
        groupTitle = hours[groupPosition - 1] + ": ";

      if (groupPosition > 0) {
        int i;

        // group is 7pm or 8pm : only search events starting today
        if (groupPosition < 3)
          i = dayID;
        else
          i = 0;

        for (; i <= dayID; i++) {
          for (Event event : MainActivity.dayList.get(i).get(0)) {
            // check if starred help has started
            if (i * 24 + event.hour <= dayID * 24 + groupPosition + 6) {
              // check if starred help ends after this currentHour

              if (i * 24 + event.hour + event.totalDuration > dayID * 24
                  + groupPosition + 6) {
                groupTitle += event.title + ", ";
              }
            }
          }
        }
      }
      name.setText(groupTitle.substring(0, groupTitle.length() - 2));
      name.setSelected(true);


      view.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          if (isExpanded)
            listView.collapseGroup(groupPosition);
          else
            listView.expandGroup(groupPosition);
        }
      });

      view.setOnLongClickListener(new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
          if (isExpanded)
            collapseAll(groupPosition);
          else
            expandAll(groupPosition);
          return false;
        }
      });

      if (isExpanded)
        view.setBackgroundResource(R.drawable.group_expanded);
      else
        view.setBackgroundResource(R.drawable.group_collapsed);


      return view;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
      return MainActivity.dayList.get(dayID).get(groupPosition).size();
    }

    @Override
    public int getGroupCount() {
      return MainActivity.dayList.get(dayID).size();
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
      return false;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
      return childPosition;
    }

    @Override
    public long getGroupId(int groupPosition) {
      return groupPosition;
    }

    @Override
    public boolean hasStableIds() {
      return false;
    }
  }
}
