package org.boardgamers.wbc;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.List;

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

  private ViewPager viewPager;
  private TabsPagerAdapter pagerAdapter;

  private String[] tabTitles={"Starred", "Schedule", "My Data"};

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

    WBCDataDbHelper dbHelper=new WBCDataDbHelper(this);
    TOTAL_EVENTS=dbHelper.getNumEvents();

    if (TOTAL_EVENTS>0) {
      databaseLoaded();
    } else {
      Intent intent=new Intent(this, SplashScreen.class);
      startActivity(intent);
    }
  }

  @Override
  protected void onResume() {
    if (TOTAL_EVENTS==0) {
      databaseLoaded();
    }
    super.onResume();
  }

  public void databaseLoaded() {
    setContentView(R.layout.main_layout);

    viewPager=(ViewPager) findViewById(R.id.pager);
    pagerAdapter=new TabsPagerAdapter(getSupportFragmentManager());

    viewPager.setAdapter(pagerAdapter);
    viewPager.setOffscreenPageLimit(3);

    WBCDataDbHelper dbHelper=new WBCDataDbHelper(this);
    TOTAL_EVENTS=dbHelper.getNumEvents();

    Toolbar toolbar=(Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    SlidingTabLayout tabs=(SlidingTabLayout) findViewById(R.id.sliding_layout);
    tabs.setViewPager(viewPager);

    Log.d(TAG, "viewpager loaded");

    // SHOW INITIAL DIALOG
    SharedPreferences sp=
        getSharedPreferences(getResources().getString(R.string.sp_file_name), Context.MODE_PRIVATE);
    int version=sp.getInt("last_app_version", 0);
    int latestDialogVersion=13;
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

    // SHOW CHANGES DIALOG
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

  public void changeEventStar(Event event) {
    Log.d(TAG, "Changing event star");

    final int GROUPS_PER_DAY=18+1;

    WBCDataDbHelper dbHelper=new WBCDataDbHelper(this);
    dbHelper.updateEventStarred(event.id, event.starred);
    dbHelper.close();

    List<Event> searchList;
    Event tempEvent;

    // add or remove from summary
    DefaultListFragment summaryListFragment=pagerAdapter.getItem(0);
    searchList=summaryListFragment.listAdapter.events.get(event.day);
    if (event.starred) {
      int index;
      for (index=0; index<searchList.size(); index++) {
        tempEvent=searchList.get(index);
        if (event.hour<tempEvent.hour ||
            (event.hour==tempEvent.hour && event.title.compareToIgnoreCase(tempEvent.title)==1)) {
          break;
        }
      }
      searchList.add(index, event);
    } else {
      for (int i=0; i<searchList.size(); i++) {
        tempEvent=searchList.get(i);
        if (tempEvent.id==event.id) {
          searchList.remove(tempEvent);
          break;
        }
      }
    }

    // change star in full schedule
    DefaultListFragment scheduleListFragment=pagerAdapter.getItem(1);
    searchList=scheduleListFragment.listAdapter.events.get(event.day*GROUPS_PER_DAY+event.hour-6);
    for (int i=0; i<searchList.size(); i++) {
      tempEvent=searchList.get(i);
      if (tempEvent.id==event.id) {
        tempEvent.starred=event.starred;
        break;
      }
    }

    // add or remove from my events in full schedule
    searchList=scheduleListFragment.listAdapter.events.get(event.day*GROUPS_PER_DAY);
    if (event.starred) {
      int index;
      for (index=0; index<searchList.size(); index++) {
        tempEvent=searchList.get(index);
        if (event.hour<tempEvent.hour ||
            (event.hour==tempEvent.hour && event.title.compareToIgnoreCase(tempEvent.title)==1)) {
          break;
        }
      }
      searchList.add(index, event);
    } else {
      for (int i=0; i<searchList.size(); i++) {
        tempEvent=searchList.get(i);
        if (tempEvent.id==event.id) {
          searchList.remove(tempEvent);
          break;
        }
      }
    }

    // change star in created events
    if (event.tournamentID==-1) {
      DefaultListFragment userDataListFragment=pagerAdapter.getItem(2);
      searchList=userDataListFragment.listAdapter.events.get(0);
      for (int i=0; i<searchList.size(); i++) {
        tempEvent=searchList.get(i);
        if (tempEvent.id==event.id) {
          tempEvent.starred=event.starred;
          break;
        }
      }
    }

    updateFragment(-1);

    Log.d(TAG, "Event star changed");
  }

  public void updateFragment(int position) {
    if (position==-1) {
      position=viewPager.getCurrentItem();
    }

    DefaultListFragment fragment=pagerAdapter.getItem(position);
    if (fragment!=null && fragment.isAdded()) {
      fragment.listAdapter.updateList();
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
    inflater.inflate(R.menu.menu_main, menu);

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
