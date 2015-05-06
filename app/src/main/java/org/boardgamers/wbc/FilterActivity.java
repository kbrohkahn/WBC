package org.boardgamers.wbc;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.List;

public class FilterActivity extends AppCompatActivity {
  private final String TAG="Filter Activity";

  private static CheckBox[] tournamentCBs;
  private List<Boolean> isTournament;

  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.filter);

    Toolbar toolbar=(Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayShowHomeEnabled(true);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    WBCDataDbHelper dbHelper=new WBCDataDbHelper(this);
    dbHelper.getReadableDatabase();
    isTournament=dbHelper.getAllVisible();
    dbHelper.close();

    // setup checkboxes
    tournamentCBs=new CheckBox[isTournament.size()];
    CheckBox temp;
    Tournament tournament=null;
    for (int i=0; i<isTournament.size(); i++) {
      //tournament=MainActivity.allTournaments.get(i);
      temp=new CheckBox(this);
      temp.setText(tournament.title);
      temp.setTextAppearance(this, R.style.medium_text);
      temp.setChecked(isTournament.get(i));

      tournamentCBs[i]=temp;
    }

    // setup checkbox layout
    LinearLayout checkBoxLayout=(LinearLayout) findViewById(R.id.filter_layout);
    checkBoxLayout.removeAllViews();
    for (CheckBox checkBox : tournamentCBs) {
      checkBoxLayout.addView(checkBox);
    }

    // setup batch buttons

    // select all image button
    Button selectAll=(Button) findViewById(R.id.filter_select_all);
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
    Button selectNonTournament=(Button) findViewById(R.id.filter_select_non_tournament);
    selectNonTournament.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        for (int i=0; i<tournamentCBs.length; i++) {
          if (isTournament.get(i)) {
            tournamentCBs[i].setChecked(true);
          }

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
    Button deselectAll=(Button) findViewById(R.id.filter_deselect_all);
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
    Button deselectNonTournament=(Button) findViewById(R.id.filter_deselect_tournament);
    deselectNonTournament.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        for (int i=0; i<tournamentCBs.length; i++) {
          if (isTournament.get(i)) {
            tournamentCBs[i].setChecked(false);
          }
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
  }

  private void showToast(String string) {
    Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onPause() {
    SharedPreferences.Editor editor=
        getSharedPreferences(getResources().getString(R.string.sp_file_name), Context.MODE_PRIVATE)
            .edit();

    boolean checked;
    for (int i=0; i<tournamentCBs.length; i++) {
      checked=tournamentCBs[i].isChecked();
      // editor.putBoolean("vis_"+MainActivity.allTournaments.get(i).title, checked);
      // MainActivity.allTournaments.get(i).visible=checked;
    }

    editor.apply();

    super.onPause();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id=item.getItemId();

    if (id==android.R.id.home) {
      finish();
      return true;
    } else {
      return super.onOptionsItemSelected(item);
    }

  }
}
