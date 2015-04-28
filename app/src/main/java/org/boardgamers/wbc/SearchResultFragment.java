package org.boardgamers.wbc;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public class SearchResultFragment extends DefaultListFragment {
  //private final String TAG="Search Fragment";

  private List<List<Event>> resultEvents;
  private boolean allStarred;
  private ImageView star;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    resultEvents=new ArrayList<>();
    for (int i=0; i<MainActivity.TOTAL_DAYS; i++) {
      resultEvents.add(new ArrayList<Event>());
    }

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
      @Override public void onClick(View v) {
        changeAllStarred();
      }
    });

    setGameStarIV();

    if (MainActivity.currentDay>-1) {
      listView.setSelectedGroup(MainActivity.currentDay);
    }

    return view;
  }

  public void loadEvents(String query) {
    allStarred=true;

    resultEvents=new ArrayList<>();
    for (int i=0; i<MainActivity.TOTAL_DAYS; i++) {
      resultEvents.add(new ArrayList<Event>());
    }

    for (int i=0; i<MainActivity.dayList.size(); i++) {
      for (Event event : MainActivity.dayList.get(i)) {
        if (event.title.toLowerCase().contains(query) ||
            event.format.toLowerCase().contains(query)) {
          resultEvents.get(i/MainActivity.GROUPS_PER_DAY).add(event);
          if (!event.starred) {
            allStarred=false;
          }
        }
      }
    }

    listAdapter.notifyDataSetChanged();
  }

  @Override
  protected DefaultScheduleListAdapter getAdapter() {
    return new SearchListAdapter(getActivity(), this);
  }

  public List<Event> getGroup(int group) {
    return resultEvents.get(group);
  }

  /**
   * Game star button clicked - change allStarred boolean and update events
   */
  public void changeAllStarred() {
    allStarred=!allStarred;
    setGameStarIV();

    Event event;
    for (int i=0; i<resultEvents.size(); i++) {
      for (int j=0; j<resultEvents.get(i).size(); j++) {
        event=resultEvents.get(i).get(j);
        if (event.starred^allStarred) {
          ((SearchListAdapter) listAdapter).changeEventStar(event, i, j, false);
        }
      }
    }
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
    for (List<Event> events : resultEvents) {
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
}