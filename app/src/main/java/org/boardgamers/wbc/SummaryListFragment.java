package org.boardgamers.wbc;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class SummaryListFragment extends DefaultListFragment {

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view=super.onCreateView(inflater, container, savedInstanceState);

    new PopulateSummaryAdapterTask(this, MainActivity.TOTAL_DAYS).execute(0, 0, 0);

    return view;
  }

  class PopulateSummaryAdapterTask extends PopulateAdapterTask {
    public PopulateSummaryAdapterTask(DefaultListFragment f, int g) {
      fragment=f;
      numGroups=g;
    }

    @Override
    protected Integer doInBackground(Integer... params) {
      listAdapter=new SummaryListAdapter(fragment, events);

      WBCDataDbHelper dbHelper=new WBCDataDbHelper(getActivity());
      List<Event> tempEvents=dbHelper.getStarredEvents();

      List<List<Event>> events=listAdapter.events;
      Event event;
      while (tempEvents.size()>0) {
        event=tempEvents.remove(0);
        events.get(event.day).add(event);
      }

      return 1;
    }
  }
}
