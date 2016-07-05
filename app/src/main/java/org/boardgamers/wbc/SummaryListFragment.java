package org.boardgamers.wbc;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class SummaryListFragment extends DefaultListFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        new PopulateSummaryAdapterTask(getActivity(), Constants.TOTAL_DAYS).execute(0, 0, 0);

        return view;
    }

    class PopulateSummaryAdapterTask extends PopulateAdapterTask {
        public PopulateSummaryAdapterTask(Context c, int g) {
            context = c;
            numGroups = g;
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            listAdapter = new SummaryListAdapter(context, events, 0);

            WBCDataDbHelper dbHelper = new WBCDataDbHelper(getActivity());
            dbHelper.getReadableDatabase();
            List<Event> tempEvents = dbHelper.getStarredEvents(MainActivity.userId);
            dbHelper.close();

            List<List<Event>> events = listAdapter.events;
            Event event;
            while (tempEvents.size() > 0) {
                event = tempEvents.remove(0);
                events.get(event.day).add(event);
            }

            return 1;
        }
    }

    class SummaryListAdapter extends DefaultListAdapter {
        public SummaryListAdapter(Context c, List<List<Event>> e, int i) {
            super(c, e, i);
        }

        @Override
        public String getGroupTitle(final int groupPosition) {
            return dayStrings[groupPosition];
        }

        @Override
        public void updateEvents(Event[] updatedEvents) {
            Event tempEvent;

            for (Event event : updatedEvents) {
                if (event.starred) {
                    int index;
                    for (index = 0; index < events.get(event.day).size(); index++) {
                        tempEvent = events.get(event.day).get(index);
                        if (event.hour < tempEvent.hour ||
                                (event.hour == tempEvent.hour && event.title.compareToIgnoreCase(tempEvent.title) == 1)) {
                            break;
                        }
                    }
                    events.get(event.day).add(index, event);
                } else {
                    for (int i = 0; i < events.get(event.day).size(); i++) {
                        tempEvent = events.get(event.day).get(i);
                        if (tempEvent.id == event.id) {
                            events.get(event.day).remove(tempEvent);
                            break;
                        }
                    }
                }
            }
        }
    }

}
