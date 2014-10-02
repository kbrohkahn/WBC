package org.boardgamers.wbc;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class Help extends Activity {
  final String TAG = "Help";

  final static int[] aboutHeaderIDs = {R.id.about_app_header,
      R.id.about_schedule_header, R.id.about_dates_header,
      R.id.about_times_header, R.id.about_heats_rounds_header,
      R.id.about_qualifying_header, R.id.about_prize_header,
      R.id.about_class_header, R.id.about_demo_preview_header,
      R.id.about_sog_header, R.id.about_format_header,
      R.id.about_length_header, R.id.about_continuous_header,
      R.id.about_location_header};
  final static int[] aboutTextIDs = {R.id.about_app_text,
      R.id.about_schedule_text, R.id.about_dates_text,
      R.id.about_times_text, R.id.about_heats_rounds_text,
      R.id.about_qualifying_text, R.id.about_prize_text,
      R.id.about_class_text, R.id.about_demo_preview_text,
      R.id.about_sog_text, R.id.about_format_text,
      R.id.about_length_text, R.id.about_continuous_text,
      R.id.about_location_text};

  TextView[] aboutTexts;
  TextView[] aboutHeaders;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.help);

    try {
      getActionBar().setDisplayHomeAsUpEnabled(true);
      getActionBar().setHomeButtonEnabled(true);
    } catch (NullPointerException e) {
      Toast.makeText(this, "Error: cannot set home button enabled", Toast.LENGTH_SHORT).show();
      Log.d(TAG, "Error: cannot set home button enabled");
    }
    final int numCategories = aboutHeaderIDs.length;

    aboutTexts = new TextView[numCategories];
    aboutHeaders = new TextView[numCategories];
    for (int i = 0; i < numCategories; i++) {
      aboutTexts[i] = (TextView) findViewById(aboutTextIDs[i]);
      aboutHeaders[i] = (TextView) findViewById(aboutHeaderIDs[i]);
      aboutHeaders[i].setOnClickListener(headerListener);
    }
  }

  private final View.OnClickListener headerListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      selectHeader(v.getId());
    }
  };

  public void selectHeader(int id) {
    int index = 0;
    for (; index < aboutHeaderIDs.length; index++) {
      if (aboutHeaderIDs[index] == id)
        break;
    }

    boolean vis = aboutTexts[index].isShown();

    if (vis) {
      aboutHeaders[index].setCompoundDrawablesWithIntrinsicBounds(
          R.drawable.list_group_closed, 0, 0, 0);
      aboutTexts[index].setVisibility(View.GONE);
    } else {
      aboutHeaders[index].setCompoundDrawablesWithIntrinsicBounds(
          R.drawable.list_group_open, 0, 0, 0);
      aboutTexts[index].setVisibility(View.VISIBLE);
    }

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
