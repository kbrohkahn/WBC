package org.boardgamers.wbc;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import java.util.List;

public class CheckEventService extends IntentService {
	private static final String TAG = "CheckEventService";

	private static int notificationType;

	public CheckEventService() {
		super("CheckEventService");
	}

	@Override
	protected void onHandleIntent(@Nullable Intent intent) {
		WBCDataDbHelper dbHelper = new WBCDataDbHelper(this);
		dbHelper.getReadableDatabase();
		List<Event> starredEvents = dbHelper.getStarredEvents(MainActivity.userId);
		dbHelper.close();


		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		notificationType = Integer.valueOf(
				sharedPref.getString(getResources().getString(R.string.pref_key_notify_type), "2"));

		long hoursIntoConvention = Helpers.getHoursIntoConvention();

		String eventsString = "";
		for (Event event : starredEvents) {
			if (hoursIntoConvention + 1 == event.day * 24 + event.hour) {
				MainActivity.selectedEventId = event.id;
				eventsString += event.title + ", ";
			}
		}

		if (eventsString.length() > 0) {
			eventsString = eventsString.substring(0, eventsString.length() - 2);
			sendNotification(eventsString);
		}
	}

	private void sendNotification(String s) {
		Log.d(TAG, "Events now: " + s);

		int TYPE_VIBRATE = 0;
		int TYPE_RING = 1;
		int TYPE_BOTH = 2;

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
		mBuilder.setContentTitle(getResources().getString(R.string.notification_title))
				.setContentText(s).setSmallIcon(R.drawable.ic_notification).setAutoCancel(true);

		if (notificationType == TYPE_VIBRATE) {
			mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
		} else if (notificationType == TYPE_RING) {
			mBuilder.setDefaults(Notification.DEFAULT_SOUND);
		} else if (notificationType == TYPE_BOTH) {
			mBuilder.setDefaults(Notification.DEFAULT_ALL);
		}

		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(this, MainActivity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(MainActivity.class);
		stackBuilder.addNextIntent(resultIntent);

		PendingIntent resultPendingIntent =
				stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager =
				(NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(0, mBuilder.build());
	}

}
