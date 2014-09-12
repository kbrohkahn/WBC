package org.boardgamers.wbc;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ScheduleActivity extends FragmentActivity implements
    ActionBar.OnNavigationListener {
	// private static String TAG="Schedule Activity";

	private static String dialogText;
	private static String dialogTitle;

	public static ArrayList<ArrayList<EventGroup>> dayList;
	private DayPagerAdapter pageAdapter;
	private ViewPager viewPager;

	private boolean filterOpened;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		// load action bar
		final ActionBar ab=getActionBar();
		ab.setDisplayShowTitleEnabled(false);
		ab.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		// setup page adapter and view pager for action bar
		pageAdapter=new DayPagerAdapter(getFragmentManager());
		viewPager=(ViewPager) findViewById(R.id.pager);
		viewPager.setAdapter(pageAdapter);
		// viewPager.setOffscreenPageLimit(1);
		viewPager
		    .setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			    @Override
			    public void onPageSelected(int position) {
				    ab.setSelectedNavigationItem(position);
			    }
		    });

		SpinnerAdapter mSpinnerAdapter=ArrayAdapter.createFromResource(this,
		    R.array.days, android.R.layout.simple_spinner_dropdown_item);

		ab.setListNavigationCallbacks(mSpinnerAdapter, this);

		// set viewpager to current day
		MyApp.updateTime(getResources());
		if (MyApp.day>-1)
			viewPager.setCurrentItem(MyApp.day);

		SharedPreferences sp=getSharedPreferences(
		    getResources().getString(R.string.sp_file_name),
		    Context.MODE_PRIVATE);

		int version=sp.getInt("last_app_version", 0);

		// alert to notify of broken ids
		if (version<11) {
			AlertDialog.Builder eventIdAlertBuilder=new AlertDialog.Builder(
			    this);
			eventIdAlertBuilder
			    .setMessage(
			        "Due to schedule changes and source fixes, "
			            +"we had to change the way starred events are saved. "
			            +"All event stars and notes will be saved differently, "
			            +"and data from previous app versions is now obsolete. "
			            +"We are sorry for the inconvenience, "
			            +"but this was necessary to ensure that all events "
			            +"will have unique identifiers throughout schedule "
			            +"variations in the future.").setTitle(
			        "Notice");
			eventIdAlertBuilder.setNeutralButton("OK",
			    new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
					    dialog.dismiss();
				    }
			    });
			eventIdAlertBuilder.create().show();
		}

		// alert to notify dev@boardgamers.org for questions
		if (version<12) {
			AlertDialog.Builder questionAlertBuilder=new AlertDialog.Builder(
			    this);
			questionAlertBuilder
			    .setMessage(
			        "Please send all questions, comments, requests, etc. to Kevin Broh-Kahn at dev@boardgamers.org (link in \"About\" page via main menu)")
			    .setTitle("Questions?");
			questionAlertBuilder.setNeutralButton("OK",
			    new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
					    dialog.dismiss();
				    }
			    });
			questionAlertBuilder.create().show();
		}

		// save current version code
		int versionCode;
		try {
			versionCode=getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			Toast.makeText(
			    this,
			    "ERROR: Could not find version code,"
			        +"contact dev@boardgamers.org for help.",
			    Toast.LENGTH_LONG).show();
			versionCode=-1;
			e.printStackTrace();
		}

		SharedPreferences.Editor editor=sp.edit();
		editor.putInt("last_app_version", versionCode);
		editor.commit();

		dialogText=getIntent().getStringExtra("changes");
		dialogTitle="Schedule Changes";
		if (!dialogText.equalsIgnoreCase("")) {
			DialogText dc=new DialogText();
			dc.show(getFragmentManager(), "changes_dialog");
		}
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		viewPager.setCurrentItem(itemPosition);
		return false;
	}

	@Override
	protected void onResume() {
		if (filterOpened) {
			filterOpened=false;
			pageAdapter.notifyDataSetChanged();
		}
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
				List<Event> events=dayList.get(d).get(i).events;
				Event event;
				for (int j=0; j<events.size(); j++) {
					event=events.get(j);
					editor.putBoolean(starPrefString+event.identifier, event.starred);
				}
			}
		}
		editor.commit();

		super.onPause();
	}

	/**
	 * Add starred event to "My Events" group in list
	 * 
	 * @param event
	 */
	public static void addStarredEvent(Event event) {
		List<Event> myEvents=dayList.get(event.day).get(0).events;

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
		ScheduleActivity.dayList.get(event.day).get(0).events.add(index,
		    starredEvent);

	}

	/**
	 * Remove starred event from "My Events" group in list
	 * 
	 * @param id
	 *          - event id
	 * @param day
	 *          - event's day, used to find which my events group
	 */
	public static void removeStarredEvent(String identifier, int day) {
		List<Event> myEvents=dayList.get(day).get(0).events;
		for (Event tempE : myEvents) {
			if (tempE.identifier.equalsIgnoreCase(identifier)) {
				ScheduleActivity.dayList.get(day).get(0).events.remove(tempE);
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
			return dayList.size();
		}
	}

	public static class DialogText extends DialogFragment {
		final String TAG="Changes Dialog";

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			Dialog dialog=super.onCreateDialog(savedInstanceState);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			return dialog;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
		    Bundle savedInstanceState) {
			View view=inflater.inflate(R.layout.dialog_text, container, false);

			TextView title=(TextView) view.findViewById(R.id.dt_title);
			title.setText(dialogTitle);

			LinearLayout layout=(LinearLayout) view
			    .findViewById(R.id.dt_layout);
			layout.removeAllViews();

			final Context context=getActivity();
			final Resources resources=getResources();
			int padding=(int) resources.getDimension(R.dimen.text_margin_small);

			TextView textView=new TextView(context);
			textView.setText(dialogText);
			textView.setTextAppearance(context, R.style.medium_text);
			textView.setGravity(Gravity.CENTER);
			textView.setPadding(padding, padding, padding, padding);

			layout.addView(textView);

			Button close=(Button) view.findViewById(R.id.dt_close);
			close.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					dismiss();
				}
			});

			return view;

		}
	}

	public static class DialogNotes extends DialogFragment {
		final String TAG="Notes Dialog";

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			Dialog dialog=super.onCreateDialog(savedInstanceState);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			return dialog;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
		    Bundle savedInstanceState) {
			View view=inflater.inflate(R.layout.dialog_text, container, false);

			TextView title=(TextView) view.findViewById(R.id.dt_title);
			title.setText("Event notes");

			LinearLayout layout=(LinearLayout) view
			    .findViewById(R.id.dt_layout);
			layout.removeAllViews();

			final Resources resources=getResources();
			final Context context=getActivity();
			int padding=(int) resources.getDimension(R.dimen.text_margin_small);

			SharedPreferences sp=context.getSharedPreferences(
			    resources.getString(R.string.sp_file_name),
			    Context.MODE_PRIVATE);
			String notePrefString=resources.getString(R.string.sp_event_note);

			TextView textView;

			String noteString;

			List<Event> events;
			for (int i=0; i<dayList.size(); i++) {
				for (int j=1; j<dayList.get(i).size(); j++) {
					events=dayList.get(i).get(j).events;
					for (Event event : events) {
						noteString=sp
						    .getString(
						        notePrefString
						            +String.valueOf(event.identifier),
						        "");
						if (noteString.length()>0) {

							textView=new TextView(context);
							textView.setText(event.title+": "+noteString);
							textView.setTextAppearance(context,
							    R.style.medium_text);
							textView.setGravity(Gravity.LEFT);
							textView.setPadding(padding, padding, padding,
							    padding);

							layout.addView(textView);
						}
					}
				}
			}

			if (layout.getChildCount()==0) {
				textView=new TextView(context);
				textView
				    .setText("Add event notes from an event screen, which can be accessed by selecting an event from a tournament screen.");
				textView.setTextAppearance(context, R.style.medium_text);
				textView.setGravity(Gravity.CENTER);
				textView.setPadding(padding, padding, padding, padding);

				layout.addView(textView);
			}

			Button close=(Button) view.findViewById(R.id.dt_close);
			close.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					dismiss();
				}
			});

			return view;

		}
	}

	public static class DialogFinish extends DialogFragment {
		final String TAG="Filter Dialog";

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			Dialog dialog=super.onCreateDialog(savedInstanceState);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			return dialog;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
		    Bundle savedInstanceState) {
			View view=inflater.inflate(R.layout.dialog_text, container, false);

			TextView title=(TextView) view.findViewById(R.id.dt_title);
			title.setText("My finishes");

			LinearLayout layout=(LinearLayout) view
			    .findViewById(R.id.dt_layout);
			layout.removeAllViews();

			final Context context=getActivity();
			final Resources resources=getResources();
			int padding=(int) resources.getDimension(R.dimen.text_margin_small);

			TextView textView;

			String finishString;
			Tournament tournament;

			for (int i=0; i<MyApp.allTournaments.size(); i++) {
				tournament=MyApp.allTournaments.get(i);

				if (tournament.finish>0) {
					Log.d(TAG, String.valueOf(tournament.title)+" finish is "
					    +String.valueOf(tournament.finish));

					switch (tournament.finish) {
					case 1:
						finishString=resources.getString(R.string.first);
						break;
					case 2:
						finishString=resources.getString(R.string.second);
						break;
					case 3:
						finishString=resources.getString(R.string.third);
						break;
					case 4:
						finishString=resources.getString(R.string.fourth);
						break;
					case 5:
						finishString=resources.getString(R.string.fifth);
						break;
					case 6:
						finishString=resources.getString(R.string.sixth);
						break;
					default:
						finishString="No finish";
						break;
					}

					finishString+=" in "+tournament.title;

					textView=new TextView(context);
					textView.setText(finishString);
					textView.setTextAppearance(context, R.style.medium_text);
					textView.setGravity(Gravity.LEFT);
					textView.setPadding(padding, padding, padding, padding);

					if (tournament.finish<=tournament.prize)
						textView.setTypeface(null, Typeface.BOLD);
					else
						textView.setTypeface(null, Typeface.ITALIC);

					layout.addView(textView);
				}
			}

			if (layout.getChildCount()==0) {
				textView=new TextView(context);
				textView
				    .setText("Select tournament finish from a tournament screen.\n\n*note: final event for tournament must have started*\n\n");
				textView.setTextAppearance(context, R.style.medium_text);
				textView.setGravity(Gravity.CENTER);
				textView.setPadding(padding, padding, padding, padding);

				layout.addView(textView);
			}

			Button close=(Button) view.findViewById(R.id.dt_close);
			close.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					dismiss();

				}
			});

			return view;

		}
	}

	/*************************** MENU ***************************/

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater=getMenuInflater();
		inflater.inflate(R.menu.main, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.create_event:
			MyApp.SELECTED_GAME_ID=0;
			MyApp.SELECTED_EVENT_ID="";

			Intent intent=new Intent(this, TournamentActivity.class);
			startActivity(intent);
			return true;
		case R.id.search:
			new DialogSearch().show(getFragmentManager(), "search_dialog");
			return true;
		case R.id.settings:
			startActivity(new Intent(this, Settings.class));
			return true;
		case R.id.filter:
			filterOpened=true;
			startActivity(new Intent(this, Filter.class));
			return true;
		case R.id.finish:
			new DialogFinish().show(getFragmentManager(), "finish_dialog");
			return true;
		case R.id.note:
			new DialogNotes().show(getFragmentManager(), "notes_dialog");
			return true;
		case R.id.help:
			Intent helpIntent=new Intent(this, Help.class);
			startActivity(helpIntent);
			return true;
		case R.id.about:
			Intent aboutIntent=new Intent(this, About.class);
			startActivity(aboutIntent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
