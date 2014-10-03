package org.boardgamers.wbc;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
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
  public static final int FORMAT_DEMO = 1000;
  public static final int FORMAT_SEMINAR = 1001;
  private final static String TAG = "Summary Activity";
  public static int SELECTED_GAME_ID;
  public static String SELECTED_EVENT_ID;
  public static int day;
  public static int hour;
  public static int NUM_EVENTS;
  public static List<Tournament> allTournaments;
  public static ArrayList<ArrayList<ArrayList<Event>>> dayList;
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
  private CharSequence drawerTitle;
  private CharSequence actionBarTitle;

  /**
   * Open game fragment
   *
   * @param c   - context
   * @param gID - game ID
   * @param eID - event ID (needed for tablets where event also displayed)
   */
  public static void selectGame(Context c, int gID, String eID) {
    SELECTED_GAME_ID = gID;
    SELECTED_EVENT_ID = eID;

    Intent intent = new Intent(c, TournamentActivity.class);
    c.startActivity(intent);

  }

  /**
   * @param fm   - fragment manager
   * @param room - string containing name of selected room
   */
  public static void showMapDialog(FragmentManager fm, String room) {
    MapFragment fragment = new MapFragment();
    fragment.roomString = room;
    fm.beginTransaction()
        .replace(R.id.content_frame, fragment)
        .commit();

  }

  /**
   * @param event - event needed for format, title, class, and qualify
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
   * @param event - event needed for format, title, class, and qualify
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
      Log.d(TAG, "Error: Could not get action bar");


    // load navigation drawer
    drawerTitles = getResources().getStringArray(R.array.drawer_titles);
    drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    drawerList = (ListView) findViewById(R.id.left_drawer);
    actionBarTitle = drawerTitle = getTitle();

    drawerList.setAdapter(new ArrayAdapter<String>(this,
        R.layout.drawer_list_item, drawerTitles));
    drawerList.setOnItemClickListener(new DrawerItemClickListener());

    drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
        R.drawable.selected, R.string.open_drawer, R.string.close_drawer) {

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

    dialogText = "";
    // TODO dialogText=getIntent().getStringExtra("changes");
    dialogTitle = "Schedule Changes";
    if (!dialogText.equalsIgnoreCase("")) {
      DialogText dc = new DialogText();
      dc.show(getFragmentManager(), "changes_dialog");
    }

    String[] dayStrings = getResources().getStringArray(R.array.days);
    ArrayList<ArrayList<Event>> temp;
    MainActivity.dayList = new ArrayList<ArrayList<ArrayList<Event>>>(dayStrings.length);

    int i = 0;
    while (i < dayStrings.length) {
      temp = new ArrayList<ArrayList<Event>>();
      for (int j = 0; j < 19; j++) {
        temp.add(new ArrayList<Event>());
      }
      MainActivity.dayList.add(temp);
      i++;
    }

    new LoadEventsTask(this).execute(null, null, null);
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

    // get hour
    hour = c.get(Calendar.HOUR_OF_DAY);

    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd",
        Locale.US);
    String dateString = dateFormatter.format(c.getTime());

    // get day
    day = -1;
    for (int i = 0; i < daysForParsing.length; i++) {
      if (daysForParsing[i].equalsIgnoreCase(dateString)) {
        day = i;
        break;
      }
    }

    super.onResume();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    return (drawerToggle.onOptionsItemSelected(item)) || super.onOptionsItemSelected(item);
  }

  /**
   * Swaps fragments in the main content view
   */
  private void selectItem(int position) {
    Fragment fragment;

    switch (position) {
      case 0:
        fragment = new SummaryFragment();
        break;
      case 1:
        startActivity(new Intent(this, ScheduleActivity.class));
        return;
      case 2:
        fragment = new SearchFragment();
        break;
      case 3:
        fragment = new FilterFragment();
        break;
      case 4:
        SELECTED_GAME_ID = 0;
        SELECTED_EVENT_ID = "";

        Intent intent = new Intent(this, TournamentActivity.class);
        startActivity(intent);
        return;
      case 5:
        fragment = new FinishesFragment();
        break;
      case 6:
        fragment = new NotesFragment();
        break;
      case 7:
        fragment = new SettingsFragment();
        break;
      case 8:
        fragment = new HelpFragment();
        break;
      case 9:
        fragment = new AboutFragment();
        break;
      default:
        // return to avoid null fragment
        return;
    }

    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction ft = fragmentManager.beginTransaction()
        .replace(R.id.content_frame, fragment);
    ft.addToBackStack("Summary");
    ft.commit();

    // Highlight the selected item, update the title, and close the drawer
    drawerList.setItemChecked(position, true);
    actionBarTitle = drawerTitles[position];
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
