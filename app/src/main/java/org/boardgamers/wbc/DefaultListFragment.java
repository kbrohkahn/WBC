package org.boardgamers.wbc;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kevin
 */
public class DefaultListFragment extends Fragment {
  private final String TAG="Default List Fragment";

  protected DefaultListAdapter listAdapter;
  protected ExpandableListView listView;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view=inflater.inflate(getLayoutId(), container, false);

    listView=(ExpandableListView) view.findViewById(R.id.default_list_view);

    return view;
  }

  public void refreshAdapter() {
    if (listAdapter!=null && !MainActivity.updatingFragments) {
      listAdapter.updateList();
    }
  }

  protected int getLayoutId() {
    return R.layout.default_list;
  }

  public void expandGroups(int count, int start, boolean all) {
    if (all) {
      for (int i=start; i<count; i++) {
        listView.expandGroup(i);
      }
    } else {
      listView.expandGroup(start);
    }
  }

  public void collapseGroups(int count, int start, boolean all) {
    if (all) {
      for (int i=start; i<count; i++) {
        listView.collapseGroup(i);
      }
    } else {
      listView.collapseGroup(start);
    }
  }

  public void updateStarredEvent(Event event) {
    if (listAdapter!=null) {
      listAdapter.updateStarredEvent(event);
    }
  }

  class PopulateAdapterTask extends AsyncTask<Integer, Integer, Integer> {
    protected DefaultListFragment fragment;
    protected List<List<Event>> events;
    protected int numGroups;

    @Override
    protected void onPostExecute(Integer integer) {
      listView.setAdapter(listAdapter);

      expandGroups(numGroups, 0, true);

      listAdapter.events=events;
      listAdapter.hoursIntoConvention=MainActivity.getHoursIntoConvention();
      listAdapter.notifyDataSetChanged();

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

}
