package org.boardgamers.wbc;

import java.util.List;

/**
 * Created by Kevin
 */
public class SearchListAdapter extends DefaultListAdapter {

  public SearchListAdapter(DefaultListFragment f, List<List<Event>> e) {
    super(f, e);
  }

  @Override
  public String getGroupTitle(final int groupPosition) {
    return dayStrings[groupPosition];
  }

  @Override
  public int getGroupViewId(final int groupPosition) {
    return R.layout.schedule_group_small;
  }

}
