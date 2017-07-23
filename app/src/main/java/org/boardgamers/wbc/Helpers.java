package org.boardgamers.wbc;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

class Helpers {
	private static final String TAG = "Helpers";

	/**
	 * Get hours elapsed since midnight on the first day
	 *
	 * @return hours elapsed since midnight of the first day
	 */
	static long getHoursIntoConvention() {

		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT-4"));
		long currentHour = calendar.get(Calendar.HOUR_OF_DAY);
		long currentDay = (calendar.getTimeInMillis() - getFirstDayCalendar().getTimeInMillis()) / (Constants.milliHour * 24);
		if (currentDay <= 0) {
			currentDay--;
		}

		Log.d("US", "Day is " + String.valueOf(currentDay) + " and hour is " + String.valueOf(currentHour));
		if (currentDay < 0) {
			return -1;
		} else {
			int day = (int) currentDay;
			return day * 24 + currentHour;
		}
	}

	static Calendar getFirstDayCalendar() {
		SimpleDateFormat dateFormatter = new SimpleDateFormat("M/dd/yyyy HH:mm:ss z", Locale.ENGLISH);
		String firstDayString = "7/21/2017 00:00:00 EST";
		Calendar firstDayCalendar = Calendar.getInstance();
		try {
			Date firstDayDate = dateFormatter.parse(firstDayString);
			firstDayCalendar.setTime(firstDayDate);
		} catch (java.text.ParseException e) {
			Log.d(TAG, "Unable to parse firstDayString: " + firstDayString);
		}

		return firstDayCalendar;
	}

}
