package org.boardgamers.wbc;

import java.util.List;

public class SearchListFragment extends DefaultListFragment {
  //private final String TAG="Search Fragment";

  private String query;
  private int tournamentId;

  /**
   * Called when event selected from search list, then star changed from Event Activity
   *
   * @param event - event whose star changed
   */
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

  /**
   * Load events, starting asynctask
   *
   * @param q - search query if search button pressed
   * @param i - tournament id if list item selected
   */
  public void loadEvents(String q, int i) {
    query=q;
    tournamentId=i;

    new PopulateSearchAdapterTask(this, MainActivity.TOTAL_DAYS).execute(0, 0, 0);
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
      if (tournamentId>-1) {
        tempEvents=dbHelper.getTournamentEvents(MainActivity.userId, tournamentId);
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
}
