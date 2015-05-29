package org.boardgamers.wbc;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Kevin Broh-Kahn
 */
public class UserDataListAdapter extends DefaultListAdapter {
  public UserDataListAdapter(DefaultListFragment f, List<List<Event>> e, int i) {
    super(f, e, i);
  }

  @Override
  public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild,
                           View view, ViewGroup parent) {
    view=super.getChildView(groupPosition, childPosition, isLastChild, null, parent);

    Event event=events.get(groupPosition).get(childPosition);

    if (groupPosition==UserDataListFragment.EVENTS_INDEX) {
      TextView title=(TextView) view.findViewById(R.id.li_hour);
      title.setText(dayStrings[event.day]+"\n"+String.valueOf(event.hour*100));

      view.setOnLongClickListener(new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
          showDeleteDialog(childPosition);
          return true;
        }
      });

      return view;
    } else {
      String titleString;

      if (groupPosition==UserDataListFragment.FINISHES_INDEX) {
        titleString=event.title;
      } else {
        titleString=event.title+": "+event.note;
      }

      view.findViewById(R.id.li_star).setVisibility(View.INVISIBLE);
      view.findViewById(R.id.li_hour).setVisibility(View.GONE);
      view.findViewById(R.id.li_duration).setVisibility(View.GONE);
      view.findViewById(R.id.li_location).setVisibility(View.GONE);

      TextView title=(TextView) view.findViewById(R.id.li_title);
      title.setText(titleString);

      return view;
    }
  }

  @Override
  public String getGroupTitle(int groupPosition) {
    switch (groupPosition) {
      case UserDataListFragment.EVENTS_INDEX:
        return fragment.getResources().getString(R.string.user_events);
      case UserDataListFragment.NOTES_INDEX:
        return fragment.getResources().getString(R.string.user_notes);
      case UserDataListFragment.FINISHES_INDEX:
        return fragment.getResources().getString(R.string.user_finishes);
      default:
        return null;
    }
  }

  @Override
  public void updateEvents(Event[] updatedEvents) {
    boolean inList;
    Event tempEvent;

    for (Event event : updatedEvents) {
      if (event.id<MainActivity.USER_EVENT_ID) {
        continue;
      }

      inList=false;
      for (int i=0; i<events.get(UserDataListFragment.EVENTS_INDEX).size(); i++) {
        tempEvent=events.get(UserDataListFragment.EVENTS_INDEX).get(i);
        if (tempEvent.id==event.id) {
          tempEvent.starred=event.starred;
          inList=true;
          break;
        }
      }

      if (!inList) {
        int index;
        for (index=0; index<events.get(UserDataListFragment.EVENTS_INDEX).size(); index++) {
          tempEvent=events.get(0).get(index);
          if (tempEvent.day*24+tempEvent.hour>event.day*24+event.hour ||
              (tempEvent.day*24+tempEvent.hour==event.day*24+event.hour &&
                  tempEvent.title.compareToIgnoreCase(event.title)==-1)) {
            tempEvent.starred=event.starred;
            break;
          }
        }
        events.get(UserDataListFragment.EVENTS_INDEX).add(index, event);
      }
    }
  }

  public void showDeleteDialog(final int index) {
    Event event=events.get(UserDataListFragment.EVENTS_INDEX).get(index);

    String dayString=fragment.getResources().getStringArray(R.array.days)[event.day];
    String title="Confirm";
    String message="Are you sure you want to delete "+event.title+" on "+dayString+" at "+
        String.valueOf(event.hour)+"?";

    AlertDialog.Builder deleteDialog=new AlertDialog.Builder(fragment.getActivity());
    deleteDialog.setTitle(title).setMessage(message)
        .setNegativeButton(fragment.getResources().getString(R.string.cancel),
            new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
              }
            }).setPositiveButton("Yes, "+fragment.getResources().getString(R.string.delete),
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            deleteEvent(index);
            dialog.dismiss();
          }
        });
    deleteDialog.create().show();

  }

  public void deleteEvent(int index) {
    WBCDataDbHelper dbHelper=new WBCDataDbHelper(fragment.getActivity());
    dbHelper.getWritableDatabase();
    dbHelper.deleteUserEvent(MainActivity.userId,
        events.get(UserDataListFragment.EVENTS_INDEX).get(index).id);
    dbHelper.close();

    Event[] deletedEvents=new Event[] {events.get(UserDataListFragment.EVENTS_INDEX).remove(index)};
    notifyDataSetChanged();
    ((UserDataListFragment) fragment).deleteEvents(deletedEvents);
  }
}
