package org.boardgamers.wbc;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SummaryFragment extends Fragment {
  final static String TAG = "Summary Activity";
  public static ArrayList<ArrayList<Event>> summaryList;
  private static SummaryListAdapter listAdapter;
  private ExpandableListView listView;
  private String[] dayStrings;


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.summary, container, false);

    dayStrings = getResources().getStringArray(R.array.days);

    SummaryFragment.summaryList = new ArrayList<ArrayList<Event>>(dayStrings.length);
    for (int i = 0; i < dayStrings.length; i++) {
      SummaryFragment.summaryList.add(new ArrayList<Event>());
    }

    // setup list adapter and list view
    listAdapter = new SummaryListAdapter();

    listView = (ExpandableListView) view.findViewById(R.id.summary_list_view);
    listView.setAdapter(listAdapter);
    listView.setDividerHeight(0);
    for (int i = 0; i < listAdapter.getGroupCount(); i++)
      listView.expandGroup(i);

    return view;
  }

  @Override
  public void onResume() {
    summaryList = new ArrayList<ArrayList<Event>>(MainActivity.dayList.size());
    for (int i = 0; i < MainActivity.dayList.size(); i++) {
      summaryList.add(new ArrayList<Event>());
      for (Event event : MainActivity.dayList.get(i).get(0)) {
        summaryList.get(i).add(event);
      }
    }
    listAdapter.notifyDataSetChanged();

    // get time and go to current day
    if (MainActivity.day > -1)
      listView.setSelectedGroup(MainActivity.day);

    super.onResume();
  }

  public void removeEventStar(Event event) {
    // remove from summary list
    List<Event> searchList = summaryList.get(event.day);
    for (int i = 0; i < searchList.size(); i++) {
      if (searchList.get(i).identifier.equalsIgnoreCase(event.identifier)) {
        searchList.remove(i);
      }
    }

    // remove from "My Events" in day list
    searchList = MainActivity.dayList.get(event.day).get(0);
    for (Event tempE : searchList) {
      if (tempE.identifier.equalsIgnoreCase(event.identifier)) {
        MainActivity.dayList.get(event.day).get(0).remove(tempE);
        break;
      }
    }

    // remove from day list
    searchList = MainActivity.dayList.get(event.day).get(event.hour - 6);
    for (Event tempE : searchList) {
      if (tempE.identifier.equalsIgnoreCase(event.identifier)) {
        tempE.starred = false;
        break;
      }
    }
    listAdapter.notifyDataSetChanged();
  }

  class SummaryListAdapter extends BaseExpandableListAdapter {
    private final LayoutInflater inflater;

    public SummaryListAdapter() {
      inflater = (LayoutInflater)
          getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
      return summaryList.get(groupPosition).get(childPosition);
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View view, ViewGroup parent) {
      final Event event = (Event) getChild(groupPosition, childPosition);

      int tColor = MainActivity.getTextColor(event);
      int tType = MainActivity.getTextStyle(event);

      view = inflater.inflate(R.layout.schedule_item, parent, false);

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
          MainActivity.showMapDialog(getActivity().getFragmentManager(), event.location);
        }
      });

      ImageView starIV = (ImageView) view.findViewById(R.id.si_star);
      starIV.setImageResource(event.starred ? R.drawable.star_on
          : R.drawable.star_off);
      starIV.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          removeEventStar(event);
        }
      });

      boolean started = event.day * 24 + event.hour <= MainActivity.day * 24 + MainActivity.hour;
      boolean ended = event.day * 24 + event.hour + event.totalDuration <= MainActivity.day
          * 24 + MainActivity.hour;
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
          MainActivity.selectGame(getActivity(), tID, eID);
        }
      });

      return view;
    }

    @Override
    public Object getGroup(int groupPosition) {
      return summaryList.get(groupPosition).get(groupPosition);
    }

    @Override
    public View getGroupView(final int groupPosition,
                             final boolean isExpanded, View view, ViewGroup parent) {
      if (view == null)
        view = inflater.inflate(R.layout.summary_day_label, parent, false);

      String groupTitle = dayStrings[groupPosition];
      TextView name = (TextView) view.findViewById(R.id.summary_day_text);
      name.setText(groupTitle);

      if (isExpanded)
        view.setBackgroundResource(R.drawable.group_expanded);
      else
        view.setBackgroundResource(R.drawable.group_collapsed);

      return view;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
      return summaryList.get(groupPosition).size();
    }

    @Override
    public int getGroupCount() {
      return summaryList.size();
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
