package org.boardgamers.wbc;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;

public class Filter extends FragmentActivity {

  final String TAG = "Filter Dialog";
  private static CheckBox[] tournamentCBs;

  private Boolean[] isTournament;

  @Override
  protected void onCreate(Bundle arg0) {

    setContentView(R.layout.filter);

    isTournament = new Boolean[MyApp.allTournaments.size()];
    for (int i = 0; i < isTournament.length; i++)
      isTournament[i] = MyApp.allTournaments.get(i).isTournament;

    // set up checkboxes
    LinearLayout checkBoxLayout = (LinearLayout) findViewById(R.id.filter_layout);
    checkBoxLayout.removeAllViews();
    tournamentCBs = new CheckBox[isTournament.length];

    CheckBox temp;
    Tournament tournament;
    for (int i = 0; i < isTournament.length; i++) {

      tournament = MyApp.allTournaments.get(i);
      temp = new CheckBox(this);
      temp.setText(tournament.title);
      temp.setTextAppearance(this, R.style.medium_text);
      temp.setChecked(tournament.visible);

      temp.setOnCheckedChangeListener(new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
          // TODO Auto-generated method stub
        }
      });

      tournamentCBs[i] = temp;

    }

    // add checkboxes
    for (CheckBox checkBox : tournamentCBs)
      checkBoxLayout.addView(checkBox);

    super.onCreate(arg0);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.filter, menu);

    return true;
  }

  @Override
  protected void onPause() {
    SharedPreferences.Editor editor = getSharedPreferences(
        getResources().getString(R.string.sp_file_name),
        Context.MODE_PRIVATE).edit();

    boolean checked;
    for (int i = 0; i < tournamentCBs.length; i++) {
      checked = tournamentCBs[i].isChecked();
      editor.putBoolean("vis_" + MyApp.allTournaments.get(i).title, checked);
      MyApp.allTournaments.get(i).visible = checked;
    }

    editor.apply();

    super.onPause();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.select_all:
        for (CheckBox cb : tournamentCBs) {
          cb.setChecked(true);
        }
        return true;
      case R.id.deselect_all:
        for (CheckBox cb : tournamentCBs) {
          cb.setChecked(false);
        }
        return true;
      case R.id.select_non_tournament:
        for (int i = 0; i < tournamentCBs.length; i++) {
          if (!isTournament[i])
            tournamentCBs[i].setChecked(true);

        }
        return true;
      case R.id.deselect_non_tournament:
        for (int i = 0; i < tournamentCBs.length; i++) {
          if (!isTournament[i])
            tournamentCBs[i].setChecked(false);

        }
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }
}