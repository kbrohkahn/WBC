package org.boardgamers.wbc;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by Kevin
 */
public class SearchResult extends Activity {
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

    setContentView(R.layout.search_results);
  }
}
