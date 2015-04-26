package org.boardgamers.wbc;

import android.app.ActionBar;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class SettingsActivity extends PreferenceActivity {
  private final String TAG="Settings";

  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // enable home button for navigation drawer
    final ActionBar ab=getActionBar();
    if (ab!=null) {
      ab.setDisplayHomeAsUpEnabled(true);
      ab.setHomeButtonEnabled(true);
    } else {
      Log.d(TAG, "Could not get action bar");
    }

    PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

    getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment())
        .commit();
  }
  
  public static class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      addPreferencesFromResource(R.xml.preferences);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater=getMenuInflater();
    inflater.inflate(R.menu.close, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id=item.getItemId();

    if (id==R.id.menu_close) {
      finish();
      return true;
    } else if (id==android.R.id.home) {
      finish();
      return true;
    } else {
      return super.onOptionsItemSelected(item);
    }
  }
}
