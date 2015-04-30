package org.boardgamers.wbc;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

/**
 * Created by Kevin
 * Tabs pager adapter for MainActivity. Contains SummaryFragment, ScheduleFragment,
 * and UserDataFragment
 */
public class TabsPagerAdapter extends FragmentPagerAdapter {
  private SummaryListFragment summaryListFragment;
  private ScheduleListFragment scheduleListFragment;
  private UserDataListFragment userDataListFragment;

  public TabsPagerAdapter(FragmentManager fm) {
    super(fm);

    summaryListFragment=new SummaryListFragment();
    scheduleListFragment=new ScheduleListFragment();
    userDataListFragment=new UserDataListFragment();
  }

  @Override
  public DefaultListFragment getItem(int index) {
    switch (index) {
      case 0:
        return summaryListFragment;
      case 1:
        return scheduleListFragment;
      case 2:
        return userDataListFragment;
    }

    return null;
  }

  @Override
  public int getCount() {
    // get item count - equal to number of tabs
    return 3;
  }
}

