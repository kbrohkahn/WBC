package org.boardgamers.wbc;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class NotificationService extends Service {
  private final static String TAG = "Notification Service";

  private static int notificationType;
  private static int notificationTime;

  private final Timer timer = new Timer();

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    SharedPreferences settings = getSharedPreferences(
        getResources().getString(R.string.sp_file_name), MODE_PRIVATE);

    if (!settings.getBoolean(
        getResources().getString(R.string.sp_notify_starred), false)) {
      this.stopSelf();
      return super.onStartCommand(intent, flags, startId);
    }

    notificationTime = settings.getInt(getResources().getString(R.string.sp_notify_time), 5);
    notificationType = settings.getInt(getResources().getString(R.string.sp_notify_type), 2);

    // TODO set first execution for the first day
    Calendar c = Calendar.getInstance();
    c.set(Calendar.YEAR, 2015);
    c.set(Calendar.MONTH, Calendar.AUGUST);
    c.set(Calendar.DAY_OF_MONTH, 2);
    c.set(Calendar.HOUR_OF_DAY, 6);
    c.set(Calendar.MINUTE, 60 - notificationTime);
    c.set(Calendar.SECOND, 0);

    Log.d(TAG, "First exec at " + c.getTime().toString());

    timer.scheduleAtFixedRate(new NotifyEvents(), c.getTime(), 60 * 60 * 1000);

    return super.onStartCommand(intent, flags, startId);
  }

  private void sendNotification(String s) {
    int TYPE_VIBRATE = 0;
    int TYPE_RING = 1;
    int TYPE_BOTH = 2;

    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
        this);
    mBuilder.setContentTitle("WBC Events starting")
        .setContentText(s + " in " + String.valueOf(notificationTime) + " minutes!")
        .setSmallIcon(R.drawable.ic_notification).setAutoCancel(true);

    if (notificationType == TYPE_VIBRATE)
      mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
    else if (notificationType == TYPE_RING)
      mBuilder.setDefaults(Notification.DEFAULT_SOUND);
    else if (notificationType == TYPE_BOTH)
      mBuilder.setDefaults(Notification.DEFAULT_ALL);

    // Creates an explicit intent for an Activity in your app
    Intent resultIntent = new Intent(this, SummaryFragment.class);
    TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
    stackBuilder.addParentStack(SummaryFragment.class);
    stackBuilder.addNextIntent(resultIntent);

    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
        PendingIntent.FLAG_UPDATE_CURRENT);
    mBuilder.setContentIntent(resultPendingIntent);
    NotificationManager mNotificationManager = (NotificationManager) this
        .getSystemService(Context.NOTIFICATION_SERVICE);
    // mId allows you to update the notification later on.
    mNotificationManager.notify(0, mBuilder.build());

    Log.d(TAG, "Notifying");
  }

  private void checkEvents() {
    String[] daysForParsing = this.getResources()
        .getStringArray(R.array.daysForParsing);

    Calendar c = Calendar.getInstance();
    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd",
        Locale.US);
    String dateString = dateFormatter.format(c.getTime());

    // get currentDay
    int currentDay = -1;
    for (int i = 0; i < daysForParsing.length; i++) {
      if (daysForParsing[i].equalsIgnoreCase(dateString)) {
        currentDay = i;
        break;
      }
    }

    // return if
    if (currentDay == -1)
      return;

    // get currentHour
    int currentHour = c.get(Calendar.HOUR_OF_DAY);

    // load preferences for user events and starred events
    SharedPreferences sp = getSharedPreferences(
        getResources().getString(R.string.sp_file_name),
        Context.MODE_PRIVATE);
    String userEventPrefString = getResources().getString(R.string.sp_user_event);
    String starPrefString = getResources().getString(R.string.sp_event_starred);

    String eventsString = "";

    /***** CHECK USER EVENTS *******/
    String identifier, row, eventTitle, tempString;
    String[] rowData;
    int index, day, hour;
    for (index = 0; ; index++) {
      row = sp.getString(userEventPrefString + String.valueOf(index), "");
      if (row.equalsIgnoreCase(""))
        break;

      rowData = row.split("~");

      day = Integer.valueOf(rowData[0]);
      hour = Integer.valueOf(rowData[1]);
      eventTitle = rowData[2];

      identifier = String.valueOf(day * 24 + hour) + "_" + eventTitle;
      // if help is starred, add to notification string
      if (sp.getBoolean(starPrefString + identifier, false) &&
          currentDay * 24 + currentHour + 1 == day * 24 + currentHour)
        eventsString += eventTitle + ", ";
    }

    /***** CHECK ALL EVENTS *******/

    // load schedule file
    InputStream is;
    try {
      is = getAssets().open("schedule2014.txt");
    } catch (IOException e2) {
      Toast.makeText(this, "ERROR: Could not find schedule file, " +
          "contact dev@boardgamers.org for help.", Toast.LENGTH_SHORT).show();
      e2.printStackTrace();
      return;
    }

    // read schedule file
    InputStreamReader isr;
    try {
      isr = new InputStreamReader(is);
    } catch (IllegalStateException e1) {
      Toast.makeText(this, "ERROR: Could not open schedule file " +
          "contact dev@boardgamers.org for help.", Toast.LENGTH_SHORT).show();
      e1.printStackTrace();
      return;
    }

    // parse schedule file
    BufferedReader reader = new BufferedReader(isr);
    String line;
    try {
      while ((line = reader.readLine()) != null) {
        rowData = line.split("~");

        // currentDay
        tempString = rowData[0];
        for (index = 0; index < daysForParsing.length; index++) {
          if (daysForParsing[index].equalsIgnoreCase(tempString))
            break;
        }
        if (index == -1) {
          Log.d(TAG, "Unknown date: " + rowData[2] + " in " + line);
          index = 0;
        }
        day = index;

        // title
        eventTitle = rowData[2];

        // time
        tempString = rowData[1];
        if (rowData[1].contains(":30")) {
          Log.d(TAG, rowData[2] + " starts at half past");
          tempString = tempString.substring(0, tempString.length() - 3);
        }

        hour = Integer.valueOf(tempString);

        identifier = String.valueOf(day * 24 + hour) + "_" + eventTitle;
        // if help is starred, add to notification string
        if (sp.getBoolean(starPrefString + identifier, false) &&
            currentDay * 24 + currentHour + 1 == day * 24 + currentHour)
          eventsString += eventTitle + ", ";

      }
    } catch (IOException e) {
      Toast.makeText(this, "ERROR: Could not read schedule file " +
          "contact dev@boardgamers.org for help.", Toast.LENGTH_SHORT).show();
    }

    if (eventsString.length() > 0) {
      eventsString = eventsString.substring(0, eventsString.length() - 2);

      Log.d(TAG, "Events now: " + eventsString);
      sendNotification(eventsString);
    }
  }

  @Override
  public void onDestroy() {
    timer.cancel();
    super.onDestroy();
  }

  @Override
  public IBinder onBind(Intent arg0) {
    return null;
  }

  private class NotifyEvents extends TimerTask {
    @Override
    public void run() {
      Log.d(TAG, "Checking events");
      checkEvents();
    }
  }
}
