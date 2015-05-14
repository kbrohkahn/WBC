package org.boardgamers.wbc;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

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

  public static final int PRIMARY_USER_ID=0;
  public static final int TOTAL_DAYS=9;
  public static final int USER_EVENT_ID=5000;
  public static int selectedEventId=-1;
  public static long currentDay;
  public static int currentHour;
  public static int userId;

  public static boolean updatingFragments=false;
  public static boolean differentUser=false;

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
    Log.d(TAG, "Init");

    setContentView(R.layout.main_layout);

    pagerAdapter=new TabsPagerAdapter(getSupportFragmentManager());

    viewPager=(ViewPager) findViewById(R.id.pager);
    viewPager.setAdapter(pagerAdapter);
    viewPager.setOffscreenPageLimit(2);

    Toolbar toolbar=(Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    SlidingTabLayout tabs=(SlidingTabLayout) findViewById(R.id.sliding_layout);
    tabs.setViewPager(viewPager);
    tabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

      @Override
      public void onPageSelected(int position) {
        pagerAdapter.getItem(position).refreshAdapter();
      }

      @Override
      public void onPageScrollStateChanged(int state) {}
    });

    SharedPreferences sp=getSharedPreferences(getResources().getString(R.string.user_data_file),
        Context.MODE_PRIVATE);
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

    loadUserData();
  }

  public void loadUserData() {
    Log.d(TAG, "Loading");
    userId=PreferenceManager.getDefaultSharedPreferences(this)
        .getInt(getResources().getString(R.string.pref_key_schedule_select), PRIMARY_USER_ID);

    WBCDataDbHelper dbHelper=new WBCDataDbHelper(this);
    dbHelper.getReadableDatabase();
    int starredEvents=dbHelper.getStarredEvents(userId).size();
    String scheduleName=dbHelper.getUser(userId).name;
    setTitle("WBC: "+scheduleName);
    dbHelper.close();

    if (starredEvents==0) {
      viewPager.setCurrentItem(1);
    }
    Log.d(TAG, "Loading complete");
  }

  public static void update() {
    pagerAdapter.getItem(viewPager.getCurrentItem()).refreshAdapter();
  }

  @Override
  protected void onResume() {
    if (differentUser) {
      differentUser=false;

      recreate();
      // TODO better implementation
      //loadUserData();
      //pagerAdapter=new TabsPagerAdapter(getSupportFragmentManager());
    } else {
      update();
    }
    super.onResume();
  }

  public static void removeEvents(List<Event> events) {
    for (Event event : events) {
      event.starred=false;
    }

    pagerAdapter.getItem(1).removeEvents(events);

    for (Event event : events) {
      pagerAdapter.getItem(0).updateEvent(event);
    }
  }

  public static void updateUserData(long eventId, String note, long tournamentId, int finish) {
    ((UserDataListFragment) pagerAdapter.getItem(2))
        .updateUserData(eventId, note, tournamentId, finish);
  }

  public static void changeEvents(Context context, Event[] events, int id) {
    new ChangeEventTask(context, id).execute(events);
  }

  static class ChangeEventTask extends AsyncTask<Event, Integer, Integer> {
    final Context context;
    final int currentPage;

    public ChangeEventTask(Context c, int i) {
      context=c;
      currentPage=i;
    }

    @Override
    protected void onPostExecute(Integer integer) {
      // refresh current fragment's adapter if changing star from event or search
      updatingFragments=false;
      if (SearchResultActivity.progressBar!=null) {
        SearchResultActivity.progressBar.setVisibility(View.GONE);
      }

      if (currentPage==3) {
        pagerAdapter.getItem(2).refreshAdapter();
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

        dbHelper.insertUserEventData(userId, event.id, event.starred, event.note);

        for (int j=0; j<3; j++) {
          if (j!=currentPage) {
            pagerAdapter.getItem(j).updateEvent(event);
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

  public void openShareDialog() {
    AlertDialog.Builder builder=new AlertDialog.Builder(this);

    View dialogView=getLayoutInflater().inflate(R.layout.dialog_edit_text, null);

    final EditText editText=(EditText) dialogView.findViewById(R.id.dialog_edit_text);
    editText.getText().clear();

    builder.setView(dialogView);

    builder.setTitle(R.string.file_name_title).setMessage(R.string.file_name_message)
        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            share(editText.getText().toString());
            dialog.dismiss();
          }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
      }
    });
    builder.create().show();
  }

  public void share(String scheduleName) {
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

    String outputString="wbc_data_file"+delimitter;

    outputString+=contentBreak;
    outputString+=scheduleName;

    outputString+=contentBreak+delimitter;
    for (Event event : userEvents) {
      outputString+=String.valueOf(event.id)+delimitter+event.title+delimitter+event.day+delimitter+
          event.hour+delimitter+event.duration+delimitter+event.location+delimitter;
    }
    outputString+=contentBreak+delimitter;
    for (Event event : starred) {
      outputString+=String.valueOf(event.id)+delimitter;
    }

    outputString+=contentBreak+delimitter;
    for (Event event : notes) {
      outputString+=String.valueOf(event.id)+delimitter+event.note+delimitter;
    }

    outputString+=contentBreak+delimitter;
    for (Event event : eFinishes) {
      outputString+=String.valueOf(event.tournamentID)+delimitter+event.note+delimitter;
    }

    File file;
    String fileName="Schedule.wbc.txt";
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

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId()==R.id.menu_map) {
      startActivity(new Intent(this, MapActivity.class));
    } else if (item.getItemId()==R.id.menu_share) {
      openShareDialog();
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
