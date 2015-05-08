package org.boardgamers.wbc;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.BaseColumns;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Main Activity class
 */
public class MainActivity extends AppCompatActivity {
  private final static String TAG="Main Activity";
  private final String FILENAME="wbcData.txt";

  public static final int TOTAL_DAYS=9;
  public static long SELECTED_EVENT_ID=-1;
  public static long TOTAL_EVENTS;
  public static long currentDay;
  public static int currentHour;
  public static boolean updatingFragments=false;

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
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.d(TAG, "Check 1");

    WBCDataDbHelper dbHelper=new WBCDataDbHelper(this);
    dbHelper.getReadableDatabase();
    TOTAL_EVENTS=dbHelper.getNumEvents();
    int starredEvents=dbHelper.getStarredEvents().size();
    dbHelper.close();

    Log.d(TAG, "Check 2");

    setContentView(R.layout.main_layout);

    Log.d(TAG, "Check 3");

    pagerAdapter=new TabsPagerAdapter(getSupportFragmentManager());

    Log.d(TAG, "Check 4");

    viewPager=(ViewPager) findViewById(R.id.pager);
    viewPager.setAdapter(pagerAdapter);
    viewPager.setOffscreenPageLimit(2);

    Toolbar toolbar=(Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    SlidingTabLayout tabs=(SlidingTabLayout) findViewById(R.id.sliding_layout);
    tabs.setViewPager(viewPager);
    tabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

      }

      @Override
      public void onPageSelected(int position) {
        pagerAdapter.getItem(position).refreshAdapter();
      }

      @Override
      public void onPageScrollStateChanged(int state) {

      }
    });

    Log.d(TAG, "Check 5");

    SharedPreferences sp=
        getSharedPreferences(getResources().getString(R.string.sp_file_name), Context.MODE_PRIVATE);
    int latestDialogVersion=13;
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

    if (starredEvents==0) {
      viewPager.setCurrentItem(1);
    }

    Log.d(TAG, "Check 6");
  }

  public static void update() {
    pagerAdapter.getItem(viewPager.getCurrentItem()).refreshAdapter();
  }

  @Override
  protected void onResume() {
    update();
    super.onResume();
  }

  public static void updateUserData(long eventId, String note, long tournamentId, int finish) {
    ((UserDataListFragment) pagerAdapter.getItem(2))
        .updateUserData(eventId, note, tournamentId, finish);
  }

  public static void changeEventStar(Context context, Event[] events, int id) {
    new ChangeStarTask(context, id).execute(events);
  }

  final static class ChangeStarTask extends AsyncTask<Event, Integer, Integer> {
    final Context context;
    final int id;

    public ChangeStarTask(Context c, int i) {
      context=c;
      id=i;
    }

    @Override
    protected void onPostExecute(Integer integer) {
      // refresh current fragment's adapter if changing star from event or search
      updatingFragments=false;
      if (SearchResultActivity.progressBar!=null) {
        SearchResultActivity.progressBar.setVisibility(View.GONE);
      }

      super.onPostExecute(integer);
    }

    @Override
    protected Integer doInBackground(Event... events) {
      updatingFragments=true;

      WBCDataDbHelper dbHelper=new WBCDataDbHelper(context);
      dbHelper.getWritableDatabase();

      Event event;
      for (int i=0; i<events.length; i++) {
        event=events[i];

        if (SearchResultActivity.progressBar!=null) {
          SearchResultActivity.progressBar.setProgress(i);
        }

        Log.d(TAG, "Changing event star for: "+event.title);

        dbHelper.updateEventStarred(event.id, event.starred);

        for (int j=0; j<3; j++) {
          if (j!=id) {
            pagerAdapter.getItem(j).updateStarredEvent(event);
          }
        }
      }
      dbHelper.close();
      Log.d(TAG, "Event stars changed");
      return 1;

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
    Log.d(TAG, "Share start");
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
    List<Event> user=new ArrayList<>();
    for (List<Event> events : starredGroups) {
      for (Event event : events) {
        starred.add(event);
      }
    }
    Log.d(TAG, "Received from fragments");

    String delimitter="~";
    String sameObject=";";

    String outputString="wbc_data_file"+delimitter;

    outputString+="\n"+delimitter;
    for (Event event : userEvents) {
      outputString+=String.valueOf(event.id)+sameObject+event.title+sameObject+event.day+sameObject+
          event.hour+sameObject+event.duration+sameObject+event.location+delimitter;
    }
    outputString+="\n"+delimitter;
    for (Event event : starred) {
      outputString+=String.valueOf(event.id)+delimitter;
    }
    outputString+="\n"+delimitter;
    for (Event event : eFinishes) {
      outputString+=String.valueOf(event.tournamentID)+sameObject+event.note+delimitter;
    }
    outputString+="\n"+delimitter;
    for (Event event : notes) {
      outputString+=String.valueOf(event.id)+sameObject+event.note+delimitter;
    }

    File file;
    if (isExternalStorageWritable()) {
      File sdCard=Environment.getExternalStorageDirectory();
      File dir=new File(sdCard.getAbsolutePath()+"/WBC/");
      dir.mkdirs();
      file=new File(dir, FILENAME);
    } else {
      file=new File(getCacheDir(), FILENAME);
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
    shareIntent.setType("text/plain");
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

  /* Checks if external storage is available to at least read */
  public boolean isExternalStorageReadable() {
    String state=Environment.getExternalStorageState();
    if (state.equals(Environment.MEDIA_MOUNTED) ||
        state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
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
