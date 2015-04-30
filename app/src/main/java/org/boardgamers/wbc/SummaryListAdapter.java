package org.boardgamers.wbc;

import java.util.List;

/**
 * Created by Kevin
 * Adapter for SummaryFragment ExpandableListView
 */
public class SummaryListAdapter extends DefaultListAdapter {
  public SummaryListAdapter(DefaultListFragment f, List<List<Event>> e) {
    super(f, e);
  }

  @Override
  public String getGroupTitle(final int groupPosition) {
    return dayStrings[groupPosition];
  }
}
