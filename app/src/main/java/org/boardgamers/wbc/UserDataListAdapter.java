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
  private final String TAG="User Data List Adapter";

  private final int EVENTS_INDEX=0;
  private final int NOTES_INDEX=1;
  private final int FINISHES_INDEX=2;

  public UserDataListAdapter(DefaultListFragment f, List<List<Event>> e, int i) {
    super(f, e, i);
  }

  @Override
  public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild,
                           View view, ViewGroup parent) {
    if (groupPosition==EVENTS_INDEX) {
      view=super.getChildView(groupPosition, childPosition, isLastChild, view, parent);

      view.setOnLongClickListener(new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
          AlertDialog.Builder builder=new AlertDialog.Builder(fragment.getActivity());

          builder.setTitle("Choose an action");
          builder.setMessage("What do you want to do?");

          builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              showDeleteDialog(childPosition);
            }
          });

          builder.setNeutralButton(R.string.edit, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              ((UserDataListFragment) fragment).editEvent(childPosition);
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
    } else {
      Event event=events.get(groupPosition).get(childPosition);
      view=inflater.inflate(R.layout.list_item_text, parent, false);

      if (groupPosition==NOTES_INDEX) {
        TextView text=(TextView) view.findViewById(R.id.li_text);
        text.setText(event.title+": "+event.note);

        if (childPosition%2==0) {
          view.setBackgroundResource(R.drawable.future_light);
        } else {
          view.setBackgroundResource(R.drawable.future_dark);
        }

      } else if (groupPosition==FINISHES_INDEX) {
        view=inflater.inflate(R.layout.list_item_text, parent, false);

        // event.title set to tournament.title
        TextView text=(TextView) view.findViewById(R.id.li_text);
        text.setText(event.title+": "+String.valueOf(event.note));

        if (childPosition%2==0) {
          view.setBackgroundResource(R.drawable.ended_light);
        } else {
          view.setBackgroundResource(R.drawable.ended_dark);
        }
      }
      return view;
    }
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
  public void updateStarredEvent(Event event) {
    super.updateStarredEvent(event);

    Event tempEvent;

    for (int i=0; i<events.get(0).size(); i++) {
      tempEvent=events.get(0).get(i);
      if (tempEvent.id==event.id) {
        tempEvent.starred=event.starred;
        break;
      }
    }
  }

  public void showDeleteDialog(final int index) {
    Event event=events.get(EVENTS_INDEX).get(index);

    String dayString=fragment.getResources().getStringArray(R.array.days)[event.day];
    String title="Confirm";
    String message="Are you sure you want to delete "+event.title+" on "+dayString+" at "+
        String.valueOf(event.hour)+"?";

    AlertDialog.Builder deleteDialog=new AlertDialog.Builder(fragment.getActivity());
    deleteDialog.setTitle(title).setMessage(message)
        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            dialog.dismiss();
          }
        }).setNegativeButton("Yes, "+R.string.delete, new DialogInterface.OnClickListener() {
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
    dbHelper.deleteEvent(events.get(EVENTS_INDEX).get(index).id);
    dbHelper.close();
  }
}
