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
  public TabsPagerAdapter(FragmentManager fm) {
    super(fm);
  }

  @Override
  public Fragment getItem(int index) {
    switch (index) {
      case 0:
        return new SummaryFragment();
      case 1:
        return new ScheduleFragment();
      case 2:
        return new UserDataFragment();
    }

    return null;
  }

  @Override
  public int getCount() {
    // get item count - equal to number of tabs
    return 3;
  }
}

