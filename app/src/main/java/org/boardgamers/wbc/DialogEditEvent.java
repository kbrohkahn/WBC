package org.boardgamers.wbc;

import android.view.View;

import java.util.List;

/**
 * Created by Kevin on 4/24/2015.
 * Dialog shown for editing user events. Extended from DialogCreateEvent, just edit existing views
 */
public class DialogEditEvent extends DialogCreateEvent {
  // private final String TAG="WBC EditEventDialog";

  List<Event> userEvents;
  private Event event;

  @Override
  public void onStart() {
    super.onStart();

    getDialog().setTitle(getResources().getString(R.string.edit_event));
    daysTV.setVisibility(View.GONE);
    daysLL.setVisibility(View.GONE);

    for (Event temp : userEvents) {
      if (temp.id==MainActivity.selectedEventId) {
        event=temp;
        break;
      }
    }

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

  public void saveEvent() {
    String title=titleET.getText().toString();
    int hour=hourSpinner.getSelectedItemPosition()+7;
    int duration=durationSpinner.getSelectedItemPosition()+1;
    String location=locationET.getText().toString();

    // remove event from global list and edit

    int index=0;
    for (; index<userEvents.size(); index++) {
      if (userEvents.get(index).id==MainActivity.selectedEventId) {
        break;
      }
    }

    Event editedEvent=userEvents.remove(index);
    editedEvent.hour=hour;
    editedEvent.title=title;
    editedEvent.duration=duration;
    editedEvent.location=location;

    // add edited event to tournament list
    Event tempEvent;
    index=0;
    for (; index<userEvents.size(); index++) {
      tempEvent=userEvents.get(index);
      if ((tempEvent.day*24+tempEvent.hour>editedEvent.day*24+editedEvent.hour) ||
          (tempEvent.day*24+tempEvent.hour==editedEvent.day*24+editedEvent.hour &&
              tempEvent.title.compareToIgnoreCase(title)==1)) {
        break;
      }

    }
    userEvents.add(index, editedEvent);

    EventFragment fragment=(EventFragment) getActivity().getSupportFragmentManager()
        .findFragmentById(R.id.eventFragment);

    if (fragment!=null && fragment.isInLayout()) {
      fragment.setEvent(editedEvent.id);
    }
  }
}
