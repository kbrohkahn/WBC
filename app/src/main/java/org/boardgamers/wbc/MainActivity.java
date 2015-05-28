package org.boardgamers.wbc;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Main Activity class
 */
public class MainActivity extends AppCompatActivity {
  private final static String TAG="Main Activity";

  public static final int PRIMARY_USER_ID=0;
  public static final int TOTAL_DAYS=9;
  public static final int USER_EVENT_ID=5000;
  public static int selectedEventId=-1;
  public static long currentDay;
  public static int currentHour;
  public static int userId=-1;

  public static boolean differentUser=false;
  public static boolean opened=false;

  private static ViewPager viewPager;
  private static TabsPagerAdapter pagerAdapter;

  public static void updateClock() {
    currentHour++;
    if (currentHour==24) {
      currentHour=0;
      currentDay++;
    }
  }

  @Override
  public void finish() {
    opened=false;
    super.finish();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "Init");

    setContentView(R.layout.main_layout);

    opened=true;
    pagerAdapter=new TabsPagerAdapter(getSupportFragmentManager());

    viewPager=(ViewPager) findViewById(R.id.pager);
    viewPager.setAdapter(pagerAdapter);
    viewPager.setOffscreenPageLimit(2);

    Toolbar toolbar=(Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    SlidingTabLayout tabs=(SlidingTabLayout) findViewById(R.id.sliding_layout);
    tabs.setViewPager(viewPager);

    SharedPreferences sp=getSharedPreferences(getResources().getString(R.string.user_data_file),
        Context.MODE_PRIVATE);
    int latestVersion=sp.getInt("last_app_version", 0);
    int currentVersion;
    try {
      currentVersion=getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
    } catch (PackageManager.NameNotFoundException e) {
      Log.d(TAG, "ERROR: Unable to not find version code");
      Toast.makeText(this,
          "ERROR: Could not find version code, contact "+getResources().getString(R.string.email)+
              " for help.", Toast.LENGTH_LONG).show();
      currentVersion=latestVersion; // TODO set as version code
      e.printStackTrace();
    }

    if (latestVersion<currentVersion) {
      Intent intent=new Intent(this, UpdateService.class);
      stopService(intent);
      startService(intent);

      SharedPreferences.Editor editor=sp.edit();
      editor.putInt("last_app_version", currentVersion);
      editor.apply();
    }

    loadUserData();
  }

  public void loadUserData() {
    Log.d(TAG, "Loading");
    userId=PreferenceManager.getDefaultSharedPreferences(this)
        .getInt(getResources().getString(R.string.pref_key_schedule_select), PRIMARY_USER_ID);

    WBCDataDbHelper dbHelper=new WBCDataDbHelper(this);
    dbHelper.getReadableDatabase();
    int numStarredEvents=dbHelper.getStarredEvents(userId).size();
    String scheduleName=dbHelper.getUser(userId).name;
    setTitle("WBC: "+scheduleName);
    dbHelper.close();

    if (numStarredEvents==0) {
      viewPager.setCurrentItem(1);
    }
    Log.d(TAG, "Loading complete");
  }

  @Override
  protected void onResume() {
    if (differentUser) {
      differentUser=false;

      recreate();

      // TODO better implementation
      //loadUserData();
      //pagerAdapter=new TabsPagerAdapter(getSupportFragmentManager());
    }

    super.onResume();
  }

  public static void removeEvents(Event[] events) {
    for (Event event : events) {
      event.starred=false;
    }

    pagerAdapter.getItem(1).removeEvents(events);
    pagerAdapter.getItem(0).updateEvents(events);
  }

  public static void updateUserData(long eventId, String note, long tournamentId, int finish) {
    ((UserDataListFragment) pagerAdapter.getItem(2))
        .updateUserData(eventId, note, tournamentId, finish);
    pagerAdapter.getItem(2).refreshAdapter();

  }

  public static void changeEventsInLists(Event[] events, int currentPage) {
    for (int j=0; j<3; j++) {
      if (j!=currentPage) {
        pagerAdapter.getItem(j).updateEvents(events);
      }
    }
  }

  /**
   * Get hours elapsed since midnight on the first day
   *
   * @return hours elapsed since midnight of the first day
   */
  public static int getHoursIntoConvention() {
    if (currentDay<0 || currentDay>TOTAL_DAYS) {
      return -1;
    } else {
      int day=(int) (long) currentDay;
      return day*24+currentHour;
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater=getMenuInflater();
    inflater.inflate(R.menu.menu_light_main, menu);

    SearchManager searchManager=(SearchManager) getSystemService(Context.SEARCH_SERVICE);
    final SearchView searchView=(SearchView) menu.findItem(R.id.menu_search).getActionView();
    searchView.setSearchableInfo(
        searchManager.getSearchableInfo(new ComponentName(this, SearchResultActivity.class)));
    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String query) {
        searchView.clearFocus();
        return false;
      }

      @Override
      public boolean onQueryTextChange(String newText) {
        return false;
      }
    });
    searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
      @Override
      public boolean onSuggestionSelect(int position) {
        return false;
      }

      @Override
      public boolean onSuggestionClick(int position) {
        searchView.clearFocus();
        Cursor cursor=(Cursor) searchView.getSuggestionsAdapter().getItem(position);
        int id=cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
        String title=cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
        startSearchActivity(id, title);
        return true;
      }
    });

    return true;

  }

  public void startSearchActivity(int id, String title) {
    Intent intent=new Intent(this, SearchResultActivity.class);
    intent.putExtra("query_title", title);
    intent.putExtra("query_id", id);
    startActivity(intent);
  }

  public void share() {
    //    WBCDataDbHelper dbHelper=new WBCDataDbHelper(this);
    //    dbHelper.getReadableDatabase();
    //    List<Event> starred=dbHelper.getStarredEvents();
    //    List<Event> notes=dbHelper.getEventsWithNotes();
    //    List<Tournament> tFinishes=dbHelper.getTournamentsWithFinishes();
    //    dbHelper.close();
    //    Log.d(TAG, "Received from DB");

    UserDataListFragment userDataListFragment=(UserDataListFragment) pagerAdapter.getItem(2);
    List<Event> notes=userDataListFragment.listAdapter.events.get(UserDataListFragment.NOTES_INDEX);
    List<Event> eFinishes=
        userDataListFragment.listAdapter.events.get(UserDataListFragment.FINISHES_INDEX);
    List<Event> userEvents=
        userDataListFragment.listAdapter.events.get(UserDataListFragment.EVENTS_INDEX);
    SummaryListFragment summaryListFragment=(SummaryListFragment) pagerAdapter.getItem(0);

    List<List<Event>> starredGroups=summaryListFragment.listAdapter.events;
    List<Event> starred=new ArrayList<>();
    for (List<Event> events : starredGroups) {
      for (Event event : events) {
        starred.add(event);
      }
    }

    String contentBreak="~~~";
    String delimitter=";;;";

    String outputString=
        getResources().getString(R.string.settings_schedule_name_check)+contentBreak;

    String email="Unknown user";
    Pattern emailPattern=Patterns.EMAIL_ADDRESS;
    Account[] accounts=AccountManager.get(this).getAccounts();
    for (Account account : accounts) {
      if (emailPattern.matcher(account.name).matches()) {
        email=account.name;
        break;
      }
    }
    outputString+=email+contentBreak+"0";
    for (Event event : userEvents) {
      outputString+=String.valueOf(event.id)+delimitter+event.title+delimitter+event.day+delimitter+
          event.hour+delimitter+event.duration+delimitter+event.location+delimitter;
    }
    outputString+=contentBreak+"0";
    for (Event event : starred) {
      outputString+=String.valueOf(event.id)+delimitter;
    }

    outputString+=contentBreak+"0";
    for (Event event : notes) {
      outputString+=String.valueOf(event.id)+delimitter+event.note+delimitter;
    }

    outputString+=contentBreak+"0";
    for (Event event : eFinishes) {
      outputString+=String.valueOf(event.tournamentID)+delimitter+event.note+delimitter;
    }

    File file;
    String fileName="Schedule.wbc";
    if (isExternalStorageWritable()) {
      Log.d(TAG, "Saving in external");
      File sdCard=Environment.getExternalStorageDirectory();
      File dir=new File(sdCard.getAbsolutePath()+"/WBC/");

      if (!dir.mkdirs()) {
        Log.d(TAG, "Directory not created");
      }

      file=new File(dir, fileName);
    } else {
      Log.d(TAG, "Saving in internal");
      file=new File(getCacheDir(), fileName);
    }

    Log.d(TAG, "File location: "+file.getAbsolutePath());

    FileOutputStream fileOutputStream;
    try {
      fileOutputStream=new FileOutputStream(file);
      fileOutputStream.write(outputString.getBytes());
      fileOutputStream.close();

    } catch (IOException e) {
      e.printStackTrace();
    }

    Intent shareIntent=new Intent();
    shareIntent.setAction(Intent.ACTION_SEND);
    shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
    shareIntent.setType("application/wbc");
    startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.share)));
  }

  /* Checks if external storage is available for read and write */

  public boolean isExternalStorageWritable() {
    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
      return true;
    } else {
      Log.d(TAG, "Error: external storage not writable");
      return false;
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId()==R.id.menu_map) {
      startActivity(new Intent(this, MapActivity.class));
    } else if (item.getItemId()==R.id.menu_share) {
      share();
    } else if (item.getItemId()==R.id.menu_help) {
      startActivity(new Intent(this, HelpActivity.class));
    } else if (item.getItemId()==R.id.menu_about) {
      startActivity(new Intent(this, AboutActivity.class));
      //    } else if (item.getItemId()==R.id.menu_filter) {
      //      startActivity(new Intent(this, FilterActivity.class));
    } else if (item.getItemId()==R.id.menu_settings) {
      startActivity(new Intent(this, SettingsActivity.class));
    } else {
      return super.onOptionsItemSelected(item);
    }

    return true;
  }
}
