package org.boardgamers.wbc;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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
  public static final int EVENTS_INDEX=0;
  public static final int NOTES_INDEX=1;
  public static final int FINISHES_INDEX=2;

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
      deleteAll.setOnClickListener(new View.OnClickListener() {
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

    listAdapter.events.remove(EVENTS_INDEX);
    listAdapter.events
        .add(EVENTS_INDEX, dbHelper.getTournamentEvents(MainActivity.userId, USER_TOURNAMENT_ID));

    dbHelper.close();

    refreshAdapter();
  }

  public void updateUserData(long eventId, String note, long tournamentId, int finish) {
    if (listAdapter==null || listAdapter.events==null) {
      return;
    }

    boolean noteInList=false;
    for (Event event : listAdapter.events.get(NOTES_INDEX)) {
      if (event.id==eventId) {
        noteInList=true;
        if (note.equalsIgnoreCase("")) {
          listAdapter.events.get(NOTES_INDEX).remove(event);
        } else {
          event.note=note;
        }

        break;
      }
    }

    boolean finishInList=false;
    for (Event event : listAdapter.events.get(FINISHES_INDEX)) {
      if (event.tournamentID==tournamentId) {
        finishInList=true;
        if (finish==0) {
          listAdapter.events.get(FINISHES_INDEX).remove(event);
        } else {
          event.note=String.valueOf(finish);
        }
        break;
      }
    }

    Event eventNote=null;
    Tournament tournamentFinish=null;

    WBCDataDbHelper dbHelper=new WBCDataDbHelper(getActivity());
    dbHelper.getReadableDatabase();
    if (!noteInList && !note.equalsIgnoreCase("")) {
      eventNote=dbHelper.getEvent(MainActivity.userId, eventId);
    }

    if (!finishInList && finish>0) {
      tournamentFinish=dbHelper.getTournament(MainActivity.userId, tournamentId);
    }

    int index;
    List<Event> searchList;
    if (eventNote!=null) {
      searchList=listAdapter.events.get(NOTES_INDEX);
      index=0;
      for (; index<searchList.size(); index++) {
        if (eventNote.title.compareToIgnoreCase(searchList.get(index).title)==1) {
          break;
        }
      }
      searchList.add(index, eventNote);
    }

    if (tournamentFinish!=null) {
      searchList=listAdapter.events.get(FINISHES_INDEX);
      index=0;
      for (; index<searchList.size(); index++) {
        if (tournamentFinish.title.compareToIgnoreCase(searchList.get(index).title)==1) {
          break;
        }
      }
      searchList.add(index, getEventFromTournament(tournamentFinish));
    }
    dbHelper.close();
  }

  public Event getEventFromTournament(Tournament tournament) {
    Event event=
        new Event(tournament.finalEventId, tournament.id, 0, 0, tournament.title, "", "", false, 0,
            false, 0, "");

    event.note=String.valueOf(tournament.finish);
    return event;
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
        .setNegativeButton(getResources().getString(R.string.cancel),
            new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
              }
            }).setPositiveButton("Yes, "+getResources().getString(R.string.delete),
        new DialogInterface.OnClickListener() {
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
      events.add(dbHelper.getTournamentEvents(MainActivity.userId, USER_TOURNAMENT_ID));
      events.add(dbHelper.getEventsWithNotes(MainActivity.userId));

      List<Tournament> tournamentFinishes=dbHelper.getTournamentsWithFinishes(MainActivity.userId);

      dbHelper.close();

      List<Event> eventFinishes=new ArrayList<>();
      for (Tournament tournament : tournamentFinishes) {
        eventFinishes.add(getEventFromTournament(tournament));
      }

      events.add(eventFinishes);

      listAdapter.events=events;

      return 1;
    }
  }
}
