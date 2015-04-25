package org.boardgamers.wbc;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ScheduleFragment extends DefaultListFragment {
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view=super.onCreateView(inflater, container, savedInstanceState);

    listView.setFastScrollEnabled(true);

    if (MainActivity.currentDay>-1) {
      listView.setSelectedGroup(
          MainActivity.currentDay*MainActivity.GROUPS_PER_DAY+MainActivity.currentHour-5);
    }

    return view;
  }

  @Override
  protected DefaultScheduleListAdapter getAdapter() {
    return new ScheduleListAdapter(getActivity(), this);
  }
}
