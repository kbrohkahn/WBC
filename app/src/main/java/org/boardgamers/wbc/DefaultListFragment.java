package org.boardgamers.wbc;

import android.app.Fragment;
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

    listAdapter=getAdapter();
    listView.setAdapter(listAdapter);
    listView.setDividerHeight(0);

    if (listAdapter.getGroup(0)!=null) {
      expandGroups(listAdapter.getGroupCount(), 0, true);
    }

    return view;
  }

  protected int getLayoutId() {
    return R.layout.default_list;
  }

  protected DefaultScheduleListAdapter getAdapter() {
    return null;
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
}
