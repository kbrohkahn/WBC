package org.boardgamers.wbc;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;

/**
 * Fragment containing user's WBC data, including tournament finishes, help notes, and created events
 */
public class UserDataFragment extends DefaultListFragment {
  private final String TAG="My WBC Data Activity";

  public static ArrayList<Event> userEvents;
  public ArrayList<String> userNotes;
  public ArrayList<String> userFinishes;

  public static int selectedEvent;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    updateEventsList();
    updateFinishesList();
    updateNotesList();

    View view=super.onCreateView(inflater, container, savedInstanceState);

    if (view!=null) {
      Button addEvent=(Button) view.findViewById(R.id.add_event);
      addEvent.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          DialogCreateEvent dialog=new DialogCreateEvent();
          dialog.show(getFragmentManager(), "create_event_dialog");
        }
      });
    }

    return view;
  }

  @Override
  protected int getLayoutId() {
    return R.layout.user_data;
  }

  @Override
  protected DefaultScheduleListAdapter getAdapter() {
    return new UserDataListAdapter(getActivity(), this);
  }

  public void updateNotesList() {
    userNotes=new ArrayList<>();

    SharedPreferences sp=getActivity()
        .getSharedPreferences(getResources().getString(R.string.sp_file_name),
            Context.MODE_PRIVATE);
    String notePrefString=getResources().getString(R.string.sp_event_note);

    String noteString;
    for (ArrayList<Event> events : MainActivity.dayList) {
      for (Event event : events) {
        noteString=sp.getString(notePrefString+String.valueOf(event.identifier), "");
        if (noteString.length()>0) {
          userNotes.add(noteString);
        }
      }
    }
  }

  public void updateFinishesList() {
    userFinishes=new ArrayList<>();

    String finishString;
    for (Tournament tournament : MainActivity.allTournaments) {
      if (tournament.finish>0) {
        Log.d(TAG,
            String.valueOf(tournament.title)+" finish is "+String.valueOf(tournament.finish));

        switch (tournament.finish) {
          case 1:
            finishString=getResources().getString(R.string.first);
            break;
          case 2:
            finishString=getResources().getString(R.string.second);
            break;
          case 3:
            finishString=getResources().getString(R.string.third);
            break;
          case 4:
            finishString=getResources().getString(R.string.fourth);
            break;
          case 5:
            finishString=getResources().getString(R.string.fifth);
            break;
          case 6:
            finishString=getResources().getString(R.string.sixth);
            break;
          default:
            finishString="No finish";
            break;
        }

        finishString+=" in "+tournament.title;
        userFinishes.add(finishString);
      }
    }
  }

  public static void updateEventsList() {
    userEvents=new ArrayList<>();

    for (int i=0; i<MainActivity.dayList.size(); i++) {
      if (i%MainActivity.GROUPS_PER_DAY==0) {
        continue;
      }

      for (Event event : MainActivity.dayList.get(i)) {
        if (event.tournamentID==-1) {
          userEvents.add(event);
        }
      }
    }
  }

  public String getNote(int index) {
    return userNotes.get(index);
  }

  public String getFinish(int index) {
    return userFinishes.get(index);
  }

  @Override
  public void onPause() {
    saveUserEvents();
    super.onPause();
  }

  private void saveUserEvents() {
    final Resources resources=getResources();

    SharedPreferences.Editor editor=getActivity()
        .getSharedPreferences(resources.getString(R.string.sp_file_name), Context.MODE_PRIVATE)
        .edit();
    String userEventPrefString=resources.getString(R.string.sp_user_event);
    String starPrefString=resources.getString(R.string.sp_event_starred);

    String saveString;
    String breakCharacter="~";
    Event event;
    int i;
    for (i=0; i<userEvents.size(); i++) {
      event=userEvents.get(i);

      saveString=String.valueOf(event.day)+breakCharacter+String.valueOf(event.hour)+breakCharacter+
          event.title+breakCharacter+String.valueOf(event.duration)+breakCharacter+event.location;
      editor.putString(userEventPrefString+String.valueOf(i), saveString);
      editor.putBoolean(starPrefString+String.valueOf(MainActivity.NUM_EVENTS+i), event.starred);

    }
    editor.putString(userEventPrefString+String.valueOf(i), "");
    editor.apply();
  }

  private void showCreateDialog() {
  }

  public void editEvent(int index) {
    UserDataFragment.selectedEvent=index;
    DialogEditEvent editNameDialog=new DialogEditEvent();
    editNameDialog.show(getFragmentManager(), "edit_event_dialog");
  }

  public void deleteEvents(int index) {
    UserDataFragment.selectedEvent=index;
    DialogDeleteEvent deleteDialog=new DialogDeleteEvent();
    deleteDialog.show(getFragmentManager(), "delete_event_dialog");
  }

}
