package org.boardgamers.wbc;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SettingsActivity extends Activity {
  private final String TAG = "Settings";

  private final int TYPE_VIBRATE = 0;
  private final int TYPE_RING = 1;
  private final int TYPE_BOTH = 2;

  private RadioGroup notifyType;
  private NumberPicker notifyTime;
  private CheckBox notifyCB;

  // private Switch currentHour;

  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.settings);

    // enable home button for navigation drawer
    final ActionBar ab = getActionBar();
    if (ab != null) {
      ab.setDisplayHomeAsUpEnabled(true);
      ab.setHomeButtonEnabled(true);
    } else
      Log.d(TAG, "Could not get action bar");


    notifyCB = (CheckBox) findViewById(R.id.settings_notify);
    notifyType = (RadioGroup) findViewById(R.id.settings_notify_type);

    notifyTime = (NumberPicker) findViewById(R.id.settings_notify_time);
    notifyTime.setMaxValue(60);

    Button shareButton = (Button) findViewById(R.id.share);
    shareButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        share();
      }
    });

    // currentHour=(Switch) findViewById(R.id.settings_hour);

    loadSettings();
  }

  public void share() {
    final Resources resources = getResources();

    SharedPreferences sp = getSharedPreferences(
        resources.getString(R.string.sp_file_name),
        Context.MODE_PRIVATE);
    String starPrefString = resources.getString(R.string.sp_event_starred);

    // get starred events string
    String starredEvents = "";

    Event event;
    int i = 0;
    for (; i < MainActivity.dayList.size(); i++) {
      for (int j = 1; j < MainActivity.dayList.get(i).size(); j++) {
        for (int k = 0; k < MainActivity.dayList.get(i).get(j)
            .size(); k++) {
          event = MainActivity.dayList.get(i).get(j).get(k);
          starredEvents += sp.getBoolean(starPrefString
              + event.identifier, false) ? "1" : "0";

        }
      }
    }

    // get user help strings
    String userEventPrefString = resources.getString(R.string.sp_user_event);

    List<String> userEvents = new ArrayList<String>();
    String userEvent;
    for (i = 0; ; i++) {
      userEvent = sp.getString(userEventPrefString + String.valueOf(i), "");

      if (userEvent.equalsIgnoreCase(""))
        break;

      userEvents.add(userEvent);

      starredEvents += sp.getBoolean(
          starPrefString + String.valueOf(MainActivity.NUM_EVENTS + i), false);
    }

    String userName = "";

    Pattern emailPattern = Patterns.EMAIL_ADDRESS;
    Account[] accounts = AccountManager.get(this).getAccounts();
    for (Account account : accounts) {
      if (emailPattern.matcher(account.name).matches()) {
        userName = account.name;
        break;
      }
    }

    String fileName = "wbc2014schedule_" + userName;

    FileOutputStream os = null;


    try {
      os = this.openFileOutput(fileName, Context.MODE_PRIVATE);
    } catch (FileNotFoundException e) {
      Toast.makeText(
          this,
          "ERROR: Could not create output file,"
              + "contact dev@boardgamers.org for help.",
          Toast.LENGTH_LONG).show();
      e.printStackTrace();
    }

    try {
      os.write(userName.getBytes());
      os.write("\n".getBytes());
      for (i = 0; i < userEvents.size(); i++) {
        os.write(userEvents.get(i).getBytes());
      }
      os.write("\n".getBytes());
      os.write(starredEvents.getBytes());

      os.close();
    } catch (IOException e) {
      Toast.makeText(
          this,
          "ERROR: Could not write to output file,"
              + "contact dev@boardgamers.org for help.",
          Toast.LENGTH_LONG).show();
      e.printStackTrace();
    } catch (NullPointerException e) {
      Log.d(TAG, "Error: Could not get bytes from user name");
    }

    // Intent intent = new Intent(Intent.ACTION_SEND);
    // intent.setType("document/*");

  }

  public void loadSettings() {
    SharedPreferences settings = this.getSharedPreferences(getResources()
        .getString(R.string.sp_file_name), Context.MODE_PRIVATE);

    notifyCB.setChecked(settings.getBoolean("notify_starred", false));
    notifyTime.setValue(settings.getInt("notify_time", 5));

    int typeID;
    switch (settings.getInt("notify_type", TYPE_BOTH)) {
      case TYPE_VIBRATE:
        typeID = R.id.settings_notify_vibrate;
        break;
      case TYPE_RING:
        typeID = R.id.settings_notify_ring;
        break;
      case TYPE_BOTH:
        typeID = R.id.settings_notify_both;
        break;
      default:
        typeID = -1;
        break;
    }

    notifyType.check(typeID);

    // currentHour.setActivated(settings.getBoolean("24_hour", true));

  }

  @Override
  public void onPause() {
    final Resources resources = getResources();

    SharedPreferences.Editor editor = this.getSharedPreferences(
        resources.getString(R.string.sp_file_name),
        Context.MODE_PRIVATE).edit();

    editor.putBoolean(resources.getString(R.string.sp_notify_starred),
        notifyCB.isChecked());
    editor.putInt(resources.getString(R.string.sp_notify_time),
        notifyTime.getValue());

    int type;
    switch (notifyType.getCheckedRadioButtonId()) {
      case R.id.settings_notify_vibrate:
        type = TYPE_VIBRATE;
        break;
      case R.id.settings_notify_ring:
        type = TYPE_RING;
        break;
      case R.id.settings_notify_both:
        type = TYPE_BOTH;
        break;
      default:
        type = -1;
        break;
    }

    // editor.putBoolean("24_hour", currentHour.isActivated());
    editor.putInt(resources.getString(R.string.sp_notify_type), type);
    editor.apply();

    // stop service
    Intent intent = new Intent(this, NotificationService.class);
    this.stopService(intent);

    // start service if notifyCB is checked
    if (notifyCB.isChecked()) {
      Log.d("", "service starting");
      this.startService(intent);
    }
    super.onPause();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.close, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_close:
        finish();
        return true;
      case android.R.id.home:
        finish();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }
}
