package org.boardgamers.wbc;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.SearchManager;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Main Activity class
 */
public class MainActivity extends FragmentActivity
    implements ActionBar.TabListener, SearchView.OnQueryTextListener {
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
  public static ArrayList<ArrayList<Event>> dayList;
  public static String[] dayStrings;

  public static String allChanges;

  private static ViewPager viewPager;
  private static TabsPagerAdapter pagerAdapter;
  private ActionBar actionBar;

  // Tab titles
  private String[] tabs={"Starred", "Full Schedule", "My Data"};

  public static Activity activity;

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

    activity=this;

    viewPager=(ViewPager) findViewById(R.id.pager);
    actionBar=getActionBar();
    pagerAdapter=new TabsPagerAdapter(getFragmentManager());

    viewPager.setAdapter(pagerAdapter);
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

    for (String tabString : tabs) {
      actionBar.addTab(actionBar.newTab().setText(tabString).setTabListener(this));
    }

    /**
     * on swiping the viewpager make respective tab selected
     * */
    viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

      @Override
      public void onPageSelected(int position) {
        // on changing the page
        // make respected tab selected
        actionBar.setSelectedNavigationItem(position);

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

    handleIntent(getIntent());
  }

  @Override
  protected void onNewIntent(Intent intent) {
    handleIntent(intent);
  }

  private void handleIntent(Intent intent) {
    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
      String query=intent.getStringExtra(SearchManager.QUERY);
      Log.d(TAG, "Text input: "+query);
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

  @Override
  public boolean onQueryTextSubmit(String query) {
    return false;
  }

  @Override
  public boolean onQueryTextChange(String newText) {
    Log.d(TAG, "TEST:"+newText);
    return true;
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

    for (ArrayList<Event> events : dayList) {
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
    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
    searchView.setOnQueryTextListener(this);

    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    return super.onPrepareOptionsMenu(menu);
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

  //
  //  /**
  //   * Game star button clicked - change allStarred boolean and update events
  //   */
  //  public static void changeAllStarred(Activity a) {
  //    allStarred=!allStarred;
  //    setGameStar();
  //
  //    for (Event event : tournamentEvents) {
  //      if (event.starred^allStarred) {
  //        changeEventStar(event.identifier, allStarred, a);
  //      }
  //    }
  //  }
  //
  //  /**
  //   * set tournamentEventsStarIV image resource
  //   */
  //  public static void setGameStar() {
  //    tournamentEventsStarIV.setImageResource(allStarred ? R.drawable.star_on : R.drawable.star_off);
  //  }
  //
  //  /**
  //   * Event star changed - check for change in allStarred boolean and set game star image view.
  //   * Call setGameStar before return
  //   */
  //  public static void setAllStared() {
  //    allStarred=true;
  //    for (Event tEvent : tournamentEvents) {
  //      if (!tEvent.starred) {
  //        allStarred=false;
  //        return;
  //      }
  //    }
  //    setGameStar();
  //  }
}
