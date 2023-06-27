package org.boardgamers.wbcscdmgr;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Created by Kevin on 4/24/2015
 * Dialog shown for creating user events
 */
public class DialogCreateEvent extends DialogFragment {
	//private final String TAG="WBC CreateEventDialog";

	private final int[] ceDayIDs =
			{R.id.ce_repeat_d0, R.id.ce_repeat_d1, R.id.ce_repeat_d2, R.id.ce_repeat_d3,
					R.id.ce_repeat_d4, R.id.ce_repeat_d5, R.id.ce_repeat_d6, R.id.ce_repeat_d7,
					R.id.ce_repeat_d8, R.id.ce_repeat_d9};

	private Spinner hourSpinner;
	private Spinner durationSpinner;
	private EditText titleET;
	private EditText locationET;

	private CheckBox[] days;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dialog_event, container);

		getDialog().setTitle(getResources().getString(R.string.create_event));

		titleET = view.findViewById(R.id.ce_title);
		locationET = view.findViewById(R.id.ce_location);

		TextView daysTV = view.findViewById(R.id.ce_days);
		daysTV.setVisibility(View.VISIBLE);
		LinearLayout daysLL = view.findViewById(R.id.ce_days_layout);
		daysLL.setVisibility(View.VISIBLE);

		hourSpinner = view.findViewById(R.id.ce_hour);

		ArrayAdapter<CharSequence> hourA = ArrayAdapter
				.createFromResource(getActivity(), R.array.hours_24, android.R.layout.simple_spinner_item);
		hourA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		hourSpinner.setAdapter(hourA);

		durationSpinner = view.findViewById(R.id.ce_duration);
		ArrayAdapter<CharSequence> durationA = ArrayAdapter
				.createFromResource(getActivity(), R.array.duration, android.R.layout.simple_spinner_item);
		durationA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		durationSpinner.setAdapter(durationA);

		Button add = view.findViewById(R.id.ce_add);
		add.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				createEvents();
			}
		});

		Button cancel = view.findViewById(R.id.ce_cancel);
		cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		String[] dayStrings = getResources().getStringArray(R.array.days);
		days = new CheckBox[dayStrings.length];
		for (int i = 0; i < dayStrings.length; i++) {
			days[i] = view.findViewById(ceDayIDs[i]);
			days[i].setText(dayStrings[i]);
		}

		if (getDialog().getWindow() != null) {
			getDialog().getWindow()
					.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		}

		return view;
	}

	private void createEvents() {
		String title = titleET.getText().toString();
		int hour = hourSpinner.getSelectedItemPosition() + 7;
		int duration = durationSpinner.getSelectedItemPosition() + 1;
		String location = locationET.getText().toString();

		WBCDataDbHelper dbHelper = new WBCDataDbHelper(getActivity());
		dbHelper.getWritableDatabase();

		for (int day = 0; day < days.length; day++) {
			if (days[day].isChecked()) {
				dbHelper.insertUserEvent(MainActivity.userId, title, day, hour, duration, location);
			}
		}

		dbHelper.close();

		dismiss();
	}
}
