package org.boardgamers.wbc;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class EventActivity extends AppCompatActivity {
  private final String TAG="Event Activity";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.event_activity);

    Toolbar toolbar=(Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayShowHomeEnabled(true);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater=getMenuInflater();
    inflater.inflate(R.menu.menu_event, menu);
    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    MenuItem item=menu.findItem(R.id.menu_star);

    EventFragment eventFragment=
        (EventFragment) getSupportFragmentManager().findFragmentById(R.id.eventFragment);
    if (eventFragment!=null && eventFragment.isAdded()) {
      if (eventFragment.event.starred) {
        item.setIcon(R.drawable.star_on);
      } else {
        item.setIcon(R.drawable.star_off);
      }
    }
    return super.onPrepareOptionsMenu(menu);
  }

  public void changeEventStar() {
    EventFragment eventFragment=
        (EventFragment) getSupportFragmentManager().findFragmentById(R.id.eventFragment);
    if (eventFragment!=null && eventFragment.isAdded()) {
      eventFragment.event.starred=!eventFragment.event.starred;

      invalidateOptionsMenu();

      ((MainActivity) getParent()).changeEventStar(eventFragment.event);
    } else {
      Log.d(TAG, "ERROR: Could not get event fragment");
    }

  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id=item.getItemId();

    if (id==R.id.menu_help) {
      Intent intent=new Intent(this, HelpActivity.class);
      startActivity(intent);
      return true;
    } else if (id==R.id.menu_star) {
      changeEventStar();
      return true;
    } else if (id==android.R.id.home) {
      finish();
      return true;
    } else {
      return super.onOptionsItemSelected(item);
    }
  }
}
