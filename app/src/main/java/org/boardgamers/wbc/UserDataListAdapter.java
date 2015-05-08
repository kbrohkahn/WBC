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


  public UserDataListAdapter(DefaultListFragment f, List<List<Event>> e, int i) {
    super(f, e, i);
  }

  @Override
  public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild,
                           View view, ViewGroup parent) {
    view=super.getChildView(groupPosition, childPosition, isLastChild, view, parent);

    if (groupPosition==UserDataListFragment.EVENTS_INDEX) {
      view.setOnLongClickListener(new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
          showDeleteDialog(childPosition);
          return true;
        }
      });

      return view;
    } else {
      Event event=events.get(groupPosition).get(childPosition);

      view.findViewById(R.id.li_star).setVisibility(View.INVISIBLE);
      view.findViewById(R.id.li_hour).setVisibility(View.GONE);
      view.findViewById(R.id.li_duration).setVisibility(View.GONE);
      view.findViewById(R.id.li_location).setVisibility(View.GONE);

      TextView title=(TextView) view.findViewById(R.id.li_title);
      title.setText(event.title+": "+event.note);

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
    dbHelper.deleteEvent(events.get(UserDataListFragment.EVENTS_INDEX).get(index).id);
    dbHelper.close();
  }
}
