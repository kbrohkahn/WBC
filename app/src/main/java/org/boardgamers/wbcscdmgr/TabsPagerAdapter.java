package org.boardgamers.wbcscdmgr;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Kevin
 * Tabs pager adapter for MainActivity. Contains SummaryFragment, ScheduleFragment,
 * and UserDataFragment
 */
class TabsPagerAdapter extends FragmentPagerAdapter {
	private final SummaryListFragment summaryListFragment;
	private final ScheduleListFragment scheduleListFragment;
	private final UserDataListFragment userDataListFragment;

	private final String[] tabTitles = {"Starred", "Schedule", "My Data"};

	public TabsPagerAdapter(FragmentManager fm) {
		super(fm);

		summaryListFragment = new SummaryListFragment();
		scheduleListFragment = new ScheduleListFragment();
		userDataListFragment = new UserDataListFragment();
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
		return tabTitles.length;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return tabTitles[position];
	}
}

