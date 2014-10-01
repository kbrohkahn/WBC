package org.boardgamers.wbc;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Summary extends FragmentActivity {
  final static String TAG = "Summary Actviity";

  private static String dialogText;
  private static String dialogTitle;

  private SummaryListAdapter listAdapter;
  private ExpandableListView listView;
  private static ArrayList<Event>[] summaryList;
  private String[] dayStrings;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.summary);
    getActionBar().setHomeButtonEnabled(true);
    getActionBar().setDisplayHomeAsUpEnabled(true);

    dayStrings = getResources().getStringArray(R.array.days);

    summaryList = new ArrayList<Event>[dayStrings.length;
    for (int i = 0; i < dayStrings.length; i++) {
      summaryList[i] = new ArrayList<Event>();
      ArrayList<Event> group = MyApp.dayList.get(i).get(0).events;
      for (int j = 0; j < group.size(); j++) {
        summary[i].add(group.get(j));
      }
    }

    listAdapter = new ExpandableListAdapter();

    listView = (ExpandableListView) findViewById(R.id.summary_list_view);
    listView.setAdapter(listAdapter);
    listView.setDividerHeight(0);

    MyApp.updateTime(getResources());

    SharedPreferences sp = getSharedPreferences(
        getResources().getString(R.string.sp_file_name),
        Context.MODE_PRIVATE);

    int version = sp.getInt("last_app_version", 0);

    // alert to notify of broken ids
    if (version < 11) {
      AlertDialog.Builder eventIdAlertBuilder = new AlertDialog.Builder(
          this);
      eventIdAlertBuilder
          .setMessage(
              "Due to schedule changes and source fixes, "
                  + "we had to change the way starred events are saved. "
                  + "All event stars and notes will be saved differently, "
                  + "and data from previous app versions is now obsolete. "
                  + "We are sorry for the inconvenience, "
                  + "but this was necessary to ensure that all events "
                  + "will have unique identifiers throughout schedule "
                  + "variations in the future.").setTitle(
          "Notice");
      eventIdAlertBuilder.setNeutralButton("OK",
          new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();
            }
          });
      eventIdAlertBuilder.create().show();
    }

    // alert to notify dev@boardgamers.org for questions
    if (version < 12) {
      AlertDialog.Builder questionAlertBuilder = new AlertDialog.Builder(
          this);
      questionAlertBuilder
          .setMessage(
              "Please send all questions, comments, requests, etc. to Kevin Broh-Kahn at dev@boardgamers.org (link in \"About\" page via main menu)")
          .setTitle("Questions?");
      questionAlertBuilder.setNeutralButton("OK",
          new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();
            }
          });
      questionAlertBuilder.create().show();
    }

    // save current version code
    int versionCode;
    try {
      versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
    } catch (NameNotFoundException e) {
      Toast.makeText(
          this,
          "ERROR: Could not find version code,"
              + "contact dev@boardgamers.org for help.",
          Toast.LENGTH_LONG).show();
      versionCode = -1;
      e.printStackTrace();
    }

    SharedPreferences.Editor editor = sp.edit();
    editor.putInt("last_app_version", versionCode);
    editor.commit();

    dialogText = getIntent().getStringExtra("changes");
    dialogTitle = "Schedule Changes";
    if (!dialogText.equalsIgnoreCase("")) {
      DialogText dc = new DialogText();
      dc.show(getFragmentManager(), "changes_dialog");
    }
  }

  @Override
  public void onResume() {
    listAdapter.notifyDataSetChanged();
    super.onResume();
  }

  class SummaryListAdapter extends BaseExpandableListAdapter {
    private final LayoutInflater inflater;

    public SummaryListAdapter() {
      inflater = (LayoutInflater)
          getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
      return summaryList[groupPosition].get(childPosition);
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View view, ViewGroup parent) {
      final Event event = (Event) getChild(groupPosition, childPosition);

      int tColor = MyApp.getTextColor(event);
      int tType = MyApp.getTextStyle(event);

      view = inflater.inflate(R.layout.schedule_item, parent, false);

      TextView title = (TextView) view.findViewById(R.id.si_name);
      if (groupPosition == 0)
        title.setText(String.valueOf(event.hour) + " - " + event.title);
      else
        title.setText(event.title);
      title.setTypeface(null, tType);
      title.setTextColor(tColor);

      if (event.title.indexOf("Junior") > -1) {
        title.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.junior_icon, 0, 0, 0);
      }

      TextView duration = (TextView) view.findViewById(R.id.si_duration);
      duration.setText(String.valueOf(event.duration));
      duration.setTypeface(null, tType);
      duration.setTextColor(tColor);

      if (event.continuous) {
        duration.setCompoundDrawablesWithIntrinsicBounds(0, 0,
            R.drawable.continuous_icon, 0);
      }

      TextView location = (TextView) view.findViewById(R.id.si_location);
      location.setTypeface(null, tType);
      location.setTextColor(tColor);

      SpannableString locationText = new SpannableString(event.location);
      locationText.setSpan(new UnderlineSpan(), 0, locationText.length(),
          0);
      location.setText(locationText);
      location.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          showMapDialog(event.location);
        }
      });

      ImageView starIV = (ImageView) view.findViewById(R.id.si_star);
      starIV.setImageResource(event.starred ? R.drawable.star_on
          : R.drawable.star_off);
      starIV.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          removeEventStar(event);
        }
      });

      boolean started = event.day * 24 + event.hour <= MyApp.day * 24 + MyApp.hour;
      boolean ended = event.day * 24 + event.hour + event.totalDuration <= MyApp.day
          * 24 + MyApp.hour;
      boolean happening = started && !ended;

      if (childPosition % 2 == 0) {
        if (ended)
          view.setBackgroundResource(R.drawable.ended_light);
        else if (happening)
          view.setBackgroundResource(R.drawable.current_light);
        else
          view.setBackgroundResource(R.drawable.future_light);
      } else {
        if (ended)
          view.setBackgroundResource(R.drawable.ended_dark);
        else if (happening)
          view.setBackgroundResource(R.drawable.current_dark);
        else
          view.setBackgroundResource(R.drawable.future_dark);
      }

      final int tID = event.tournamentID;
      final String eID = event.identifier;
      view.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          selectGame(tID, eID);
        }
      });

      return view;
    }

    public void showMapDialog(String room) {
      Intent intent = new Intent(getActivity(), Map.class);
      intent.putExtra("room", room);
      startActivity(intent);
    }

    @Override
    public Object getGroup(int groupPosition) {
      return summaryList[groupPosition].get(groupPosition);
    }

    @Override
    public View getGroupView(final int groupPosition,
                             final boolean isExpanded, View view, ViewGroup parent) {
      if (view == null)
        view = inflater.inflate(R.layout.schedule_group, parent, false);

      String groupTitle = dayStrings[groupPosition];
      TextView name = (TextView) view.findViewById(R.id.sg_name);
      name.setText(groupTitle.substring(0, groupTitle.length() - 2));

      int id;
      if (!isExpanded)
        id = R.drawable.list_group_closed;
      else
        id = R.drawable.list_group_open;

      name.setCompoundDrawablePadding(5);
      name.setCompoundDrawablesWithIntrinsicBounds(id, 0, 0, 0);

      view.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View v) {
          if (isExpanded)
            listView.collapseGroup(groupPosition);
          else
            listView.expandGroup(groupPosition);
        }
      });
      return view;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
      return MyApp.dayList[dayID].get(groupPosition).events.size();
    }

    @Override
    public int getGroupCount() {
      return MyApp.dayList[dayID].size();
    }

    @Override
    public long getGroupId(int groupPosition) {
      return MyApp.dayList[dayID].get(groupPosition).ID;
    }

    @Override
    public boolean hasStableIds() {
      return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
      return false;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
      return childPosition;
    }
  }

  public void removeEventStar(Event event) {
    // remove from summary list
    List<Event> searchList = summaryList[event.day];
    for (int i = 0; i < searchList.size(); i++) {
      if (searchList.get(i).identifier.equalsIgnoreCase(event.identifier)) {
        searchList.remove(i);
      }
    }

    // remove from "My Events" in day list
    searchList = MyApp.dayList.get(event.day).get(0).events;
    for (Event tempE : searchList) {
      if (tempE.identifier.equalsIgnoreCase(event.identifier)) {
        MyApp.dayList.get(event.day).get(0).events.remove(tempE);
        break;
      }
    }

    // remove from day list
    searchList = MyApp.dayList.get(event.day).get(event.hour - 6).events;
    for (Event tempE : searchList) {
      if (tempE.identifier.equalsIgnoreCase(event.identifier)) {
        tempE.starred = false;
        break;
      }
    }
    listAdapter.notifyDataSetChanged();
  }

  public static class DialogText extends DialogFragment {
    final String TAG = "Changes Dialog";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      Dialog dialog = super.onCreateDialog(savedInstanceState);
      dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
      return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.dialog_text, container, false);

      TextView title = (TextView) view.findViewById(R.id.dt_title);
      title.setText(dialogTitle);

      LinearLayout layout = (LinearLayout) view
          .findViewById(R.id.dt_layout);
      layout.removeAllViews();

      final Context context = getActivity();
      final Resources resources = getResources();
      int padding = (int) resources.getDimension(R.dimen.text_margin_small);

      TextView textView = new TextView(context);
      textView.setText(dialogText);
      textView.setTextAppearance(context, R.style.medium_text);
      textView.setGravity(Gravity.CENTER);
      textView.setPadding(padding, padding, padding, padding);

      layout.addView(textView);

      Button close = (Button) view.findViewById(R.id.dt_close);
      close.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View v) {
          dismiss();
        }
      });

      return view;

    }
  }

  public static class DialogNotes extends DialogFragment {
    final String TAG = "Notes Dialog";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      Dialog dialog = super.onCreateDialog(savedInstanceState);
      dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
      return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.dialog_text, container, false);

      TextView title = (TextView) view.findViewById(R.id.dt_title);
      title.setText("Event notes");

      LinearLayout layout = (LinearLayout) view
          .findViewById(R.id.dt_layout);
      layout.removeAllViews();

      final Resources resources = getResources();
      final Context context = getActivity();
      int padding = (int) resources.getDimension(R.dimen.text_margin_small);

      SharedPreferences sp = context.getSharedPreferences(
          resources.getString(R.string.sp_file_name),
          Context.MODE_PRIVATE);
      String notePrefString = resources.getString(R.string.sp_event_note);

      TextView textView;

      String noteString;

      List<Event> events;
      for (int i = 0; i < MyApp.dayList.size(); i++) {
        for (int j = 1; j < MyApp.dayList.get(i).size(); j++) {
          events = MyApp.dayList.get(i).get(j).events;
          for (Event event : events) {
            noteString = sp
                .getString(
                    notePrefString
                        + String.valueOf(event.identifier),
                    "");
            if (noteString.length() > 0) {

              textView = new TextView(context);
              textView.setText(event.title + ": " + noteString);
              textView.setTextAppearance(context,
                  R.style.medium_text);
              textView.setGravity(Gravity.LEFT);
              textView.setPadding(padding, padding, padding,
                  padding);

              layout.addView(textView);
            }
          }
        }
      }

      if (layout.getChildCount() == 0) {
        textView = new TextView(context);
        textView
            .setText("Add event notes from an event screen, which can be accessed by selecting an event from a tournament screen.");
        textView.setTextAppearance(context, R.style.medium_text);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(padding, padding, padding, padding);

        layout.addView(textView);
      }

      Button close = (Button) view.findViewById(R.id.dt_close);
      close.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View v) {
          dismiss();
        }
      });
      return view;
    }
  }

  public static class DialogFinish extends DialogFragment {
    final String TAG = "Filter Dialog";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      Dialog dialog = super.onCreateDialog(savedInstanceState);
      dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
      return dialog;
    }

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

      for (int i = 0; i < MyApp.allTournaments.size(); i++) {
        tournament = MyApp.allTournaments.get(i);

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
          textView.setGravity(Gravity.LEFT);
          textView.setPadding(padding, padding, padding, padding);

          if (tournament.finish <= tournament.prize)
            textView.setTypeface(null, Typeface.BOLD);
          else
            textView.setTypeface(null, Typeface.ITALIC);

          layout.addView(textView);
        }
      }

      if (layout.getChildCount() == 0) {
        textView = new TextView(context);
        textView
            .setText("Select tournament finish from a tournament screen.\n\n*note: final event for tournament must have started*\n\n");
        textView.setTextAppearance(context, R.style.medium_text);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(padding, padding, padding, padding);

        layout.addView(textView);
      }

      Button close = (Button) view.findViewById(R.id.dt_close);
      close.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View v) {
          dismiss();

        }
      });

      return view;

    }
  }

  /**
   * ************************ MENU **************************
   */

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.main, menu);

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.all_events:
        startActivity(new Intent(this, ScheduleActivity.class));
        return true;
      case R.id.create_event:
        MyApp.SELECTED_GAME_ID = 0;
        MyApp.SELECTED_EVENT_ID = "";

        Intent intent = new Intent(this, TournamentActivity.class);
        startActivity(intent);
        return true;
      case R.id.search:
        new DialogSearch().show(getFragmentManager(), "search_dialog");
        return true;
      case R.id.settings:
        startActivity(new Intent(this, Settings.class));
        return true;
      case R.id.filter:
        startActivity(new Intent(this, Filter.class));
        return true;
      case R.id.finish:
        new DialogFinish().show(getFragmentManager(), "finish_dialog");
        return true;
      case R.id.note:
        new DialogNotes().show(getFragmentManager(), "notes_dialog");
        return true;
      case R.id.help:
        Intent helpIntent = new Intent(this, Help.class);
        startActivity(helpIntent);
        return true;
      case R.id.about:
        Intent aboutIntent = new Intent(this, About.class);
        startActivity(aboutIntent);
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  public void selectGame(int gID, String eID) {
    MyApp.SELECTED_GAME_ID = gID;
    MyApp.SELECTED_EVENT_ID = eID;

    Intent intent = new Intent(this, TournamentActivity.class);
    startActivity(intent);
  }
}
