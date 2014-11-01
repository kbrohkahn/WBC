package org.boardgamers.wbc;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class HelpActivity extends Activity {
  private final static int[] aboutHeaderIDs = {R.id.about_app_header,
      R.id.about_schedule_header, R.id.about_dates_header,
      R.id.about_times_header, R.id.about_heats_rounds_header,
      R.id.about_qualifying_header, R.id.about_prize_header,
      R.id.about_class_header, R.id.about_demo_preview_header,
      R.id.about_sog_header, R.id.about_format_header,
      R.id.about_length_header, R.id.about_continuous_header,
      R.id.about_location_header};
  private final static int[] aboutTextIDs = {R.id.about_app_text,
      R.id.about_schedule_text, R.id.about_dates_text,
      R.id.about_times_text, R.id.about_heats_rounds_text,
      R.id.about_qualifying_text, R.id.about_prize_text,
      R.id.about_class_text, R.id.about_demo_preview_text,
      R.id.about_sog_text, R.id.about_format_text,
      R.id.about_length_text, R.id.about_continuous_text,
      R.id.about_location_text};
  private final String TAG = "Help";
  private final View.OnClickListener headerListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      selectHeader(v.getId());
    }
  };
  private TextView[] aboutTexts;
  private TextView[] aboutHeaders;

  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.help);

    // enable home button for navigation drawer
    final ActionBar ab = getActionBar();
    if (ab != null) {
      ab.setDisplayHomeAsUpEnabled(true);
      ab.setHomeButtonEnabled(true);
    } else
      Log.d(TAG, "Could not get action bar");

    final int numCategories = aboutHeaderIDs.length;

    aboutTexts = new TextView[numCategories];
    aboutHeaders = new TextView[numCategories];
    for (int i = 0; i < numCategories; i++) {
      aboutTexts[i] = (TextView) findViewById(aboutTextIDs[i]);
      aboutHeaders[i] = (TextView) findViewById(aboutHeaderIDs[i]);
      aboutHeaders[i].setOnClickListener(headerListener);
    }
  }

  public void selectHeader(int id) {
    int index = 0;
    for (; index < aboutHeaderIDs.length; index++) {
      if (aboutHeaderIDs[index] == id)
        break;
    }

    boolean vis = aboutTexts[index].isShown();
    if (vis) {
      aboutHeaders[index].setBackgroundResource(R.drawable.group_collapsed);
      aboutTexts[index].setVisibility(View.GONE);
    } else {
      aboutHeaders[index].setBackgroundResource(R.drawable.group_expanded);
      aboutTexts[index].setVisibility(View.VISIBLE);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.close, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_close:
        finish();
        return true;
      case android.R.id.home:
        finish();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }
}
