package org.boardgamers.wbc;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.app.Application;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;

public class MyApp extends Application {
	// private static final String TAG="MyApp";

	public static int NUM_EVENTS;

	public static int COLOR_JUNIOR;
	public static int COLOR_SEMINAR;
	public static int COLOR_QUALIFY;
	public static int COLOR_OPEN_TOURNAMENT;
	public static int COLOR_NON_TOURNAMENT;

	public static int SELECTED_GAME_ID;
	public static String SELECTED_EVENT_ID;

	public static int day;
	public static int hour;

	public static List<Tournament> allTournaments;
	public static ArrayList<ArrayList<Event>>[] dayList;

	public static void updateTime(Resources resources) {
		String[] daysForParsing=resources
		    .getStringArray(R.array.daysForParsing);

		Calendar c=Calendar.getInstance();

		// get hour
		hour=c.get(Calendar.HOUR_OF_DAY);

		SimpleDateFormat dateFormatter=new SimpleDateFormat("yyyy-MM-dd",
		    Locale.US);
		String dateString=dateFormatter.format(c.getTime());

		// get day
		day=-1;
		for (int i=0; i<daysForParsing.length; i++) {
			if (daysForParsing[i].equalsIgnoreCase(dateString)) {
				day=i;
				break;
			}
		}
	}

	public static int getTextColor(Event event) {
		if (event.qualify)
			return COLOR_QUALIFY;
		else if (event.title.indexOf("Junior")>-1)
			return COLOR_JUNIOR;
		else if (event.format.equalsIgnoreCase("Seminar"))
			return COLOR_SEMINAR;
		else if (event.format.equalsIgnoreCase("SOG")
		    ||event.format.equalsIgnoreCase("MP Game")
		    ||event.title.indexOf("Open Gaming")==0)
			return COLOR_OPEN_TOURNAMENT;
		else if (event.eClass.length()==0)
			return COLOR_NON_TOURNAMENT;
		else
			return Color.BLACK;
	}

	public static int getTextStyle(Event event) {
		if (event.qualify)
			return Typeface.BOLD;
		else if (event.title.indexOf("Junior")>-1)
			return Typeface.NORMAL;
		else if (event.eClass.length()==0
		    ||event.format.equalsIgnoreCase("Demo"))
			return Typeface.ITALIC;
		else
			return Typeface.NORMAL;
	}
}
