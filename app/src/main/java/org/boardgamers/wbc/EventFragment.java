package org.boardgamers.wbc;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.InputStream;

public class EventFragment extends Fragment {
  private final String TAG="Event Fragment";

  private final int[] finishIDs=
      {R.id.ef_finish_0, R.id.ef_finish_1, R.id.ef_finish_2, R.id.ef_finish_3, R.id.ef_finish_4,
          R.id.ef_finish_5, R.id.ef_finish_6};

  private final int[] upstairsIDs=
      {R.drawable.room_conestoga_1, R.drawable.room_conestoga_2, R.drawable.room_conestoga_3,
          R.drawable.room_good_spirits_bar, R.drawable.room_heritage, R.drawable.room_lampeter,
          R.drawable.room_laurel_grove, R.drawable.room_open_gaming_pavilion,
          R.drawable.room_showroom, R.drawable.room_vistas_cd, R.drawable.room_wheatland};
  private final int[] downstairsIDs=
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
  private ImageView boxIV;

  // user data
  private EditText noteET;
  private String note;
  private RadioGroup finishGroup;
  private RadioButton[] finishButtons;
  // views
  private LinearLayout finishLayout;
  private LinearLayout timeLayout;
  private ScrollView scrollView;
  private ImageView map;
  private ImageView mapOverlay;

  private int roomID;
  private boolean overlayOn;
  private boolean updateActive;
  private Handler handler=new Handler();
  private final Runnable runnable=new Runnable() {
    @Override
    public void run() {
      updateActive=false;
      changeMapOverlay();
    }
  };

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    Log.d(TAG, "Creating");
    View view=inflater.inflate(R.layout.event_fragment, container, false);

    timeLayout=(LinearLayout) view.findViewById(R.id.ef_time_layout);
    scrollView=(ScrollView) view.findViewById(R.id.ef_scroll_view);
    titleTV=(TextView) view.findViewById(R.id.ef_title);
    dayTV=(TextView) view.findViewById(R.id.ef_day);
    timeTV=(TextView) view.findViewById(R.id.ef_time);
    formatTV=(TextView) view.findViewById(R.id.ef_format);
    classTV=(TextView) view.findViewById(R.id.ef_class);
    locationTV=(TextView) view.findViewById(R.id.ef_location);
    gMTV=(TextView) view.findViewById(R.id.ef_gm);
    previewTV=(TextView) view.findViewById(R.id.ef_preview_link);
    reportTV=(TextView) view.findViewById(R.id.ef_report_link);

    map=(ImageView) view.findViewById(R.id.ef_map);
    mapOverlay=(ImageView) view.findViewById(R.id.ef_map_overlay);
    updateActive=false;

    Button share=(Button) view.findViewById(R.id.ef_share);
    share.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        share();
      }
    });

    Button clear=(Button) view.findViewById(R.id.ef_clear);
    clear.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        noteET.getText().clear();
      }
    });

    noteET=(EditText) view.findViewById(R.id.ef_note);

    finishGroup=(RadioGroup) view.findViewById(R.id.ef_finish);
    finishLayout=(LinearLayout) view.findViewById(R.id.ef_finish_layout);
    finishButtons=new RadioButton[finishIDs.length];
    for (int i=0; i<finishIDs.length; i++) {
      finishButtons[i]=(RadioButton) view.findViewById(finishIDs[i]);
    }

    boxIV=(ImageView) view.findViewById(R.id.ef_box_image);

    Log.d(TAG, "Created");
    setEvent(MainActivity.SELECTED_EVENT_ID);
    Log.d(TAG, "Event set");
    return view;
  }

  @Override
  public void onResume() {
    super.onResume();

    if (roomID>-1) {
      startOverlayUpdate();
    }
  }

  public void setEvent(long id) {
    if (id==-1) {
      titleTV.setVisibility(View.VISIBLE);
      titleTV.setText(getResources().getString(R.string.event_title));
      dayTV.setText(getResources().getString(R.string.event_day));
      timeTV.setText(getResources().getString(R.string.event_time));
      locationTV.setText(getResources().getString(R.string.event_location));
      formatTV.setText(getResources().getString(R.string.event_format));
      classTV.setText(getResources().getString(R.string.event_class));
      gMTV.setText(getResources().getString(R.string.event_gm));

      hideNonTournamentViews();
    } else {
      WBCDataDbHelper dbHelper=new WBCDataDbHelper(getActivity());
      dbHelper.getReadableDatabase();
      event=dbHelper.getEvent(id);
      tournament=dbHelper.getTournament(event.tournamentID);
      dbHelper.close();

      if (event==null) {
        Log.d(TAG, "ERROR: returned null event from database");
        MainActivity.SELECTED_EVENT_ID=-1;
        setEvent(-1);
        return;
      }


      if (getActivity() instanceof EventActivity) {
        titleTV.setVisibility(View.GONE);
        getActivity().setTitle(event.title);
      } else {
        titleTV.setVisibility(View.VISIBLE);
        titleTV.setText(event.title);
      }

      String[] dayStrings=getResources().getStringArray(R.array.days);
      dayTV.setText(dayStrings[event.day]);

      String minute;
      if (event.duration<1) {
        minute=String.valueOf((int) (event.duration*60+.5));
      } else {
        minute="00";
      }
      timeTV.setText(String.valueOf(event.hour)+"00 to "+
          String.valueOf((event.hour+(int) event.duration)+minute));
      if (event.continuous) {
        timeTV.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.continuous_icon, 0);
      }

      locationTV.setText("@"+event.location);
      formatTV.setText(getResources().getString(R.string.event_format)+" "+event.format);
      classTV.setText(getResources().getString(R.string.event_class)+" "+event.eClass);
      gMTV.setText(getResources().getString(R.string.event_gm)+" "+tournament.gm);

      setRoom(event.location);

      noteET.setText(event.note);

      int hoursIntoConvention=MainActivity.getHoursIntoConvention();
      boolean started=event.day*24+event.hour<=hoursIntoConvention;
      boolean ended=event.day*24+event.hour+event.totalDuration<=hoursIntoConvention;
      boolean happening=started && !ended;

      if (ended) {
        timeLayout.setBackgroundResource(R.drawable.ended_light);
      } else if (happening) {
        timeLayout.setBackgroundResource(R.drawable.current_light);
      } else {
        timeLayout.setBackgroundResource(R.drawable.future_light);
      }

      // check if last event is class (is tournament)
      if (tournament.isTournament) {
        finishLayout.setVisibility(View.VISIBLE);
        previewTV.setVisibility(View.VISIBLE);
        reportTV.setVisibility(View.VISIBLE);

        new DownloadImageTask().execute("http://boardgamers.org/boxart/"+tournament.label+".jpg");

        previewTV.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            getActivity().startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://boardgamers.org/yearbkex/"+tournament.label+"pge.htm")));
          }
        });

        reportTV.setOnClickListener(new View.OnClickListener() {

          @Override
          public void onClick(View v) {
            getActivity().startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://boardgamers.org/yearbook13/"+tournament.label+"pge.htm")));
          }
        });

        // if last event started, allow finish
        for (int i=0; i<=6; i++) {
          finishButtons[i].setTextColor(started ? Color.BLACK : Color.GRAY);

          if (i>0 && i<=tournament.prize) {
            finishButtons[i].setTypeface(null, Typeface.BOLD);
          } else {
            finishButtons[i].setTypeface(null, Typeface.ITALIC);
          }
        }
        finishGroup.check(finishIDs[tournament.finish]);
      } else {
        hideNonTournamentViews();
      }
    }
    scrollView.fullScroll(ScrollView.FOCUS_UP);
  }

  public void hideNonTournamentViews() {
    finishLayout.setVisibility(View.GONE);
    previewTV.setVisibility(View.INVISIBLE);
    reportTV.setVisibility(View.INVISIBLE);
  }

  private void share() {
    String s=event.title+": "+note;

    Intent sharingIntent=new Intent(Intent.ACTION_SEND);
    sharingIntent.setType("text/plain");

    String subjectString=event.title;
    sharingIntent.putExtra(Intent.EXTRA_SUBJECT, subjectString);
    sharingIntent.putExtra(Intent.EXTRA_TITLE, subjectString);

    String contentString=s+" #WBC";
    sharingIntent.putExtra(Intent.EXTRA_TEXT, contentString);

    startActivity(Intent.createChooser(sharingIntent, "Share via"));

  }

  @Override
  public void onPause() {
    if (event!=null) {
      // save note on pause
      note=noteET.getText().toString();

      WBCDataDbHelper dbHelper=new WBCDataDbHelper(getActivity());
      dbHelper.getWritableDatabase();
      dbHelper.updateEventNote(event.id, note);

      if (tournament.isTournament) {
        for (int i=0; i<finishButtons.length; i++) {
          if (finishButtons[i].isChecked()) {
            dbHelper.updateTournamentFinish(tournament.id, i);

            break;
          }
        }
      }

      dbHelper.close();
    }

    if (updateActive) {
      updateActive=false;
      handler.removeCallbacks(runnable);
    }

    super.onPause();
  }

  public void setRoom(String room) {
    roomID=-1;
    String[] roomsDownstairs=getResources().getStringArray(R.array.rooms_downstairs);
    for (int i=0; i<roomsDownstairs.length; i++) {
      if (roomsDownstairs[i].equalsIgnoreCase(room)) {
        roomID=i;
        setImageViews();
        return;
      }
    }

    String[] roomsUpstairs=getResources().getStringArray(R.array.rooms_upstairs);
    for (int i=0; i<roomsUpstairs.length; i++) {
      if (roomsUpstairs[i].equalsIgnoreCase(room)) {
        roomID=50+i;
        setImageViews();
        return;
      }
    }
  }

  public void setImageViews() {
    if (roomID>=50) {
      map.setImageResource(R.drawable.upstairs);
      mapOverlay.setImageResource(upstairsIDs[roomID-50]);
    } else {
      map.setImageResource(R.drawable.downstairs);
      mapOverlay.setImageResource(downstairsIDs[roomID]);
    }
    overlayOn=true;

    startOverlayUpdate();

  }

  private void changeMapOverlay() {
    mapOverlay.setVisibility(overlayOn ? View.GONE : View.VISIBLE);
    overlayOn=!overlayOn;
    startOverlayUpdate();
  }

  private void startOverlayUpdate() {
    if (!updateActive) {
      updateActive=true;
      handler.postDelayed(runnable, 500);
    }
  }

  private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

    protected Bitmap doInBackground(String... urls) {
      String urldisplay=urls[0];
      Bitmap bitmap=null;
      try {
        InputStream in=new java.net.URL(urldisplay).openStream();
        bitmap=BitmapFactory.decodeStream(in);
      } catch (Exception e) {
        Log.e("Error", e.getMessage());
        e.printStackTrace();
      }
      return bitmap;
    }

    protected void onPostExecute(Bitmap result) {
      boxIV.setImageBitmap(result);
    }
  }
}
