package org.boardgamers.wbc;

import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

public class NotificationService extends Service {
  private final static String TAG = "Notification Service";

  private static int type;
  private static int time;

  private static Context context;
  private final Timer timer = new Timer();

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    final Resources resources = getResources();

    context = this;

    SharedPreferences settings = getSharedPreferences(
        resources.getString(R.string.sp_file_name), MODE_PRIVATE);

    if (!settings.getBoolean(
        resources.getString(R.string.sp_notify_starred), false)) {
      this.stopSelf();
      return super.onStartCommand(intent, flags, startId);
    }

    time = settings.getInt(resources.getString(R.string.sp_notify_time), 5);
    type = settings.getInt(resources.getString(R.string.sp_notify_type), 2);

    Calendar c = Calendar.getInstance();
    int currentMin = c.get(Calendar.MINUTE);

    c.set(Calendar.MINUTE, 60 - time);
    c.set(Calendar.SECOND, 0);

    if (currentMin >= 60 - time) {
      int currentHour = c.get(Calendar.HOUR);
      c.set(Calendar.HOUR, currentHour + 1);
    }
    long msHour = 60 * 60 * 1000;

    Log.d(TAG, "First exec at " + c.getTime().toString());
    timer.scheduleAtFixedRate(new NotifyEvents(), c.getTime(), msHour);

    return super.onStartCommand(intent, flags, startId);
  }

  private class NotifyEvents extends TimerTask {
    @Override
    public void run() {
      Log.d(TAG, "Checking events");
      new LoadEventsTask(context).execute(null, null, null);

    }
  }

  public static void checkEvents(Context context) {
    MyApp.updateTime(context.getResources());
    Event event = null;
    String eventsString = "";

    for (int i = 0; i < MyApp.dayList.size(); i++) {
      List<Event> events = MyApp.dayList.get(i).get(0).events;
      for (int k = 0; k < events.size(); k++) {
        event = events.get(k);

        boolean starting = event.day * 24 + event.hour == MyApp.day * 24
            + MyApp.hour + 1;
        if (starting)
          eventsString += event.title + ", ";
      }
    }

    if (eventsString.length() > 0) {
      eventsString = eventsString.substring(0, eventsString.length() - 2);

      Log.d(TAG, "Events now: " + eventsString);
      sendNotification(eventsString);
    }
  }

  public static void sendNotification(String s) {
    int TYPE_VIBRATE = 0;
    int TYPE_RING = 1;
    int TYPE_BOTH = 2;

    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
        context);
    mBuilder.setContentTitle("WBC Events starting")
        .setContentText(s + " in " + String.valueOf(time) + " minutes!")
        .setSmallIcon(R.drawable.ic_notification).setAutoCancel(true);

    if (type == TYPE_VIBRATE)
      mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
    else if (type == TYPE_RING)
      mBuilder.setDefaults(Notification.DEFAULT_SOUND);
    else if (type == TYPE_BOTH)
      mBuilder.setDefaults(Notification.DEFAULT_ALL);

    // Creates an explicit intent for an Activity in your app
    Intent resultIntent = new Intent(context, SplashScreen.class);
    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
    stackBuilder.addParentStack(SplashScreen.class);
    stackBuilder.addNextIntent(resultIntent);

    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
        PendingIntent.FLAG_UPDATE_CURRENT);
    mBuilder.setContentIntent(resultPendingIntent);
    NotificationManager mNotificationManager = (NotificationManager) context
        .getSystemService(Context.NOTIFICATION_SERVICE);
    // mId allows you to update the notification later on.
    mNotificationManager.notify(0, mBuilder.build());

    Log.d(TAG, "Notifying");
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

}
