package org.boardgamers.wbc;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Kevin Broh-Kahn
 */
public class UserDataListAdapter extends DefaultScheduleListAdapter {
  private final String TAG="User Data List Adapter";

  private final int EVENTS_INDEX=0;
  private final int NOTES_INDEX=1;
  private final int FINISHES_INDEX=2;

  public UserDataListAdapter(Context c, UserDataFragment f) {
    super(c, f);
  }

  @Override
  public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild,
                           View view, ViewGroup parent) {
    if (groupPosition==EVENTS_INDEX) {
      view=super.getChildView(groupPosition, childPosition, isLastChild, view, parent);

      view.setOnLongClickListener(new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
          AlertDialog.Builder builder=new AlertDialog.Builder(null);

          builder.setTitle("Choose an action");
          builder.setMessage("What do you want to do?");

          builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              deleteEvents(childPosition);
            }
          });

          builder.setNeutralButton(R.string.edit, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              editEvent(childPosition);
            }
          });

          builder.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              dialog.dismiss();
            }
          });

          builder.create().show();

          return false;
        }
      });


      return view;
    } else if (groupPosition==NOTES_INDEX) {
      view=inflater.inflate(R.layout.schedule_item_text, parent, false);

      TextView text=(TextView) view.findViewById(R.id.si_text);
      text.setText(((UserDataFragment) fragment).getNote(childPosition));

      return view;
    } else if (groupPosition==FINISHES_INDEX) {
      view=inflater.inflate(R.layout.schedule_item_text, parent, false);

      TextView text=(TextView) view.findViewById(R.id.si_text);
      text.setText(((UserDataFragment) fragment).getFinish(childPosition));

      return view;
    } else {
      return null;
    }
  }

  public void changeEventStar(Event event, int groupPosition, int childPosition) {
    event.starred=!event.starred;

    if (event.starred) {
      MainActivity.addStarredEvent(event);
    } else {
      ArrayList<Event> events=
          MainActivity.dayList.get(event.day*MainActivity.GROUPS_PER_DAY+event.hour-6);
      for (Event tempEvent : events) {
        if (tempEvent.identifier.equalsIgnoreCase(event.identifier)) {
          tempEvent.starred=false;
          break;
        }
      }

      MainActivity.removeStarredEvent(event.identifier, event.day);

    }

    super.changeEventStar(event, groupPosition, childPosition);
  }

  @Override
  public String getGroupTitle(int groupPosition) {
    switch (groupPosition) {
      case EVENTS_INDEX:
        return fragment.getResources().getString(R.string.user_events);
      case NOTES_INDEX:
        return fragment.getResources().getString(R.string.user_notes);
      case FINISHES_INDEX:
        return fragment.getResources().getString(R.string.user_finishes);
      default:
        return null;
    }
  }

  @Override
  public Object getGroup(int groupPosition) {
    switch (groupPosition) {
      case EVENTS_INDEX:
        return ((UserDataFragment) fragment).userEvents;
      case NOTES_INDEX:
        return ((UserDataFragment) fragment).userNotes;
      case FINISHES_INDEX:
        return ((UserDataFragment) fragment).userFinishes;
    }
    return null;
  }

  @Override
  public int getGroupCount() {
    return 3;
  }

  private void editEvent(int index) {
    ((UserDataFragment) fragment).selectedEvent=index;
    DialogEditEvent editNameDialog=new DialogEditEvent();
    editNameDialog.show(MainActivity.activity.getFragmentManager(), "edit_event_dialog");
  }

  private void deleteEvents(int index) {
    ((UserDataFragment) fragment).selectedEvent=index;
    DialogDeleteEvent deleteDialog=new DialogDeleteEvent();
    deleteDialog.show(MainActivity.activity.getFragmentManager(), "delete_event_dialog");
  }
}
