package org.boardgamers.wbc;

import android.view.View;

import java.util.List;

/**
 * Created by Kevin on 4/24/2015.
 */
public class DialogEditEvent extends DialogCreateEvent {
  // private final String TAG="WBC EditEventDialog";

  private int oldTime;
  private Event event;

  @Override
  public void onStart() {
    super.onStart();

    dialogTitle.setText(getResources().getString(R.string.edit_event));
    daysTV.setVisibility(View.GONE);
    daysLL.setVisibility(View.GONE);

    for (Event temp : UserDataFragment.userEvents) {
      if (temp.identifier.equalsIgnoreCase(MainActivity.SELECTED_EVENT_ID)) {
        event=temp;
        break;
      }
    }

    oldTime=event.hour;

    hourSpinner.setSelection(event.hour-7);
    durationSpinner.setSelection((int) event.duration-1);
    titleET.setText(event.title);
    locationET.setText(event.location);

    add.setText(getResources().getString(R.string.save));
    add.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        saveEvent();
        getDialog().dismiss();

      }
    });
  }

  @Override
  public void checkButton() {
    add.setEnabled(
        titleET.getText().toString().length()>0 && locationET.getText().toString().length()>0);

  }

  public void saveEvent() {
    String title=titleET.getText().toString();
    int hour=hourSpinner.getSelectedItemPosition()+7;
    int duration=durationSpinner.getSelectedItemPosition()+1;
    String location=locationET.getText().toString();

    // remove help from global list and edit

    int index=0;
    for (; index<UserDataFragment.userEvents.size(); index++) {
      if (UserDataFragment.userEvents.get(index).identifier
          .equalsIgnoreCase(MainActivity.SELECTED_EVENT_ID)) {
        break;
      }
    }

    Event editedEvent=UserDataFragment.userEvents.remove(index);
    editedEvent.hour=hour;
    editedEvent.title=title;
    editedEvent.duration=duration;
    editedEvent.location=location;

    // add edited help to tournament list
    Event tempEvent;
    index=0;
    for (; index<UserDataFragment.userEvents.size(); index++) {
      tempEvent=UserDataFragment.userEvents.get(index);
      if ((tempEvent.day*24+tempEvent.hour>editedEvent.day*24+editedEvent.hour) ||
          (tempEvent.day*24+tempEvent.hour==editedEvent.day*24+editedEvent.hour &&
              tempEvent.title.compareToIgnoreCase(title)==1)) {
        break;
      }

    }
    UserDataFragment.userEvents.add(index, editedEvent);

    // remove old help from schedule
    List<Event> events=MainActivity.dayList.get(event.day*MainActivity.GROUPS_PER_DAY+oldTime-6);
    for (int i=0; i<events.size(); i++) {
      if (events.get(i).identifier.equalsIgnoreCase(event.identifier)) {
        events.remove(i);
        break;
      }
    }

    // add new help to schedule
    MainActivity.dayList.get(event.day*MainActivity.GROUPS_PER_DAY+hour-6).add(0, editedEvent);

    // check for starred help
    if (event.starred) {
      MainActivity.removeStarredEvent(event.identifier, event.day);
      MainActivity.addStarredEvent(editedEvent);
    }

    MainActivity.SELECTED_EVENT_ID=event.identifier;

    UserDataFragment.updateEventsList();

    EventFragment fragment=
        (EventFragment) getActivity().getFragmentManager().findFragmentById(R.id.eventFragment);

    if (fragment!=null && fragment.isInLayout()) {
      fragment.setEvent(event);
    }
  }
}
