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
 * Fragment containing user's WBC data, including tournament finishes, event notes, and created
 * events
 */
public class UserDataListFragment extends DefaultListFragment {
  //private final String TAG="My WBC Data Activity";

  public static final int EVENTS_INDEX=0;
  public static final int NOTES_INDEX=1;
  public static final int FINISHES_INDEX=2;

  public String[] finishStrings;

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

    finishStrings=getResources().getStringArray(R.array.finish_strings);

    new PopulateUserDataAdapterTask(this, 3).execute(0, 0, 0);
    return view;
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
          event.title=event.title.substring(0, event.title.length()-event.note.length());
          event.note=note;
          event.title+=note;
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
          event.title=event.title.substring(0, event.title.length()-3);
          event.note=String.valueOf(finish);
          event.title+=finishStrings[finish-1];
        }
        break;
      }
    }

    WBCDataDbHelper dbHelper=new WBCDataDbHelper(getActivity());
    dbHelper.getReadableDatabase();

    if (!noteInList && !note.equalsIgnoreCase("")) {
      Event eventNote=dbHelper.getEvent(MainActivity.userId, eventId);
      eventNote.title+=": "+eventNote.note;

      int noteIndex;
      List<Event> notesSearchList=listAdapter.events.get(NOTES_INDEX);
      noteIndex=0;
      for (; noteIndex<notesSearchList.size(); noteIndex++) {
        if (eventNote.title.compareToIgnoreCase(notesSearchList.get(noteIndex).title)==1) {
          break;
        }
      }
      notesSearchList.add(noteIndex, eventNote);
    }

    if (!finishInList && finish>0) {
      Tournament tournamentFinish=dbHelper.getTournament(MainActivity.userId, tournamentId);
      Event eventFinish=dbHelper.getEvent(MainActivity.userId, tournamentFinish.finalEventId);

      eventFinish.title+=": "+finishStrings[tournamentFinish.finish-1];
      eventFinish.note=String.valueOf(tournamentFinish.finish);

      List<Event> finishesSearchList=listAdapter.events.get(FINISHES_INDEX);
      int finishIndex=0;
      for (; finishIndex<finishesSearchList.size(); finishIndex++) {
        if (tournamentFinish.title.compareToIgnoreCase(finishesSearchList.get(finishIndex).title)==
            1) {
          break;
        }
      }
      finishesSearchList.add(finishIndex, eventFinish);
    }

    dbHelper.close();
  }

  @Override
  protected int getLayoutId() {
    return R.layout.user_data;
  }

  //  public void editEvent(int index) {
  //    MainActivity.selectedEventId=index;
  //    DialogEditEvent editNameDialog=new DialogEditEvent();
  //    editNameDialog.show(getFragmentManager(), "edit_event_dialog");
  //  }

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
    dbHelper.deleteAllUserEvents(MainActivity.userId);
    dbHelper.close();

    int count=listAdapter.events.get(EVENTS_INDEX).size();
    Event[] events=new Event[count];
    while (count>0) {
      events[count-1]=listAdapter.events.get(EVENTS_INDEX).remove(count-1);
      count--;
    }
    refreshAdapter();
    deleteEvents(events);
  }

  public void deleteEvents(Event[] events) {
    MainActivity.removeEvents(events);
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
      events.add(dbHelper.getUserEvents(MainActivity.userId, ""));
      List<Event> eventNotes=dbHelper.getEventsWithNotes(MainActivity.userId);
      List<Tournament> tournamentFinishes=dbHelper.getTournamentsWithFinishes(MainActivity.userId);

      for (Event event : eventNotes) {
        event.title+=": "+event.note;
      }
      events.add(eventNotes);

      List<Event> eventFinishes=new ArrayList<>();
      for (Tournament tournament : tournamentFinishes) {
        Tournament tournamentFinish=dbHelper.getTournament(MainActivity.userId, tournament.id);
        Event eventFinish=dbHelper.getEvent(MainActivity.userId, tournamentFinish.finalEventId);

        eventFinish.title+=": "+finishStrings[tournamentFinish.finish-1];
        eventFinish.note=String.valueOf(tournamentFinish.finish);

        eventFinishes.add(eventFinish);
      }
      dbHelper.close();

      events.add(eventFinishes);

      listAdapter.events=events;

      return 1;
    }
  }
}
