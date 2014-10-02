package org.boardgamers.wbc;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class EventActivity extends FragmentActivity {
  private final String TAG = "Event Activity";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    try {
      getActionBar().setDisplayHomeAsUpEnabled(true);
      getActionBar().setHomeButtonEnabled(true);
    } catch (NullPointerException e) {
      Toast.makeText(this, "Error: cannot set home button enabled", Toast.LENGTH_SHORT).show();
      Log.d(TAG, "Error: cannot set home button enabled");
    }
    setContentView(R.layout.event_activity);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.event, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.event_help:
        Intent intent = new Intent(this, Help.class);
        startActivity(intent);
        return true;
      case android.R.id.home:
        finish();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }
}
