package org.boardgamers.wbc;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.List;

/**
 * Fragment containing user's WBC data, including tournament finishes, help notes, and created events
 */
public class UserDataListFragment extends DefaultListFragment {
  // private final String TAG="My WBC Data Activity";

  public static List<Event> userEvents;
  public List<String> userNotes;
  public List<String> userFinishes;

  public static int selectedEvent;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
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

    refreshUserData();

    return view;
  }

  public void refreshUserData() {
    new PopulateUserDataAdapterTask(this, 3).execute(0, 0, 0);
  }

  @Override
  protected int getLayoutId() {
    return R.layout.user_data;
  }

  public String getNote(int index) {
    return userNotes.get(index);
  }

  public String getFinish(int index) {
    return userFinishes.get(index);
  }

  public void editEvent(int index) {
    UserDataListFragment.selectedEvent=index;
    DialogEditEvent editNameDialog=new DialogEditEvent();
    editNameDialog.show(getFragmentManager(), "edit_event_dialog");
  }

  public void deleteEvents(int index) {
    UserDataListFragment.selectedEvent=index;
    DialogDeleteEvent deleteDialog=new DialogDeleteEvent();
    deleteDialog.show(getFragmentManager(), "delete_event_dialog");
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

      userNotes=dbHelper.getAllNotes();
      userFinishes=dbHelper.getAllFinishes();
      userEvents=dbHelper.getUserEvents();

      dbHelper.close();

      return 1;
    }
  }
}
