package org.boardgamers.wbc;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

public class SplashScreen extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.splash_screen);

		final Resources resources=getResources();
		MyApp.COLOR_JUNIOR=resources.getColor(R.color.junior);
		MyApp.COLOR_SEMINAR=resources.getColor(R.color.seminar);
		MyApp.COLOR_QUALIFY=resources.getColor(R.color.qualify);
		MyApp.COLOR_NON_TOURNAMENT=resources.getColor(R.color.non_tournament);
		MyApp.COLOR_OPEN_TOURNAMENT=resources.getColor(R.color.open_tournament);

		new LoadEventsTask(this).execute(null, null, null);

		super.onCreate(savedInstanceState);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setContentView(R.layout.splash_screen);
	}
}