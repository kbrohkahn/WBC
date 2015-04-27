package org.boardgamers.wbc;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Main Activity class
 */
public class MainActivity extends FragmentActivity implements ActionBar.TabListener {
  private final static String TAG="Main Activity";

  public static final int SUMMARY_FRAGMENT_POSITION=0;
  public static final int SCHEDULE_FRAGMENT_POSITION=1;
  public static final int USER_DATA_FRAGMENT_POSITION=2;

  public static final int HOURS_PER_DAY=18;
  public static final int GROUPS_PER_DAY=HOURS_PER_DAY+1;
  public static final int TOTAL_DAYS=9;

  public static String SELECTED_EVENT_ID="";
  public static String SELECTED_ROOM;
  public static int NUM_EVENTS;

  public static int currentDay;
  public static int currentHour;

  public static List<Tournament> allTournaments;
  public static List<List<Event>> dayList;
  public static String[] dayStrings;

  private static ViewPager viewPager;
  private static TabsPagerAdapter pagerAdapter;

  // Tab titles
  private String[] tabs={"Starred", "Schedule", "My Data"};

  /**
   * Add starred help to "My Events" group in list
   *
   * @param event - Event that was starred
   */
  public static void addStarredEvent(Event event) {
    Event starredEvent=
        new Event(event.identifier, event.tournamentID, event.day, event.hour, event.title,
            event.eClass, event.format, event.qualify, event.duration, event.continuous,
            event.totalDuration, event.location);
    starredEvent.starred=true;

    // add event to day list(time, then title)
    List<Event> events=dayList.get(starredEvent.day*GROUPS_PER_DAY);
    Event tempEvent;
    int index;
    for (index=0; index<events.size(); index++) {
      tempEvent=events.get(index);
      if (starredEvent.hour<tempEvent.hour || (starredEvent.hour==tempEvent.hour &&
          starredEvent.title.compareToIgnoreCase(tempEvent.title)==1)) {
        break;
      }
    }

    events.add(index, starredEvent);
  }

  /**
   * Remove starred help from "My Events" group in list
   *
   * @param identifier - help id
   * @param day        - help's currentDay, used to find which my events group
   */
  public static void removeStarredEvent(String identifier, int day) {
    List<Event> myEvents=dayList.get(day*GROUPS_PER_DAY);
    for (Event tempE : myEvents) {
      if (tempE.identifier.equalsIgnoreCase(identifier)) {
        myEvents.remove(tempE);
        break;
      }
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.main_layout);

    viewPager=(ViewPager) findViewById(R.id.pager);
    pagerAdapter=new TabsPagerAdapter(getFragmentManager());
    viewPager.setAdapter(pagerAdapter);

    final ActionBar actionBar=getActionBar();
    if (actionBar!=null) {
      actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
      for (String tabString : tabs) {
        actionBar.addTab(actionBar.newTab().setText(tabString).setTabListener(this));
      }
    } else {
      Log.d(TAG, "Error: could not get action bar");
    }

    viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

      @Override
      public void onPageSelected(int position) {
        if (actionBar!=null) {
          actionBar.setSelectedNavigationItem(position);
        } else {
          Log.d(TAG, "Error: could not get action bar");
        }
        updateFragment(position);
      }

      @Override
      public void onPageScrolled(int arg0, float arg1, int arg2) {
      }

      @Override
      public void onPageScrollStateChanged(int arg0) {
      }
    });

    int latestDialogVersion=13;

    SharedPreferences sp=
        getSharedPreferences(getResources().getString(R.string.sp_file_name), Context.MODE_PRIVATE);
    int version=sp.getInt("last_app_version", 0);

    // alert to notify email for questions
    if (version<latestDialogVersion) {
      AlertDialog.Builder initialBuilder=new AlertDialog.Builder(this);
      initialBuilder.setTitle(R.string.initial_dialog_title)
          .setMessage(R.string.initial_dialog_text)
          .setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();
            }
          });
      initialBuilder.create().show();
    }

    // save current version code
    int versionCode;
    try {
      versionCode=getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
    } catch (PackageManager.NameNotFoundException e) {
      Log.d(TAG,
          "ERROR: Could not find version code,"+"contact "+getResources().getString(R.string.email)+
              " for help.");
      versionCode=latestDialogVersion;
      e.printStackTrace();
    }

    SharedPreferences.Editor editor=sp.edit();
    editor.putInt("last_app_version", versionCode);
    editor.apply();

    // check for changes
    String allChanges=getIntent().getStringExtra("allChanges");
    if (allChanges!=null && !allChanges.equalsIgnoreCase("")) {
      AlertDialog.Builder changesBuilder=new AlertDialog.Builder(this);
      changesBuilder.setTitle(R.string.changes_dialog_title).setMessage(allChanges)
          .setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              dialog.dismiss();
            }
          });

      changesBuilder.create().show();
    }

  }

  public void updateFragment(int position) {
    switch (position) {
      case SUMMARY_FRAGMENT_POSITION:
        SummaryFragment summaryFragment=
            (SummaryFragment) pagerAdapter.getItem(SUMMARY_FRAGMENT_POSITION);
        if (summaryFragment!=null) {
          summaryFragment.updateList();
        }
        break;

      case SCHEDULE_FRAGMENT_POSITION:
        ScheduleFragment scheduleFragment=
            (ScheduleFragment) pagerAdapter.getItem(SCHEDULE_FRAGMENT_POSITION);
        if (scheduleFragment!=null) {
          scheduleFragment.updateList();
        }
        break;

      case USER_DATA_FRAGMENT_POSITION:
        UserDataFragment userDataFragment=
            (UserDataFragment) pagerAdapter.getItem(USER_DATA_FRAGMENT_POSITION);
        if (userDataFragment!=null) {
          userDataFragment.updateList();
        }
        break;

      default:
        updateFragment(viewPager.getCurrentItem());
        break;
    }
  }

  @Override
  protected void onResume() {
    setCurrentTime(getResources().getStringArray(R.array.daysForParsing));
    super.onResume();
  }

  @Override
  public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
  }

  @Override
  public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
    viewPager.setCurrentItem(tab.getPosition());
  }

  @Override
  public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
  }

  /**
   * Sets apps currentDay and currentHour variables
   * Static allows for use in notification service
   */
  public static void setCurrentTime(String[] daysForParsing) {
    Calendar calendar=Calendar.getInstance();

    // get currentHour
    currentHour=calendar.get(Calendar.HOUR_OF_DAY);

    SimpleDateFormat dateFormatter=new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    String dateString=dateFormatter.format(calendar.getTime());

    // get currentDay
    currentDay=-1;
    for (int i=0; i<daysForParsing.length; i++) {
      if (daysForParsing[i].equalsIgnoreCase(dateString)) {
        currentDay=i;
        break;
      }
    }

    // TODO comment before release
    // set currentDay to day of week for testing
    currentDay=(calendar.get(Calendar.DAY_OF_WEEK)-2)%7;

  }

  @Override
  public void onPause() {
    // save starred states for all events
    SharedPreferences.Editor editor=
        getSharedPreferences(getResources().getString(R.string.sp_file_name), Context.MODE_PRIVATE)
            .edit();
    String starPrefString=getResources().getString(R.string.sp_event_starred);

    for (List<Event> events : dayList) {
      for (Event event : events) {
        editor.putBoolean(starPrefString+event.identifier, event.starred);
      }
    }
    editor.apply();

    super.onPause();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater=getMenuInflater();
    inflater.inflate(R.menu.main, menu);

    SearchManager searchManager=(SearchManager) getSystemService(Context.SEARCH_SERVICE);
    SearchView searchView=(SearchView) menu.findItem(R.id.menu_search).getActionView();
    searchView.setSearchableInfo(
        searchManager.getSearchableInfo(new ComponentName(this, SearchResultActivity.class)));

    //searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
    searchView.setSubmitButtonEnabled(true);

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    if (item.getItemId()==R.id.menu_map) {
      startActivity(new Intent(this, MapActivity.class));
    } else if (item.getItemId()==R.id.menu_help) {
      startActivity(new Intent(this, HelpActivity.class));
    } else if (item.getItemId()==R.id.menu_about) {
      startActivity(new Intent(this, AboutActivity.class));
    } else if (item.getItemId()==R.id.menu_filter) {
      startActivity(new Intent(this, FilterActivity.class));
    } else if (item.getItemId()==R.id.menu_settings) {
      startActivity(new Intent(this, SettingsActivity.class));
    } else {
      return super.onOptionsItemSelected(item);
    }

    return true;
  }

}
