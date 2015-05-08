package org.boardgamers.wbc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class SettingsActivity extends AppCompatActivity {
  //private final String TAG="Settings";

  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.settings);

    Toolbar toolbar=(Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    if (getSupportActionBar()!=null) {
      getSupportActionBar().setDisplayShowHomeEnabled(true);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

    getFragmentManager().beginTransaction().replace(R.id.setings_content, new SettingsFragment())
        .commit();
  }

  public static class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      addPreferencesFromResource(R.xml.preferences);

      SharedPreferences sp=getPreferenceManager().getSharedPreferences();

      Preference notifyTime=findPreference(getResources().getString(R.string.pref_key_notify_time));
      notifyTime.setSummary(
          String.valueOf(sp.getInt(getResources().getString(R.string.pref_key_notify_time), 5)));

      final String[] notifyTypeEntries=
          getResources().getStringArray(R.array.settings_notify_type_entries);

      Preference notifyType=findPreference(getResources().getString(R.string.pref_key_notify_type));
      notifyType.setSummary(notifyTypeEntries[Integer
          .valueOf(sp.getString(getResources().getString(R.string.pref_key_notify_type), "2"))]);
      notifyType.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
          preference.setSummary(notifyTypeEntries[Integer.valueOf((String) newValue)]);
          return true;
        }
      });

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

  @Override
  public void onDestroy() {
    Intent intent=new Intent(this, UpdateService.class);

    stopService(intent);
    startService(intent);

    super.onDestroy();
  }
}
