package org.boardgamers.wbc;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class ScheduleFragment extends DefaultListFragment {
  public static List<List<Event>> dayList;

  /**
   * Add starred help to "My Events" group in list
   *
   * @param event - Event that was starred
   */
  public static void addStarredEvent(Event event) {
    Event starredEvent=
        new Event(event.id, event.identifier, event.tournamentID, event.day, event.hour, event.title,
            event.eClass, event.format, event.qualify, event.duration, event.continuous,
            event.totalDuration, event.location);
    starredEvent.starred=true;

    // add event to day list(time, then title)
    List<Event> events=dayList.get(starredEvent.day*MainActivity.GROUPS_PER_DAY);
    Event tempEvent;
    int index;
    for (index=0; index<events.size(); index++) {
      tempEvent=events.get(index);
      if (starredEvent.hour<tempEvent.hour || (starredEvent.hour==tempEvent.hour &&
          starredEvent.title.compareToIgnoreCase(tempEvent.title)==1)) {
        break;
      }
    }

    events.add(index, starredEvent);
  }

  /**
   * Remove starred help from "My Events" group in list
   *
   * @param identifier - help id
   * @param day        - help's currentDay, used to find which my events group
   */
  public static void removeStarredEvent(String identifier, int day) {
    List<Event> myEvents=dayList.get(day*MainActivity.GROUPS_PER_DAY);
    for (Event tempE : myEvents) {
      if (tempE.identifier.equalsIgnoreCase(identifier)) {
        myEvents.remove(tempE);
        break;
      }
    }
  }


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view=super.onCreateView(inflater, container, savedInstanceState);
    if (view!=null) {
      view.findViewById(R.id.sl_hour).setVisibility(View.GONE);
      view.findViewById(R.id.sl_hour_divider).setVisibility(View.GONE);
    }

    listView.setFastScrollEnabled(true);
    listView.setSelectedGroup(MainActivity.getCurrentGroup());

    return view;
  }



  public void startLoadTask() {
    new LoadScheduleAdapterClass(this).doInBackground(0, 0, 0);
  }

  class LoadScheduleAdapterClass extends DefaultListFragment.LoadAdapterClass {
    public LoadScheduleAdapterClass(ScheduleFragment f) {
      fragment=f;
    }

    @Override
    protected void onPostExecute(Integer integer) {
      listView.setSelectedGroup(MainActivity.getCurrentGroup()/MainActivity.GROUPS_PER_DAY);

      super.onPostExecute(integer);
    }

    @Override
    protected Integer doInBackground(Integer... params) {
      listAdapter=new ScheduleListAdapter(getActivity(), (ScheduleFragment) fragment);

      return super.doInBackground(params);
    }
  }





}
