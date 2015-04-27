package org.boardgamers.wbc;

import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

/**
 * Created by Kevin
 */
public class SearchResultActivity extends Activity {
  private final String TAG="Search Activity";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    final ActionBar ab=getActionBar();
    if (ab!=null) {
      ab.setDisplayHomeAsUpEnabled(true);
      ab.setHomeButtonEnabled(true);
    } else {
      Log.d(TAG, "Could not get action bar");
    }

    // Get the intent, verify the action and get the query
    handleIntent(getIntent());

    setContentView(R.layout.search_results);
  }



  @Override
  protected void onNewIntent(Intent intent) {
    Log.d(TAG, "new intent");
    handleIntent(intent);
  }

  private void handleIntent(Intent intent) {
    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
      String query=intent.getStringExtra(SearchManager.QUERY);
      Log.d(TAG, "Text input: "+query);
      setTitle(query);

    }
  }


  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id=item.getItemId();

    if (id==android.R.id.home) {
      finish();
      return true;
    } else {
      return super.onOptionsItemSelected(item);
    }
  }
}
