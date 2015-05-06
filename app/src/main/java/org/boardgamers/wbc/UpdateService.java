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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UpdateService extends Service {
  private final String TAG="Notification Service";

  private static int notificationType;

  private final Handler handler=new Handler();

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {

    // GET CALENDAR FOR FIRST DAY
    String[] daysForParsing=getResources().getStringArray(R.array.daysForParsing);
    SimpleDateFormat dateFormatter=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.US);
    String firstDayString=daysForParsing[0]+" 12:00:00 GMT+7";
    Calendar firstDayCalendar=Calendar.getInstance();
    try {
      Date firstDayDate=dateFormatter.parse(firstDayString);
      firstDayCalendar.setTime(firstDayDate);
    } catch (java.text.ParseException e) {
      Log.d(TAG, "Unable to parse firstDayString: "+firstDayString);
    }

    int milliHour=60*60*1000;

    Calendar calendar=Calendar.getInstance();
    MainActivity.currentHour=calendar.get(Calendar.HOUR_OF_DAY);

    MainActivity.currentDay=
        (firstDayCalendar.getTimeInMillis()-calendar.getTimeInMillis())/(milliHour*24);

    // TODO TESTING: set currentDay to day of week
    MainActivity.currentDay=(calendar.get(Calendar.DAY_OF_WEEK));

    Log.d(TAG, "Day is "+String.valueOf(MainActivity.currentDay));

    // set calendar to next hour
    calendar.setTimeInMillis(calendar.getTimeInMillis()/milliHour*milliHour+milliHour);
    //Log.d(TAG, "First execution of clockUpdate: "+calendar.getTime().toString());

    handler.postDelayed(clockUpdate, calendar.getTimeInMillis());

    SharedPreferences sharedPref=PreferenceManager.getDefaultSharedPreferences(this);
    if (!sharedPref.getBoolean(getResources().getString(R.string.pref_key_notify), false)) {
      Log.d(TAG, "No notifications");
    } else {
      int notificationTime=Integer.valueOf(
          sharedPref.getString(getResources().getString(R.string.pref_key_notify_time), "5"));
      notificationType=Integer.valueOf(
          sharedPref.getString(getResources().getString(R.string.pref_key_notify_type), "2"));

      if (MainActivity.currentDay<0) {
        calendar=firstDayCalendar;
      } else {
        int hour=calendar.get(Calendar.HOUR_OF_DAY);
        if (60-notificationTime<=calendar.get(Calendar.MINUTE)+1) {
          hour++;
        }

        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, 60-notificationTime);
        calendar.set(Calendar.SECOND, 0);
      }

      //Log.d(TAG, "First exec of notificationUpdate "+calendar.getTime().toString());

      handler.postDelayed(notificationUpdate, calendar.getTimeInMillis());
    }

    return super.onStartCommand(intent, flags, startId);
  }

  private void checkEvents() {
    WBCDataDbHelper dbHelper=new WBCDataDbHelper(this);
    dbHelper.getReadableDatabase();
    List<Event> starredEvents=dbHelper.getStarredEvents();
    dbHelper.close();

    int hoursIntoConvention=MainActivity.getHoursIntoConvention();

    String eventsString="";
    for (Event event : starredEvents) {
      if (hoursIntoConvention+1==event.day*24+event.hour) {
        eventsString+=event.title+", ";
      }
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
  }

  @Override
  public void onDestroy() {
    handler.removeCallbacks(notificationUpdate);
    handler.removeCallbacks(clockUpdate);
    super.onDestroy();
  }

  @Override
  public IBinder onBind(Intent arg0) {
    return null;
  }

  /**
   * Calls checkEvents every hour, at notificationTime minutes before the hour
   */
  private final Runnable notificationUpdate=new Runnable() {
    @Override
    public void run() {
      handler.postDelayed(this, 60*60*1000);
      checkEvents();
    }
  };

  /**
   * Calls updateClock every hour, on the hour
   */
  private final Runnable clockUpdate=new Runnable() {
    @Override
    public void run() {
      handler.postDelayed(this, 60*60*1000);
      MainActivity.updateClock();
    }
  };
}
