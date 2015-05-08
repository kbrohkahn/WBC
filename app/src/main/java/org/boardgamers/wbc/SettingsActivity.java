package org.boardgamers.wbc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import java.io.FileInputStream;
import java.io.IOException;

public class SettingsActivity extends AppCompatActivity {
  private final String TAG="Settings";

  public static final int GET_FILE_REQUEST_CODE=0;
  public static boolean notifyChanged=false;

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

    // Get intent, action and MIME type
    Intent intent=getIntent();
    String action=intent.getAction();
    String type=intent.getType();

    if (Intent.ACTION_VIEW.equals(action) && type!=null) {
      if ("text/plain".equals(type)) {
        handleSendText(intent); // Handle text being sent
      }
    }
  }

  void handleSendText(Intent intent) {
    Uri data=intent.getData();

    FileInputStream inputStream;
    try {

      inputStream=openFileInput(data.getLastPathSegment());
      StringBuffer fileContent=new StringBuffer("");

      byte[] buffer=new byte[1024];
      int n;
      while ((n=inputStream.read(buffer))!=-1) {
        fileContent.append(new String(buffer, 0, n));
      }

      Log.d(TAG, fileContent.toString());
    } catch (IOException e) {

      e.printStackTrace();

    }

  }

  public static class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      addPreferencesFromResource(R.xml.preferences);

      SharedPreferences sp=getPreferenceManager().getSharedPreferences();

      Preference notify=findPreference(getResources().getString(R.string.pref_key_notify));
      notify.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
          notifyChanged=true;
          return false;
        }
      });

      Preference notifyTime=findPreference(getResources().getString(R.string.pref_key_notify_time));
      notifyTime.setSummary(
          String.valueOf(sp.getInt(getResources().getString(R.string.pref_key_notify_time), 5)));
      notifyTime.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
          notifyChanged=true;
          preference.setSummary(String.valueOf(newValue));
          return false;
        }
      });

      final String[] notifyTypeEntries=
          getResources().getStringArray(R.array.settings_notify_type_entries);

      Preference notifyType=findPreference(getResources().getString(R.string.pref_key_notify_type));
      notifyType.setSummary(notifyTypeEntries[Integer
          .valueOf(sp.getString(getResources().getString(R.string.pref_key_notify_type), "2"))]);
      notifyType.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
          notifyChanged=true;
          preference.setSummary(notifyTypeEntries[Integer.valueOf((String) newValue)]);
          return false;
        }
      });

      Preference loadSchedule=
          findPreference(getResources().getString(R.string.pref_key_schedule_load));
      loadSchedule.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
          Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
          intent.setType("text/plain");
          //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          startActivityForResult(intent, GET_FILE_REQUEST_CODE);
          return true;
        }
      });

    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode==-1) {
      Log.d(TAG, "Bad result");
    } else {
      Log.d(TAG, "Good resuilt");
      if (requestCode==GET_FILE_REQUEST_CODE) {
        handleSendText(data);
      }
    }

    super.onActivityResult(requestCode, resultCode, data);
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
