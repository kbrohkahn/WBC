package org.boardgamers.wbc;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment containing user's WBC data, including tournament finishes, help notes, and created events
 */
public class UserDataListFragment extends DefaultListFragment {
  private final String TAG="My WBC Data Activity";

  public static final int USER_TOURNAMENT_ID=1000;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view=super.onCreateView(inflater, container, savedInstanceState);

    if (view!=null) {
      Button addEvent=(Button) view.findViewById(R.id.add_event);
      addEvent.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          showCreateDialog();
        }
      });

      Button deleteAll=(Button) view.findViewById(R.id.delete_all);
      addEvent.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          showDeleteDialog();
        }
      });
    }

    new PopulateUserDataAdapterTask(this, 3).execute(0, 0, 0);
    return view;
  }

  public void reloadUserEvents() {
    WBCDataDbHelper dbHelper=new WBCDataDbHelper(getActivity());
    dbHelper.getReadableDatabase();

    listAdapter.events.remove(0);
    listAdapter.events.add(0, dbHelper.getTournamentEvents(USER_TOURNAMENT_ID));

    dbHelper.close();

    refreshAdapter();
  }

  public void updateUserData(long eventId, String note, long tournamentId, int finish) {
    for (Event event : listAdapter.events.get(1)) {
      if (event.id==eventId) {
        event.note=note;
        break;
      }
    }

    for (Event event : listAdapter.events.get(2)) {
      if (event.tournamentID==tournamentId) {
        event.note=String.valueOf(finish);
        break;
      }
    }
  }

  @Override
  protected int getLayoutId() {
    return R.layout.user_data;
  }

  public void editEvent(int index) {
    MainActivity.SELECTED_EVENT_ID=index;
    DialogEditEvent editNameDialog=new DialogEditEvent();
    editNameDialog.show(getFragmentManager(), "edit_event_dialog");
  }

  public void showCreateDialog() {
    DialogCreateEvent dialog=new DialogCreateEvent();
    dialog.setTargetFragment(this, 0);
    dialog.show(getFragmentManager(), "create_event_dialog");
  }

  public void showDeleteDialog() {
    String title="Confirm";
    String message="Are you sure you want to delete all your events";

    AlertDialog.Builder deleteDialog=new AlertDialog.Builder(getActivity());
    deleteDialog.setTitle(title).setMessage(message)
        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            dialog.dismiss();
          }
        }).setNegativeButton("Yes, "+R.string.delete, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int id) {
        deleteAllEvents();
        dialog.dismiss();
      }
    });
    deleteDialog.create().show();

  }

  public void deleteAllEvents() {
    WBCDataDbHelper dbHelper=new WBCDataDbHelper(getActivity());
    dbHelper.getWritableDatabase();
    dbHelper.deleteTournamentEvents(USER_TOURNAMENT_ID);
    dbHelper.close();

    listAdapter.events.get(0).clear();

    refreshAdapter();
  }

  class PopulateUserDataAdapterTask extends PopulateAdapterTask {
    public PopulateUserDataAdapterTask(DefaultListFragment f, int g) {
      fragment=f;
      numGroups=g;
    }

    @Override
    protected Integer doInBackground(Integer... params) {
      listAdapter=new UserDataListAdapter(fragment, events, 2);

      WBCDataDbHelper dbHelper=new WBCDataDbHelper(getActivity());
      dbHelper.getReadableDatabase();

      events=new ArrayList<>();
      events.add(dbHelper.getTournamentEvents(USER_TOURNAMENT_ID));
      events.add(dbHelper.getEventsWithNotes());

      List<Tournament> tournamentFinishes=dbHelper.getTournamentsWithFinishes();

      dbHelper.close();

      List<Event> eventFinishes=new ArrayList<>();
      Event event;
      for (Tournament tournament : tournamentFinishes) {
        event=new Event(tournament.finalEventId, "", tournament.id, 0, 0, tournament.title, "",
            "", false, 0, false, 0, "");
        event.note=String.valueOf(tournament.finish);

        eventFinishes.add(event);
      }

      events.add(eventFinishes);

      listAdapter.events=events;

      return 1;
    }
  }
}
