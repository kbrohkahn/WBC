package org.boardgamers.wbc;

import android.content.Context;

import java.util.ArrayList;

/**
 * Created by Kevin
 * Adapter for SummaryFragment ExpandableListView
 */
public class SummaryListAdapter extends DefaultScheduleListAdapter {

  public SummaryListAdapter(Context c, SummaryFragment f) {
    super(c, f);
  }

  @Override
  public void changeEventStar(Event event, int groupPosition, int childPosition) {
    MainActivity.dayList.get(groupPosition*MainActivity.GROUPS_PER_DAY).remove(childPosition);

    ArrayList<Event> events=
        MainActivity.dayList.get(event.day*MainActivity.GROUPS_PER_DAY+event.hour-6);
    for (Event tempEvent : events) {
      if (tempEvent.identifier.equalsIgnoreCase(event.identifier)) {
        tempEvent.starred=false;
        break;
      }
    }

    super.changeEventStar(event, groupPosition, childPosition);
  }

  @Override
  public String getGroupTitle(final int groupPosition) {
    return MainActivity.dayStrings[groupPosition];
  }

  @Override
  public Object getGroup(int groupPosition) {
    return MainActivity.dayList.get(groupPosition*MainActivity.GROUPS_PER_DAY);
  }

  @Override
  public int getGroupCount() {
    return MainActivity.TOTAL_DAYS;
  }

}

