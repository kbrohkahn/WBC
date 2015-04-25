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
      case MainActivity.SUMMARY_FRAGMENT_POSITION:
        return new SummaryFragment();
      case MainActivity.SCHEDULE_FRAGMENT_POSITION:
        return new ScheduleFragment();
      case MainActivity.USER_DATA_FRAGMENT_POSITION:
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

