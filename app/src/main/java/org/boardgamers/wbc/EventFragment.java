package org.boardgamers.wbc;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

public class EventFragment extends Fragment {
  private final String TAG="Event Fragment";

  private final int[] finishIDs=
      {R.id.ef_finish_0, R.id.ef_finish_1, R.id.ef_finish_2, R.id.ef_finish_3, R.id.ef_finish_4,
          R.id.ef_finish_5, R.id.ef_finish_6};

  public ImageView star;
  private Event event;
  private Tournament tournament;

  private ImageView boxIV;
  private RelativeLayout timeLayout;
  private TextView eTitle;
  private TextView eFormat;
  private TextView eClass;
  private TextView location;
  private TextView time;
  private EditText noteET;
  private String note;

  private TextView gameGM;
  private TextView previewLink;
  private TextView reportLink;
  private LinearLayout finishLayout;
  private RadioGroup finishGroup;
  private RadioButton[] finishButtons;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view=inflater.inflate(R.layout.event_fragment, container, false);

    timeLayout=(RelativeLayout) view.findViewById(R.id.ef_time_layout);
    eTitle=(TextView) view.findViewById(R.id.ef_title);
    eFormat=(TextView) view.findViewById(R.id.ef_format);
    eClass=(TextView) view.findViewById(R.id.ef_class);
    location=(TextView) view.findViewById(R.id.ef_location);
    time=(TextView) view.findViewById(R.id.ef_time);

    star=(ImageView) view.findViewById(R.id.ef_star);
    star.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        changeEventStar(!event.starred);

      }
    });

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

    gameGM=(TextView) view.findViewById(R.id.ef_gm);

    previewLink=(TextView) view.findViewById(R.id.ef_preview_link);
    reportLink=(TextView) view.findViewById(R.id.ef_report_link);

    finishGroup=(RadioGroup) view.findViewById(R.id.ef_finish);
    finishLayout=(LinearLayout) view.findViewById(R.id.ef_finish_layout);
    finishButtons=new RadioButton[finishIDs.length];
    for (int i=0; i<finishIDs.length; i++) {
      finishButtons[i]=(RadioButton) view.findViewById(finishIDs[i]);
    }

    boxIV=(ImageView) view.findViewById(R.id.ef_box_image);

    if (!MainActivity.SELECTED_EVENT_ID.equalsIgnoreCase("")) {
      for (ArrayList<Event> events : MainActivity.dayList) {
        for (Event event : events) {
          if (event.identifier.equalsIgnoreCase(MainActivity.SELECTED_EVENT_ID)) {
            setEvent(event);
            return view;
          }
        }
      }
    }

    return view;

  }

  public void setEvent(Event e) {
    event=e;

    eTitle.setText(event.title);

    eFormat.setText("Format: "+event.format);

    eClass.setText("Class: "+event.eClass);

    SpannableString locationText=new SpannableString(event.location);
    locationText.setSpan(new UnderlineSpan(), 0, locationText.length(), 0);
    location.setText(locationText);

    location.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        MainActivity.SELECTED_ROOM=event.location;
        Intent intent=new Intent(getActivity(), MapActivity.class);
        getActivity().startActivity(intent);
      }
    });

    String minute;
    if (event.duration<1) {
      minute=String.valueOf((int) (event.duration*60+.5));
    } else {
      minute="00";
    }

    time.setText(MainActivity.dayStrings[event.day]+", "+String.valueOf(event.hour)+"00 to "+
        String.valueOf((event.hour+(int) event.duration)+minute));

    if (event.continuous) {
      time.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.continuous_icon, 0);
    }

    star.setImageResource(event.starred ? R.drawable.star_on : R.drawable.star_off);

    SharedPreferences sp=getActivity().
        getSharedPreferences(getResources().getString(R.string.sp_file_name), Context.MODE_PRIVATE);
    note=sp.getString(getResources().getString(R.string.sp_event_note)+event.identifier, "");
    noteET.setText(note);

    boolean started=event.day*24+event.hour<=MainActivity.currentDay*24+MainActivity.currentHour;
    boolean ended=event.day*24+event.hour+event.totalDuration<=
        MainActivity.currentDay*24+MainActivity.currentHour;
    boolean happening=started && !ended;

    if (ended) {
      timeLayout.setBackgroundResource(R.drawable.ended_light);
    } else if (happening) {
      timeLayout.setBackgroundResource(R.drawable.current_light);
    } else {
      timeLayout.setBackgroundResource(R.drawable.future_light);
    }

    tournament=MainActivity.allTournaments.get(event.tournamentID);

    // check if last event is class (is tournament)
    if (tournament.isTournament && tournament.ID<1000) {

      // links available for tournament
      previewLink.setVisibility(View.VISIBLE);
      previewLink.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View v) {
          getActivity().startActivity(new Intent(Intent.ACTION_VIEW,
              Uri.parse("http://boardgamers.org/yearbkex/"+tournament.label+"pge.htm")));
        }
      });

      reportLink.setVisibility(View.VISIBLE);
      reportLink.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View v) {
          getActivity().startActivity(new Intent(Intent.ACTION_VIEW,
              Uri.parse("http://boardgamers.org/yearbook13/"+tournament.label+"pge.htm")));
        }
      });

      // if last event started, allow finish
      for (int i=0; i<=6; i++) {
        finishButtons[i].setClickable(started);
        finishButtons[i].setTextColor(started ? Color.BLACK : Color.GRAY);
        finishButtons[i].setVisibility(View.VISIBLE);

        if (i>0 && i<=tournament.prize) {
          finishButtons[i].setTypeface(null, Typeface.BOLD);
        } else {
          finishButtons[i].setTypeface(null, Typeface.ITALIC);
        }
      }

      finishGroup.check(finishIDs[tournament.finish]);

      finishLayout.setVisibility(View.VISIBLE);
    } else {
      // not tournament
      finishLayout.setVisibility(View.GONE);
      previewLink.setVisibility(View.GONE);
      reportLink.setVisibility(View.GONE);
    }

    // set title
    //getActivity().setTitle(tournament.title);

    // set GM
    gameGM.setText("GM: "+tournament.gm);

    try {
      InputStream is=(InputStream) new URL("http://boardgamers.org/boxart/"+tournament.label+".jpg")
          .getContent();
      boxIV.setImageDrawable(Drawable.createFromStream(is, "src name"));

    } catch (Exception ex) {
      Log.d(TAG, "Unable to load image");
    }

  }

  private void changeEventStar(boolean starred) {
    event.starred=starred;
    star.setImageResource(starred ? R.drawable.star_on : R.drawable.star_off);

    // update in schedule activity
    ArrayList<Event> eventList=
        MainActivity.dayList.get(event.day*MainActivity.GROUPS_PER_DAY+event.hour-6);
    for (Event tempE : eventList) {
      if (tempE.identifier.equalsIgnoreCase(event.identifier)) {
        tempE.starred=starred;
        break;
      }
    }
    if (starred) {
      MainActivity.addStarredEvent(event);
    } else {
      MainActivity.removeStarredEvent(event.identifier, event.day);
    }
  }

  @Override
  public void onPause() {
    // save note on pause

    note=noteET.getText().toString();

    SharedPreferences.Editor editor=getActivity()
        .getSharedPreferences(getResources().getString(R.string.sp_file_name), Context.MODE_PRIVATE)
        .edit();

    editor.putString(getResources().getString(R.string.sp_event_note)+event.identifier, note);

    if (tournament.isTournament) {
      for (int i=0; i<finishButtons.length; i++) {
        if (finishButtons[i].isChecked()) {
          editor.putInt("fin_"+tournament.title, i);

          MainActivity.allTournaments.get(tournament.ID).finish=i;
          break;
        }
      }
    }

    editor.apply();

    super.onPause();
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
}
