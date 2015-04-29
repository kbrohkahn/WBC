package org.boardgamers.wbc;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class NotificationService extends Service {
  private final static String TAG="Notification Service";

  private static int notificationType;

  private Handler handler=new Handler();

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    SharedPreferences sharedPref=PreferenceManager.getDefaultSharedPreferences(this);
    if (!sharedPref.getBoolean(getResources().getString(R.string.pref_key_notify), false)) {
      Log.d(TAG, "No notifications");
      this.stopSelf();
      return -1;
    }

    int notificationTime=Integer.valueOf(
        sharedPref.getString(getResources().getString(R.string.pref_key_notify_time), "5"));
    notificationType=Integer.valueOf(
        sharedPref.getString(getResources().getString(R.string.pref_key_notify_type), "2"));

    Calendar calendar=Calendar.getInstance();
    if (MainActivity.getHoursIntoConvention()==-1) {
      // set first execution to the first day of con
      String firstDay=getResources().getStringArray(R.array.daysForParsing)[0];
      SimpleDateFormat simpleDateFormat=new SimpleDateFormat("M/d/yyyy");
      simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+7"));

      // set date according to string of con's first day
      Date date;
      try {
        date=simpleDateFormat.parse(firstDay);
      } catch (ParseException e) {
        date=Calendar.getInstance().getTime();
        Log.d(TAG, "ERROR: Unable to parse string of first day");
        Toast.makeText(this, "ERROR: Unable to parse string of first day, "+
            "contact "+getResources().getString(R.string.email)+" for help.", Toast.LENGTH_LONG)
            .show();
      }

      calendar.setTime(date);
      calendar.set(Calendar.HOUR_OF_DAY, 6);
      calendar.set(Calendar.MINUTE, 60-notificationTime);
      calendar.set(Calendar.SECOND, 0);

    } else {
      int hour=calendar.get(Calendar.HOUR_OF_DAY);
      if (60-notificationTime<=calendar.get(Calendar.MINUTE)+1) {
        hour++;
      }

      calendar.set(Calendar.HOUR_OF_DAY, hour);
      calendar.set(Calendar.MINUTE, 60-notificationTime);
      calendar.set(Calendar.SECOND, 0);
    }

    Log.d(TAG, "First exec at "+calendar.getTime().toString());

    handler.postDelayed(runnable, calendar.getTimeInMillis());

    return super.onStartCommand(intent, flags, startId);
  }

  private void checkEvents() {
    String[] daysForParsing=getResources().getStringArray(R.array.daysForParsing);

    // load preferences for user events and starred events
    SharedPreferences sp=
        getSharedPreferences(getResources().getString(R.string.sp_file_name), Context.MODE_PRIVATE);
    String userEventPrefString=getResources().getString(R.string.sp_user_event);
    String starPrefString=getResources().getString(R.string.sp_event_starred);

    String eventsString="";

    int hoursIntoConvention=MainActivity.getHoursIntoConvention();

    /***** CHECK USER EVENTS *******/
    String identifier, row, eventTitle, tempString;
    String[] rowData;
    int index, day, hour;
    for (index=0; ; index++) {
      row=sp.getString(userEventPrefString+String.valueOf(index), "");
      if (row.equalsIgnoreCase("")) {
        break;
      }

      rowData=row.split("~");

      day=Integer.valueOf(rowData[0]);
      hour=Integer.valueOf(rowData[1]);
      eventTitle=rowData[2];

      identifier=String.valueOf(day*24+hour)+eventTitle;

      // if event is starred, add to notification string
      if (sp.getBoolean(starPrefString+identifier, false)) {
        if (hoursIntoConvention+1==day*24+hour) {
          eventsString+=eventTitle+", ";
        }
      }
    }

    /***** CHECK ALL EVENTS *******/

    // load schedule file
    InputStream is;
    try {
      is=getAssets().open("schedule2014.txt");
    } catch (IOException e2) {
      Toast.makeText(this, "ERROR: Could not find schedule file, "+
          "contact "+getResources().getString(R.string.email)+" for help.", Toast.LENGTH_SHORT)
          .show();
      e2.printStackTrace();
      return;
    }

    // read schedule file
    InputStreamReader isr;
    try {
      isr=new InputStreamReader(is);
    } catch (IllegalStateException e1) {
      Toast.makeText(this, "ERROR: Could not open schedule file "+
          "contact "+getResources().getString(R.string.email)+" for help.", Toast.LENGTH_SHORT)
          .show();
      e1.printStackTrace();
      return;
    }

    // parse schedule file
    BufferedReader reader=new BufferedReader(isr);
    String line;
    try {
      while ((line=reader.readLine())!=null) {
        rowData=line.split("~");

        // currentDay
        tempString=rowData[0];
        for (index=0; index<daysForParsing.length; index++) {
          if (daysForParsing[index].equalsIgnoreCase(tempString)) {
            break;
          }
        }
        if (index==-1) {
          Log.d(TAG, "Unknown date: "+rowData[2]+" in "+line);
          index=0;
        }
        day=index;

        // title
        eventTitle=rowData[2];

        // time
        tempString=rowData[1];
        if (rowData[1].contains(":30")) {
          Log.d(TAG, rowData[2]+" starts at half past");
          tempString=tempString.substring(0, tempString.length()-3);
        }

        hour=Integer.valueOf(tempString);

        identifier=String.valueOf(day*24+hour)+eventTitle;

        // if event is starred, add to notification string
        if (sp.getBoolean(starPrefString+identifier, false) &&
            hoursIntoConvention+1==day*24+hoursIntoConvention%24) {
          eventsString+=eventTitle+", ";
        }

      }
    } catch (IOException e) {
      Toast.makeText(this, "ERROR: Could not read schedule file "+
          "contact "+getResources().getString(R.string.email)+" for help.", Toast.LENGTH_SHORT)
          .show();
    }

    if (eventsString.length()>0) {
      eventsString=eventsString.substring(0, eventsString.length()-2);
      sendNotification(eventsString);
    }
  }

  private void sendNotification(String s) {
    Log.d(TAG, "Events now: "+s);

    int TYPE_VIBRATE=0;
    int TYPE_RING=1;
    int TYPE_BOTH=2;

    NotificationCompat.Builder mBuilder=new NotificationCompat.Builder(this);
    mBuilder.setContentTitle(getResources().getString(R.string.notification_title))
        .setContentText(s).setSmallIcon(R.drawable.ic_notification).setAutoCancel(true);

    if (notificationType==TYPE_VIBRATE) {
      mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
    } else if (notificationType==TYPE_RING) {
      mBuilder.setDefaults(Notification.DEFAULT_SOUND);
    } else if (notificationType==TYPE_BOTH) {
      mBuilder.setDefaults(Notification.DEFAULT_ALL);
    }

    // Creates an explicit intent for an Activity in your app
    Intent resultIntent=new Intent(this, MainActivity.class);
    TaskStackBuilder stackBuilder=TaskStackBuilder.create(this);
    stackBuilder.addParentStack(MainActivity.class);
    stackBuilder.addNextIntent(resultIntent);

    PendingIntent resultPendingIntent=
        stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    mBuilder.setContentIntent(resultPendingIntent);
    NotificationManager mNotificationManager=
        (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
    // mId allows you to update the notification later on.
    mNotificationManager.notify(0, mBuilder.build());

    Log.d(TAG, "Notifying");
  }

  @Override
  public void onDestroy() {
    handler.removeCallbacks(runnable);
    super.onDestroy();
  }

  @Override
  public IBinder onBind(Intent arg0) {
    return null;
  }

  private final Runnable runnable=new Runnable() {
    @Override
    public void run() {
      handler.postDelayed(this, 60*60*1000);
      checkEvents();
    }
  };
}
