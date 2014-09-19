package org.boardgamers.wbc;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

public class DialogSearch extends DialogFragment {
	private ImageButton eventSearch;
	private ImageButton eventInputSearch;
	private ImageButton formatSearch;
	private ImageButton roomSearch;

	private Spinner eventsSpinner;
	private Spinner formatsSpinner;
	private Spinner roomsSpinner;
	private AutoCompleteTextView eventACTV;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog=super.onCreateDialog(savedInstanceState);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		return dialog;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	    Bundle savedInstanceState) {
		View view=inflater.inflate(R.layout.dialog_search, container);

		final String[] tournamentTitles=new String[MyApp.allTournaments.size()];
		for (int i=0; i<tournamentTitles.length; i++)
			tournamentTitles[i]=MyApp.allTournaments.get(i).title;

		eventACTV=(AutoCompleteTextView) view
		    .findViewById(R.id.ds_tournament_input);
		ArrayAdapter<String> tvAdapter=new ArrayAdapter<String>(getActivity(),
		    android.R.layout.simple_spinner_dropdown_item, tournamentTitles);
		tvAdapter
		    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		eventACTV.setAdapter(tvAdapter);
		eventACTV.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {
				eventInputSearch.setEnabled(eventACTV.getText().toString()
				    .length()>0);
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
			    int arg2, int arg3) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
			    int arg3) {
				// TODO Auto-generated method stub
			}
		});

		eventsSpinner=(Spinner) view.findViewById(R.id.ds_tournament);
		ArrayAdapter<String> titleAdapter=new ArrayAdapter<String>(
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
			public void onNothingSelected(AdapterView<?> arg0) {}
		});

		eventSearch=(ImageButton) view.findViewById(R.id.ds_search_event);
		eventSearch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				selectGame(eventsSpinner.getSelectedItemPosition());
			}
		});

		eventInputSearch=(ImageButton) view
		    .findViewById(R.id.ds_search_event_input);
		eventInputSearch.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String searchString=eventACTV.getText().toString();

				int index=0;
				for (; index<tournamentTitles.length; index++) {
					if (tournamentTitles[index].equalsIgnoreCase(searchString)) {
						break;
					}
				}

				if (index<tournamentTitles.length)
					selectGame(index);
				else
					Toast.makeText(getActivity(), "No such tournament",
					    Toast.LENGTH_SHORT).show();

				InputMethodManager imm=(InputMethodManager) getActivity()
				    .getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(eventACTV.getWindowToken(), 0);

				eventACTV.getText().clear();

			}
		});
		eventInputSearch.setEnabled(false);

		// formats search
		final String[] formats=new String[] { "Demo", "Meeting", "MP Game",
		    "Preview",
		    "Sales", "Seminar", "Sign-In", "SOG" };
		ArrayAdapter<String> formatAdapter=new ArrayAdapter<String>(
		    getActivity(), android.R.layout.simple_spinner_item, formats);
		formatAdapter
		    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		formatsSpinner=(Spinner) view.findViewById(R.id.ds_formats);
		formatsSpinner.setAdapter(formatAdapter);

		formatSearch=(ImageButton) view.findViewById(R.id.ds_search_format);
		formatSearch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				viewFormat(formats[formatsSpinner.getSelectedItemPosition()]);
			}
		});

		// room search
		roomsSpinner=(Spinner) view.findViewById(R.id.ds_rooms);
		ArrayAdapter<CharSequence> roomAdapter=ArrayAdapter.createFromResource(
		    getActivity(), R.array.rooms_available,
		    android.R.layout.simple_spinner_item);
		roomAdapter
		    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		roomsSpinner.setAdapter(roomAdapter);

		roomSearch=(ImageButton) view.findViewById(R.id.ds_search_room);
		roomSearch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showMapDialog((String) roomsSpinner.getSelectedItem());
			}
		});

		Button close=(Button) view.findViewById(R.id.ds_close);
		close.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				closeDialog();
			}
		});

		return view;
	}

	public void selectGame(int g) {
		MyApp.SELECTED_GAME_ID=g;

		Intent intent=new Intent(getActivity(), TournamentActivity.class);
		startActivity(intent);

		closeDialog();
	}

	public void viewFormat(String f) {
		Intent intent=new Intent(getActivity(), Summary.class);
		intent.putExtra("format", f);
		startActivity(intent);

		closeDialog();
	}

	public void showMapDialog(String room) {
		Intent intent=new Intent(getActivity(), Map.class);
		intent.putExtra("room", room);
		startActivity(intent);

		closeDialog();
	}

	public void closeDialog() {
		this.dismiss();
	}
}
