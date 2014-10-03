package org.boardgamers.wbc;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Notes fragment to track user notes
 */
public class NotesFragment extends Fragment {

  final String TAG = "Notes Dialog";

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.dialog_text, container, false);

    TextView title = (TextView) view.findViewById(R.id.dt_title);
    title.setText("Event notes");

    LinearLayout layout = (LinearLayout) view
        .findViewById(R.id.dt_layout);
    layout.removeAllViews();

    final Resources resources = getResources();
    final Context context = getActivity();
    int padding = (int) resources.getDimension(R.dimen.text_margin_small);

    SharedPreferences sp = context.getSharedPreferences(
        resources.getString(R.string.sp_file_name),
        Context.MODE_PRIVATE);
    String notePrefString = resources.getString(R.string.sp_event_note);

    TextView textView;

    String noteString;

    List<Event> events;
    int i = 0;
    while (i < MainActivity.dayList.size()) {
      for (int j = 1; j < MainActivity.dayList.get(i).size(); j++) {
        events = MainActivity.dayList.get(i).get(j);
        for (Event event : events) {
          noteString = sp
              .getString(
                  notePrefString
                      + String.valueOf(event.identifier),
                  "");
          if (noteString.length() > 0) {

            textView = new TextView(context);
            textView.setText(event.title + ": " + noteString);
            textView.setTextAppearance(context,
                R.style.medium_text);
            textView.setGravity(Gravity.START);
            textView.setPadding(padding, padding, padding,
                padding);

            layout.addView(textView);
          }
        }
      }
      i++;
    }

    if (layout.getChildCount() == 0) {
      textView = new TextView(context);
      textView
          .setText("Add event notes from an event screen," +
              "which can be accessed by selecting an event from a tournament screen.");
      textView.setTextAppearance(context, R.style.medium_text);
      textView.setGravity(Gravity.CENTER);
      textView.setPadding(padding, padding, padding, padding);

      layout.addView(textView);
    }

    return view;
  }
}
