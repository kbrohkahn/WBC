package org.boardgamers.wbc;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Finishes fragment to track user's finishes
 */
public class FinishesFragment extends Fragment {

  final String TAG = "Filter Fragment";

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.dialog_text, container, false);

    TextView title = (TextView) view.findViewById(R.id.dt_title);
    title.setText("My finishes");

    LinearLayout layout = (LinearLayout) view
        .findViewById(R.id.dt_layout);
    layout.removeAllViews();

    final Context context = getActivity();
    final Resources resources = getResources();
    int padding = (int) resources.getDimension(R.dimen.text_margin_small);

    TextView textView;
    String finishString;
    Tournament tournament;
    int i = 0;
    while (i < MainActivity.allTournaments.size()) {
      tournament = MainActivity.allTournaments.get(i);

      if (tournament.finish > 0) {
        Log.d(TAG, String.valueOf(tournament.title) + " finish is "
            + String.valueOf(tournament.finish));

        switch (tournament.finish) {
          case 1:
            finishString = resources.getString(R.string.first);
            break;
          case 2:
            finishString = resources.getString(R.string.second);
            break;
          case 3:
            finishString = resources.getString(R.string.third);
            break;
          case 4:
            finishString = resources.getString(R.string.fourth);
            break;
          case 5:
            finishString = resources.getString(R.string.fifth);
            break;
          case 6:
            finishString = resources.getString(R.string.sixth);
            break;
          default:
            finishString = "No finish";
            break;
        }

        finishString += " in " + tournament.title;

        textView = new TextView(context);
        textView.setText(finishString);
        textView.setTextAppearance(context, R.style.medium_text);
        textView.setGravity(Gravity.START);
        textView.setPadding(padding, padding, padding, padding);

        if (tournament.finish <= tournament.prize)
          textView.setTypeface(null, Typeface.BOLD);
        else
          textView.setTypeface(null, Typeface.ITALIC);

        layout.addView(textView);
      }
      i++;
    }

    if (layout.getChildCount() == 0) {
      textView = new TextView(context);
      textView
          .setText("Select tournament finish from a tournament screen.\n\n" +
              "*note: final event for tournament must have started*\n\n");
      textView.setTextAppearance(context, R.style.medium_text);
      textView.setGravity(Gravity.CENTER);
      textView.setPadding(padding, padding, padding, padding);

      layout.addView(textView);
    }
    return view;
  }
}
