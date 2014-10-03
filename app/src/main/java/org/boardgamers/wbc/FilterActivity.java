package org.boardgamers.wbc;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

public class FilterActivity extends Activity {

  private static CheckBox[] tournamentCBs;
  final String TAG = "Filter Dialog";
  private Boolean[] isTournament;

  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.filter);

    // enable home button for navigation drawer
    final ActionBar ab = getActionBar();
    if (ab != null) {
      ab.setDisplayHomeAsUpEnabled(true);
      ab.setHomeButtonEnabled(true);
    } else
      Log.d(TAG, "Could not get action bar");

    isTournament = new Boolean[MainActivity.allTournaments.size()];
    for (int i = 0; i < isTournament.length; i++)
      isTournament[i] = MainActivity.allTournaments.get(i).isTournament;

    // select all image button
    ImageButton selectAll = (ImageButton) findViewById(R.id.filter_select_all);
    selectAll.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        for (CheckBox cb : tournamentCBs) {
          cb.setChecked(true);
        }
      }
    });
    selectAll.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        showToast(getResources().getString(R.string.select_all));
        return false;
      }
    });

    // select non tournament image button
    ImageButton selectNonTournament = (ImageButton) findViewById(R.id.filter_select_non_tournament);
    selectNonTournament.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        for (int i = 0; i < tournamentCBs.length; i++) {
          if (isTournament[i])
            tournamentCBs[i].setChecked(true);

        }
      }
    });
    selectNonTournament.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        showToast(getResources().getString(R.string.select_tournament));
        return false;
      }
    });

    // deselect all image button
    ImageButton deselectAll = (ImageButton) findViewById(R.id.filter_deselect_all);
    deselectAll.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        for (CheckBox cb : tournamentCBs) {
          cb.setChecked(false);
        }

      }
    });
    deselectAll.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        showToast(getResources().getString(R.string.deselect_all));
        return false;
      }
    });

    // deselect non tournament image button
    ImageButton deselectNonTournament = (ImageButton) findViewById(R.id.filter_deselect_tournament);
    deselectNonTournament.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        for (int i = 0; i < tournamentCBs.length; i++) {
          if (isTournament[i])
            tournamentCBs[i].setChecked(false);
        }
      }
    });
    deselectNonTournament.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        showToast(getResources().getString(R.string.deselect_tournament));
        return false;
      }
    });

    // set up checkboxes
    LinearLayout checkBoxLayout = (LinearLayout) findViewById(R.id.filter_layout);
    checkBoxLayout.removeAllViews();
    tournamentCBs = new CheckBox[isTournament.length];

    CheckBox temp;
    Tournament tournament;
    for (int i = 0; i < isTournament.length; i++) {
      tournament = MainActivity.allTournaments.get(i);
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

  }


  private void showToast(String string) {
    Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
  }


  @Override
  public void onPause() {
    SharedPreferences.Editor editor = getSharedPreferences(
        getResources().getString(R.string.sp_file_name),
        Context.MODE_PRIVATE).edit();

    boolean checked;
    for (int i = 0; i < tournamentCBs.length; i++) {
      checked = tournamentCBs[i].isChecked();
      editor.putBoolean("vis_" + MainActivity.allTournaments.get(i).title, checked);
      MainActivity.allTournaments.get(i).visible = checked;
    }

    editor.apply();

    super.onPause();
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