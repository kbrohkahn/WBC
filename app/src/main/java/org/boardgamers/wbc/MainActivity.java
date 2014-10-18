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
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
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
  private final static String TAG = "Summary Activity";
  public static int SELECTED_GAME_ID;
  public static String SELECTED_EVENT_ID;
  public static int currentDay;
  public static int currentHour;
  public static int NUM_EVENTS;
  public static List<Tournament> allTournaments;
  public static ArrayList<ArrayList<ArrayList<Event>>> dayList;
  public static String[] dayStrings;
  public static String allChanges;
  public static CharSequence drawerTitle;
  private static int COLOR_JUNIOR;
  private static int COLOR_SEMINAR;
  private static int COLOR_QUALIFY;
  private static int COLOR_OPEN_TOURNAMENT;
  private static int COLOR_NON_TOURNAMENT;
  private static String dialogText;
  private static String dialogTitle;
  private String[] drawerTitles;
  private DrawerLayout drawerLayout;
  private ActionBarDrawerToggle drawerToggle;
  private ListView drawerList;
  private CharSequence actionBarTitle;

  /**
   * Add starred help to "My Events" group in list
   *
   * @param event - Event that was starred
   */
  public static void addStarredEvent(Event event) {
    List<Event> myEvents = dayList.get(event.day).get(0);

    Event starredEvent = new Event(event.identifier, event.tournamentID,
        event.day, event.hour, event.title, event.eClass, event.format,
        event.qualify, event.duration, event.continuous,
        event.totalDuration, event.location);
    starredEvent.starred = true;
    // get position in starred list to add (time, then title)
    int index = 0;
    for (Event eTemp : myEvents) {
      if (starredEvent.hour < eTemp.hour
          || (starredEvent.hour == eTemp.hour && starredEvent.title
          .compareToIgnoreCase(eTemp.title) == 1))
        break;
      else
        index++;
    }
    dayList.get(event.day).get(0).add(index,
        starredEvent);

  }

  /**
   * Remove starred help from "My Events" group in list
   *
   * @param identifier - help id
   * @param day        - help's currentDay, used to find which my events group
   */
  public static void removeStarredEvent(String identifier, int day) {
    List<Event> myEvents = dayList.get(day).get(0);
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
    SELECTED_GAME_ID = gID;
    SELECTED_EVENT_ID = eID;

    Intent intent = new Intent(c, TournamentActivity.class);
    c.startActivity(intent);

  }

  /**
   * @param c    - calling context
   * @param room - string containing name of selected room
   */
  public static void openMap(Context c, String room) {
    Intent intent = new Intent(c, MapActivity.class);
    intent.putExtra("roomName", room);
    c.startActivity(intent);

  }

  /**
   * @param event - help needed for format, title, class, and qualify
   * @return integer value of color
   */
  public static int getTextColor(Event event) {
    if (event.qualify)
      return COLOR_QUALIFY;
    else if (event.title.contains("Junior"))
      return COLOR_JUNIOR;
    else if (event.format.equalsIgnoreCase("Seminar"))
      return COLOR_SEMINAR;
    else if (event.format.equalsIgnoreCase("SOG")
        || event.format.equalsIgnoreCase("MP Game")
        || event.title.indexOf("Open Gaming") == 0)
      return COLOR_OPEN_TOURNAMENT;
    else if (event.eClass.length() == 0)
      return COLOR_NON_TOURNAMENT;
    else
      return Color.BLACK;
  }

  /**
   * @param event - help needed for format, title, class, and qualify
   * @return integer value of typeface
   */
  public static int getTextStyle(Event event) {
    if (event.qualify)
      return Typeface.BOLD;
    else if (event.title.contains("Junior"))
      return Typeface.NORMAL;
    else if (event.eClass.length() == 0
        || event.format.equalsIgnoreCase("Demo"))
      return Typeface.ITALIC;
    else
      return Typeface.NORMAL;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.main_layout);

    Log.d(TAG, "MAIN ACTIVITY STARTED");

    // get resources
    COLOR_JUNIOR = getResources().getColor(R.color.junior);
    COLOR_SEMINAR = getResources().getColor(R.color.seminar);
    COLOR_QUALIFY = getResources().getColor(R.color.qualify);
    COLOR_NON_TOURNAMENT = getResources().getColor(R.color.non_tournament);
    COLOR_OPEN_TOURNAMENT = getResources().getColor(R.color.open_tournament);

    // enable home button for navigation drawer
    final ActionBar ab = getActionBar();
    if (ab != null) {
      ab.setDisplayHomeAsUpEnabled(true);
      ab.setHomeButtonEnabled(true);
    } else
      Log.d(TAG, "Could not get action bar");

    // load navigation drawer
    drawerTitles = getResources().getStringArray(R.array.drawer_titles);
    drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    drawerList = (ListView) findViewById(R.id.left_drawer);
    actionBarTitle = drawerTitle = getTitle();

    drawerList.setAdapter(new ArrayAdapter<String>(this,
        R.layout.drawer_list_item, drawerTitles));
    drawerList.setOnItemClickListener(new DrawerItemClickListener());

    drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
        R.drawable.ic_drawer, R.string.open_drawer, R.string.close_drawer) {

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

    SharedPreferences sp = getSharedPreferences(
        getResources().getString(R.string.sp_file_name),
        Context.MODE_PRIVATE);
    int version = sp.getInt("last_app_version", 0);

    // alert to notify dev@boardgamers.org for questions
    if (version < 12) {
      AlertDialog.Builder questionAlertBuilder = new AlertDialog.Builder(
          this);
      questionAlertBuilder
          .setMessage(
              "Please send all questions, comments, requests, etc. to Kevin Broh-Kahn " +
                  "at dev@boardgamers.org (link in \"About\" page via main menu)")
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
      versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
    } catch (PackageManager.NameNotFoundException e) {
      Toast.makeText(
          this,
          "ERROR: Could not find version code,"
              + "contact dev@boardgamers.org for help.",
          Toast.LENGTH_LONG).show();
      versionCode = -1;
      e.printStackTrace();
    }

    SharedPreferences.Editor editor = sp.edit();
    editor.putInt("last_app_version", versionCode);
    editor.apply();

    // check for changes
    dialogText = allChanges;
    dialogTitle = "Schedule Changes";
    if (!dialogText.equalsIgnoreCase("")) {
      DialogText dc = new DialogText();
      dc.show(getFragmentManager(), "changes_dialog");
    }

    Fragment summaryFragment = new SummaryFragment();
    FragmentManager fragmentManager = getFragmentManager();
    fragmentManager.beginTransaction()
        .replace(R.id.content_frame, summaryFragment)
        .commit();

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

  @Override
  protected void onResume() {
    String[] daysForParsing = getResources()
        .getStringArray(R.array.daysForParsing);

    Calendar c = Calendar.getInstance();

    // get currentHour
    currentHour = c.get(Calendar.HOUR_OF_DAY);

    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd",
        Locale.US);
    String dateString = dateFormatter.format(c.getTime());

    // get currentDay
    currentDay = -1;
    for (int i = 0; i < daysForParsing.length; i++) {
      if (daysForParsing[i].equalsIgnoreCase(dateString)) {
        currentDay = i;
        break;
      }
    }

    super.onResume();
  }


  @Override
  public void onPause() {
    // save starred states for all events
    SharedPreferences.Editor editor = getSharedPreferences(
        getResources().getString(R.string.sp_file_name),
        Context.MODE_PRIVATE).edit();
    String starPrefString = getResources().getString(R.string.sp_event_starred);

    for (ArrayList<ArrayList<Event>> searchDayList : dayList) {
      for (ArrayList<Event> events : searchDayList) {
        for (Event event : events) {
          editor.putBoolean(starPrefString + event.identifier, event.starred);
        }
      }
    }
    editor.apply();

    super.onPause();
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    // If the nav drawer is open, hide action items related to the content view
    boolean drawerOpen = drawerLayout.isDrawerOpen(drawerList);

    menu.findItem(R.id.menu_search).setVisible(!drawerOpen);
    menu.findItem(R.id.menu_filter).setVisible(!drawerOpen);
    menu.findItem(R.id.menu_help).setVisible(!drawerOpen);
    menu.findItem(R.id.menu_about).setVisible(!drawerOpen);
    menu.findItem(R.id.menu_settings).setVisible(!drawerOpen);

    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (drawerToggle.onOptionsItemSelected(item))
      return true;

    Intent intent;
    switch (item.getItemId()) {
      case R.id.menu_search:
        intent = new Intent(this, SearchActivity.class);
        break;
      case R.id.menu_filter:
        intent = new Intent(this, FilterActivity.class);
        break;
      case R.id.menu_help:
        intent = new Intent(this, HelpActivity.class);
        break;
      case R.id.menu_about:
        intent = new Intent(this, AboutActivity.class);
        break;
      case R.id.menu_settings:
        intent = new Intent(this, SettingsActivity.class);
        break;
      default:
        return super.onOptionsItemSelected(item);
    }

    startActivity(intent);

    return true;
  }

  /**
   * Swaps fragments in the main content view
   */
  private void selectItem(int position) {
    actionBarTitle = drawerTitles[position];

    Fragment fragment;
    switch (position) {
      case 0:
        fragment = new SummaryFragment();
        break;
      case 1:
        actionBarTitle = dayStrings[Math.max(currentDay, 0)];
        fragment = new ScheduleContainer();
        break;
      case 2:
        fragment = new MyWBCData();
        break;
      default:
        // return to avoid null fragment
        return;
    }

    FragmentManager fragmentManager = getFragmentManager();
    fragmentManager.beginTransaction()
        .replace(R.id.content_frame, fragment).commit();

    // Highlight the selected item, update the title, and close the drawer
    //drawerList.setSelection(position);
    //drawerList.setItemChecked(position, true);
    setActionBarTitle(actionBarTitle);
    drawerLayout.closeDrawer(drawerList);
  }

  public void setActionBarTitle(CharSequence title) {
    final ActionBar ab = getActionBar();
    if (ab != null)
      ab.setTitle(title);
    else
      Log.d(TAG, "Error: Could not get action bar");
  }

  /**
   * Dialog Text
   */
  public static class DialogText extends DialogFragment {
    final String TAG = "Changes Dialog";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      Dialog dialog = super.onCreateDialog(savedInstanceState);
      dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
      return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.dialog_text, container, false);

      TextView title = (TextView) view.findViewById(R.id.dt_title);
      title.setText(dialogTitle);

      LinearLayout layout = (LinearLayout) view
          .findViewById(R.id.dt_layout);
      layout.removeAllViews();

      final Context context = getActivity();
      final Resources resources = getResources();
      int padding = (int) resources.getDimension(R.dimen.text_margin_small);

      TextView textView = new TextView(context);
      textView.setText(dialogText);
      textView.setTextAppearance(context, R.style.medium_text);
      textView.setGravity(Gravity.CENTER);
      textView.setPadding(padding, padding, padding, padding);

      layout.addView(textView);

      Button close = (Button) view.findViewById(R.id.dt_close);
      close.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View v) {
          dismiss();
        }
      });

      return view;

    }
  }

  private class DrawerItemClickListener implements ListView.OnItemClickListener {
    @Override
    public void onItemClick(AdapterView parent, View view, int position, long id) {
      selectItem(position);
    }
  }
}
