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
public class ScheduleListAdapter extends DefaultListAdapter implements SectionIndexer {
  private final int GROUPS_PER_DAY=18+1;

  private final String[] hours;
  private final String[] sections;

  public List<Boolean> tournamentsVisible;

  public ScheduleListAdapter(DefaultListFragment f, List<List<Event>> e) {
    super(f, e);

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
    Event temp=events.get(groupPosition).get(childPosition);
    if (tournamentsVisible!=null && !tournamentsVisible.get(temp.tournamentID)) {
      return inflater.inflate(R.layout.schedule_item_gone, parent, false);
    }

    view=super.getChildView(groupPosition, childPosition, isLastChild, view, parent);
    if (groupPosition%GROUPS_PER_DAY!=0) {
      view.findViewById(R.id.si_hour).setVisibility(View.GONE);
    }
    return view;
  }

  @Override
  public int getGroupViewId(final int groupPosition) {
    if (groupPosition%GROUPS_PER_DAY==0) {
      return R.layout.schedule_group_large;
    } else {
      return R.layout.schedule_group_small;
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

}
