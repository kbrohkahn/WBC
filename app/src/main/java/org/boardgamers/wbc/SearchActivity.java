package org.boardgamers.wbc;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

public class SearchActivity extends Activity {
  private final String TAG = "Search";

  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.search);

    // enable home button for navigation drawer
    final ActionBar ab = getActionBar();
    if (ab != null) {
      ab.setDisplayHomeAsUpEnabled(true);
      ab.setHomeButtonEnabled(true);
    } else
      Log.d(TAG, "Could not get action bar");

    final String[] tournamentTitles = new String[MainActivity.allTournaments.size()];
    for (int i = 0; i < tournamentTitles.length; i++)
      tournamentTitles[i] = MainActivity.allTournaments.get(i).title;

    // load views
    final AutoCompleteTextView eventACTV = (AutoCompleteTextView) findViewById(R.id.ds_tournament_input);
    final ImageButton eventInputSearchButton = (ImageButton) findViewById(R.id.ds_search_event_input);
    final Spinner eventsSpinner = (Spinner) findViewById(R.id.ds_tournament);
    ImageButton eventSearchButton = (ImageButton) findViewById(R.id.ds_search_event);
    final Spinner formatsSpinner = (Spinner) findViewById(R.id.ds_formats);
    final Spinner roomsSpinner = (Spinner) findViewById(R.id.ds_rooms);
    ImageButton roomSearchButton = (ImageButton) findViewById(R.id.ds_search_room);
    ImageButton formatSearchButton = (ImageButton) findViewById(R.id.ds_search_format);

    // setup event auto complete text view
    ArrayAdapter<String> tvAdapter = new ArrayAdapter<String>(this,
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
          showToast("No such tournament");

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(eventACTV.getWindowToken(), 0);

        eventACTV.getText().clear();

      }
    });
    eventInputSearchButton.setEnabled(false);


    // setup events spinner
    ArrayAdapter<String> titleAdapter = new ArrayAdapter<String>(
        this, android.R.layout.simple_spinner_item,
        tournamentTitles);
    titleAdapter
        .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    eventsSpinner.setAdapter(titleAdapter);
    eventsSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

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
        ArrayAdapter.createFromResource(this,
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
        this, R.array.rooms_available,
        android.R.layout.simple_spinner_item);
    roomAdapter
        .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    roomsSpinner.setAdapter(roomAdapter);

    // setup rooms search button
    roomSearchButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        showMap((String) roomsSpinner.getSelectedItem());
      }
    });
  }

  public void showMap(String room) {
    MainActivity.openMap(this, room);
    finish();
  }

  private void showToast(String string) {
    Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
  }

  public void selectGame(int g) {
    MainActivity.SELECTED_GAME_ID = g;
    Intent intent = new Intent(this, TournamentActivity.class);
    startActivity(intent);
    finish();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.close, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_close:
        finish();
        return true;
      case android.R.id.home:
        finish();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }
}
