package org.boardgamers.wbc;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {
  private static final String TAG="Settings";

  private static List<User> users;
  private static final int GET_FILE_REQUEST_CODE=0;
  private static boolean overwrite;
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

  public void getFile() {
    Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
    intent.setType("file/*");
    startActivityForResult(intent, GET_FILE_REQUEST_CODE);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode==GET_FILE_REQUEST_CODE) {
      if (resultCode==RESULT_OK) {
        Log.d(TAG, "Good result");
        handleSendText(data);
      } else {
        Log.d(TAG, "Bad result");
      }
    }

    super.onActivityResult(requestCode, resultCode, data);
  }

  public void handleSendText(Intent intent) {
    InputStream inputStream;
    try {
      Log.d(TAG, "Getting content");
      StringBuilder fileContent=new StringBuilder("");

      inputStream=getContentResolver().openInputStream(intent.getData());
      byte[] buffer=new byte[1024];
      int n;
      while ((n=inputStream.read(buffer))!=-1) {
        fileContent.append(new String(buffer, 0, n));
      }

      new SaveScheduleTask(this).execute(fileContent.toString());
    } catch (IOException e) {
      Log.d(TAG, "IO exception");
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

      Log.d(TAG, "INPUT:\n"+data);

      String contentBreak="~~~";
      String delimitter=";;;";

      String[] splitData=data.split(contentBreak);
      String dataCheck=splitData[0];

      if (!dataCheck.substring(0, dataCheck.indexOf(delimitter))
          .equalsIgnoreCase("wbc_data_file")) {
        Toast.makeText(context, "This is not an authentic WBC schedule file!", Toast.LENGTH_SHORT)
            .show();
        return -1;
      }

      String scheduleName=splitData[1];
      String[] createdEvents=splitData[2].split(delimitter);
      String[] starredEvents=splitData[3].split(delimitter);
      String[] eventNotes=splitData[4].split(delimitter);
      String[] tournamentFinishes=splitData[5].split(delimitter);

      WBCDataDbHelper dbHelper=new WBCDataDbHelper(context);
      dbHelper.getWritableDatabase();

      // insert new user
      if (users==null) {
        users=dbHelper.getUsers(null);
      }
      int uId=users.size();
      dbHelper.insertUser(uId, scheduleName, "");
      users.add(new User(uId, scheduleName, ""));

      // insert created events
      String title, location;
      int eId, day, hour;
      double duration;
      int newEId=MainActivity.totalEvents+dbHelper.getNumUserEvents();

      int[] oldIds=new int[createdEvents.length/6];
      int[] newEIds=new int[createdEvents.length/6];

      for (int i=0; i+6<=createdEvents.length; i+=6) {
        eId=Integer.valueOf(createdEvents[i]);
        title=createdEvents[i+1];
        day=Integer.valueOf(createdEvents[i+2]);
        hour=Integer.valueOf(createdEvents[i+3]);
        duration=Double.valueOf(createdEvents[i+4]);
        location=createdEvents[i+5];

        dbHelper.insertUserEvent(newEId, uId, title, day, hour, duration, location);

        oldIds[i/6]=eId;
        newEIds[i/6]=newEId;

        newEId++;
      }

      // insert user event data (notes)
      String note;
      boolean starred;
      for (int i=0; i+2<=eventNotes.length; i+=2) {
        eId=Integer.valueOf(eventNotes[i]);
        note=eventNotes[i+1];

        newEId=-1;
        if (eId>=MainActivity.totalEvents) {
          for (int j=0; j<oldIds.length; j++) {
            if (oldIds[j]==eId) {
              newEId=newEIds[j];
              break;
            }
          }
        } else {
          newEId=eId;
        }

        // check if event is also starred
        starred=false;
        for (int j=0; j<starredEvents.length; j++) {
          if (starredEvents[j].equalsIgnoreCase(eventNotes[i])) {
            starred=true;
            starredEvents[j]="-1";
            break;
          }
        }

        // insert user event data with note and starred
        dbHelper.insertUserEventData(uId, newEId, starred, note);
      }

      // insert user event data (starred)
      for (String eIdString : starredEvents) {
        eId=Integer.valueOf(eIdString);
        newEId=-1;
        if (eId>=MainActivity.totalEvents) {
          for (int i=0; i<oldIds.length; i++) {
            if (oldIds[i]==eId) {
              newEId=newEIds[i];
              break;
            }
          }
        } else {
          newEId=eId;
        }

        // if star added in notes, don't edit event and erase note
        if (newEId>-1) {
          dbHelper.insertUserEventData(uId, newEId, true, "");
        }
      }

      int finish, tId;
      for (int i=0; i+2<=tournamentFinishes.length; i+=2) {
        tId=Integer.valueOf(tournamentFinishes[i]);
        finish=Integer.valueOf(tournamentFinishes[i+1]);

        dbHelper.insertUserTournamentData(uId, tId, finish);

      }

      dbHelper.close();

      Log.d(TAG, "Finish");

      return null;
    }
  }

  public static class SettingsFragment extends PreferenceFragment {
    private static Preference scheduleMerge;
    private static Preference scheduleDelete;
    private static Preference scheduleExport;

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      addPreferencesFromResource(R.xml.preferences);

      WBCDataDbHelper dbHelper=new WBCDataDbHelper(getActivity());
      dbHelper.getReadableDatabase();
      users=dbHelper.getUsers(null);
      dbHelper.close();

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

      Preference scheduleImport=
          findPreference(getResources().getString(R.string.pref_key_schedule_import));
      scheduleImport.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
          ((SettingsActivity) getActivity()).getFile();
          return true;
        }
      });

      DialogPreferenceSchedulePicker scheduleSelect=(DialogPreferenceSchedulePicker) findPreference(
          getResources().getString(R.string.pref_key_schedule_select));
      scheduleSelect.setSummary("Current: "+users.get(MainActivity.userId).name);

      scheduleMerge=findPreference(getResources().getString(R.string.pref_key_schedule_merge));
      scheduleMerge.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
          showMergeScheduleDialog();
          return true;
        }
      });

      scheduleDelete=findPreference(getResources().getString(R.string.pref_key_schedule_delete));
      scheduleDelete.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
          showDeleteScheduleDialog();
          return true;
        }
      });

      scheduleExport=findPreference(getResources().getString(R.string.pref_key_schedule_export));
      scheduleExport.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
          showShareScheduleDialog();
          return true;
        }
      });

      updatePreferences();

    }

    public static void updatePreferences() {
      int newUserId=MainActivity.userId;
      int defaultUserId=MainActivity.PRIMARY_USER_ID;

      if (newUserId==defaultUserId) {
        scheduleMerge.setEnabled(false);
        scheduleDelete.setEnabled(false);
      } else {
        scheduleMerge.setEnabled(true);
        scheduleDelete.setEnabled(true);
      }

      scheduleMerge.setSummary("Merge "+users.get(newUserId).name+" with "+
          users.get(defaultUserId).name+".");
      scheduleDelete.setSummary("Remove "+users.get(newUserId).name+" from schedules");
      scheduleExport.setSummary("Export "+users.get(newUserId).name+" to device storage and share");

    }

    public void showMergeScheduleDialog() {
      String newSchedule=users.get(MainActivity.userId).name;
      String mySchedule=users.get(0).name;

      String[] choices=new String[] {"Overwrite "+mySchedule, "Merge with "+mySchedule};

      AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
      builder.setTitle(getResources().getString(R.string.settings_schedule_merge))
          .setSingleChoiceItems(choices, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              overwrite=which==0;
            }
          }).setMessage("How do you want to merge the schedule "+newSchedule+
          "?").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          ((SettingsActivity) getActivity()).mergeSchedule(MainActivity.userId);
          MainActivity.userId=MainActivity.PRIMARY_USER_ID;
          updatePreferences();
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

    public void showDeleteScheduleDialog() {
      String newSchedule=users.get(MainActivity.userId).name;

      AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
      builder.setTitle(getResources().getString(R.string.settings_schedule_delete))
          .setMessage("Are you sure you want to delete the schedule "+newSchedule+
              "?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          deleteSchedule();
          MainActivity.userId=MainActivity.PRIMARY_USER_ID;
          updatePreferences();
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

    public void deleteSchedule() {
      WBCDataDbHelper dbHelper=new WBCDataDbHelper(getActivity());
      dbHelper.getWritableDatabase();
      dbHelper.deleteUserData(MainActivity.userId);
      dbHelper.close();

    }

    public void showShareScheduleDialog() {
      AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());

      View dialogView=getActivity().getLayoutInflater().inflate(R.layout.dialog_edit_text, null);

      final EditText editText=(EditText) dialogView.findViewById(R.id.dialog_edit_text);
      editText.getText().clear();

      builder.setView(dialogView);

      builder.setTitle(R.string.file_name_title).setMessage(R.string.file_name_message)
          .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              ((SettingsActivity) getActivity()).share(editText.getText().toString());
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

  }

  public void mergeSchedule(int uId) {
    new MergeScheduleTask(this).execute(uId, 0, 0);
  }

  class MergeScheduleTask extends AsyncTask<Integer, Integer, Integer> {
    private Context context;

    public MergeScheduleTask(Context c) {
      context=c;
    }

    @Override
    protected Integer doInBackground(Integer... params) {
      int userId=params[0];

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

  public void share(String fileName) {
    int uId=MainActivity.userId;
    WBCDataDbHelper dbHelper=new WBCDataDbHelper(this);
    dbHelper.getReadableDatabase();
    List<Event> starred=dbHelper.getStarredEvents(uId);
    List<Event> notes=dbHelper.getEventsWithNotes(uId);
    List<Tournament> tFinishes=dbHelper.getTournamentsWithFinishes(uId);
    List<Event> userEvents=dbHelper.getUserEvents(uId, null);
    dbHelper.close();
    Log.d(TAG, "Received from DB");

    String contentBreak="~~~";
    String delimitter=";;;";

    String outputString="wbc_data_file"+delimitter;

    outputString+=contentBreak;
    outputString+=fileName;

    outputString+=contentBreak;
    for (Event event : userEvents) {
      outputString+=String.valueOf(event.id)+delimitter+event.title+delimitter+event.day+delimitter+
          event.hour+delimitter+event.duration+delimitter+event.location+delimitter;
    }
    outputString+=contentBreak;
    for (Event event : starred) {
      outputString+=String.valueOf(event.id)+delimitter;
    }

    outputString+=contentBreak;
    for (Event event : notes) {
      outputString+=String.valueOf(event.id)+delimitter+event.note+delimitter;
    }

    outputString+=contentBreak;
    for (Tournament tournament : tFinishes) {
      outputString+=
          String.valueOf(tournament.id)+delimitter+String.valueOf(tournament.finish)+delimitter;
    }

    File file;
    fileName=fileName+".wbc.txt";
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
