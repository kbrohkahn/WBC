package org.boardgamers.wbc;

import android.support.v7.app.AppCompatActivity;

public class FilterActivity extends AppCompatActivity {
  //  //private final String TAG="Filter Activity";
  //
  //  private boolean[] initialVisible;
  //  private List<Tournament> tournaments;
  //
  //  protected void onCreate(Bundle savedInstanceState) {
  //    super.onCreate(savedInstanceState);
  //
  //    setContentView(R.layout.filter);
  //
  //    Toolbar toolbar=(Toolbar) findViewById(R.id.toolbar);
  //    setSupportActionBar(toolbar);
  //    if (getSupportActionBar()!=null) {
  //      getSupportActionBar().setDisplayShowHomeEnabled(true);
  //      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
  //    }
  //
  //    WBCDataDbHelper dbHelper=new WBCDataDbHelper(this);
  //    dbHelper.getReadableDatabase();
  //    tournaments=dbHelper.getAllTournaments(MainActivity.userId);
  //    dbHelper.close();
  //
  //    initialVisible=new boolean[tournaments.size()];
  //    for (int i=0; i<initialVisible.length; i++) {
  //      initialVisible[i]=tournaments.get(i).visible;
  //    }
  //
  //    ListView listView=(ListView) findViewById(R.id.filter_list_view);
  //    final FilterListAdapter listAdapter=new FilterListAdapter();
  //    listView.setAdapter(listAdapter);
  //
  //    // select all image button
  //    Button selectAll=(Button) findViewById(R.id.filter_select_all);
  //    selectAll.setOnClickListener(new View.OnClickListener() {
  //      @Override
  //      public void onClick(View v) {
  //        for (Tournament tournament : tournaments) {
  //          tournament.visible=true;
  //        }
  //        listAdapter.notifyDataSetChanged();
  //      }
  //    });
  //    selectAll.setOnLongClickListener(new View.OnLongClickListener() {
  //      @Override
  //      public boolean onLongClick(View v) {
  //        showToast(getResources().getString(R.string.select_all));
  //        return false;
  //      }
  //    });
  //
  //    // select non tournament image button
  //    Button selectTournaments=(Button) findViewById(R.id.filter_select_tournament);
  //    selectTournaments.setOnClickListener(new View.OnClickListener() {
  //      @Override
  //      public void onClick(View v) {
  //        for (Tournament tournament : tournaments) {
  //          if (tournament.isTournament) {
  //            tournament.visible=true;
  //          }
  //        }
  //        listAdapter.notifyDataSetChanged();
  //      }
  //    });
  //    selectTournaments.setOnLongClickListener(new View.OnLongClickListener() {
  //      @Override
  //      public boolean onLongClick(View v) {
  //        showToast(getResources().getString(R.string.select_tournament));
  //        return false;
  //      }
  //    });
  //
  //    // deselect all image button
  //    Button deselectAll=(Button) findViewById(R.id.filter_deselect_all);
  //    deselectAll.setOnClickListener(new View.OnClickListener() {
  //      @Override
  //      public void onClick(View v) {
  //        for (Tournament tournament : tournaments) {
  //          tournament.visible=false;
  //
  //        }
  //        listAdapter.notifyDataSetChanged();
  //
  //      }
  //    });
  //    deselectAll.setOnLongClickListener(new View.OnLongClickListener() {
  //      @Override
  //      public boolean onLongClick(View v) {
  //        showToast(getResources().getString(R.string.deselect_all));
  //        return false;
  //      }
  //    });
  //
  //    // deselect non tournament image button
  //    Button deselectTournaments=(Button) findViewById(R.id.filter_deselect_tournament);
  //    deselectTournaments.setOnClickListener(new View.OnClickListener() {
  //      @Override
  //      public void onClick(View v) {
  //        for (Tournament tournament : tournaments) {
  //          if (tournament.isTournament) {
  //            tournament.visible=false;
  //          }
  //        }
  //        listAdapter.notifyDataSetChanged();
  //      }
  //    });
  //    deselectTournaments.setOnLongClickListener(new View.OnLongClickListener() {
  //      @Override
  //      public boolean onLongClick(View v) {
  //        showToast(getResources().getString(R.string.deselect_tournament));
  //        return false;
  //      }
  //    });
  //  }
  //
  //  private void showToast(String string) {
  //    Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
  //  }
  //
  //  @Override
  //  public void onPause() {
  //    WBCDataDbHelper dbHelper=new WBCDataDbHelper(this);
  //    dbHelper.getWritableDatabase();
  //    Tournament tournament;
  //    for (int i=0; i<initialVisible.length; i++) {
  //      tournament=tournaments.get(i);
  //      if (initialVisible[i]^tournament.visible) {
  //        //dbHelper.updateTournamentVisible(MainActivity.userId, tournament.id, tournament.visible);
  //      }
  //    }
  //    dbHelper.close();
  //
  //    super.onPause();
  //  }
  //
  //  @Override
  //  public boolean onOptionsItemSelected(MenuItem item) {
  //    int id=item.getItemId();
  //
  //    if (id==android.R.id.home) {
  //      finish();
  //      return true;
  //    } else {
  //      return super.onOptionsItemSelected(item);
  //    }
  //
  //  }
  //
  //  public class FilterListAdapter extends BaseAdapter {
  //    LayoutInflater inflater;
  //
  //    public FilterListAdapter() {
  //      inflater=(LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  //    }
  //
  //    @Override
  //    public int getCount() {
  //      return tournaments.size();
  //    }
  //
  //    @Override
  //    public boolean hasStableIds() {
  //      return true;
  //    }
  //
  //    @Override
  //    public long getItemId(int position) {
  //      return position;
  //    }
  //
  //    @Override
  //    public Object getItem(int position) {
  //      return tournaments.get(position);
  //    }
  //
  //    @Override
  //    public View getView(int position, View view, ViewGroup parent) {
  //      if (view==null) {
  //        view=inflater.inflate(R.layout.filter_list_item, parent, false);
  //      }
  //
  //      Tournament tournament=tournaments.get(position);
  //      CheckBox checkBox=(CheckBox) view.findViewById(R.id.filter_list_checkbox);
  //      checkBox.setText(tournament.title);
  //      checkBox.setChecked(tournament.visible);
  //
  //      return view;
  //    }
  //
  //  }

}
