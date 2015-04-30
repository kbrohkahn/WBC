package org.boardgamers.wbc;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Kevin on 4/24/2015.
 * Dialog shown for deleting user events
 */
public class DialogDeleteEvent extends DialogFragment {
  // private final String TAG="DeleteDialog";

  private boolean ALL_EVENTS;
  protected UserDataListFragment fragment;

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    Dialog dialog=super.onCreateDialog(savedInstanceState);
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    return dialog;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view=inflater.inflate(R.layout.dialog_delete, container, false);

    ALL_EVENTS=UserDataListFragment.selectedEvent==-1;

    String string;
    if (ALL_EVENTS) {
      string="all your events?";
    } else {
      Event event=UserDataListFragment.userEvents.get(UserDataListFragment.selectedEvent);
      String day=getResources().getStringArray(R.array.days)[event.day];

      SharedPreferences settings=getActivity()
          .getSharedPreferences(getResources().getString(R.string.sp_file_name),
              Context.MODE_PRIVATE);

      boolean hours24=settings.getBoolean("24_hour", true);
      int hoursID=(hours24 ? R.array.hours_24 : R.array.hours_12);
      String[] hours=getResources().getStringArray(hoursID);

      String time=hours[event.hour-7];
      string=event.title+" on "+day+" at "+time+"?";

    }

    TextView text=(TextView) view.findViewById(R.id.dd_text);
    text.setText(string);

    Button delete=(Button) view.findViewById(R.id.dd_delete);
    delete.setEnabled(true);
    delete.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        deleteItem();
        getDialog().dismiss();
      }
    });

    Button cancel=(Button) view.findViewById(R.id.dd_cancel);
    cancel.setEnabled(true);
    cancel.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        getDialog().dismiss();
      }
    });

    return view;

  }

  public void deleteItem() {
    Event event;

    int index;
    if (ALL_EVENTS) {
      index=0;
    } else {
      index=UserDataListFragment.selectedEvent;
    }

    WBCDataDbHelper dbHelper=new WBCDataDbHelper(getActivity());
    dbHelper.getWritableDatabase();
    while (index<UserDataListFragment.userEvents.size()) {
      event=UserDataListFragment.userEvents.remove(index);

      dbHelper.deleteEvent(event.id);

      if (!ALL_EVENTS) {
        index=UserDataListFragment.userEvents.size();
      }
    }
  }
}
