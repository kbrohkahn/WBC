package org.boardgamers.wbc;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

/**
 * Main Activity class
 */
public class MainActivity extends AppCompatActivity {
  private final static String TAG="Main Activity";

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
    viewPager.setOffscreenPageLimit(3);

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

  @Override
  protected void onResume() {
    pagerAdapter.getItem(viewPager.getCurrentItem()).refreshAdapter();

    super.onResume();
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
    SearchView searchView=(SearchView) menu.findItem(R.id.menu_search).getActionView();
    searchView.setSearchableInfo(
        searchManager.getSearchableInfo(new ComponentName(this, SearchResultActivity.class)));
    searchView.setIconifiedByDefault(false);
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
