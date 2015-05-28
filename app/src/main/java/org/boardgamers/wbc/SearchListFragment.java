package org.boardgamers.wbc;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public class SearchListFragment extends DefaultListFragment {
  //private final String TAG="Search Fragment";

  private boolean allStarred;
  private ImageView star;
  private String query;
  private int id;
  private ProgressDialog dialog;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view=super.onCreateView(inflater, container, savedInstanceState);

    star=(ImageView) view.findViewById(R.id.sl_star);

    int margin=(int) getResources().getDimension(R.dimen.activity_margin);
    LinearLayout.LayoutParams lp=
        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
    lp.setMargins(margin, margin, margin, margin);
    star.setLayoutParams(lp);
    star.setVisibility(View.VISIBLE);
    star.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        new SaveEventData().execute();
      }
    });

    return view;
  }

  class SaveEventData extends AsyncTask<Integer, Integer, Void> {
    List<Event> changedEvents;

    @Override
    protected void onProgressUpdate(Integer... values) {
      super.onProgressUpdate(values);

      dialog.setProgress(values[0]);
    }

    @Override
    protected void onPreExecute() {
      super.onPreExecute();

      allStarred=!allStarred;
      setGameStarIV();

      dialog=new ProgressDialog(getActivity());
      dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
      dialog.setCancelable(false);
      dialog.setTitle("Saving, please wait...");
      dialog.show();

      // update events in list
      Event event;
      changedEvents=new ArrayList<>();
      for (int i=0; i<listAdapter.events.size(); i++) {
        for (int j=0; j<listAdapter.events.get(i).size(); j++) {
          event=listAdapter.events.get(i).get(j);
          if (event.starred^allStarred) {
            event.starred=!event.starred;
            changedEvents.add(event);
          }
        }
      }

      dialog.setMax(changedEvents.size());
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
      Event event;
      for (int i=0; i<changedEvents.size(); i++) {
        event=changedEvents.get(i);

        dbHelper.insertUserEventData(MainActivity.userId, event.id, event.starred, event.note);

        onProgressUpdate(i);
      }
      dbHelper.close();

      return null;
    }
  }

  public void changeEventStar(Event event) {
    List<Event> searchList=listAdapter.events.get(event.day);
    for (Event tempEvent : searchList) {
      if (event.id==tempEvent.id) {
        tempEvent.starred=event.starred;
        listAdapter.notifyDataSetChanged();
        setAllStared();
        return;
      }
    }
  }

  public void loadEvents(String q, int i) {
    query=q;
    id=i;

    new PopulateSearchAdapterTask(this, MainActivity.TOTAL_DAYS).execute(0, 0, 0);
  }

  /**
   * set tournamentEventsStarIV image resource
   */
  public void setGameStarIV() {
    star.setImageResource(allStarred ? R.drawable.star_on : R.drawable.star_off);
  }

  /**
   * Event star changed - check for change in allStarred boolean and set game star image view.
   * Call setGameStar before return
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

  class PopulateSearchAdapterTask extends PopulateAdapterTask {
    public PopulateSearchAdapterTask(DefaultListFragment f, int g) {
      fragment=f;
      numGroups=g;
    }

    @Override
    protected void onPostExecute(Integer integer) {
      listAdapter.notifyDataSetChanged();
      setAllStared();

      super.onPostExecute(integer);
    }

    @Override
    protected Integer doInBackground(Integer... params) {
      listAdapter=new SearchListAdapter(fragment, events, 3);

      WBCDataDbHelper dbHelper=new WBCDataDbHelper(getActivity());
      dbHelper.getReadableDatabase();

      List<Event> tempEvents;
      if (id>-1) {
        tempEvents=dbHelper.getTournamentEvents(MainActivity.userId, id);
      } else {
        tempEvents=dbHelper.getEventsFromQuery(MainActivity.userId, query);
      }
      dbHelper.close();

      List<List<Event>> events=listAdapter.events;
      Event event;
      while (tempEvents.size()>0) {
        event=tempEvents.remove(0);
        events.get(event.day).add(event);
      }

      listAdapter.events=events;

      return 1;
    }
  }

  @Override
  public void onPause() {
    if (dialog!=null && dialog.isShowing()) {
      dialog.dismiss();
    }

    super.onPause();
  }
}
