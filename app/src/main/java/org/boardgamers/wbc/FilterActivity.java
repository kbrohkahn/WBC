package org.boardgamers.wbc;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
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

import java.util.ArrayList;
import java.util.List;

public class FilterActivity extends AppCompatActivity {
  private final String TAG="Filter Activity";

  private final int SECTION_TOURNAMENT_ID=1000;

  private boolean[] initialVisible;
  private List<Tournament> tournaments;

  private Character[] sections;
  private Integer[] sectionIndices;

  ProgressDialog dialog;

  private FilterListAdapter listAdapter;

  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.filter);

    dialog=new ProgressDialog(this);

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

    List<Character> sectionsList=new ArrayList<>();
    List<Integer> sectionIndicesList=new ArrayList<>();

    // add first char so list is not empty
    Character letter=tournaments.get(0).title.charAt(0);
    sectionsList.add(letter);
    sectionIndicesList.add(0);
    tournaments.add(0, new Tournament(SECTION_TOURNAMENT_ID, ""+letter, "", false, 0, "", 0));

    int numSections=1;
    for (int i=1; i<tournaments.size(); i++) {
      letter=tournaments.get(i).title.charAt(0);

      if (sectionsList.get(numSections-1)!=letter) {
        sectionsList.add(letter);
        sectionIndicesList.add(i);
        tournaments.add(i,
            new Tournament(SECTION_TOURNAMENT_ID+numSections, ""+letter, "", false, 0, "", 0));

        numSections++;
        i++;
      }
    }

    sections=new Character[sectionsList.size()];
    sectionsList.toArray(sections);

    sectionIndices=new Integer[sectionIndicesList.size()];
    sectionIndicesList.toArray(sectionIndices);

    initialVisible=new boolean[tournaments.size()];
    for (int i=0; i<initialVisible.length; i++) {
      initialVisible[i]=tournaments.get(i).visible;
    }

    ListView listView=(ListView) findViewById(R.id.filter_list_view);

    //listView.setFastScrollEnabled(true);
    listAdapter=new FilterListAdapter();

    listView.setAdapter(listAdapter);

    FilterSideSelector sideSelector=(FilterSideSelector) findViewById(R.id.filter_side_selector);
    sideSelector.setListView(listView);

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
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater=getMenuInflater();
    inflater.inflate(R.menu.menu_filter, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id=item.getItemId();

    if (id==android.R.id.home) {
      save();
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

  @Override
  protected void onPause() {
    if (dialog!=null && dialog.isShowing()) {
      dialog.dismiss();
    }

    super.onPause();
  }

  @Override
  public void onBackPressed() {
    save();
  }

  public void save() {
    new SaveTournamentVisible().execute();
  }

  public class FilterListAdapter extends BaseAdapter implements SectionIndexer {
    private final LayoutInflater inflater;

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
      int sectionIndex=-1;
      for (int i=0; i<sectionIndices.length; i++) {
        if (sectionIndices[i]==position) {
          sectionIndex=i;
          break;
        }
      }

      if (sectionIndex==-1) {
        view=inflater.inflate(R.layout.filter_list_item, parent, false);
        Tournament tournament=tournaments.get(position);

        TextView titleTV=(TextView) view.findViewById(R.id.filter_title);
        titleTV.setText(tournament.title);

        TextView labelTV=(TextView) view.findViewById(R.id.filter_label);
        labelTV.setText(tournament.label);

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
      } else {
        view=inflater.inflate(R.layout.list_group_small, parent, false);
        Character letter=sections[sectionIndex];

        TextView textView=(TextView) view.findViewById(R.id.sg_name);
        textView.setText(""+letter);

        view.setBackgroundResource(R.drawable.group_collapsed);
      }

      return view;
    }

    public Object[] getSections() {
      return sections;
    }

    public int getPositionForSection(int i) {
      return sectionIndices[i];
    }

    public int getSectionForPosition(int position) {
      for (int i=0; i<sectionIndices.length; i++) {
        if (sectionIndices[i]==position) {
          return i;
        }
      }
      return 0;
    }
  }

  class SaveTournamentVisible extends AsyncTask<Integer, Integer, Void> {
    public List<Tournament> changedTournaments;

    @Override
    protected void onPreExecute() {
      super.onPreExecute();

      dialog.setCancelable(false);
      dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
      dialog.setTitle("Saving, please wait...");
      dialog.show();

      changedTournaments=new ArrayList<>();
      Tournament tournament;
      for (int i=0; i<initialVisible.length; i++) {
        tournament=tournaments.get(i);
        if (initialVisible[i]^tournament.visible && tournament.id<SECTION_TOURNAMENT_ID) {
          changedTournaments.add(tournament);
        }
      }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
      if (dialog.isShowing()) {
        dialog.dismiss();
      }
      finish();

      super.onPostExecute(aVoid);
    }

    @Override
    protected Void doInBackground(Integer... params) {
      WBCDataDbHelper dbHelper=new WBCDataDbHelper(getBaseContext());
      dbHelper.getWritableDatabase();
      dbHelper.updateTournamentsVisible(changedTournaments);
      dbHelper.close();

      return null;
    }
  }
}
