package org.boardgamers.wbc;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SectionIndexer;

import java.util.List;

/**
 * Created by Kevin
 * Adapter for full schedule ExpandableListAdapter
 */
public class ScheduleListAdapter extends DefaultScheduleListAdapter implements SectionIndexer {
  private final String[] hours;
  private final String[] sections;

  public ScheduleListAdapter(Context c, ScheduleFragment f) {
    super(c, f);

    SharedPreferences settings=
        c.getSharedPreferences(c.getResources().getString(R.string.sp_file_name),
            Context.MODE_PRIVATE);
    int hoursID=(settings.getBoolean("24_hour", true) ? R.array.hours_24 : R.array.hours_12);
    hours=c.getResources().getStringArray(hoursID);

    sections=new String[MainActivity.TOTAL_DAYS];
    System.arraycopy(MainActivity.dayStrings, 0, sections, 0, sections.length);
  }

  @Override
  public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild,
                           View view, ViewGroup parent) {
    view=super.getChildView(groupPosition, childPosition, isLastChild, view, parent);
    if (groupPosition%MainActivity.GROUPS_PER_DAY!=0) {
      view.findViewById(R.id.si_hour).setVisibility(View.GONE);
    }
    return view;
  }

  @Override
  public void changeEventStar(Event event, int groupPosition, int childPosition) {
    event.starred=!event.starred;

    if (event.starred) {
      MainActivity.addStarredEvent(event);
    } else {
      if (groupPosition%MainActivity.GROUPS_PER_DAY==0) {
        MainActivity.dayList.get(groupPosition).remove(childPosition);

        List<Event> events=
            MainActivity.dayList.get(event.day*MainActivity.GROUPS_PER_DAY+event.hour-6);
        for (Event tempEvent : events) {
          if (tempEvent.identifier.equalsIgnoreCase(event.identifier)) {
            tempEvent.starred=false;
            break;
          }
        }

      } else {
        MainActivity.removeStarredEvent(event.identifier, event.day);
      }
    }

    super.changeEventStar(event, groupPosition, childPosition);
  }

  @Override
  public int getGroupViewId(final int groupPosition) {
    if (groupPosition%MainActivity.GROUPS_PER_DAY==0) {
      return R.layout.schedule_group_large;
    } else {
      return R.layout.schedule_group_small;
    }
  }

  @Override
  public String getGroupTitle(final int groupPosition) {
    if (groupPosition%MainActivity.GROUPS_PER_DAY==0) {
      return MainActivity.dayStrings[groupPosition/MainActivity.GROUPS_PER_DAY];
    } else {
      String groupTitle=hours[(groupPosition%MainActivity.GROUPS_PER_DAY)-1]+": ";

      for (int i=0; i<MainActivity.TOTAL_DAYS; i++) {
        for (Event event : MainActivity.dayList.get(i*MainActivity.GROUPS_PER_DAY)) {
          // check if starred help has started
          if (groupPosition/MainActivity.GROUPS_PER_DAY*24+groupPosition+6>=i*24+event.hour &&
              groupPosition/MainActivity.GROUPS_PER_DAY*24+groupPosition+6<
                  i*24+event.hour+event.totalDuration) {
            groupTitle+=event.title+", ";
          }
        }
      }

      return groupTitle.substring(0, groupTitle.length()-2);
    }
  }

  @Override
  public Object getGroup(int groupPosition) {
    return MainActivity.dayList.get(groupPosition);
  }

  @Override
  public int getGroupCount() {
    return MainActivity.dayList.size();
  }

  @Override
  public int getPositionForSection(int sectionIndex) {
    return sectionIndex*MainActivity.GROUPS_PER_DAY;
  }

  public int getSectionForPosition(int position) {
    return position/MainActivity.GROUPS_PER_DAY;
  }

  public Object[] getSections() {
    return sections;
  }
}
