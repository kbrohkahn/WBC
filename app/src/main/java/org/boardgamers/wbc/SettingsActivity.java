package org.boardgamers.wbc;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {
  private static final String TAG="Settings";

  private static final int GET_FILE_REQUEST_CODE=0;
  private static boolean overwrite;
  private static boolean notifyChanged=false;

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

  public void getFile() {
    Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
    intent.setType("file/*");
    startActivityForResult(intent, GET_FILE_REQUEST_CODE);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode==GET_FILE_REQUEST_CODE) {
      if (resultCode==RESULT_OK) {
        handleSendText(data);
      } else {
        Log.d(TAG, "Bad result");
      }
    }

    super.onActivityResult(requestCode, resultCode, data);
  }

  public void handleSendText(Intent intent) {
    Uri data=intent.getData();

    FileInputStream inputStream;
    try {
      inputStream=openFileInput(data.getLastPathSegment());
      StringBuilder fileContent=new StringBuilder("");

      byte[] buffer=new byte[1024];
      int n;
      while ((n=inputStream.read(buffer))!=-1) {
        fileContent.append(new String(buffer, 0, n));
      }

      if (fileContent.toString().indexOf("wbc_data_file")==0) {
        new SaveScheduleTask(this).execute(fileContent.toString());
      } else {
        Toast.makeText(this, "This is not an authentic WBC schedule file!", Toast.LENGTH_SHORT)
            .show();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  class SaveScheduleTask extends AsyncTask<String, Integer, Integer> {
    private Context context;

    public SaveScheduleTask(Context c) {
      context=c;
    }

    @Override
    protected Integer doInBackground(String... params) {
      String data=params[0];

      String contentBreak="~~~";
      String delimitter=";;;";

      String[] splitData=data.split(contentBreak);

      // events
      String[] eventData=splitData[1].split(delimitter);

      WBCDataDbHelper dbHelper=new WBCDataDbHelper(context);
      dbHelper.getWritableDatabase();

      dbHelper.close();

      return null;
    }
  }

  public static class SettingsFragment extends PreferenceFragment {
    private List<String[]> users;

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
          return true;
        }
      });

      Preference notifyTime=findPreference(getResources().getString(R.string.pref_key_notify_time));
      notifyTime.setSummary(
          String.valueOf(sp.getInt(getResources().getString(R.string.pref_key_notify_time), 5)));
      notifyTime.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
          notifyChanged=true;
          return true;
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
          return true;
        }
      });

      Preference loadSchedule=
          findPreference(getResources().getString(R.string.pref_key_schedule_load));
      loadSchedule.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
          ((SettingsActivity) getActivity()).getFile();
          return true;
        }
      });

      WBCDataDbHelper dbHelper=new WBCDataDbHelper(getActivity());
      dbHelper.getReadableDatabase();
      users=dbHelper.getUsers(null);
      dbHelper.close();

      Preference selectSchedule=
          findPreference(getResources().getString(R.string.pref_key_schedule_select));
      selectSchedule.setSummary("Current: "+users.get(MainActivity.userId)[1]);
      selectSchedule.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
          MainActivity.userId=(int) newValue;
          preference.setSummary(users.get(MainActivity.userId)[1]);
          return true;
        }
      });

      Preference saveSchedule=
          findPreference(getResources().getString(R.string.pref_key_schedule_save));
      saveSchedule.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
          showSaveScheduleDialog();
          return true;
        }
      });

    }

    public void showSaveScheduleDialog() {
      String newSchedule=users.get(MainActivity.userId)[1];
      String mySchedule=users.get(0)[1];

      String[] choices=new String[] {"Overwrite "+mySchedule, "Merge with "+mySchedule};

      AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
      builder.setTitle(getResources().getString(R.string.settings_schedule_save))
          .setSingleChoiceItems(choices, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              overwrite=which==0;
            }
          }).setMessage("How do you want to merge the schedule "+newSchedule+
          "?").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          ((SettingsActivity) getActivity()).mergeSchedule();
          dialog.dismiss();
        }
      }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          dialog.dismiss();
        }
      });
      builder.create().show();
    }
  }

  public void mergeSchedule() {
    new MergeScheduleTask(this).execute(0, 0, 0);
  }

  class MergeScheduleTask extends AsyncTask<Integer, Integer, Integer> {
    private Context context;

    public MergeScheduleTask(Context c) {
      context=c;
    }

    @Override
    protected Integer doInBackground(Integer... params) {
      int userId=MainActivity.userId;

      WBCDataDbHelper dbHelper=new WBCDataDbHelper(context);
      dbHelper.getWritableDatabase();
      if (overwrite) {
        dbHelper.deleteUserData(userId);
      }
      dbHelper.mergeUserData(userId);
      dbHelper.close();

      return null;
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater=getMenuInflater();
    inflater.inflate(R.menu.menu_light_settings, menu);

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id=item.getItemId();

    if (id==android.R.id.home) {
      finish();
      return true;
    } else if (id==R.id.menu_reset) {
      showResetDialog();
      return true;
    } else {
      return super.onOptionsItemSelected(item);
    }
  }

  public void showResetDialog() {
    AlertDialog.Builder builder=new AlertDialog.Builder(this);
    builder.setTitle(R.string.reset).setMessage(R.string.reset_message)
        .setPositiveButton("Yes, reset", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            resetPrefs();
          }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
      }
    });

    builder.create().show();
  }

  public void resetPrefs() {
    PreferenceManager.getDefaultSharedPreferences(this).edit().clear().commit();
    recreate();
  }

  @Override
  public void onDestroy() {
    if (notifyChanged) {
      Intent intent=new Intent(this, UpdateService.class);
      stopService(intent);
      startService(intent);
    }

    super.onDestroy();
  }
}
