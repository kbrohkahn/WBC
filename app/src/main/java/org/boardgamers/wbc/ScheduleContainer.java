package org.boardgamers.wbc;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ScheduleContainer extends Fragment implements
    ActionBar.OnNavigationListener {
  private static String TAG = "Schedule Activity";

  private DayPagerAdapter pageAdapter;
  private ViewPager viewPager;

  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.schedule_container, container, false);

    // setup page adapter and view pager for action bar
    pageAdapter = new DayPagerAdapter(getFragmentManager());
    viewPager = (ViewPager) view.findViewById(R.id.pager);
    viewPager.setAdapter(pageAdapter);

    // viewPager.setOffscreenPageLimit(1);
    viewPager
        .setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
          @Override
          public void onPageSelected(int position) {
            MainActivity.drawerTitle = "WBC-" + MainActivity.dayStrings[position];
            MainActivity.activity.getActionBar().setTitle(MainActivity.drawerTitle);
          }
        });

    // set viewpager to current currentDay
    if (MainActivity.currentDay > -1)
      viewPager.setCurrentItem(MainActivity.currentDay);

    return view;
  }

  @Override
  public boolean onNavigationItemSelected(int itemPosition, long itemId) {
    viewPager.setCurrentItem(itemPosition);
    return false;
  }

  @Override
  public void onResume() {
    pageAdapter.notifyDataSetChanged();
    super.onResume();
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
