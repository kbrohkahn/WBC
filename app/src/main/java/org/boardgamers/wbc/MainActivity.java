package org.boardgamers.wbc;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Main Activity class
 */
public class MainActivity extends FragmentActivity implements ActionBar.TabListener {
  private final static String TAG="Main Activity";

  public static final int HOURS_PER_DAY=18;
  public static final int GROUPS_PER_DAY=HOURS_PER_DAY+1;
  public static final int TOTAL_DAYS=9;

  public static String SELECTED_EVENT_ID="";
  public static int NUM_EVENTS;

  private static int currentDay;
  private static int currentHour;

  private static ViewPager viewPager;
  private static TabsPagerAdapter pagerAdapter;

  private Handler handler=new Handler();

  // Tab titles
  private String[] tabs={"Starred", "Schedule", "My Data"};

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.main_layout);

    viewPager=(ViewPager) findViewById(R.id.pager);
    pagerAdapter=new TabsPagerAdapter(getFragmentManager());
    viewPager.setAdapter(pagerAdapter);

    NUM_EVENTS=getIntent().getIntExtra("totalEvents", -1);

    final ActionBar actionBar=getActionBar();
    if (actionBar!=null) {
      actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
      for (String tabString : tabs) {
        actionBar.addTab(actionBar.newTab().setText(tabString).setTabListener(this));
      }
    } else {
      Log.d(TAG, "Error: could not get action bar");
    }

    viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

      @Override
      public void onPageSelected(int position) {
        if (actionBar!=null) {
          actionBar.setSelectedNavigationItem(position);
        } else {
          Log.d(TAG, "Error: could not get action bar");
        }
        updateFragment(position);
      }

      @Override
      public void onPageScrolled(int arg0, float arg1, int arg2) {
      }

      @Override
      public void onPageScrollStateChanged(int arg0) {
      }
    });

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

    // SET TIME AND START RUNNABLE
    Calendar calendar=Calendar.getInstance();
    currentHour=calendar.get(Calendar.HOUR_OF_DAY);

    SimpleDateFormat dateFormatter=new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    String dateString=dateFormatter.format(calendar.getTime());

    currentDay=-1;
    String[] daysForParsing=getResources().getStringArray(R.array.daysForParsing);
    for (int i=0; i<daysForParsing.length; i++) {
      if (daysForParsing[i].equalsIgnoreCase(dateString)) {
        currentDay=i;
        break;
      }
    }

    // TODO TESTING: set currentDay to day of week
    currentDay=(calendar.get(Calendar.DAY_OF_WEEK));

    if (currentDay>-1) {
      handler.postAtTime(runnable, calendar.getTimeInMillis()/60*60*1000+60*60*1000);
    }

    Log.d(TAG, "onCreate finished");
  }

  public void updateFragment(int position) {
    if (position==-1) {
      position=viewPager.getCurrentItem();
    }

    DefaultListFragment fragment=(DefaultListFragment) pagerAdapter.getItem(position);
    if (fragment!=null && fragment.isAdded()) {
      fragment.updateList();
    }
  }

  @Override
  public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
  }

  @Override
  public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
    int position=tab.getPosition();

    viewPager.setCurrentItem(position);

    DefaultListFragment fragment=(DefaultListFragment) pagerAdapter.getItem(position);
    if (fragment!=null && fragment.isAdded()) {
      fragment.updateList();
    }
  }

  @Override
  public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
  }

  /**
   * Update currentHour (and currentDay), run every hour when app is running
   */
  private final Runnable runnable=new Runnable() {
    @Override
    public void run() {
      handler.postDelayed(this, 60*60*1000);

      currentHour++;
      if (currentHour==24) {
        currentHour=0;
        currentDay++;
        if (currentDay==9) {
          currentDay=0;
        }
      }
    }
  };

  /**
   * Get hours elapsed since midnight on the first day
   *
   * @return hours elapsed since midnight of the first day
   */
  public static int getHoursIntoConvention() {
    if (currentDay==-1) {
      return currentDay;
    } else {
      return currentDay*24+currentHour;
    }
  }

  /**
   * Get the current group, based on currentDay and currentHour. If hour is between 24
   * and 7, select group 0 of that day
   *
   * @return groupNumber
   */
  public static int getCurrentGroup() {
    if (currentDay==-1) {
      return 0;
    } else {
      return currentDay*GROUPS_PER_DAY+Math.max(0, currentHour-6);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater=getMenuInflater();
    inflater.inflate(R.menu.main, menu);

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
