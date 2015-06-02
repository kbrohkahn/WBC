package org.boardgamers.wbc;

import android.view.View;
import android.view.ViewGroup;
import android.widget.SectionIndexer;

import java.util.List;

/**
 * Created by Kevin Adapter for full schedule ExpandableListAdapter
 */
public class ScheduleListAdapter extends DefaultListAdapter implements SectionIndexer {
  public static final int GROUPS_PER_DAY=18+1;
  // 18 hours per day (0700 thru 2400) plus "My Events"
  private final int GROUP_HOUR_OFFSET=7-1;    // first hour is 7, offset 1 hour for "My Events"

  private final String[] hours;
  private final String[] sections;

  public ScheduleListAdapter(DefaultListFragment f, List<List<Event>> e, int i) {
    super(f, e, i);

    hours=f.getResources().getStringArray(R.array.hours_24);

    sections=dayStrings;
  }

  @Override
  public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild,
                           View view, ViewGroup parent) {
    view=super.getChildView(groupPosition, childPosition, isLastChild, null, parent);
    view.findViewById(R.id.li_hour).setVisibility(View.GONE);

    return view;
  }

  @Override
  public View getGroupView(final int groupPosition, final boolean isExpanded, View view,
                           ViewGroup parent) {
    view=super.getGroupView(groupPosition, isExpanded, view, parent);

    if (groupPosition%GROUPS_PER_DAY==0) {
      view.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          if (isExpanded) {
            fragment.collapseGroups(groupPosition, GROUPS_PER_DAY);
          } else {
            fragment.expandGroups(groupPosition, GROUPS_PER_DAY);
          }
        }
      });
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

      int groupHoursIntoConvention=
          groupPosition/GROUPS_PER_DAY*24+groupPosition%GROUPS_PER_DAY+GROUP_HOUR_OFFSET;
      for (int i=0; i<=groupPosition; i++) {
        for (Event event : events.get(i)) {
          if (event.starred && event.day*24+event.hour<=groupHoursIntoConvention &&
              event.day*24+event.hour+event.duration>groupHoursIntoConvention) {
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
  public void updateEvents(Event[] updatedEvents) {
    for (Event event : updatedEvents) {
      int group=event.day*GROUPS_PER_DAY+(int) event.hour-GROUP_HOUR_OFFSET;
      Event tempEvent;

      boolean isInList=false;
      for (int i=0; i<events.get(group).size(); i++) {
        tempEvent=events.get(group).get(i);
        if (tempEvent.id==event.id) {
          tempEvent.starred=event.starred;
          isInList=true;
          break;
        }
      }

      if (!isInList) {
        events.get(group).add(0, event);
      }

      // add or remove from my events in full schedule
      //      group=event.day*GROUPS_PER_DAY;
      //      if (event.starred) {
      //        int index;
      //        for (index=0; index<events.get(group).size(); index++) {
      //          tempEvent=events.get(group).get(index);
      //          if (event.hour<tempEvent.hour ||
      //              (event.hour==tempEvent.hour && event.title.compareToIgnoreCase(tempEvent.title)==1)) {
      //            break;
      //          }
      //        }
      //        events.get(group).add(index, event);
      //      } else {
      //        for (int i=0; i<events.get(group).size(); i++) {
      //          tempEvent=events.get(group).get(i);
      //          if (tempEvent.id==event.id) {
      //            events.get(group).remove(tempEvent);
      //            break;
      //          }
      //        }
      //      }
    }
  }

  @Override
  public void removeEvents(Event[] deletedEvents) {
    int group;
    for (Event event : deletedEvents) {
      group=event.day*GROUPS_PER_DAY+(int) event.hour-GROUP_HOUR_OFFSET;
      for (int i=0; i<events.get(group).size(); i++) {
        if (events.get(group).get(i).id==event.id) {
          events.get(group).remove(i);
          break;
        }
      }

      group=event.day*GROUPS_PER_DAY;
      for (int i=0; i<events.get(group).size(); i++) {
        if (events.get(group).get(i).id==event.id) {
          events.get(group).remove(i);
          break;
        }
      }

    }
  }
}
