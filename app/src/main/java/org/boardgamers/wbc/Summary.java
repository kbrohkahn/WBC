package org.boardgamers.wbc;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class Summary extends FragmentActivity {
	final static String TAG="Summary Activity";

  private String[] mPlanetTitles;
  private DrawerLayout mDrawerLayout;
  private ActionBarDrawerToggle mDrawerToggle;
  private ListView mDrawerList;
  private CharSequence mDrawerTitle;
  private CharSequence mTitle;

  private static String dialogText;
	private static String dialogTitle;

	private static SummaryListAdapter listAdapter;
	private ExpandableListView listView;
	public static ArrayList<ArrayList<Event>> summaryList;

	private String[] dayStrings;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.summary);

    // get resources
    MyApp.COLOR_JUNIOR = getResources().getColor(R.color.junior);
    MyApp.COLOR_SEMINAR = getResources().getColor(R.color.seminar);
    MyApp.COLOR_QUALIFY = getResources().getColor(R.color.qualify);
    MyApp.COLOR_NON_TOURNAMENT = getResources().getColor(R.color.non_tournament);
    MyApp.COLOR_OPEN_TOURNAMENT = getResources().getColor(R.color.open_tournament);

    dayStrings=getResources().getStringArray(R.array.days);

    // load drawer
    mPlanetTitles = getResources().getStringArray(R.array.drawer_titles);
    mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    mDrawerList = (ListView) findViewById(R.id.left_drawer);
    mTitle = mDrawerTitle = getTitle();

    // Set the adapter for the list view
    mDrawerList.setAdapter(new ArrayAdapter<String>(this,
        R.layout.drawer_list_item, mPlanetTitles));
    // Set the list's click listener
    mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

    mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
        R.drawable.selected, R.string.open_drawer, R.string.close_drawer) {

      /** Called when a drawer has settled in a completely closed state. */
      public void onDrawerClosed(View view) {
        super.onDrawerClosed(view);
        getActionBar().setTitle(mTitle);
        invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
      }

      /** Called when a drawer has settled in a completely open state. */
      public void onDrawerOpened(View drawerView) {
        super.onDrawerOpened(drawerView);
        getActionBar().setTitle(mDrawerTitle);
        invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
      }
    };

    // Set the drawer toggle as the DrawerListener
    mDrawerLayout.setDrawerListener(mDrawerToggle);


    getActionBar().setDisplayHomeAsUpEnabled(true);
    getActionBar().setHomeButtonEnabled(true);


    // initiate list
    summaryList=new ArrayList<ArrayList<Event>>(dayStrings.length);
    for (int i=0; i<dayStrings.length; i++) {
      summaryList.add(new ArrayList<Event>());
    }

    // initiate main day list
    ArrayList<ArrayList<Event>> temp;
    MyApp.dayList=new ArrayList<ArrayList<ArrayList<Event>>>(dayStrings.length);
    for (int i=0; i<dayStrings.length; i++) {
      temp=new ArrayList<ArrayList<Event>>();
      for (int j=0; j<19; j++) {
        temp.add(new ArrayList<Event>());
      }
      MyApp.dayList.add(temp);
    }

    // setup list adapter and list view
		listAdapter=new SummaryListAdapter();

		listView=(ExpandableListView) findViewById(R.id.summary_list_view);
		listView.setAdapter(listAdapter);
		listView.setDividerHeight(0);
    for (int i=0; i<listAdapter.getGroupCount(); i++)
      listView.expandGroup(i);


    SharedPreferences sp=getSharedPreferences(
		    getResources().getString(R.string.sp_file_name),
		    Context.MODE_PRIVATE);
		int version=sp.getInt("last_app_version", 0);

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
		editor.apply();

    dialogText = "";
		// TODO dialogText=getIntent().getStringExtra("changes");
		dialogTitle="Schedule Changes";
		if (!dialogText.equalsIgnoreCase("")) {
			DialogText dc=new DialogText();
			dc.show(getFragmentManager(), "changes_dialog");
		}

    new LoadEventsTask(this).execute(null, null, null);
  }


  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    // Sync the toggle state after onRestoreInstanceState has occurred.
    mDrawerToggle.syncState();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    mDrawerToggle.onConfigurationChanged(newConfig);
  }


  @Override
	public void onResume() {
    updateList();
    MyApp.updateTime(getResources());
    if (MyApp.day > -1)
      listView.setSelectedGroup(MyApp.day);

    super.onResume();
	}

  public static void updateList() {
    summaryList=new ArrayList<ArrayList<Event>>(MyApp.dayList.size());
    for (int i=0; i<MyApp.dayList.size(); i++) {
      summaryList.add(new ArrayList<Event>());
      for (Event event: MyApp.dayList.get(i).get(0)) {
        summaryList.get(i).add(event);
      }
    }
    listAdapter.notifyDataSetChanged();
  }

	class SummaryListAdapter extends BaseExpandableListAdapter {
		private final LayoutInflater inflater;

		public SummaryListAdapter() {
			inflater=(LayoutInflater)
			    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return summaryList.get(groupPosition).get(childPosition);
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
		    boolean isLastChild, View view, ViewGroup parent) {
			final Event event=(Event) getChild(groupPosition, childPosition);

			int tColor=MyApp.getTextColor(event);
			int tType=MyApp.getTextStyle(event);

			view=inflater.inflate(R.layout.schedule_item, parent, false);

			TextView title=(TextView) view.findViewById(R.id.si_name);
			if (groupPosition==0)
				title.setText(String.valueOf(event.hour)+" - "+event.title);
			else
				title.setText(event.title);
			title.setTypeface(null, tType);
			title.setTextColor(tColor);

			if (event.title.contains("Junior")) {
				title.setCompoundDrawablesWithIntrinsicBounds(
				    R.drawable.junior_icon, 0, 0, 0);
			}

			TextView duration=(TextView) view.findViewById(R.id.si_duration);
			duration.setText(String.valueOf(event.duration));
			duration.setTypeface(null, tType);
			duration.setTextColor(tColor);

			if (event.continuous) {
				duration.setCompoundDrawablesWithIntrinsicBounds(0, 0,
				    R.drawable.continuous_icon, 0);
			}

			TextView location=(TextView) view.findViewById(R.id.si_location);
			location.setTypeface(null, tType);
			location.setTextColor(tColor);

			SpannableString locationText=new SpannableString(event.location);
			locationText.setSpan(new UnderlineSpan(), 0, locationText.length(),
			    0);
			location.setText(locationText);
			location.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showMapDialog(event.location);
				}
			});

			ImageView starIV=(ImageView) view.findViewById(R.id.si_star);
			starIV.setImageResource(event.starred ? R.drawable.star_on
			    : R.drawable.star_off);
			starIV.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					removeEventStar(event);
				}
			});

			boolean started=event.day*24+event.hour<=MyApp.day*24+MyApp.hour;
			boolean ended=event.day*24+event.hour+event.totalDuration<=MyApp.day
			    *24+MyApp.hour;
			boolean happening=started&&!ended;

			if (childPosition%2==0) {
				if (ended)
					view.setBackgroundResource(R.drawable.ended_light);
				else if (happening)
					view.setBackgroundResource(R.drawable.current_light);
				else
					view.setBackgroundResource(R.drawable.future_light);
			} else {
				if (ended)
					view.setBackgroundResource(R.drawable.ended_dark);
				else if (happening)
					view.setBackgroundResource(R.drawable.current_dark);
				else
					view.setBackgroundResource(R.drawable.future_dark);
			}

			final int tID=event.tournamentID;
			final String eID=event.identifier;
			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					selectGame(tID, eID);
				}
			});

			return view;
		}

		@Override
		public Object getGroup(int groupPosition) {
			return summaryList.get(groupPosition).get(groupPosition);
		}

		@Override
		public View getGroupView(final int groupPosition,
		    final boolean isExpanded, View view, ViewGroup parent) {
			if (view==null)
				view=inflater.inflate(R.layout.summary_day_label, parent, false);

			String groupTitle=dayStrings[groupPosition];
			TextView name=(TextView) view.findViewById(R.id.summary_day_text);
			name.setText(groupTitle);

      if (isExpanded)
        view.setBackgroundResource(R.drawable.group_expanded);
      else
        view.setBackgroundResource(R.drawable.group_collapsed);

			return view;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return summaryList.get(groupPosition).size();
		}

		@Override
		public int getGroupCount() {
			return summaryList.size();
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return false;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}
	}

	public void showMapDialog(String room) {
		Intent intent=new Intent(this, Map.class);
		intent.putExtra("room", room);
		startActivity(intent);
	}

  public void selectGame(int gID, String eID) {
    MyApp.SELECTED_GAME_ID=gID;
    MyApp.SELECTED_EVENT_ID=eID;

    Intent intent=new Intent(this, TournamentActivity.class);
    startActivity(intent);
  }

	public void removeEventStar(Event event) {
		// remove from summary list
		List<Event> searchList=summaryList.get(event.day);
		for (int i=0; i<searchList.size(); i++) {
			if (searchList.get(i).identifier.equalsIgnoreCase(event.identifier)) {
				searchList.remove(i);
			}
		}

		// remove from "My Events" in day list
		searchList=MyApp.dayList.get(event.day).get(0);
		for (Event tempE : searchList) {
			if (tempE.identifier.equalsIgnoreCase(event.identifier)) {
				MyApp.dayList.get(event.day).get(0).remove(tempE);
				break;
			}
		}

		// remove from day list
		searchList=MyApp.dayList.get(event.day).get(event.hour-6);
		for (Event tempE : searchList) {
			if (tempE.identifier.equalsIgnoreCase(event.identifier)) {
				tempE.starred=false;
				break;
			}
		}
		listAdapter.notifyDataSetChanged();
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
			for (int i=0; i<MyApp.dayList.size(); i++) {
				for (int j=1; j<MyApp.dayList.get(i).size(); j++) {
					events=MyApp.dayList.get(i).get(j);
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
							textView.setGravity(Gravity.START);
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
					textView.setGravity(Gravity.START);
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

	/**
	 * ************************ MENU **************************
	 */


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
    // Pass the event to ActionBarDrawerToggle, if it returns
    // true, then it has handled the app icon touch event
    if (mDrawerToggle.onOptionsItemSelected(item)) {
      return true;
    }
    // Handle your other action bar items...

		return super.onOptionsItemSelected(item);
	}


  /* Called whenever we call invalidateOptionsMenu() */
  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    // If the nav drawer is open, hide action items related to the content view
    boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
    //menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
    return super.onPrepareOptionsMenu(menu);
  }

  private class DrawerItemClickListener implements ListView.OnItemClickListener {
    @Override
    public void onItemClick(AdapterView parent, View view, int position, long id) {
      selectItem(position);
    }
  }

  /** Swaps fragments in the main content view */
  private void selectItem(int position) {
    switch (position) {
      case 0:
        startActivity(new Intent(this, ScheduleActivity.class));
        break;
      case 1:
        MyApp.SELECTED_GAME_ID=0;
        MyApp.SELECTED_EVENT_ID="";

        Intent intent=new Intent(this, TournamentActivity.class);
        startActivity(intent);
        break;
      case 2:
        new DialogSearch().show(getFragmentManager(), "search_dialog");
        break;
      case 3:
        startActivity(new Intent(this, Settings.class));
        break;
      case 4:
        startActivity(new Intent(this, Filter.class));
        break;
      case 5:
        new DialogFinish().show(getFragmentManager(), "finish_dialog");
        break;
      case 6:
        new DialogNotes().show(getFragmentManager(), "notes_dialog");
        break;
      case 7:
        Intent helpIntent=new Intent(this, Help.class);
        startActivity(helpIntent);
        break;
      case 8:
        Intent aboutIntent=new Intent(this, About.class);
        startActivity(aboutIntent);
        break;
    }
    // Highlight the selected item, update the mTitle, and close the drawer
    mDrawerList.setItemChecked(position, true);
    setTitle(mPlanetTitles[position]);
    mDrawerLayout.closeDrawer(mDrawerList);
  }

  @Override
  public void setTitle(CharSequence title) {
    mTitle = title;
    getActionBar().setTitle(mTitle);
  }


}
