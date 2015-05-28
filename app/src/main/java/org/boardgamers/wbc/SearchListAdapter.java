package org.boardgamers.wbc;

import java.util.List;

/**
 * Created by Kevin
 */
public class SearchListAdapter extends DefaultListAdapter {

  public SearchListAdapter(DefaultListFragment f, List<List<Event>> e, int i) {
    super(f, e, i);
  }

  @Override
  public String getGroupTitle(final int groupPosition) {
    return dayStrings[groupPosition];
  }

  @Override
  public int getGroupViewId(final int groupPosition) {
    return R.layout.list_group_small;
  }

  @Override
  public void changeEventStar(Event event) {
    super.changeEventStar(event);
    ((SearchListFragment) fragment).setAllStared();
  }
}
