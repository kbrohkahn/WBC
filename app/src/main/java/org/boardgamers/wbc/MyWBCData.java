package org.boardgamers.wbc;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 10/3/14.
 */
public class MyWBCData extends Fragment {
  private final String TAG = "My WBC Data Activity";

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.my_wbc_data, container, false);

    /** ADD FINISHES **/
    LinearLayout finishesLayout = (LinearLayout) view.findViewById(R.id.my_wbc_data_finishes);
    finishesLayout.removeAllViews();

    int padding = (int) getResources().getDimension(R.dimen.text_margin_small);

    TextView textView;
    String finishString;

    for (Tournament tournament : MainActivity.allTournaments) {
      if (tournament.finish > 0) {
        Log.d(TAG, String.valueOf(tournament.title) + " finish is "
            + String.valueOf(tournament.finish));

        switch (tournament.finish) {
          case 1:
            finishString = getResources().getString(R.string.first);
            break;
          case 2:
            finishString = getResources().getString(R.string.second);
            break;
          case 3:
            finishString = getResources().getString(R.string.third);
            break;
          case 4:
            finishString = getResources().getString(R.string.fourth);
            break;
          case 5:
            finishString = getResources().getString(R.string.fifth);
            break;
          case 6:
            finishString = getResources().getString(R.string.sixth);
            break;
          default:
            finishString = "No finish";
            break;
        }

        finishString += " in " + tournament.title;

        textView = new TextView(getActivity());
        textView.setText(finishString);
        textView.setTextAppearance(getActivity(), R.style.medium_text);
        textView.setGravity(Gravity.START);
        textView.setPadding(padding, padding, padding, padding);

        if (tournament.finish <= tournament.prize)
          textView.setTypeface(null, Typeface.BOLD);
        else
          textView.setTypeface(null, Typeface.ITALIC);

        finishesLayout.addView(textView);
      }
    }

    /** ADD NOTES **/

    LinearLayout notesLayout = (LinearLayout) view
        .findViewById(R.id.my_wbc_data_notes);
    notesLayout.removeAllViews();

    SharedPreferences sp = getActivity().getSharedPreferences(
        getResources().getString(R.string.sp_file_name),
        Context.MODE_PRIVATE);
    String notePrefString = getResources().getString(R.string.sp_event_note);

    String noteString;

    List<Event> events;
    for (ArrayList<ArrayList<Event>> searchDayList : MainActivity.dayList) {
      for (ArrayList<Event> searchList : searchDayList) {
        for (Event event : searchList) {
          noteString = sp
              .getString(
                  notePrefString
                      + String.valueOf(event.identifier),
                  "");
          if (noteString.length() > 0) {

            textView = new TextView(getActivity());
            textView.setText(event.title + ": " + noteString);
            textView.setTextAppearance(getActivity(),
                R.style.medium_text);
            textView.setGravity(Gravity.START);
            textView.setPadding(padding, padding, padding,
                padding);

            notesLayout.addView(textView);
          }
        }
      }
    }

    return view;
  }
}
