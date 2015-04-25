package org.boardgamers.wbc;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
  public static ArrayList<ArrayList<Event>> dayList;
  public static String[] dayStrings;

  public static String allChanges;

  private static ViewPager viewPager;
  private static TabsPagerAdapter pagerAdapter;
  private ActionBar actionBar;

  // Tab titles
  private String[] tabs={"Starred", "Full Schedule", "My Data"};

  private static String dialogText;
  private static String dialogTitle;

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
    actionBar.setHomeButtonEnabled(false);
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
        }
      }

      @Override
      public void onPageScrolled(int arg0, float arg1, int arg2) {
      }

      @Override
      public void onPageScrollStateChanged(int arg0) {
      }
    });

    // enable home button for navigation drawer
    final ActionBar ab=getActionBar();
    if (ab!=null) {
      ab.setDisplayHomeAsUpEnabled(true);
      ab.setHomeButtonEnabled(true);
    } else {
      Log.d(TAG, "Could not get action bar");
    }

    // loaded, show dialogs
    SharedPreferences sp=
        getSharedPreferences(getResources().getString(R.string.sp_file_name), Context.MODE_PRIVATE);
    int version=sp.getInt("last_app_version", 0);

    // alert to notify email for questions
    if (version<12) {
      AlertDialog.Builder questionAlertBuilder=new AlertDialog.Builder(this);
      questionAlertBuilder
          .setMessage("Welcome to "+getResources().getString(R.string.app_name_long)+
              " for Android! Please send all questions, comments, requests, etc. to Kevin Broh-Kahn "+
              "at "+getResources().getString(R.string.email)+
              " (link in \"About\" page via main menu)").setTitle("Welcome!");
      questionAlertBuilder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
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
    } catch (PackageManager.NameNotFoundException e) {
      Toast.makeText(this,
          "ERROR: Could not find version code,"+"contact "+getResources().getString(R.string.email)+
              " for help.", Toast.LENGTH_LONG).show();
      versionCode=-1;
      e.printStackTrace();
    }

    SharedPreferences.Editor editor=sp.edit();
    editor.putInt("last_app_version", versionCode);
    editor.apply();

    // check for changes
    dialogText=allChanges;
    dialogTitle="Schedule Changes";
    if (!dialogText.equalsIgnoreCase("")) {
      DialogText dc=new DialogText();
      dc.show(getFragmentManager(), "changes_dialog");
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
    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    // If the nav drawer is open, hide action items related to the content view

    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    if (item.getItemId()==R.id.menu_search) {

    } else {
      return super.onOptionsItemSelected(item);
    }

    // TODO
    //    Intent intent;
    //Intent intent=new Intent(this, SearchActivity.class);
    //startActivity(intent);
    //    switch (position) {
    //      case HOTEL_MAP_ACTIVITY_INDEX:
    //        Log.d(TAG, String.valueOf(SELECTED_ROOM));
    //        intent=new Intent(activity, MapActivity.class);
    //        break;
    //      case HELP_ACTIVITY_INDEX:
    //        intent=new Intent(activity, HelpActivity.class);
    //        break;
    //      case ABOUT_ACTIVITY_INDEX:
    //        intent=new Intent(activity, AboutActivity.class);
    //        break;
    //      case FILTER_ACTIVITY_INDEX:
    //        intent=new Intent(activity, FilterActivity.class);
    //        break;
    //      case SETTINGS_ACTIVITY_INDEX:
    //        intent=new Intent(activity, SettingsActivity.class);
    //        break;
    //      default:
    //        return;
    //    }
    //    startActivity(intent);

    return true;
  }

  /**
   * Dialog Text
   */
  public static class DialogText extends DialogFragment {
    //private final String TAG="Changes Dialog";

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

      TextView textView=(TextView) view.findViewById(R.id.dt_text);
      textView.setText(dialogText);

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
