package org.boardgamers.wbc;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

/**
 * Created by Kevin
 */
public class SearchResultActivity extends AppCompatActivity {
  //private final String TAG="Search Activity";

  public static boolean fromEventActivity=false;

  public static ProgressBar progressBar;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.search_results);

    progressBar=(ProgressBar) findViewById(R.id.search_progress_bar);

    Toolbar toolbar=(Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    if (getSupportActionBar()!=null) {
      getSupportActionBar().setDisplayShowHomeEnabled(true);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    handleIntent(getIntent());
  }

  @Override
  protected void onResume() {
    super.onResume();

    if (fromEventActivity) {
      fromEventActivity=false;
      WBCDataDbHelper dbHelper=new WBCDataDbHelper(this);
      dbHelper.getReadableDatabase();
      Event event=dbHelper.getEvent(MainActivity.SELECTED_EVENT_ID);
      dbHelper.close();

      SearchListFragment fragment=
          (SearchListFragment) getSupportFragmentManager().findFragmentById(R.id.searchFragment);
      fragment.changeEventStar(event);
    }
  }

  @Override
  protected void onNewIntent(Intent intent) {
    handleIntent(intent);
  }

  private void handleIntent(Intent intent) {
    String query=intent.getStringExtra("query_title");
    int id=intent.getIntExtra("query_id", -1);

    if (intent.getAction()!=null && intent.getAction().equalsIgnoreCase(Intent.ACTION_SEARCH)) {
      query=intent.getStringExtra(SearchManager.QUERY).toLowerCase();
    }

    if (query!=null || id>-1) {
      setTitle(query);
      SearchListFragment fragment=
          (SearchListFragment) getSupportFragmentManager().findFragmentById(R.id.searchFragment);
      fragment.loadEvents(query, id);
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

  @Override
  public void finish() {
    if (MainActivity.updatingFragments) {
      Toast.makeText(this, "Updating lists, please wait", Toast.LENGTH_SHORT).show();
    } else {
      super.finish();
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId()==android.R.id.home) {
      finish();
      return true;
    } else if (item.getItemId()==R.id.menu_map) {
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
