package org.boardgamers.wbc;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

public class SearchFragment extends Fragment {
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.search, container);

    final String[] tournamentTitles = new String[MainActivity.allTournaments.size()];
    for (int i = 0; i < tournamentTitles.length; i++)
      tournamentTitles[i] = MainActivity.allTournaments.get(i).title;

    // load views
    final AutoCompleteTextView eventACTV = (AutoCompleteTextView) view
        .findViewById(R.id.ds_tournament_input);
    final ImageButton eventInputSearchButton = (ImageButton) view
        .findViewById(R.id.ds_search_event_input);
    final Spinner eventsSpinner = (Spinner) view.findViewById(R.id.ds_tournament);
    ImageButton eventSearchButton = (ImageButton) view.findViewById(R.id.ds_search_event);
    final Spinner formatsSpinner = (Spinner) view.findViewById(R.id.ds_formats);
    final Spinner roomsSpinner = (Spinner) view.findViewById(R.id.ds_rooms);
    ImageButton roomSearchButton = (ImageButton) view.findViewById(R.id.ds_search_room);
    ImageButton formatSearchButton = (ImageButton) view.findViewById(R.id.ds_search_format);

    // setup event auto complete text view
    ArrayAdapter<String> tvAdapter = new ArrayAdapter<String>(getActivity(),
        android.R.layout.simple_spinner_dropdown_item, tournamentTitles);
    tvAdapter
        .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    eventACTV.setAdapter(tvAdapter);
    eventACTV.addTextChangedListener(new TextWatcher() {

      @Override
      public void afterTextChanged(Editable arg0) {
        eventInputSearchButton.setEnabled(eventACTV.getText().toString()
            .length() > 0);
      }

      @Override
      public void beforeTextChanged(CharSequence arg0, int arg1,
                                    int arg2, int arg3) {
      }

      @Override
      public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                                int arg3) {
      }
    });

    // setup event search button
    eventInputSearchButton.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        String searchString = eventACTV.getText().toString();

        int index = 0;
        for (; index < tournamentTitles.length; index++) {
          if (tournamentTitles[index].equalsIgnoreCase(searchString)) {
            break;
          }
        }

        if (index < tournamentTitles.length)
          selectGame(index);
        else
          Toast.makeText(getActivity(), "No such tournament",
              Toast.LENGTH_SHORT).show();

        InputMethodManager imm = (InputMethodManager) getActivity()
            .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(eventACTV.getWindowToken(), 0);

        eventACTV.getText().clear();

      }
    });
    eventInputSearchButton.setEnabled(false);


    // setup events spinner
    ArrayAdapter<String> titleAdapter = new ArrayAdapter<String>(
        getActivity(), android.R.layout.simple_spinner_item,
        tournamentTitles);
    titleAdapter
        .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    eventsSpinner.setAdapter(titleAdapter);
    eventsSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

      @Override
      public void onItemSelected(AdapterView<?> arg0, View arg1,
                                 int arg2, long arg3) {
        eventACTV.getText().clear();
      }

      @Override
      public void onNothingSelected(AdapterView<?> arg0) {
      }
    });

    // setup events spinner search button
    eventSearchButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        selectGame(eventsSpinner.getSelectedItemPosition());
      }
    });

    // formats search
    ArrayAdapter<CharSequence> formatAdapter =
        ArrayAdapter.createFromResource(getActivity(),
            R.array.search_formats, android.R.layout.simple_spinner_item);
    formatAdapter
        .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

    // setup formats spinner
    formatsSpinner.setAdapter(formatAdapter);

    formatSearchButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        selectGame(1000 + formatsSpinner.getSelectedItemPosition());
      }
    });

    // setup rooms spinner
    ArrayAdapter<CharSequence> roomAdapter = ArrayAdapter.createFromResource(
        getActivity(), R.array.rooms_available,
        android.R.layout.simple_spinner_item);
    roomAdapter
        .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    roomsSpinner.setAdapter(roomAdapter);

    // setup rooms search button
    roomSearchButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        showMapDialog((String) roomsSpinner.getSelectedItem());
      }
    });

    return view;
  }

  public void selectGame(int g) {
    MainActivity.SELECTED_GAME_ID = g;
    Intent intent = new Intent(getActivity(), TournamentActivity.class);
    startActivity(intent);
  }


  public void showMapDialog(String room) {
    MapFragment fragment = new MapFragment();
    fragment.roomString = room;
    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction ft = fragmentManager.beginTransaction()
        .replace(R.id.content_frame, fragment);
    ft.commit();
  }
}
