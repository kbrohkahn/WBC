package org.boardgamers.wbc;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
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
public class MainActivity extends Activity {
  private final static String TAG="Main Activity";
  private final static String userFragmentTag="user";

  private final int SUMMARY_FRAGMENT_INDEX=0;
  private final int SCHEDULE_FRAGMENT_INDEX=1;
  private final int USER_DATA_FRAGMENT_INDEX=2;
  private final int CONVENTION_INFO_DIVIDER_INDEX=3;
  private final int HOTEL_MAP_ACTIVITY_INDEX=4;
  private final int HELP_ACTIVITY_INDEX=5;
  private final int ABOUT_ACTIVITY_INDEX=6;
  private final int SETTINGS_DIVIDER_INDEX=7;
  private final int FILTER_ACTIVITY_INDEX=8;
  private final int SETTINGS_ACTIVITY_INDEX=9;

  public static int SELECTED_GAME_ID;
  public static String SELECTED_EVENT_ID;
  public static String SELECTED_ROOM;
  public static int currentDay;
  public static int currentHour;
  public static int NUM_EVENTS;
  public static List<Tournament> allTournaments;
  public static ArrayList<ArrayList<ArrayList<Event>>> dayList;
  public static String[] dayStrings;
  public static String allChanges;
  private String drawerTitle;
  private static int COLOR_JUNIOR;
  private static int COLOR_SEMINAR;
  private static int COLOR_QUALIFY;
  private static int COLOR_OPEN_TOURNAMENT;
  private static int COLOR_NON_TOURNAMENT;
  protected static int currentFragment=-1;
  private static String dialogText;
  private static String dialogTitle;
  public static Activity activity;
  private final int drawerIconIDs[]=
      {R.drawable.ic_drawer_star, R.drawable.ic_drawer_view_as_list, R.drawable.ic_drawer_finish, 0,
          R.drawable.ic_drawer_map, R.drawable.ic_drawer_help, R.drawable.ic_drawer_about, 0,
          R.drawable.ic_drawer_filter, R.drawable.ic_drawer_settings};
  protected static String[] drawerTitles;
  protected static DrawerLayout drawerLayout;
  protected ActionBarDrawerToggle drawerToggle;
  protected static ListView drawerList;
  protected static String actionBarTitle;

  /**
   * Add starred help to "My Events" group in list
   *
   * @param event - Event that was starred
   */
  public static void addStarredEvent(Event event) {
    List<Event> myEvents=dayList.get(event.day).get(0);

    Event starredEvent=
        new Event(event.identifier, event.tournamentID, event.day, event.hour, event.title,
            event.eClass, event.format, event.qualify, event.duration, event.continuous,
            event.totalDuration, event.location);
    starredEvent.starred=true;

    // get position in starred list to add (time, then title)
    int index=0;
    for (Event eTemp : myEvents) {
      if (starredEvent.hour<eTemp.hour || (starredEvent.hour==eTemp.hour &&
          starredEvent.title.compareToIgnoreCase(eTemp.title)==1)) {
        break;
      } else {
        index++;
      }
    }

    dayList.get(event.day).get(0).add(index, starredEvent);

  }

  /**
   * Remove starred help from "My Events" group in list
   *
   * @param identifier - help id
   * @param day        - help's currentDay, used to find which my events group
   */
  public static void removeStarredEvent(String identifier, int day) {
    List<Event> myEvents=dayList.get(day).get(0);
    for (Event tempE : myEvents) {
      if (tempE.identifier.equalsIgnoreCase(identifier)) {
        myEvents.remove(tempE);
        break;
      }
    }
  }

  /**
   * Open game fragment
   *
   * @param c   - context
   * @param gID - game ID
   * @param eID - help ID (needed for tablets where help also displayed)
   */
  public static void selectGame(Context c, int gID, String eID) {
    SELECTED_GAME_ID=gID;
    SELECTED_EVENT_ID=eID;

    if (SELECTED_GAME_ID>-1) {
      // start wbc tournament
      Intent intent=new Intent(c, TournamentActivity.class);
      c.startActivity(intent);
    } else {
      // start user tournament
      final int position=2;
      String tag=userFragmentTag;

      FragmentManager fragmentManager=activity.getFragmentManager();
      Fragment fragment=fragmentManager.findFragmentByTag(tag);

      actionBarTitle=drawerTitles[position];

      if (fragment==null) {
        fragment=new UserDataFragment();
      }

      // switch fragments
      fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, tag).commit();

      // highlight the selected item, update the title, and refresh the drawer
      drawerList.setItemChecked(position, true);
      currentFragment=position;
      activity.setTitle(actionBarTitle);
      activity.invalidateOptionsMenu();
    }
  }

  /**
   * @param room - string containing name of selected room
   */
  public static void openMap(String room) {
    SELECTED_ROOM=room;
    Intent intent=new Intent(activity, MapActivity.class);
    activity.startActivity(intent);
  }

  private void selectMenuItem(int position) {
    drawerLayout.closeDrawer(drawerList);
    if (position<CONVENTION_INFO_DIVIDER_INDEX) {
      FragmentManager fragmentManager=getFragmentManager();
      Fragment fragment;
      String tag;

      switch (position) {
        case SUMMARY_FRAGMENT_INDEX:
          actionBarTitle=drawerTitles[position];
          tag="summary";

          fragment=fragmentManager.findFragmentByTag(tag);
          if (fragment==null) {
            fragment=new SummaryFragment();
          }

          break;
        case SCHEDULE_FRAGMENT_INDEX:
          actionBarTitle="WBC-"+dayStrings[Math.max(currentDay, 0)];
          tag="schedule";

          fragment=fragmentManager.findFragmentByTag(tag);
          if (fragment==null) {
            fragment=new ScheduleContainer();
          }

          break;
        case USER_DATA_FRAGMENT_INDEX:
          actionBarTitle=drawerTitles[position];
          tag=userFragmentTag;

          fragment=fragmentManager.findFragmentByTag(tag);
          if (fragment==null) {
            fragment=new UserDataFragment();
          }

          break;
        default:
          return;
      }

      // switch fragments
      fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, tag).commit();

      // highlight the selected item, update the title, and refresh the drawer
      drawerList.setItemChecked(position, true);
      currentFragment=position;
      setTitle(actionBarTitle);
      invalidateOptionsMenu();
    } else if (position>CONVENTION_INFO_DIVIDER_INDEX) {
      Intent intent;

      switch (position) {
        case HOTEL_MAP_ACTIVITY_INDEX:
          Log.d(TAG, String.valueOf(SELECTED_ROOM));
          intent=new Intent(activity, MapActivity.class);
          break;
        case HELP_ACTIVITY_INDEX:
          intent=new Intent(activity, HelpActivity.class);
          break;
        case ABOUT_ACTIVITY_INDEX:
          intent=new Intent(activity, AboutActivity.class);
          break;
        case FILTER_ACTIVITY_INDEX:
          intent=new Intent(activity, FilterActivity.class);
          break;
        case SETTINGS_ACTIVITY_INDEX:
          intent=new Intent(activity, SettingsActivity.class);
          break;
        default:
          return;
      }
      startActivity(intent);
    }
  }

  /**
   * @param event - help needed for format, title, class, and qualify
   * @return integer value of color
   */
  public static int getTextColor(Event event) {
    if (event.qualify) {
      return COLOR_QUALIFY;
    } else if (event.title.contains("Junior")) {
      return COLOR_JUNIOR;
    } else if (event.format.equalsIgnoreCase("Seminar")) {
      return COLOR_SEMINAR;
    } else if (event.format.equalsIgnoreCase("SOG") || event.format.equalsIgnoreCase("MP Game") ||
        event.title.indexOf("Open Gaming")==0) {
      return COLOR_OPEN_TOURNAMENT;
    } else if (event.eClass.length()==0) {
      return COLOR_NON_TOURNAMENT;
    } else {
      return Color.BLACK;
    }
  }

  /**
   * @param event - help needed for format, title, class, and qualify
   * @return integer value of typeface
   */
  public static int getTextStyle(Event event) {
    if (event.qualify) {
      return Typeface.BOLD;
    } else if (event.title.contains("Junior")) {
      return Typeface.NORMAL;
    } else if (event.eClass.length()==0 || event.format.equalsIgnoreCase("Demo")) {
      return Typeface.ITALIC;
    } else {
      return Typeface.NORMAL;
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.main_layout);

    // get resources
    COLOR_JUNIOR=getResources().getColor(R.color.junior);
    COLOR_SEMINAR=getResources().getColor(R.color.seminar);
    COLOR_QUALIFY=getResources().getColor(R.color.qualify);
    COLOR_NON_TOURNAMENT=getResources().getColor(R.color.non_tournament);
    COLOR_OPEN_TOURNAMENT=getResources().getColor(R.color.open_tournament);

    activity=this;

    // enable home button for navigation drawer
    final ActionBar ab=getActionBar();
    if (ab!=null) {
      ab.setDisplayHomeAsUpEnabled(true);
      ab.setHomeButtonEnabled(true);
    } else {
      Log.d(TAG, "Could not get action bar");
    }

    // load navigation drawer
    drawerTitles=getResources().getStringArray(R.array.drawer_titles);
    drawerLayout=(DrawerLayout) findViewById(R.id.drawer_layout);

    drawerList=(ListView) findViewById(R.id.left_drawer);
    drawerList.setAdapter(new DrawerAdapter(this));

    drawerToggle=
        new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer, R.string.open_drawer,
            R.string.close_drawer) {

          public void onDrawerClosed(View view) {
            super.onDrawerClosed(view);
            setActionBarTitle(actionBarTitle);
            invalidateOptionsMenu();
          }

          public void onDrawerOpened(View drawerView) {
            super.onDrawerOpened(drawerView);
            setActionBarTitle(drawerTitle);
            invalidateOptionsMenu();
          }
        };
    drawerLayout.setDrawerListener(drawerToggle);

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

    // start summary fragment and set action bar title
    currentFragment=SUMMARY_FRAGMENT_INDEX;
    selectMenuItem(currentFragment);
    drawerTitle=getResources().getString(R.string.app_name_short);
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    // Sync the toggle state after onRestoreInstanceState has occurred.
    drawerToggle.syncState();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    drawerToggle.onConfigurationChanged(newConfig);
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
  protected void onResume() {
    setCurrentTime(getResources().getStringArray(R.array.daysForParsing));
    super.onResume();
  }

  @Override
  public void onPause() {
    // save starred states for all events
    SharedPreferences.Editor editor=
        getSharedPreferences(getResources().getString(R.string.sp_file_name), Context.MODE_PRIVATE)
            .edit();
    String starPrefString=getResources().getString(R.string.sp_event_starred);

    for (ArrayList<ArrayList<Event>> searchDayList : dayList) {
      for (ArrayList<Event> events : searchDayList) {
        for (Event event : events) {
          editor.putBoolean(starPrefString+event.identifier, event.starred);
        }
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
    boolean drawerOpen=drawerLayout.isDrawerOpen(drawerList);

    menu.findItem(R.id.menu_search).setVisible(!drawerOpen);

    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (drawerToggle.onOptionsItemSelected(item)) {
      return true;
    }

    if (item.getItemId()==R.id.menu_search) {
      Intent intent=new Intent(this, SearchActivity.class);
      startActivity(intent);
    } else {
      return super.onOptionsItemSelected(item);
    }

    return true;
  }

  private void setActionBarTitle(CharSequence title) {
    final ActionBar ab=getActionBar();
    if (ab!=null) {
      ab.setTitle(title);
    } else {
      Log.d(TAG, "Error: Could not get action bar");
    }
  }

  /**
   * Dialog Text
   */
  public static class DialogText extends DialogFragment {
    private final String TAG="Changes Dialog";

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

  public class DrawerAdapter extends ArrayAdapter<String> {
    final LayoutInflater inflater;
    final int layoutResID;

    public DrawerAdapter(Context context) {
      super(context, R.layout.drawer_list_item);

      layoutResID=R.layout.drawer_list_item;
      inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
      return drawerTitles.length;
    }

    @Override
    public String getItem(int position) {
      return drawerTitles[position];
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
      View view=convertView;

      if (view==null) {
        if (position==CONVENTION_INFO_DIVIDER_INDEX || position==SETTINGS_DIVIDER_INDEX) {
          view=inflater.inflate(R.layout.drawer_list_header, parent, false);

          TextView title=(TextView) view.findViewById(R.id.drawer_title);
          title.setText(drawerTitles[position]);

        } else {
          view=inflater.inflate(R.layout.drawer_list_item, parent, false);

          TextView title=(TextView) view.findViewById(R.id.drawer_title);
          title.setText(drawerTitles[position]);

          ImageView icon=(ImageView) view.findViewById(R.id.drawer_image);
          icon.setImageResource(drawerIconIDs[position]);

          view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              selectMenuItem(position);
            }
          });
        }
      }
      return view;
    }
  }

  /**
   * If showing fragment is user data or full schedule, return to summary before exitting app
   */
  @Override
  public void onBackPressed() {
    if (currentFragment==SUMMARY_FRAGMENT_INDEX) {
      super.onBackPressed();
    } else {
      int p=SUMMARY_FRAGMENT_INDEX;
      FragmentManager fragmentManager=activity.getFragmentManager();
      Fragment fragment;
      String tag;

      actionBarTitle=drawerTitles[p];
      tag="summary";

      fragment=fragmentManager.findFragmentByTag(tag);
      if (fragment==null) {
        fragment=new SummaryFragment();
      }

      // switch fragments
      fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, tag).commit();

      // highlight the selected item, update the title, and refresh the drawer
      drawerList.setItemChecked(p, true);
      currentFragment=p;
      activity.setTitle(actionBarTitle);
      activity.invalidateOptionsMenu();
    }
  }
}
