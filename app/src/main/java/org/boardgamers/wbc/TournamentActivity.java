package org.boardgamers.wbc;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class TournamentActivity extends FragmentActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    getActionBar().setHomeButtonEnabled(true);
    getActionBar().setDisplayHomeAsUpEnabled(true);

    setContentView(R.layout.tournament_activity);

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.game, menu);

    if (MyApp.SELECTED_GAME_ID > 0)
      menu.getItem(0).setVisible(false);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.create_event:
        TournamentFragment.showCreateDialog();
        return true;
      case R.id.game_help:
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
