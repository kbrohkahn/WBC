package org.boardgamers.wbc;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import java.util.List;

public class ScheduleActivity extends FragmentActivity implements
    ActionBar.OnNavigationListener {
	private static String TAG="Schedule Activity";

	private DayPagerAdapter pageAdapter;
	private ViewPager viewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		// load action bar
    try {
      getActionBar().setDisplayHomeAsUpEnabled(true);
      getActionBar().setHomeButtonEnabled(true);
    } catch (NullPointerException e) {
      Toast.makeText(this, "Error: cannot set home button enabled", Toast.LENGTH_SHORT).show();
      Log.d(TAG, "Error: cannot set home button enabled");
    }    getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		// setup page adapter and view pager for action bar
		pageAdapter=new DayPagerAdapter(getFragmentManager());
		viewPager=(ViewPager) findViewById(R.id.pager);
		viewPager.setAdapter(pageAdapter);

		// viewPager.setOffscreenPageLimit(1);
		viewPager
		    .setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			    @Override
			    public void onPageSelected(int position) {
				    getActionBar().setSelectedNavigationItem(position);
			    }
		    });

		SpinnerAdapter mSpinnerAdapter=ArrayAdapter.createFromResource(this,
		    R.array.days, android.R.layout.simple_spinner_dropdown_item);

		getActionBar().setListNavigationCallbacks(mSpinnerAdapter, this);

		// set viewpager to current day
		if (MyApp.day>-1)
			viewPager.setCurrentItem(MyApp.day);

	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		viewPager.setCurrentItem(itemPosition);
		return false;
	}

	@Override
	protected void onResume() {
		pageAdapter.notifyDataSetChanged();
		super.onResume();
	}

	@Override
	protected void onPause() {
		// save starred states for all events
		SharedPreferences.Editor editor=getSharedPreferences(
		    getResources().getString(R.string.sp_file_name),
		    Context.MODE_PRIVATE).edit();
		String starPrefString=getResources().getString(R.string.sp_event_starred);

		int numDays=getResources().getStringArray(R.array.days).length;
		for (int d=0; d<numDays; d++) {
			for (int i=1; i<18; i++) {
				List<Event> events=MyApp.dayList.get(d).get(i);
        for (Event event:events) {
					editor.putBoolean(starPrefString+event.identifier, event.starred);
				}
			}
		}
    editor.apply();

		super.onPause();
	}

	/**
	 * Add starred event to "My Events" group in list
	 * 
	 * @param event - Event that was starred
	 */
	public static void addStarredEvent(Event event) {
		List<Event> myEvents=MyApp.dayList.get(event.day).get(0);

		Event starredEvent=new Event(event.identifier, event.tournamentID,
		    event.day, event.hour, event.title, event.eClass, event.format,
		    event.qualify, event.duration, event.continuous,
		    event.totalDuration, event.location);
		starredEvent.starred=true;
		// get position in starred list to add (time, then title)
		int index=0;
		for (Event eTemp : myEvents) {
			if (starredEvent.hour<eTemp.hour
			    ||(starredEvent.hour==eTemp.hour&&starredEvent.title
			        .compareToIgnoreCase(eTemp.title)==1))
				break;
			else
				index++;
		}
		MyApp.dayList.get(event.day).get(0).add(index,
		    starredEvent);

		myEvents=Summary.summaryList.get(event.day);
		index=0;
		for (Event eTemp : myEvents) {
			if (starredEvent.hour<eTemp.hour
			    ||(starredEvent.hour==eTemp.hour&&starredEvent.title
			        .compareToIgnoreCase(eTemp.title)==1))
				break;
			else
				index++;
		}

		// TODO add subsequent events
		// myEvents.add(event, index);
	}

	/**
	 * Remove starred event from "My Events" group in list
	 * 
	 * @param identifier
	 *          - event id
	 * @param day
	 *          - event's day, used to find which my events group
	 */
	public static void removeStarredEvent(String identifier, int day) {
		List<Event> myEvents=MyApp.dayList.get(day).get(0);
		for (Event tempE : myEvents) {
			if (tempE.identifier.equalsIgnoreCase(identifier)) {
				myEvents.remove(tempE);
				break;
			}
		}

		myEvents=Summary.summaryList.get(day);
		for (Event tempE : myEvents) {
			if (tempE.identifier.equalsIgnoreCase(identifier)) {
				myEvents.remove(tempE);
				break;
			}
		}
	}

	public static class DayPagerAdapter extends FragmentPagerAdapter {
		public DayPagerAdapter(FragmentManager fragmentManager) {
			super(fragmentManager);
		}

		@Override
		public Fragment getItem(int arg0) {
			Fragment f=new ScheduleFragment();
			Bundle args=new Bundle();
			args.putInt("current_day", arg0);
			f.setArguments(args);
			return f;
		}

		@Override
		public int getCount() {
			return MyApp.dayList.size();
		}
	}
}
