package org.boardgamers.wbc;

import android.content.Context;

/**
 * Created by Kevin
 */
public class SearchListAdapter extends DefaultScheduleListAdapter {

  public SearchListAdapter(Context c, SearchResultFragment f) {
    super(c, f);
  }

  @Override
  public void changeEventStar(Event event, int groupPosition, int childPosition) {
    changeEventStar(event, groupPosition, childPosition, true);
  }

  public void changeEventStar(Event event, int groupPosition, int childPosition,
                              boolean updateAllStarred) {
    event.starred=!event.starred;

    if (updateAllStarred) {
      ((SearchResultFragment) fragment).setAllStared();
    }

    super.changeEventStar(event, groupPosition, childPosition);
  }

  @Override
  public String getGroupTitle(final int groupPosition) {
    return dayStrings[groupPosition];
  }

  @Override
  public int getGroupViewId(final int groupPosition) {
    return R.layout.schedule_group_small;
  }

  @Override
  public Object getGroup(int groupPosition) {
    return ((SearchResultFragment) fragment).getGroup(groupPosition);
  }

  @Override
  public int getGroupCount() {
    return MainActivity.TOTAL_DAYS;
  }

}
