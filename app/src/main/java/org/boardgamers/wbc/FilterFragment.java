package org.boardgamers.wbc;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

public class FilterFragment extends Fragment {

  private static CheckBox[] tournamentCBs;
  final String TAG = "Filter Dialog";
  private Boolean[] isTournament;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.filter, container, false);

    isTournament = new Boolean[MainActivity.allTournaments.size()];
    for (int i = 0; i < isTournament.length; i++)
      isTournament[i] = MainActivity.allTournaments.get(i).isTournament;

    // select all image button
    ImageButton selectAll = (ImageButton) view.findViewById(R.id.filter_select_all);
    selectAll.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        for (CheckBox cb : tournamentCBs) {
          cb.setChecked(true);
        }
      }
    });
    selectAll.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        Toast.makeText(getActivity(),
            getResources().getString(R.string.select_all), Toast.LENGTH_SHORT).show();
        return false;
      }
    });

    // select non tournament image button
    ImageButton selectNonTournament = (ImageButton) view.findViewById(R.id.filter_select_non_tournament);
    selectNonTournament.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        for (int i = 0; i < tournamentCBs.length; i++) {
          if (!isTournament[i])
            tournamentCBs[i].setChecked(true);

        }
      }
    });
    selectNonTournament.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        Toast.makeText(getActivity(),
            getResources().getString(R.string.select_non_tournament), Toast.LENGTH_SHORT).show();
        return false;
      }
    });

    // deselect all image button
    ImageButton deselectAll = (ImageButton) view.findViewById(R.id.filter_deselect_all);
    deselectAll.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        for (CheckBox cb : tournamentCBs) {
          cb.setChecked(false);
        }

      }
    });
    deselectAll.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        Toast.makeText(getActivity(),
            getResources().getString(R.string.deselect_all), Toast.LENGTH_SHORT).show();
        return false;
      }
    });

    // deselect non tournament image button
    ImageButton deselectNonTournament = (ImageButton) view.findViewById(R.id.filter_deselect_non_tournament);
    deselectNonTournament.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        for (int i = 0; i < tournamentCBs.length; i++) {
          if (!isTournament[i])
            tournamentCBs[i].setChecked(false);
        }
      }
    });
    deselectNonTournament.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        Toast.makeText(getActivity(),
            getResources().getString(R.string.deselect_non_tournament), Toast.LENGTH_SHORT).show();
        return false;
      }
    });

    // set up checkboxes
    LinearLayout checkBoxLayout = (LinearLayout) view.findViewById(R.id.filter_layout);
    checkBoxLayout.removeAllViews();
    tournamentCBs = new CheckBox[isTournament.length];

    CheckBox temp;
    Tournament tournament;
    for (int i = 0; i < isTournament.length; i++) {
      tournament = MainActivity.allTournaments.get(i);
      temp = new CheckBox(getActivity());
      temp.setText(tournament.title);
      temp.setTextAppearance(getActivity(), R.style.medium_text);
      temp.setChecked(tournament.visible);

      temp.setOnCheckedChangeListener(new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
          // TODO Auto-generated method stub
        }
      });

      tournamentCBs[i] = temp;
    }

    // add checkboxes
    for (CheckBox checkBox : tournamentCBs)
      checkBoxLayout.addView(checkBox);

    return view;
  }


  @Override
  public void onPause() {
    SharedPreferences.Editor editor = getActivity().getSharedPreferences(
        getResources().getString(R.string.sp_file_name),
        Context.MODE_PRIVATE).edit();

    boolean checked;
    for (int i = 0; i < tournamentCBs.length; i++) {
      checked = tournamentCBs[i].isChecked();
      editor.putBoolean("vis_" + MainActivity.allTournaments.get(i).title, checked);
      MainActivity.allTournaments.get(i).visible = checked;
    }

    editor.apply();

    super.onPause();
  }

}