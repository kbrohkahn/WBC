package org.boardgamers.wbc;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class SearchFragment extends DefaultListFragment {
  //private final String TAG="Search Fragment";

  private ArrayList<ArrayList<Event>> resultEvents;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    loadEvents();

    View view=super.onCreateView(inflater, container, savedInstanceState);

    if (MainActivity.currentDay>-1) {
      listView.setSelectedGroup(MainActivity.currentDay);
    }

    return view;
  }

  public void loadEvents() {
    resultEvents=new ArrayList<>();
    for (int i=0; i<MainActivity.TOTAL_DAYS; i++) {
      resultEvents.add(new ArrayList<Event>());
    }

    String query=getActivity().getIntent().getStringExtra("query");
    getActivity().setTitle(query);

    for (int i=0; i<MainActivity.dayList.size(); i++) {
      for (Event event : MainActivity.dayList.get(i)) {
        if (event.title.contains(query) || event.format.contains(query)) {
          resultEvents.get(i/MainActivity.GROUPS_PER_DAY).add(event);
        }
      }
    }
  }

  @Override
  protected DefaultScheduleListAdapter getAdapter() {
    return new SearchListAdapter(getActivity(), this);
  }

  public ArrayList<Event> getGroup(int group) {
    return resultEvents.get(group);
  }
}
