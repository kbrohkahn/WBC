package org.boardgamers.wbc;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ScheduleContainer extends Fragment implements
    ActionBar.OnNavigationListener {
  private final String TAG = "Schedule Container";
  private ViewPager viewPager;
  private int currentPage = -1;

  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.schedule_container, container, false);

    // setup page adapter and view pager for action bar
    viewPager = (ViewPager) view.findViewById(R.id.pager);
    viewPager.setAdapter(new DayPagerAdapter(getFragmentManager()));
    viewPager.setOffscreenPageLimit(4);
    viewPager
        .setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
          @Override
          public void onPageSelected(int position) {
            MainActivity.actionBarTitle = "WBC-" + MainActivity.dayStrings[position];

            try {
              MainActivity.activity.getActionBar().setTitle(MainActivity.actionBarTitle);
            } catch (NullPointerException e) {
              Log.d(TAG, "ERROR: Could not get action bar");
            }
          }
        });

    // set viewpager to current currentDay
    if (currentPage == -1) {
      if (MainActivity.currentDay > -1)
        currentPage = MainActivity.currentDay;
      else
        currentPage = 0;
    }

    viewPager.setCurrentItem(currentPage);

    return view;
  }

  @Override
  public boolean onNavigationItemSelected(int itemPosition, long itemId) {
    viewPager.setCurrentItem(itemPosition);
    return false;
  }

  public static class DayPagerAdapter extends FragmentPagerAdapter {
    public DayPagerAdapter(FragmentManager fragmentManager) {
      super(fragmentManager);
    }

    @Override
    public Fragment getItem(int arg0) {
      Fragment f = new ScheduleFragment();
      Bundle args = new Bundle();
      args.putInt("current_day", arg0);
      f.setArguments(args);
      return f;
    }

    @Override
    public int getCount() {
      return MainActivity.dayList.size();
    }
  }
}
