package org.boardgamers.wbc;

import java.util.List;

/**
 * Created by Kevin
 * Adapter for SummaryFragment ExpandableListView
 */
public class SummaryListAdapter extends DefaultListAdapter {
  public SummaryListAdapter(DefaultListFragment f, List<List<Event>> e, int i) {
    super(f, e, i);
  }

  @Override
  public String getGroupTitle(final int groupPosition) {
    return dayStrings[groupPosition];
  }

  @Override
  public void updateEvent(Event event) {
    Event tempEvent;

    if (event.starred) {
      int index;
      for (index=0; index<events.get(event.day).size(); index++) {
        tempEvent=events.get(event.day).get(index);
        if (event.hour<tempEvent.hour ||
            (event.hour==tempEvent.hour && event.title.compareToIgnoreCase(tempEvent.title)==1)) {
          break;
        }
      }
      events.get(event.day).add(index, event);
    } else {
      for (int i=0; i<events.get(event.day).size(); i++) {
        tempEvent=events.get(event.day).get(i);
        if (tempEvent.id==event.id) {
          events.get(event.day).remove(tempEvent);
          break;
        }
      }
    }
  }
}
