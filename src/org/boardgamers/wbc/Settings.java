package org.boardgamers.wbc;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.Toast;

public class Settings extends FragmentActivity {
  private final int TYPE_VIBRATE = 0;
  private final int TYPE_RING = 1;
  private final int TYPE_BOTH = 2;

  private RadioGroup notifyType;
  private NumberPicker notifyTime;
  private CheckBox notifyCB;

  private Button shareButton;

  // private Switch hour;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.settings);

    // load action bar
    final ActionBar ab = getActionBar();
    ab.setHomeButtonEnabled(true);
    ab.setDisplayHomeAsUpEnabled(true);

    getWindow().setSoftInputMode(
        WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

    notifyCB = (CheckBox) findViewById(R.id.settings_notify);
    notifyType = (RadioGroup) findViewById(R.id.settings_notify_type);

    notifyTime = (NumberPicker) findViewById(R.id.settings_notify_time);
    notifyTime.setMaxValue(60);

    shareButton = (Button) findViewById(R.id.share);
    shareButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        share();
      }
    });

    // hour=(Switch) findViewById(R.id.settings_hour);

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
    for (; i < MyApp.dayList.size(); i++) {
      for (int j = 1; j < MyApp.dayList.get(i).size(); j++) {
        for (int k = 0; k < MyApp.dayList.get(i).get(j)
            .size(); k++) {
          event = MyApp.dayList.get(i).get(j).get(k);
          starredEvents += sp.getBoolean(starPrefString
              + event.identifier, false) ? "1" : "0";

        }
      }
    }

    // get user event strings
    String userEventPrefString = resources.getString(R.string.sp_user_event);

    List<String> userEvents = new ArrayList<String>();
    String userEvent;
    for (i = 0; ; i++) {
      userEvent = sp.getString(userEventPrefString + String.valueOf(i), "");

      if (userEvent.equalsIgnoreCase(""))
        break;

      userEvents.add(userEvent);

      starredEvents += sp.getBoolean(
          starPrefString + String.valueOf(MyApp.NUM_EVENTS + i), false);
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
      os = openFileOutput(fileName, Context.MODE_PRIVATE);
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
    }

    Intent intent = new Intent(Intent.ACTION_SEND);
    // intent.setType("document/*");

  }

  public void loadSettings() {
    SharedPreferences settings = getSharedPreferences(getResources()
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

    // hour.setActivated(settings.getBoolean("24_hour", true));

  }

  @Override
  protected void onPause() {
    final Resources resources = getResources();

    SharedPreferences.Editor editor = getSharedPreferences(
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

    // editor.putBoolean("24_hour", hour.isActivated());
    editor.putInt(resources.getString(R.string.sp_notify_type), type);
    editor.commit();

    // stop service
    Intent intent = new Intent(this, NotificationService.class);
    stopService(intent);

    // start service if notifyCB is checked
    if (notifyCB.isChecked()) {
      Log.d("", "service starting");
      startService(intent);
    }
    super.onPause();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        finish();
        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }

}
