package org.boardgamers.wbc;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Kevin
 */
public class DefaultListAdapter extends BaseExpandableListAdapter {
  //private final String TAG="Default Adapter";

  private int COLOR_JUNIOR;
  private int COLOR_SEMINAR;
  private int COLOR_QUALIFY;
  private int COLOR_OPEN_TOURNAMENT;
  private int COLOR_NON_TOURNAMENT;

  protected final LayoutInflater inflater;
  protected final DefaultListFragment fragment;
  protected final String[] dayStrings;
  private final int id;

  public long hoursIntoConvention;

  protected List<List<Event>> events;

  public DefaultListAdapter(DefaultListFragment f, List<List<Event>> e, int i) {
    inflater=(LayoutInflater) f.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    fragment=f;
    events=e;
    id=i;

    hoursIntoConvention=MainActivity.getHoursIntoConvention();
    dayStrings=f.getResources().getStringArray(R.array.days);

    // get resources
    COLOR_JUNIOR=f.getResources().getColor(R.color.junior);
    COLOR_SEMINAR=f.getResources().getColor(R.color.seminar);
    COLOR_QUALIFY=f.getResources().getColor(R.color.qualify);
    COLOR_NON_TOURNAMENT=f.getResources().getColor(R.color.non_tournament);
    COLOR_OPEN_TOURNAMENT=f.getResources().getColor(R.color.open_tournament);
  }

  @Override
  public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View view,
                           ViewGroup parent) {

    final Event event=(Event) getChild(groupPosition, childPosition);

    if (view==null) {
      view=inflater.inflate(R.layout.list_item, parent, false);

      int tColor=getTextColor(event);
      int tType=getTextStyle(event);

      TextView title=(TextView) view.findViewById(R.id.li_title);
      title.setText(event.title);
      title.setTypeface(null, tType);
      title.setTextColor(tColor);

      if (event.title.contains("Junior")) {
        title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.junior_icon, 0, 0, 0);
      }

      TextView hour=(TextView) view.findViewById(R.id.li_hour);
      hour.setText(String.valueOf(event.hour*100));
      hour.setTypeface(null, tType);
      hour.setTextColor(tColor);

      TextView duration=(TextView) view.findViewById(R.id.li_duration);
      duration.setText(String.valueOf(event.duration));
      duration.setTypeface(null, tType);
      duration.setTextColor(tColor);

      if (event.continuous) {
        duration.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.continuous_icon, 0);
      }

      TextView location=(TextView) view.findViewById(R.id.li_location);
      location.setText(event.location);
      location.setTypeface(null, tType);
      location.setTextColor(tColor);

      view.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          selectEvent(event);
        }
      });
    }

    ImageView starIV=(ImageView) view.findViewById(R.id.li_star);
    starIV.setImageResource(event.starred ? R.drawable.star_on : R.drawable.star_off);
    starIV.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        changeEventStar(event);
      }
    });

    boolean started=event.day*24+event.hour<=hoursIntoConvention;
    boolean ended=event.day*24+event.hour+event.totalDuration<=hoursIntoConvention;
    boolean happening=started && !ended;

    if (event.id==MainActivity.selectedEventId) {
      view.setBackgroundResource(R.drawable.selected);
    } else if (childPosition%2==0) {
      if (ended) {
        view.setBackgroundResource(R.drawable.ended_light);
      } else if (happening) {
        view.setBackgroundResource(R.drawable.current_light);
      } else {
        view.setBackgroundResource(R.drawable.future_light);
      }
    } else {
      if (ended) {
        view.setBackgroundResource(R.drawable.ended_dark);
      } else if (happening) {
        view.setBackgroundResource(R.drawable.current_dark);
      } else {
        view.setBackgroundResource(R.drawable.future_dark);
      }
    }

    return view;
  }

  public void selectEvent(Event event) {
    EventFragment eventFragment=(EventFragment) fragment.getActivity().getSupportFragmentManager()
        .findFragmentById(R.id.eventFragment);
    if (eventFragment!=null) {
      // don't call setEvent if it's the same event
      if (MainActivity.selectedEventId!=event.id) {
        eventFragment.setEvent(event.id);
        MainActivity.selectedEventId=event.id;
      }
    } else {
      MainActivity.selectedEventId=event.id;
      Intent intent=new Intent(fragment.getActivity(), EventActivity.class);
      fragment.getActivity().startActivity(intent);
    }
    notifyDataSetChanged();
  }

  @Override
  public View getGroupView(final int groupPosition, final boolean isExpanded, View view,
                           final ViewGroup parent) {
    view=inflater.inflate(getGroupViewId(groupPosition), parent, false);

    TextView name=(TextView) view.findViewById(R.id.sg_name);
    name.setText(getGroupTitle(groupPosition));
    name.setSelected(true);

    if (isExpanded) {
      view.setBackgroundResource(R.drawable.group_expanded);
    } else {
      view.setBackgroundResource(R.drawable.group_collapsed);
    }

    return view;
  }

  public int getGroupViewId(int groupPosition) {
    return R.layout.list_group_large;
  }

  public String getGroupTitle(int groupPosition) {
    return null;
  }

  @Override
  public boolean isChildSelectable(int groupPosition, int childPosition) {
    return true;
  }

  @Override
  public long getChildId(int groupPosition, int childPosition) {
    long id;
    try {
      id=getGroupId(groupPosition)+events.get(groupPosition).get(childPosition).id;
    } catch (NullPointerException e) {
      id=-1;
    }
    return id;
  }

  @Override
  public long getGroupId(int groupPosition) {
    return groupPosition*1000;
  }

  @Override
  public boolean hasStableIds() {
    return true;
  }

  /**
   * @param event - event needed for format, title, class, and qualify
   * @return integer value of color
   */
  public int getTextColor(Event event) {
    if (event.qualify) {
      return COLOR_QUALIFY;
    } else if (event.title.contains("Junior")) {
      return COLOR_JUNIOR;
    } else if (event.format.equalsIgnoreCase("Seminar")) {
      return COLOR_SEMINAR;
    } else if (event.format.equalsIgnoreCase("SOG") || event.format.equalsIgnoreCase("MP Game") ||
        event.title.indexOf("Open Gaming")==0) {
      return COLOR_OPEN_TOURNAMENT;
    } else if (event.eClass.length()==0) {
      return COLOR_NON_TOURNAMENT;
    } else {
      return Color.BLACK;
    }
  }

  /**
   * @param event - event needed for format, title, class, and qualify
   * @return integer value of typeface
   */
  public int getTextStyle(Event event) {
    if (event.qualify) {
      return Typeface.BOLD;
    } else if (event.title.contains("Junior")) {
      return Typeface.NORMAL;
    } else if (event.eClass.length()==0 || event.format.equalsIgnoreCase("Demo")) {
      return Typeface.ITALIC;
    } else {
      return Typeface.NORMAL;
    }
  }

  @Override
  public Object getChild(int groupPosition, int childPosition) {
    return events.get(groupPosition).get(childPosition);
  }

  @Override
  public int getChildrenCount(int groupPosition) {
    return events.get(groupPosition).size();
  }

  @Override
  public Object getGroup(int groupPosition) {
    return events.get(groupPosition);
  }

  @Override
  public int getGroupCount() {
    return events.size();
  }

  public void updateList() {
    hoursIntoConvention=MainActivity.getHoursIntoConvention();
    notifyDataSetChanged();
  }

  public void changeEventStar(Event event) {
    event.starred=!event.starred;
    updateEvent(event);
    notifyDataSetChanged();

    Event[] events={event};
    MainActivity.changeEvents(fragment.getActivity(), events, id);
  }

  public void updateEvent(Event event) {}

  public void removeEvents(List<Event> deletedEvents) {}
}
