package org.boardgamers.wbc;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.util.List;

public class FilterActivity extends AppCompatActivity {
  private final String TAG="Filter Activity";

  private boolean[] initialVisible;
  private List<Tournament> tournaments;
  private FilterListAdapter listAdapter;

  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.filter);

    Toolbar toolbar=(Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    if (getSupportActionBar()!=null) {
      getSupportActionBar().setDisplayShowHomeEnabled(true);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    WBCDataDbHelper dbHelper=new WBCDataDbHelper(this);
    dbHelper.getReadableDatabase();
    tournaments=dbHelper.getAllTournaments(MainActivity.userId);
    dbHelper.close();

    initialVisible=new boolean[tournaments.size()];
    for (int i=0; i<initialVisible.length; i++) {
      initialVisible[i]=tournaments.get(i).visible;
    }

    ListView listView=(ListView) findViewById(R.id.filter_list_view);
    listView.setFastScrollEnabled(true);
    listAdapter=new FilterListAdapter();
    listView.setAdapter(listAdapter);

    // select all image button
    Button selectAll=(Button) findViewById(R.id.filter_select_all);
    selectAll.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        selectAll();
      }
    });

    // select non tournament image button
    Button selectTournaments=(Button) findViewById(R.id.filter_select_tournament);
    selectTournaments.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        selectTournaments();
      }
    });

    // deselect all image button
    Button deselectAll=(Button) findViewById(R.id.filter_deselect_all);
    deselectAll.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        deselectAll();
      }
    });

    Button deselectTournaments=(Button) findViewById(R.id.filter_deselect_tournament);
    deselectTournaments.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        deselectTournaments();
      }
    });
  }

  public void selectAll() {
    for (Tournament tournament : tournaments) {
      tournament.visible=true;
    }
    listAdapter.notifyDataSetChanged();
  }

  public void selectTournaments() {
    for (Tournament tournament : tournaments) {
      if (tournament.isTournament) {
        tournament.visible=true;
      }
    }
    listAdapter.notifyDataSetChanged();
  }

  public void deselectAll() {
    for (Tournament tournament : tournaments) {
      tournament.visible=false;
    }
    listAdapter.notifyDataSetChanged();
  }

  public void deselectTournaments() {
    for (Tournament tournament : tournaments) {
      if (tournament.isTournament) {
        tournament.visible=false;
      }
    }
    listAdapter.notifyDataSetChanged();
  }

  @Override
  public void onPause() {
    ProgressDialog dialog=new ProgressDialog(this);
    dialog.setCancelable(false);
    dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    dialog.setTitle("Saving, please wait...");
    dialog.setMax(initialVisible.length);
    dialog.setProgress(0);
    dialog.show();

    WBCDataDbHelper dbHelper=new WBCDataDbHelper(this);
    dbHelper.getWritableDatabase();
    Tournament tournament;
    for (int i=0; i<initialVisible.length; i++) {
      dialog.setProgress(i);
      tournament=tournaments.get(i);
      if (initialVisible[i]^tournament.visible) {
        dbHelper.updateTournamentVisible(tournament.id, tournament.visible);
      }
    }
    dbHelper.close();

    dialog.dismiss();

    super.onPause();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater=getMenuInflater();
    inflater.inflate(R.menu.menu_filter, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id=item.getItemId();

    if (id==android.R.id.home) {
      finish();
      return true;
    } else if (id==R.id.filter_select_all) {
      selectAll();
      return true;
    } else if (id==R.id.filter_select_tournament) {
      selectTournaments();
      return true;
    } else if (id==R.id.filter_deselect_all) {
      deselectAll();
      return true;
    } else if (id==R.id.filter_deselect_tournament) {
      deselectTournaments();
      return true;
    } else {
      return super.onOptionsItemSelected(item);
    }
  }

  public class FilterListAdapter extends BaseAdapter implements SectionIndexer {
    private final LayoutInflater inflater;
    private final String[] sections=
        {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R",
            "S", "T", "U", "V", "W", "X", "Y", "Z"};

    public FilterListAdapter() {
      inflater=(LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
      return tournaments.size();
    }

    @Override
    public boolean hasStableIds() {
      return true;
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public Object getItem(int position) {
      return tournaments.get(position);
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
      if (view==null) {
        view=inflater.inflate(R.layout.filter_list_item, parent, false);
      }

      Tournament tournament=tournaments.get(position);

      TextView titleTV=(TextView) view.findViewById(R.id.filter_title);
      titleTV.setText(tournament.title);

      TextView labelTV=(TextView) view.findViewById(R.id.filter_label);

      if (tournament.isTournament) {
        labelTV.setText(tournament.label);
        labelTV.setVisibility(View.VISIBLE);
      } else {
        labelTV.setVisibility(View.GONE);
      }

      ImageView boxIV=(ImageView) view.findViewById(R.id.filter_image_view);
      boxIV.setImageResource(MainActivity.getBoxIdFromLabel(tournament.label, getResources()));

      final CheckBox checkBox=(CheckBox) view.findViewById(R.id.filter_checkbox);
      checkBox.setChecked(tournament.visible);
      checkBox.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          Log.d(TAG, "View clicked");
          tournaments.get(position).visible=((CheckBox) v).isChecked();
        }
      });

      view.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          boolean newCheck=!checkBox.isChecked();
          tournaments.get(position).visible=newCheck;
          checkBox.setChecked(newCheck);
        }
      });

      return view;
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
      int index;
      if (sectionIndex>13) {
        String letter=sections[sectionIndex-1];
        for (index=tournaments.size()-1; index>=0; index--) {
          if (tournaments.get(index).title.startsWith(letter)) {
            break;
          }
        }
        index++;
      } else {
        String letter=sections[sectionIndex];
        for (index=0; index<tournaments.size(); index++) {
          if (tournaments.get(index).title.startsWith(letter)) {
            break;
          }
        }
      }
      return index;
    }

    public int getSectionForPosition(int position) {
      String letter=tournaments.get(position).title.substring(0, 1);

      int index;
      for (index=0; index<sections.length; index++) {
        if (sections[index].equals(letter))
          break;
      }
      return index;
    }

    public String[] getSections() {
      return sections;
    }

  }

}
