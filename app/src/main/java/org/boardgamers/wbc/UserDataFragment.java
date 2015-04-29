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
public class UserDataFragment extends DefaultListFragment {
  // private final String TAG="My WBC Data Activity";

  public static List<Event> userEvents;
  public List<String> userNotes;
  public List<String> userFinishes;

  public static int selectedEvent;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    WBCDataDbHelper dbHelper=new WBCDataDbHelper(getActivity());
    dbHelper.getAllNotes();

    refreshUserData(true);

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

  public void refreshUserData(boolean allData) {
    WBCDataDbHelper dbHelper=new WBCDataDbHelper(getActivity());
    dbHelper.onOpen(dbHelper.getReadableDatabase());

    userEvents=dbHelper.getUserEvents();
    if (allData) {
      userNotes=dbHelper.getAllNotes();
      userFinishes=dbHelper.getAllFinishes();
    }

    dbHelper.close();

  }

  @Override
  protected int getLayoutId() {
    return R.layout.user_data;
  }

  public void startLoadTask() {
    new LoadUserDataAdapterClass(this).doInBackground(0, 0, 0);
  }

  class LoadUserDataAdapterClass extends DefaultListFragment.LoadAdapterClass {
    public LoadUserDataAdapterClass(UserDataFragment f) {
      fragment=f;
    }

    @Override
    protected void onPostExecute(Integer integer) {
      listView.setSelectedGroup(MainActivity.getCurrentGroup()/MainActivity.GROUPS_PER_DAY);

      super.onPostExecute(integer);
    }

    @Override
    protected Integer doInBackground(Integer... params) {
      listAdapter=new UserDataListAdapter(getActivity(), (UserDataFragment) fragment);

      return super.doInBackground(params);
    }
  }

  public String getNote(int index) {
    return userNotes.get(index);
  }

  public String getFinish(int index) {
    return userFinishes.get(index);
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
