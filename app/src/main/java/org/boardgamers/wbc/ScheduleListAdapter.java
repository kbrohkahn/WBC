package org.boardgamers.wbc;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Kevin
 * Adapter for full schedule ExpandableListAdapter
 */
public class ScheduleListAdapter extends DefaultListAdapter implements SectionIndexer {
  private final int GROUPS_PER_DAY=18+1;

  private final String[] hours;
  private final String[] sections;

  public List<Boolean> tournamentsVisible;

  public ScheduleListAdapter(DefaultListFragment f, List<List<Event>> e, int i) {
    super(f, e, i);

    SharedPreferences settings=f.getActivity()
        .getSharedPreferences(f.getResources().getString(R.string.sp_file_name),
            Context.MODE_PRIVATE);
    int hoursID=(settings.getBoolean("24_hour", true) ? R.array.hours_24 : R.array.hours_12);
    hours=f.getResources().getStringArray(hoursID);

    sections=dayStrings;
  }

  @Override
  public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild,
                           View view, ViewGroup parent) {
    view=super.getChildView(groupPosition, childPosition, isLastChild, view, parent);

    view.findViewById(R.id.li_hour).setVisibility(View.GONE);
    if (groupPosition%GROUPS_PER_DAY==0) {
      Event event=events.get(groupPosition).get(childPosition);

//      int minutes=0;
//      if (event.duration%1!=0) {
//        minutes=(int) (event.duration%1*60);
//      }
//      int lastHour=((event.hour+(int) event.duration)%24)*100 + minutes;
      TextView titleTV=(TextView) view.findViewById(R.id.li_title);
      titleTV.setText(event.hour*100 + ": " + event.title);
    }
    return view;
  }


  @Override
  public int getGroupViewId(final int groupPosition) {
    if (groupPosition%GROUPS_PER_DAY==0) {
      return R.layout.list_group_large;
    } else {
      return R.layout.list_group_small;
    }
  }

  @Override
  public String getGroupTitle(final int groupPosition) {
    if (groupPosition%GROUPS_PER_DAY==0) {
      return dayStrings[groupPosition/GROUPS_PER_DAY];
    } else {
      String groupTitle=hours[(groupPosition%GROUPS_PER_DAY)-1]+": ";

      int day;
      for (int i=0; i<groupPosition/GROUPS_PER_DAY+1; i++) {
        for (Event event : events.get(i*GROUPS_PER_DAY)) {
          // check if starred help has started
          day=groupPosition/GROUPS_PER_DAY;
          if (day*24+groupPosition+6>=i*24+event.hour &&
              day*24+groupPosition+6<i*24+event.hour+event.totalDuration) {
            groupTitle+=event.title+", ";
          }
        }
      }

      return groupTitle.substring(0, groupTitle.length()-2);
    }
  }

  @Override
  public int getPositionForSection(int sectionIndex) {
    return sectionIndex*GROUPS_PER_DAY;
  }

  public int getSectionForPosition(int position) {
    return position/GROUPS_PER_DAY;
  }

  public Object[] getSections() {
    return sections;
  }

  @Override
  public void updateStarredEvent(Event event) {
    int group=event.day*GROUPS_PER_DAY+event.hour-6;
    Event tempEvent;

    for (int i=0; i<events.get(group).size(); i++) {
      tempEvent=events.get(group).get(i);
      if (tempEvent.id==event.id) {
        tempEvent.starred=event.starred;
        break;
      }
    }

    // add or remove from my events in full schedule
    group=event.day*GROUPS_PER_DAY;
    if (event.starred) {
      int index;
      for (index=0; index<events.get(group).size(); index++) {
        tempEvent=events.get(group).get(index);
        if (event.hour<tempEvent.hour ||
            (event.hour==tempEvent.hour && event.title.compareToIgnoreCase(tempEvent.title)==1)) {
          break;
        }
      }
      events.get(group).add(index, event);
    } else {
      for (int i=0; i<events.get(group).size(); i++) {
        tempEvent=events.get(group).get(i);
        if (tempEvent.id==event.id) {
          events.get(group).remove(tempEvent);
          break;
        }
      }
    }
  }
}
