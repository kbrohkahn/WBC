package org.boardgamers.wbc;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

public class About extends Activity {
	final String TAG="About";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.about);

		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		TextView appVersion=(TextView) findViewById(R.id.about_app_version);

		String versionString;
		try {
			versionString=getPackageManager().getPackageInfo(getPackageName(),
					0).versionName;
		} catch (NameNotFoundException e) {
			versionString="\nCOULD NOT READ VERSION NUMBER";
			e.printStackTrace();
		}

		long update;
		try {
			update=this.getPackageManager().getPackageInfo(
					"org.boardgamers.wbc", 0).lastUpdateTime;
		} catch (NameNotFoundException e) {
			update=-1;
			e.printStackTrace();
		}

		String updateString;
		if (update==-1)
			updateString="COULD NOT READ";
		else {

			DateFormat formatter=new SimpleDateFormat("MMM d, yyyy", Locale.US);

			// Create a calendar object that will convert the date and time
			// value in
			// milliseconds to date.
			Calendar calendar=Calendar.getInstance();
			calendar.setTimeInMillis(update);
			updateString=formatter.format(calendar.getTime());

		}

		appVersion.setText("App version: "+versionString+"\nLast update: "
				+updateString);

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
