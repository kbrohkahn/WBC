package org.boardgamers.wbc;

import android.app.Fragment;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AboutFragment extends Fragment {
  final String TAG = "About";

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.about, container, false);

    TextView appVersion = (TextView) view.findViewById(R.id.about_app_version);

    String versionString;
    try {
      versionString = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(),
          0).versionName;
    } catch (NameNotFoundException e) {
      versionString = "\nCOULD NOT READ VERSION NUMBER";
      e.printStackTrace();
    }

    long update;
    try {
      update = getActivity().getPackageManager().getPackageInfo(
          "org.boardgamers.wbc", 0).lastUpdateTime;
    } catch (NameNotFoundException e) {
      update = -1;
      e.printStackTrace();
    }

    String updateString;
    if (update == -1)
      updateString = "COULD NOT READ";
    else {
      DateFormat formatter = new SimpleDateFormat("MMM d, yyyy", Locale.US);

      // Create a calendar object that will convert the date and time
      // value in milliseconds to date.
      Calendar calendar = Calendar.getInstance();
      calendar.setTimeInMillis(update);
      updateString = formatter.format(calendar.getTime());
    }

    appVersion.setText("App version: " + versionString + "\nLast update: "
        + updateString);

    return view;
  }
}
