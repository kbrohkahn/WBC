package org.boardgamers.wbc;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

/**
 * Created by Kevin
 */
public class DefaultListFragment extends Fragment {
  protected DefaultScheduleListAdapter listAdapter;
  protected ExpandableListView listView;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view=inflater.inflate(getLayoutId(), container, false);

    listView=(ExpandableListView) view.findViewById(R.id.default_list_view);
    listView.setDividerHeight(0);

    startLoadTask();

    return view;
  }

  public void startLoadTask() {}

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

  public void updateList() {
    if (listAdapter!=null) {
      listAdapter.notifyDataSetChanged();
    }
  }

  class LoadAdapterClass extends AsyncTask<Integer, Integer, Integer> {
    protected DefaultListFragment fragment;

    public LoadAdapterClass() {}

    @Override
    protected void onPostExecute(Integer integer) {
      if (listAdapter.getGroup(0)!=null) {
        expandGroups(listAdapter.getGroupCount(), 0, true);
      }

      listAdapter.hoursIntoConvention=MainActivity.getHoursIntoConvention();
      listAdapter.notifyDataSetChanged();

      super.onPostExecute(integer);
    }

    @Override
    protected Integer doInBackground(Integer... params) {
      listView.setAdapter(listAdapter);

      return null;
    }
  }
}
