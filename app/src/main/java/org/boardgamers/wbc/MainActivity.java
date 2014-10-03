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
import android.os.AsyncTask;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
  private static int COLOR_JUNIOR;
  private static int COLOR_SEMINAR;
  private static int COLOR_QUALIFY;
  private static int COLOR_OPEN_TOURNAMENT;
  private static int COLOR_NON_TOURNAMENT;
  private static String dialogText;
  private static String dialogTitle;
  protected String allChanges;
  private String[] drawerTitles;
  private DrawerLayout drawerLayout;
  private ActionBarDrawerToggle drawerToggle;
  private ListView drawerList;
  private CharSequence drawerTitle;
  private CharSequence actionBarTitle;
  public static boolean listReady = false;
  public static String[] dayStrings;

  /**
   * Add starred event to "My Events" group in list
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
   * Remove starred event from "My Events" group in list
   *
   * @param identifier - event id
   * @param day        - event's currentDay, used to find which my events group
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
   * @param eID - event ID (needed for tablets where event also displayed)
   */
  public static void selectGame(Context c, int gID, String eID) {
    SELECTED_GAME_ID = gID;
    SELECTED_EVENT_ID = eID;

    Intent intent = new Intent(c, TournamentActivity.class);
    c.startActivity(intent);

  }

  /**
   * @param c - calling context
   * @param room - string containing name of selected room
   */
  public static void openMap(Context c, String room) {
    Intent intent = new Intent(c, MapActivity.class);
    intent.putExtra("roomName", room);
    c.startActivity(intent);

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

    dayStrings = getResources().getStringArray(R.array.days);

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

    dialogText = "";
    // TODO dialogText=getIntent().getStringExtra("changes");
    dialogTitle = "Schedule Changes";
    if (!dialogText.equalsIgnoreCase("")) {
      DialogText dc = new DialogText();
      dc.show(getFragmentManager(), "changes_dialog");
    }


    if (dayList == null)
      new LoadEventsTask().execute(null, null, null);
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

    boolean menuVisible = listReady && !drawerOpen;

    menu.findItem(R.id.menu_search).setVisible(menuVisible);
    menu.findItem(R.id.menu_filter).setVisible(menuVisible);
    menu.findItem(R.id.menu_help).setVisible(menuVisible);
    menu.findItem(R.id.menu_about).setVisible(menuVisible);
    menu.findItem(R.id.menu_settings).setVisible(menuVisible);

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
        actionBarTitle = getResources().getStringArray(R.array.days)[Math.max(currentDay, 0)];
        fragment = new ScheduleContainer();
        break;
      case 2:
        SELECTED_GAME_ID = 0;
        SELECTED_EVENT_ID = "";

        Intent intent = new Intent(this, TournamentActivity.class);
        startActivity(intent);
        return;
      case 3:
        fragment = new FinishesFragment();
        break;
      case 4:
        fragment = new NotesFragment();
        break;
      default:
        // return to avoid null fragment
        return;
    }

    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction ft = fragmentManager.beginTransaction()
        .replace(R.id.content_frame, fragment);

    //ft.addToBackStack("Summary");
    ft.commit();

    // Highlight the selected item, update the title, and close the drawer

    drawerList.setItemChecked(position, true);
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

  public void taskFinished() {
    // Insert the fragment by replacing any existing fragment
    SummaryFragment summaryFragment = new SummaryFragment();
    FragmentManager fragmentManager =getFragmentManager();
    fragmentManager.beginTransaction()
        .add(R.id.content_frame, summaryFragment)
        .commit();
    MainActivity.listReady = true;

    invalidateOptionsMenu();
  }

  public void showToast(String string) {
    Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
  }

  /**
   * Load Events Task to load schedule
   */
  public class LoadEventsTask extends AsyncTask<Integer, Integer, Integer> {
    private final static String TAG = "Load Events Task";

    @Override
    protected void  onPreExecute() {

      ArrayList<ArrayList<Event>> temp;
      dayList = new ArrayList<ArrayList<ArrayList<Event>>>(dayStrings.length);

      int i = 0;
      while (i < dayStrings.length) {
        temp = new ArrayList<ArrayList<Event>>();
        for (int j = 0; j < 19; j++) {
          temp.add(new ArrayList<Event>());
        }
        dayList.add(temp);
        i++;
      }
      super.onPreExecute();
    }

    @Override
    protected Integer doInBackground(Integer... params) {
      SharedPreferences sp = getSharedPreferences(
          getResources().getString(R.string.sp_file_name),
          Context.MODE_PRIVATE);
      String userEventPrefString = getResources().getString(R.string.sp_user_event);
      String starPrefString = getResources().getString(R.string.sp_event_starred);

      /***** LOAD USER EVENTS *******/

      String identifier, row, temp, eventTitle, eClass, format, gm, tempString, location;
      String[] rowData;
      int tournamentID, index, day, hour, prize, lineNum = 0;
      int numTournaments = 0, numPreviews = 0, numJuniors = 0, numSeminars = 0;

      double duration, totalDuration;
      boolean continuous, qualify, isTournamentEvent;

      Event event, tempEvent, prevEvent = null;
      Tournament tournament;

      String tournamentTitle = "My Events", tournamentLabel = "", shortEventTitle = "";
      String change;

      List<Tournament> tournaments = new ArrayList<Tournament>();

      tournamentID = 0;
      tournament = new Tournament(tournamentID, tournamentTitle,
          tournamentLabel, false, 0, "Me");
      tournament.visible = sp.getBoolean("vis_" + tournamentTitle, true);
      tournament.finish = sp.getInt("fin_" + tournamentTitle, 0);

      tournaments.add(tournament);

      for (index = 0; ; index++) {
        row = sp.getString(userEventPrefString + String.valueOf(index), "");
        if (row.equalsIgnoreCase(""))
          break;

        rowData = row.split("~");

        day = Integer.valueOf(rowData[0]);
        hour = Integer.valueOf(rowData[1]);
        eventTitle = rowData[2];
        duration = Double.valueOf(rowData[3]);
        location = rowData[4];

        identifier = String.valueOf(day * 24 + hour) + eventTitle;
        event = new Event(identifier, 0, day, hour, eventTitle, "", "",
            false, duration, false, duration, location);
        event.starred = sp.getBoolean(starPrefString + identifier, false);

        MainActivity.dayList.get(day).get(hour - 6).add(0, event);
        if (event.starred)
          MainActivity.addStarredEvent(event);
      }

      /***** LOAD SHARED EVENTS *******/

      // get events (may need update)
      int scheduleVersion = sp.getInt(
          getResources().getString(R.string.sp_2014_schedule_version), -1);
      int newVersion = 0;

      /***** PARSE SCHEDULE *****/

      Log.d(TAG, "Starting parsing");

      // find schedule file
      InputStream is;
      try {
        is = getAssets().open("schedule2014.txt");
      } catch (IOException e2) {
            showToast(
            "ERROR: Could not find schedule file,"
                + "contact dev@boardgamers.org for help.");
        e2.printStackTrace();
        return -1;
      }
      // read schedule file
      InputStreamReader isr;
      try {
        isr = new InputStreamReader(is);
      } catch (IllegalStateException e1) {
        showToast(
            "ERROR: Could not open schedule file,"
                + "contact dev@boardgamers.org for help.");
        e1.printStackTrace();
        return -1;
      }

      // parse schedule file
      BufferedReader reader = new BufferedReader(isr);
      try {
        final String preExtraStrings[] = {" AFC", " NFC", " FF", " PC",
            " Circus", " After Action", " Aftermath"};
        final String postExtraStrings[] = {" Demo"};

        final String daysForParsing[] = getResources()
            .getStringArray(R.array.daysForParsing);


        String line;
        while ((line = reader.readLine()) != null) {
          rowData = line.split("~");

          // currentDay
          tempString = rowData[0];
          for (index = 0; index < daysForParsing.length; index++) {
            if (daysForParsing[index].equalsIgnoreCase(tempString))
              break;
          }
          if (index == -1) {
            Log.d(TAG, "Unknown date: " + rowData[2] + " in " + line);
            index = 0;
          }
          day = index;

          // title
          eventTitle = rowData[2];

          // time
          boolean halfPast = false;
          tempString = rowData[1];
          if (rowData[1].contains(":30")) {
            Log.d(TAG, rowData[2] + " starts at half past");
            tempString = tempString.substring(0, tempString.length() - 3);
            halfPast = true;

          }
          hour = Integer.valueOf(tempString);

          if (rowData.length < 8) {
            prize = 0;
            eClass = "";
            format = "";
            duration = 0;
            continuous = false;
            gm = "";
            location = "";
          } else {
            // prize
            temp = rowData[3];
            if (temp.equalsIgnoreCase("") || temp.equalsIgnoreCase("-"))
              temp = "0";
            prize = Integer.valueOf(temp);

            // class
            eClass = rowData[4];

            // format
            format = rowData[5];

            // duration
            if (rowData[6].equalsIgnoreCase("")
                || rowData[6].equalsIgnoreCase("-"))
              duration = 0;
            else
              duration = Double.valueOf(rowData[6]);

            if (duration > .33 && duration < .34)
              duration = .33;

            // continuous
            continuous = rowData[7].equalsIgnoreCase("Y");

            // gm
            gm = rowData[8];

            // location
            location = rowData[9];

          }

          // get tournament title and label and event short name
          temp = eventTitle;

          // search through extra strings
          for (String eventExtraString : preExtraStrings) {
            index = temp.indexOf(eventExtraString);
            if (index > -1) {
              temp = temp.substring(0, index)
                  + temp.substring(index + eventExtraString.length());
            }
          }

          // split title in two, first part is tournament title,
          // second is short event title (H1/1)
          isTournamentEvent = eClass.length() > 0;

          if (isTournamentEvent || format.equalsIgnoreCase("Preview")) {
            index = temp.lastIndexOf(" ");
            shortEventTitle = temp.substring(index + 1);
            temp = temp.substring(0, index);

            if (index == -1) {
              Log.d(TAG, "");
            }
          }

          if (eventTitle.contains("Junior")
              || eventTitle.indexOf("COIN series") == 0
              || format.equalsIgnoreCase("SOG")
              || format.equalsIgnoreCase("Preview")) {
            tournamentTitle = temp;
            tournamentLabel = "-";

          } else if (isTournamentEvent) {
            for (String eventExtraString : postExtraStrings) {
              index = temp.indexOf(eventExtraString);
              if (index > -1) {
                temp = temp.substring(0, index);
              }
            }

            tournamentLabel = rowData[10];
            tournamentTitle = temp;
          } else {
            tournamentTitle = temp;
            tournamentLabel = "";

            if (eventTitle.indexOf("Auction") == 0)
              tournamentTitle = "AuctionStore";

            // search for non tournament main titles
            String[] nonTournamentStrings = {"Open Gaming",
                "Registration", "Vendors Area", "World at War",
                "Wits & Wagers", "Texas Roadhouse BPA Fundraiser",
                "Memoir: D-Day"};
            for (String nonTournamentString : nonTournamentStrings) {
              if (temp.indexOf(nonTournamentString) == 0) {
                tournamentTitle = nonTournamentString;
                break;
              }
            }
          }

          // check if last 5 in list contains this tournament
          tournamentID = -1;
          for (index = Math.max(0, tournaments.size() - 5); index < tournaments
              .size(); index++) {
            if (tournaments.get(index).title
                .equalsIgnoreCase(tournamentTitle)) {
              tournamentID = index;
              break;
            }
          }

          if (tournamentID > -1) {
            // update existing tournament
            tournament = tournaments.get(tournamentID);
            if (prize > 0)
              tournament.prize = prize;
            if (isTournamentEvent)
              tournament.isTournament = true;

            tournament.gm = gm;
          } else {
            tournamentID = tournaments.size();
            tournament = new Tournament(tournamentID, tournamentTitle,
                tournamentLabel, isTournamentEvent, prize, gm);

            tournament.visible = sp.getBoolean("vis_" + tournamentTitle,
                true);
            tournament.finish = sp.getInt("fin_" + tournamentTitle, 0);

            tournaments.add(tournament);

            if (format.equalsIgnoreCase("Preview"))
              numPreviews++;
            else if (eventTitle.contains("Junior"))
              numJuniors++;
            else if (format.equalsIgnoreCase("Seminar"))
              numSeminars++;
            else if (isTournamentEvent)
              numTournaments++;

          }

          // Log.d(TAG, String.valueOf(tournamentID)+": "+tournamentTitle
          // +";;;E: "+eventTitle);

          totalDuration = duration;
          qualify = false;

          if (isTournamentEvent || format.equalsIgnoreCase("Junior")) {
            if (shortEventTitle.indexOf("SF") == 0) {
              qualify = true;
            } else if (shortEventTitle.indexOf("QF") == 0) {
              qualify = true;
            } else if (shortEventTitle.indexOf("F") == 0) {
              qualify = true;
            } else if (shortEventTitle.indexOf("QF/SF/F") == 0) {
              qualify = true;
              totalDuration *= 3;
            } else if (shortEventTitle.indexOf("SF/F") == 0) {
              qualify = true;
              totalDuration *= 2;
            } else if (continuous && shortEventTitle.indexOf("R") == 0
                && shortEventTitle.contains("/")) {
              int dividerIndex = shortEventTitle.indexOf("/");
              int startRound = Integer.valueOf(shortEventTitle
                  .substring(1, dividerIndex));
              int endRound = Integer.valueOf(shortEventTitle
                  .substring(dividerIndex + 1));

              int currentTime = hour;
              for (int round = 0; round < endRound - startRound; round++) {
                // if time passes midnight, next round
                // starts at
                // 9 the next currentDay
                if (currentTime > 24) {
                  if (currentTime >= 24 + 9)
                    Log.d(TAG, "Event " + eventTitle
                        + " goes past 9");
                  totalDuration += 9 - (currentTime - 24);
                  currentTime = 9;
                }

                totalDuration += duration;
                currentTime += duration;

              }

              if (prevEvent.tournamentID == tournamentID) {
                // update previous event total duration
                temp = prevEvent.title;

                // search through extra strings
                for (String eventExtraString : preExtraStrings) {
                  index = temp.indexOf(eventExtraString);
                  if (index > -1) {
                    temp = temp.substring(0, index)
                        + temp.substring(index
                        + eventExtraString.length());
                  }
                }

                index = temp.lastIndexOf(" ");

                if (index > -1) {
                  shortEventTitle = temp.substring(index + 1);
                  if (shortEventTitle.indexOf("R") == 0) {
                    dividerIndex = shortEventTitle.indexOf("/");

                    if (dividerIndex == -1)
                      Log.d(TAG, "huh: " + shortEventTitle);
                    else {

                      int prevStartRound = Integer
                          .valueOf(shortEventTitle
                              .substring(1,
                                  dividerIndex));

                      int realNumRounds = startRound
                          - prevStartRound;

                      currentTime = hour;
                      prevEvent.totalDuration = 0;
                      for (int round = 0; round < realNumRounds; round++) {
                        // if time passes midnight, next
                        // round
                        // starts at
                        // 9 the next currentDay
                        if (currentTime > 24) {
                          if (currentTime >= 24 + 9)
                            Log.d(TAG, "Event "
                                + prevEvent.title
                                + " goes past 9");
                          prevEvent.totalDuration += 9 - (currentTime - 24);
                          currentTime = 9;
                        }

                        prevEvent.totalDuration += prevEvent.duration;
                        currentTime += prevEvent.duration;
                      }
                    }

                    List<Event> searchList = MainActivity.dayList
                        .get(prevEvent.day).get(
                            prevEvent.hour - 6);
                    for (Event searchEvent : searchList) {
                      if (searchEvent.identifier
                          .equalsIgnoreCase(prevEvent.identifier))
                        searchEvent.totalDuration = prevEvent.totalDuration;
                    }

                    searchList = MainActivity.dayList.get(
                        prevEvent.day).get(0);
                    for (Event searchEvent : searchList) {
                      if (searchEvent.identifier
                          .equalsIgnoreCase(prevEvent.identifier))
                        searchEvent.totalDuration = prevEvent.totalDuration;
                    }

                    Log.d(TAG,
                        "Event "
                            + prevEvent.title
                            + " duration changed to "
                            + String.valueOf(prevEvent.totalDuration));
                  }
                }
              }
            } else if (continuous) {
              Log.d(TAG, "Unknown continuous event: " + eventTitle);
            }
          } else if (continuous) {
            Log.d(TAG, "Non tournament event " + eventTitle + " is cont");
          }

          if (halfPast)
            eventTitle = eventTitle + " (" + rowData[1] + ")";

          identifier = String.valueOf(day * 24 + hour) + "_" + eventTitle;

          event = new Event(identifier, tournamentID, day, hour,
              eventTitle, eClass, format, qualify, duration,
              continuous, totalDuration, location);

          event.starred = sp.getBoolean(starPrefString + event.identifier,
              false);

          prevEvent = event;

          /********* LOAD INTO DAYLIST *************/
          change = "";

          // TODO add changes here
          /*
           * if (event.title.equalsIgnoreCase("Age of Renaissance H1/3 PC")) { int
           * newDay=5;
           *
           * if (scheduleVersion<0) { change=event.title+": Day changed from "
           * +dayStrings[event.currentDay]+" to " +dayStrings[newDay]; newVersion=0; }
           *
           * event.currentDay=newDay; }
           */

          if (!change.equalsIgnoreCase(""))
            allChanges += "\t" + change + "\n\n";

          // LOAD INTO LIST
          ArrayList<Event> searchList = MainActivity.dayList.get(
              event.day).get(event.hour - 6);
          if (eventTitle.contains("Junior")) {
            index = 0;
            for (; index < searchList.size(); index++) {
              tempEvent = searchList.get(index);
              if (!tempEvent.title.contains("Junior")
                  && tempEvent.tournamentID > 0)
                break;
            }
          } else if (!isTournamentEvent || format.equalsIgnoreCase("Demo")) {
            index = 0;
            for (; index < searchList.size(); index++) {
              tempEvent = searchList.get(index);
              if (tempEvent.eClass.length() > 0
                  && !tempEvent.title.contains("Junior")
                  && !tempEvent.format.equalsIgnoreCase("Demo"))
                break;
            }
          } else if (event.qualify) {
            index = searchList.size();
          } else {
            index = 0;
            for (; index < searchList.size(); index++) {
              tempEvent = searchList.get(index);
              if (tempEvent.qualify)
                break;
            }
          }

          MainActivity.dayList.get(event.day).get(event.hour - 6)
              .add(index, event);

          if (event.starred)
            MainActivity.addStarredEvent(event);

          lineNum++;

        }

        isr.close();
        is.close();
        reader.close();

        MainActivity.NUM_EVENTS = lineNum;

        // save update version
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(
            getResources().getString(R.string.sp_2014_schedule_version),
            newVersion);
        editor.apply();

      } catch (IOException e) {
        showToast(
            "ERROR: Could not parse schedule file,"
                + "contact dev@boardgamers.org for help.");
        e.printStackTrace();
        return -1;
      }

      MainActivity.allTournaments = tournaments;

      Log.d(TAG, "Finished load, " + String.valueOf(tournamentID)
          + " total tournaments and " + String.valueOf(lineNum)
          + " total events");
      Log.d(TAG, "Of total, " + String.valueOf(numTournaments)
          + " are tournaments, " + String.valueOf(numJuniors)
          + " are juniors, " + String.valueOf(numPreviews) + " are previews, "
          + String.valueOf(numSeminars) + " are seminars, ");

      return 1;
    }

    @Override
    protected void onPostExecute(Integer result) {
        taskFinished();
        super.onPostExecute(result);
    }
  }
}
