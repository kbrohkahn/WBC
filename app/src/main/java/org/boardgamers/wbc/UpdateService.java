package org.boardgamers.wbc;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import java.util.Calendar;
import java.util.TimeZone;

public class UpdateService extends IntentService {

	public UpdateService() {
		super("UpdateService");
	}

	@Override
	protected void onHandleIntent(@Nullable Intent intent) {


		// GET CURRENT TIME IN EST
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT-4"));

		// TESTING: set currentDay to day of week
		//MainActivity.currentDay=(calendar.get(Calendar.DAY_OF_WEEK));

		// SET DELAY TO NEXT HOUR, IN MILLIS, FOR UPDATE
		calendar.setTimeInMillis(calendar.getTimeInMillis() / Constants.milliHour * Constants.milliHour + Constants.milliHour);

		// CHECK IF NOTIFICATION ACTIVE
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		if (sharedPref.getBoolean(getResources().getString(R.string.pref_key_notify), false)) {

			int notificationTime =
					sharedPref.getInt(getResources().getString(R.string.pref_key_notify_time), 5);

			if (Helpers.getHoursIntoConvention() < 0) {
				calendar = Helpers.getFirstDayCalendar();
			} else {
				calendar = Calendar.getInstance();
				int hour = calendar.get(Calendar.HOUR_OF_DAY);
				if (60 - notificationTime <= calendar.get(Calendar.MINUTE)) {
					hour++;
				}
				calendar.set(Calendar.HOUR_OF_DAY, hour);
				calendar.set(Calendar.MINUTE, 60 - notificationTime);
				calendar.set(Calendar.SECOND, 0);
			}

			AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

			Intent checkEventServiceIntent = new Intent(this, CheckEventService.class);
			PendingIntent serviceScheduleIntent = PendingIntent.getService(this,
					1324,
					checkEventServiceIntent,
					PendingIntent.FLAG_CANCEL_CURRENT);

			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP
					, calendar.getTimeInMillis()
					, Constants.milliHour
					, serviceScheduleIntent);

		}
	}

}
