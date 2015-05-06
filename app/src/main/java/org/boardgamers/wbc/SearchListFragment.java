package org.boardgamers.wbc;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.List;

public class SearchListFragment extends DefaultListFragment {
  private final String TAG="Search Fragment";

  private boolean allStarred;
  private ImageView star;
  private String query;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view=super.onCreateView(inflater, container, savedInstanceState);

    star=(ImageView) view.findViewById(R.id.sl_star);
    star.setVisibility(View.VISIBLE);

    int margin=(int) getResources().getDimension(R.dimen.activity_margin);
    LinearLayout.LayoutParams lp=
        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
    lp.setMargins(margin, margin, margin, margin);
    star.setLayoutParams(lp);

    star.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        changeAllStarred();
      }
    });

    setGameStarIV();

    return view;
  }

  /**
   * Game star button clicked - change allStarred boolean and update events
   */
  public void changeAllStarred() {
    allStarred=!allStarred;
    setGameStarIV();

    Event event;
    for (int i=0; i<listAdapter.events.size(); i++) {
      for (int j=0; j<listAdapter.events.get(i).size(); j++) {
        event=listAdapter.events.get(i).get(j);
        if (event.starred^allStarred) {
          ((MainActivity) getActivity().getParent()).changeEventStar(event);
        }
      }
    }
  }

  public void loadEvents(String q) {
    query=q;
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
    protected Integer doInBackground(Integer... params) {
      listAdapter=new SearchListAdapter(fragment, events);

      WBCDataDbHelper dbHelper=new WBCDataDbHelper(getActivity());
      List<Event> tempEvents=dbHelper.getEventsFromQuery(query);

      List<List<Event>> events=listAdapter.events;
      Event event;
      while (tempEvents.size()>0) {
        event=tempEvents.remove(0);
        events.get(event.day).add(event);
      }

      listAdapter.events=events;

      listAdapter.notifyDataSetChanged();

      return 1;
    }
  }
}
