package org.boardgamers.wbc;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SummaryFragment extends DefaultListFragment {
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view=super.onCreateView(inflater, container, savedInstanceState);

    if (MainActivity.currentDay>-1) {
      listView.setSelectedGroup(MainActivity.currentDay);
    }

    return view;
  }

  @Override
  protected DefaultScheduleListAdapter getAdapter() {
    return new SummaryListAdapter(getActivity(), this);
  }
}
