package org.boardgamers.wbc;

import android.os.Bundle;

import java.util.List;

public class SummaryFragment extends DefaultListFragment {
  private List<Event> starredEvents;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    WBCDataDbHelper dbHelper=new WBCDataDbHelper(f.getActivity());
    starredEvents=dbHelper.getStarredEvents();
  }

  public void startLoadTask() {
    new LoadSummaryAdapterClass(this).doInBackground(0, 0, 0);
  }

  class LoadSummaryAdapterClass extends LoadAdapterClass {
    public LoadSummaryAdapterClass(SummaryFragment f) {
      fragment=f;
    }

    @Override
    protected void onPostExecute(Integer integer) {
      listView.setSelectedGroup(MainActivity.getCurrentGroup()/MainActivity.GROUPS_PER_DAY);

      super.onPostExecute(integer);
    }

    @Override
    protected Integer doInBackground(Integer... params) {
      listAdapter=new SummaryListAdapter(getActivity(), (SummaryFragment) fragment);

      return super.doInBackground(params);
    }
  }
}
