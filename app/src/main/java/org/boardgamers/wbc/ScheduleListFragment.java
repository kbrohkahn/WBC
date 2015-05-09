package org.boardgamers.wbc;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class ScheduleListFragment extends DefaultListFragment {
  //private final String TAG="Schedule List Fragment";

  private final int GROUPS_PER_DAY=18+1;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view=super.onCreateView(inflater, container, savedInstanceState);
    if (view!=null) {
      view.findViewById(R.id.sl_hour).setVisibility(View.GONE);
      view.findViewById(R.id.sl_hour_divider).setVisibility(View.GONE);
    }

    listView.setFastScrollEnabled(true);

    new PopulateScheduleAdapterTask(this, GROUPS_PER_DAY*MainActivity.TOTAL_DAYS).execute(0, 0, 0);

    return view;
  }

  /**
   * Get the current group, based on currentDay and currentHour. If hour is between 24
   * and 7, select group 0 of that day
   *
   * @return groupNumber
   */
  public int getCurrentGroup() {
    int hoursIntoConvention=MainActivity.getHoursIntoConvention();

    if (hoursIntoConvention==-1) {
      return 0;
    } else {
      return hoursIntoConvention/24*GROUPS_PER_DAY+Math.max(0, hoursIntoConvention%24-6);
    }
  }

  class PopulateScheduleAdapterTask extends PopulateAdapterTask {
    public PopulateScheduleAdapterTask(DefaultListFragment f, int g) {
      fragment=f;
      numGroups=g;
    }

    @Override
    protected void onPostExecute(Integer integer) {
      super.onPostExecute(integer);

      listView.setSelectedGroup(getCurrentGroup());
    }

    @Override
    protected Integer doInBackground(Integer... params) {
      listAdapter=new ScheduleListAdapter(fragment, events, 1);

      WBCDataDbHelper dbHelper=new WBCDataDbHelper(getActivity());
      dbHelper.getReadableDatabase();
      //List<Boolean> visible=dbHelper.getAllVisible();
      List<Event> tempEvents=dbHelper.getAllEvents(MainActivity.userId);
      dbHelper.close();

      Event event;
      while (tempEvents.size()>0) {
        event=tempEvents.remove(0);

        events.get(event.day*GROUPS_PER_DAY+event.hour-6).add(event);

        if (event.starred) {
          events.get(event.day*GROUPS_PER_DAY).add(event);
        }
      }

      listAdapter.events=events;

      return 1;
    }
  }
}
