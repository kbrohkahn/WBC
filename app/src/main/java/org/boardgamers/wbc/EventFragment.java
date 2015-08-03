package org.boardgamers.wbc;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

public class EventFragment extends Fragment {
    //private final String TAG="Event Fragment";

    private int initialFinish = -1;

    private final int[] finishIDs =
            {R.id.ef_finish_0, R.id.ef_finish_1, R.id.ef_finish_2, R.id.ef_finish_3, R.id.ef_finish_4,
                    R.id.ef_finish_5, R.id.ef_finish_6};

    private final int[] upstairsIDs =
            {R.drawable.room_conestoga_1, R.drawable.room_conestoga_2, R.drawable.room_conestoga_3,
                    R.drawable.room_good_spirits_bar, R.drawable.room_heritage, R.drawable.room_lampeter,
                    R.drawable.room_laurel_grove, R.drawable.room_open_gaming_pavilion,
                    R.drawable.room_showroom, R.drawable.room_vistas_cd, R.drawable.room_wheatland};
    private final int[] downstairsIDs =
            {R.drawable.room_ballroom_b_corridor, R.drawable.room_ballroom_a,
                    R.drawable.room_ballrooms_a_and_b, R.drawable.room_ballroom_b, R.drawable.room_cornwall,
                    R.drawable.room_hopewell, R.drawable.room_kinderhook, R.drawable.room_limerock,
                    R.drawable.room_marietta, R.drawable.room_new_holland, R.drawable.room_paradise,
                    R.drawable.room_strasburg, R.drawable.room_terrace, R.drawable.room_terrace,
                    R.drawable.room_terrace, R.drawable.room_terrace, R.drawable.room_terrace,
                    R.drawable.room_terrace, R.drawable.room_terrace, R.drawable.room_terrace};

    public Event event;
    private Tournament tournament;

    // event description
    private TextView titleTV;
    private TextView dayTV;
    private TextView timeTV;
    private TextView locationTV;
    private TextView formatTV;
    private TextView classTV;
    private TextView gMTV;
    private TextView previewTV;
    private TextView reportTV;
    private TextView tournamentTV;
    private ImageView boxIV;

    // user data
    private EditText noteET;
    private Button shareButton;
    private Button clearButton;
    private TextView finishTV;
    private RadioGroup finishGroup;
    private RadioButton[] finishButtons;
    // views
    private LinearLayout timeLayout;
    private ToggleButton mapOverlayToggle;
    private ImageView map;
    private ImageView mapOverlay;

    private int roomID;
    private boolean overlayOn;
    private boolean runnableActive;
    private Handler handler = new Handler();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.event_fragment, container, false);

        timeLayout = (LinearLayout) view.findViewById(R.id.ef_time_layout);
        titleTV = (TextView) view.findViewById(R.id.ef_title);
        dayTV = (TextView) view.findViewById(R.id.ef_day);
        timeTV = (TextView) view.findViewById(R.id.ef_time);
        formatTV = (TextView) view.findViewById(R.id.ef_format);
        classTV = (TextView) view.findViewById(R.id.ef_class);
        locationTV = (TextView) view.findViewById(R.id.ef_location);
        gMTV = (TextView) view.findViewById(R.id.ef_gm);

        previewTV = (TextView) view.findViewById(R.id.ef_preview_link);
        previewTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPreviewLink();
            }
        });

        reportTV = (TextView) view.findViewById(R.id.ef_report_link);
        reportTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openReportLink();
            }
        });

        tournamentTV = (TextView) view.findViewById(R.id.ef_tournament_link);
        tournamentTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSearchActivity();
            }
        });

        boxIV = (ImageView) view.findViewById(R.id.ef_box_image);

        map = (ImageView) view.findViewById(R.id.ef_map);
        mapOverlay = (ImageView) view.findViewById(R.id.ef_map_overlay);

        runnableActive = false;
        mapOverlayToggle = (ToggleButton) view.findViewById(R.id.ef_map_toggle_button);
        mapOverlayToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapOverlayToggle.isChecked()) {
                    startOverlayUpdate();
                } else {
                    stopOverlayUpdate();
                }
            }
        });
        noteET = (EditText) view.findViewById(R.id.ef_note);

        shareButton = (Button) view.findViewById(R.id.ef_share);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share();
            }
        });

        clearButton = (Button) view.findViewById(R.id.ef_clear);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteET.getText().clear();
            }
        });

        finishTV = (TextView) view.findViewById(R.id.ef_finish_text);
        finishGroup = (RadioGroup) view.findViewById(R.id.ef_finish_group);
        finishButtons = new RadioButton[finishIDs.length];
        for (int i = 0; i < finishIDs.length; i++) {
            finishButtons[i] = (RadioButton) view.findViewById(finishIDs[i]);
            finishButtons[i].setText(String.valueOf(i));
        }

        setEvent(MainActivity.selectedEventId);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (roomID > -1) {
            startOverlayUpdate();
        }
    }

    public void setEvent(long id) {
        saveEventData();
        stopOverlayUpdate();

        if (id == -1) {
            event = null;

            titleTV.setVisibility(View.VISIBLE);
            titleTV.setText(getResources().getString(R.string.title));
            dayTV.setText(getResources().getString(R.string.day));
            timeTV.setText(getResources().getString(R.string.time));
            timeTV.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            locationTV.setText(getResources().getString(R.string.location));
            formatTV.setText(getResources().getString(R.string.format));
            classTV.setText(getResources().getString(R.string.class_string));
            gMTV.setText(getResources().getString(R.string.gm));

            timeLayout.setBackgroundResource(0);

            setRoom("");
            mapOverlay.setImageResource(0);
            map.setImageResource(0);
            stopOverlayUpdate();

            noteET.setEnabled(false);
            clearButton.setEnabled(false);
            shareButton.setEnabled(true);

            hideNonTournamentViews();
        } else {
            WBCDataDbHelper dbHelper = new WBCDataDbHelper(getActivity());
            dbHelper.getReadableDatabase();
            event = dbHelper.getEvent(MainActivity.userId, id);

            if (event.id < MainActivity.USER_EVENT_ID) {
                tournament = dbHelper.getTournament(MainActivity.userId, event.tournamentID);
            } else {
                tournament = null;
            }

            dbHelper.close();

            if (getActivity() instanceof EventActivity) {
                titleTV.setVisibility(View.GONE);
                getActivity().setTitle(event.title);
            } else {
                titleTV.setVisibility(View.VISIBLE);
                titleTV.setText(event.title);
            }

            String[] dayStrings = getResources().getStringArray(R.array.days);
            dayTV.setText(dayStrings[event.day]);

            timeTV.setText(MainActivity.getDisplayHour(event.hour, 0) + " to " + MainActivity.getDisplayHour(
                    event.hour, event.duration));
            if (event.continuous) {
                timeTV.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.continuous_icon, 0);
            } else {
                timeTV.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }

            locationTV.setText(getResources().getString(R.string.location) + ": " + event.location);

            if (event.format.equalsIgnoreCase("")) {
                formatTV.setVisibility(View.GONE);
            } else {
                formatTV.setVisibility(View.VISIBLE);
                formatTV.setText(getResources().getString(R.string.format) + ": " + event.format);
            }

            if (event.format.equalsIgnoreCase("")) {
                classTV.setVisibility(View.GONE);
            } else {
                classTV.setVisibility(View.VISIBLE);
                classTV.setText(getResources().getString(R.string.class_string) + ": " + event.eClass);
            }

            if (tournament != null) {
                gMTV.setVisibility(View.VISIBLE);
                gMTV.setText(getResources().getString(R.string.gm) + ": " + tournament.gm);
            } else {
                gMTV.setVisibility(View.GONE);
            }

            setRoom(event.location);

            noteET.setText(event.note);
            noteET.setEnabled(true);
            clearButton.setEnabled(true);
            shareButton.setEnabled(true);

            int hoursIntoConvention = UpdateService.getHoursIntoConvention();
            boolean started = event.day * 24 + event.hour <= hoursIntoConvention;
            boolean ended = event.day * 24 + event.hour + event.duration <= hoursIntoConvention;
            boolean happening = started && !ended;

            if (ended) {
                timeLayout.setBackgroundResource(R.drawable.ended_light);
            } else if (happening) {
                timeLayout.setBackgroundResource(R.drawable.current_light);
            } else {
                timeLayout.setBackgroundResource(R.drawable.future_light);
            }

            if (tournament != null) {
                tournamentTV.setVisibility(View.VISIBLE);
                //tournamentTV.setText("View all " + tournament.title + " events");
            }

            // check if last event is class (is tournament)
            if (tournament != null && tournament.isTournament) {
                finishTV.setText(getResources().getString(R.string.finish) + " in " + tournament.title);
                finishTV.setVisibility(View.VISIBLE);
                finishGroup.setVisibility(View.VISIBLE);
                previewTV.setVisibility(View.VISIBLE);
                reportTV.setVisibility(View.VISIBLE);

                int boxId = MainActivity.getBoxIdFromLabel(tournament.label, getResources());
                if (boxId == 0) {
                    boxIV.setImageResource(R.drawable.box_iv_no_image_text);
                    Log.d("", "not found");
                } else {
                    boxIV.setImageResource(boxId);
                }

                // if last event started, allow finish
                for (int i = 0; i <= 6; i++) {
                    finishButtons[i].setTextColor(started ? Color.BLACK : Color.GRAY);

                    if (i > 0 && i <= tournament.prize) {
                        finishButtons[i].setTypeface(null, Typeface.BOLD);
                    } else {
                        finishButtons[i].setTypeface(null, Typeface.ITALIC);
                    }
                }
                initialFinish = tournament.finish;
                finishGroup.check(finishIDs[initialFinish]);
            } else {
                hideNonTournamentViews();
            }
        }
    }

    public void startSearchActivity() {
        Intent intent = new Intent(getActivity(), SearchResultActivity.class);
        intent.putExtra("query_title", tournament.title);
        intent.putExtra("query_id", tournament.id);
        startActivity(intent);
    }

    public void openPreviewLink() {
        if (tournament != null && tournament.isTournament) {
            getActivity().startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://boardgamers.org/yearbkex/" + tournament.label + "pge.htm")));
        }
    }

    public void openReportLink() {
        if (tournament != null && tournament.isTournament) {
            getActivity().startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://boardgamers.org/yearbook14/" + tournament.label + "pge.htm")));
        }
    }

    public void hideNonTournamentViews() {
        finishGroup.setVisibility(View.GONE);
        finishTV.setVisibility(View.GONE);
        previewTV.setVisibility(View.INVISIBLE);
        reportTV.setVisibility(View.INVISIBLE);
        boxIV.setImageResource(R.drawable.box_iv_no_image_text);
    }

    private void share() {
        String s = event.title + ": " + noteET.getText().toString();

        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");

        String subjectString = event.title;
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, subjectString);
        sharingIntent.putExtra(Intent.EXTRA_TITLE, subjectString);

        String contentString = s + " #WBC";
        sharingIntent.putExtra(Intent.EXTRA_TEXT, contentString);

        startActivity(Intent.createChooser(sharingIntent, "Share via"));

    }

    public void saveEventData() {
        if (event != null) {
            String note = noteET.getText().toString();
            int finish;

            if (event.tournamentID < MainActivity.USER_EVENT_ID && tournament.isTournament) {
                finish = 0;
                for (int i = 0; i < finishButtons.length; i++) {
                    if (finishButtons[i].isChecked()) {
                        finish = i;
                        break;
                    }
                }
            } else {
                finish = -1;
            }

            if (note.equalsIgnoreCase(event.note) && (tournament == null || finish == tournament.finish))
                return;

            // save data
            WBCDataDbHelper dbHelper = new WBCDataDbHelper(getActivity());
            dbHelper.getWritableDatabase();
            dbHelper.insertUserEventData(MainActivity.userId, event.id, event.starred, note);
            if (finish > -1 && finish != initialFinish) {
                dbHelper.insertUserTournamentData(MainActivity.userId, event.tournamentID, finish);
            }
            dbHelper.close();

            MainActivity.updateUserData(event.id, note, event.tournamentID, finish);
        }
    }

    @Override
    public void onPause() {
        saveEventData();
        stopOverlayUpdate();

        super.onPause();
    }

    public void setRoom(String room) {
        mapOverlayToggle.setVisibility(View.GONE);
        roomID = -1;
        String[] roomsDownstairs = getResources().getStringArray(R.array.rooms_downstairs);
        for (int i = 0; i < roomsDownstairs.length; i++) {
            if (roomsDownstairs[i].equalsIgnoreCase(room)) {
                roomID = i;
                setImageViews();
                return;
            }
        }

        String[] roomsUpstairs = getResources().getStringArray(R.array.rooms_upstairs);
        for (int i = 0; i < roomsUpstairs.length; i++) {
            if (roomsUpstairs[i].equalsIgnoreCase(room)) {
                roomID = 50 + i;
                setImageViews();
                return;
            }
        }
    }

    public void setImageViews() {
        mapOverlayToggle.setVisibility(View.VISIBLE);

        if (roomID >= 50) {
            map.setImageResource(R.drawable.upstairs);
            mapOverlay.setImageResource(upstairsIDs[roomID - 50]);
        } else {
            map.setImageResource(R.drawable.downstairs);
            mapOverlay.setImageResource(downstairsIDs[roomID]);
        }
        overlayOn = true;

        startOverlayUpdate();

    }

    private void startOverlayUpdate() {
        if (!runnableActive) {
            mapOverlayToggle.setChecked(true);
            runnableActive = true;
            handler.postDelayed(runnable, 500);
        }
    }

    private void stopOverlayUpdate() {
        if (runnableActive) {
            mapOverlay.setVisibility(View.GONE);
            mapOverlayToggle.setChecked(false);
            runnableActive = false;
            handler.removeCallbacks(runnable);
        }
    }

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            changeMapOverlayVisibity();
        }
    };

    private void changeMapOverlayVisibity() {
        runnableActive = false;
        mapOverlay.setVisibility(overlayOn ? View.GONE : View.VISIBLE);
        overlayOn = !overlayOn;
        startOverlayUpdate();
    }
}
