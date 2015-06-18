package org.boardgamers.wbc;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kevin
 */
public class DefaultListFragment extends Fragment {
  //private final String TAG="Default List Fragment";

  protected DefaultListAdapter listAdapter;
  protected ExpandableListView listView;

  private boolean allStarred;
  private ImageView star;
  private List<Event> changedEvents;
  private ProgressDialog dialog;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view=inflater.inflate(getLayoutId(), container, false);

    listView=(ExpandableListView) view.findViewById(R.id.default_list_view);

    star=(ImageView) view.findViewById(R.id.sl_star);
    star.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        showSaveDialog();
      }
    });

    return view;
  }

  public void showSaveDialog() {
    changedEvents=listAdapter.getChangedEvents(!allStarred);

    AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
    builder.setTitle("Are you sure?").setMessage(
        "Do you really want to change the star for "+String.valueOf(changedEvents.size())+
            " events?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
        new SaveEventData().execute();
      }
    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
      }
    }).setCancelable(true);

    builder.create().show();
  }

  /**
   * set tournamentEventsStarIV image resource
   */
  public void setGameStarIV() {
    star.setImageResource(allStarred ? R.drawable.star_on : R.drawable.star_off);
  }

  /**
   * Event star changed - check for change in allStarred boolean and set game star image view. Call
   * setGameStar before return
   */
  public void setAllStared() {
    allStarred=true;
    for (List<Event> events : listAdapter.events) {
      for (Event event : events) {
        if (!event.starred) {
          allStarred=false;
          setGameStarIV();
          return;
        }
      }
    }
    setGameStarIV();
  }

  public void expandGroups(int group, int count) {
    for (int i=group; i<group+count; i++) {
      listView.expandGroup(i);
    }
  }

  public void collapseGroups(int group, int count) {
    for (int i=group; i<group+count; i++) {
      listView.collapseGroup(i);
    }
  }

  public void refreshAdapter() {
    listAdapter.updateList();
  }

  protected int getLayoutId() {
    return R.layout.default_list;
  }

  public void updateEvents(Event[] events) {
    listAdapter.updateEvents(events);
    listAdapter.notifyDataSetChanged();

    setAllStared();

  }

  public void removeEvents(Event[] events) {
    listAdapter.removeEvents(events);
    listAdapter.notifyDataSetChanged();

    setAllStared();
  }

  public void reloadAdapterData() {

  }

  class PopulateAdapterTask extends AsyncTask<Integer, Integer, Integer> {
    protected DefaultListFragment fragment;
    protected List<List<Event>> events;
    protected int numGroups;

    @Override
    protected void onPostExecute(Integer integer) {
      listView.setAdapter(listAdapter);

      for (int i=0; i<numGroups; i++) {
        listView.expandGroup(i);
      }

      listAdapter.events=events;
      listAdapter.hoursIntoConvention=UpdateService.getHoursIntoConvention();
      listAdapter.notifyDataSetChanged();

      setAllStared();

      super.onPostExecute(integer);
    }

    @Override
    protected void onPreExecute() {
      super.onPreExecute();

      events=new ArrayList<>();
      for (int i=0; i<numGroups; i++) {
        events.add(new ArrayList<Event>());
      }
    }

    @Override
    protected Integer doInBackground(Integer... params) {
      return -1;
    }
  }

  class SaveEventData extends AsyncTask<Integer, Integer, Void> {
    @Override
    protected void onPreExecute() {
      super.onPreExecute();

      dialog=new ProgressDialog(getActivity());
      dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
      dialog.setCancelable(false);
      dialog.setTitle("Saving, please wait...");
      dialog.show();

      allStarred=!allStarred;
      setGameStarIV();

      for (Event event : changedEvents) {
        event.starred=!event.starred;
      }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
      // update events in Main lists
      Event[] changedEventsArray=new Event[changedEvents.size()];
      MainActivity.changeEventsInLists(changedEvents.toArray(changedEventsArray), -1);

      if (dialog.isShowing()) {
        dialog.dismiss();
      }
      refreshAdapter();
      super.onPostExecute(aVoid);
    }

    @Override
    protected Void doInBackground(Integer... params) {
      // save events in DB
      WBCDataDbHelper dbHelper=new WBCDataDbHelper(getActivity());
      dbHelper.getWritableDatabase();
      dbHelper.insertUserEventData(MainActivity.userId, changedEvents);
      dbHelper.close();

      return null;
    }
  }

  @Override
  public void onPause() {

    if (dialog!=null&&dialog.isShowing()) {
      dialog.dismiss();
    }
    super.onPause();
  }
}
