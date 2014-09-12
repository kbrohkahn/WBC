package org.boardgamers.wbc;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

public class ScheduleFragment extends Fragment {
	final static String TAG="Schedule Fragment";

	private ExpandListAdapter listAdapter;
	private ExpandableListView listView;

	// private Vibrator vibrator;

	private int dayID;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	    Bundle savedInstanceState) {

		View view=inflater
		    .inflate(R.layout.schedule_fragment, container, false);

		listView=(ExpandableListView) view.findViewById(R.id.sf_schedule);

		// get and set current day
		dayID=getArguments().getInt("current_day");

		listAdapter=new ExpandListAdapter(this.getActivity(),
		    ScheduleActivity.dayList.get(dayID));
		listView.setAdapter(listAdapter);
		listView.setDividerHeight(0);

		expandAll(0);

		return view;
	}

	@Override
	public void onResume() {
		MyApp.updateTime(getResources());
		if (dayID==MyApp.day)
			listView.setSelectedGroup(MyApp.hour-5);

		listAdapter.notifyDataSetChanged();

		super.onResume();
	}

	public void expandAll(int start) {
		for (int i=start; i<listAdapter.getGroupCount(); i++)
			listView.expandGroup(i);
	}

	public void collapseAll(int start) {
		for (int i=start; i<listAdapter.getGroupCount(); i++)
			listView.collapseGroup(i);
	}

	public void selectGame(int gID, String eID) {
		MyApp.SELECTED_GAME_ID=gID;
		MyApp.SELECTED_EVENT_ID=eID;

		Intent intent=new Intent(getActivity(), TournamentActivity.class);
		startActivity(intent);

	}

	class ExpandListAdapter extends BaseExpandableListAdapter {
		private final ArrayList<EventGroup> hours;
		private final LayoutInflater inflater;

		public ExpandListAdapter(Context c, ArrayList<EventGroup> h) {
			hours=h;

			inflater=LayoutInflater.from(c);
		}

		public void addItem(Event e, EventGroup h) {
			if (!hours.contains(h)) {
				hours.add(h);
			}
			int index=hours.indexOf(h);
			hours.get(index).events.add(e);
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return hours.get(groupPosition).events.get(childPosition);
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
		    boolean isLastChild, View view, ViewGroup parent) {
			final Event event=(Event) getChild(groupPosition, childPosition);

			int tColor=getTextColor(event);
			int tType=getTextStyle(event);

			if (event.starred
			    ||MyApp.allTournaments.get(event.tournamentID).visible)
				view=inflater.inflate(R.layout.schedule_item, parent, false);
			else
				return inflater.inflate(R.layout.schedule_gone, parent, false);

			view.setVisibility(View.VISIBLE);

			TextView title=(TextView) view.findViewById(R.id.si_name);
			if (groupPosition==0)
				title.setText(String.valueOf(event.hour)+" - "+event.title);
			else
				title.setText(event.title);
			title.setTypeface(null, tType);
			title.setTextColor(tColor);

			if (event.title.indexOf("Junior")>-1) {
				title.setCompoundDrawablesWithIntrinsicBounds(
				    R.drawable.junior_icon, 0, 0, 0);
			}

			TextView duration=(TextView) view.findViewById(R.id.si_duration);
			duration.setText(String.valueOf(event.duration));
			duration.setTypeface(null, tType);
			duration.setTextColor(tColor);

			if (event.continuous) {
				duration.setCompoundDrawablesWithIntrinsicBounds(0, 0,
				    R.drawable.continuous_icon, 0);
			}

			TextView location=(TextView) view.findViewById(R.id.si_location);
			location.setTypeface(null, tType);
			location.setTextColor(tColor);

			SpannableString locationText=new SpannableString(event.location);
			locationText.setSpan(new UnderlineSpan(), 0, locationText.length(),
			    0);
			location.setText(locationText);

			location.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showMapDialog(event.location);
				}
			});

			final int group=groupPosition;
			ImageView starIV=(ImageView) view.findViewById(R.id.si_star);
			starIV.setImageResource(event.starred ? R.drawable.star_on
			    : R.drawable.star_off);
			starIV.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					changeEventStar(event, !event.starred, group);
				}
			});

			boolean started=event.day*24+event.hour<=MyApp.day*24+MyApp.hour;
			boolean ended=event.day*24+event.hour+event.totalDuration<=MyApp.day
			    *24+MyApp.hour;
			boolean happening=started&&!ended;

			if (childPosition%2==0) {
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

			final int tID=event.tournamentID;
			final String eID=event.identifier;
			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					selectGame(tID, eID);
				}
			});

			return view;
		}

		public void showMapDialog(String room) {
			Intent intent=new Intent(getActivity(), Map.class);
			intent.putExtra("room", room);
			startActivity(intent);
		}

		@Override
		public Object getGroup(int groupPosition) {
			return hours.get(groupPosition);
		}

		@Override
		public View getGroupView(final int groupPosition,
		    final boolean isExpanded, View view, ViewGroup parent) {
			if (view==null)
				view=inflater.inflate(R.layout.schedule_group, parent, false);

			TextView name=(TextView) view.findViewById(R.id.sg_name);

			SharedPreferences settings=getActivity().getSharedPreferences(
			    getResources().getString(R.string.sp_file_name),
			    Context.MODE_PRIVATE);

			boolean hours24=settings.getBoolean("24_hour", true);
			int hoursID=(hours24 ? R.array.hours_24 : R.array.hours_12);
			String[] hours=getResources().getStringArray(hoursID);

			String groupTitle;
			if (groupPosition==0)
				groupTitle="My Events: ";
			else
				groupTitle=hours[groupPosition-1]+": ";

			if (groupPosition>0) {
				int i;

				// group is 7pm or 8pm : only search events starting today
				if (groupPosition<3)
					i=dayID;
				else
					i=0;

				for (; i<=dayID; i++) {
					for (Event event : ScheduleActivity.dayList.get(i).get(0).events) {
						// check if starred event has started
						if (i*24+event.hour<=dayID*24+groupPosition+6) {
							// check if starred event ends after this hour

							if (i*24+event.hour+event.totalDuration>dayID*24
							    +groupPosition+6) {
								groupTitle+=event.title+", ";
							}
						}
					}
				}

			}
			name.setText(groupTitle.substring(0, groupTitle.length()-2));
			name.setSelected(true);

			int id=0;
			if (!isExpanded)
				id=R.drawable.list_group_closed;
			else
				id=R.drawable.list_group_open;

			name.setCompoundDrawablePadding(5);
			name.setCompoundDrawablesWithIntrinsicBounds(id, 0, 0, 0);

			view.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// vibrator.vibrate(50);

					if (isExpanded)
						listView.collapseGroup(groupPosition);
					else
						listView.expandGroup(groupPosition);
				}
			});

			view.setOnLongClickListener(new View.OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					// vibrator.vibrate(new long[] { 0, 20, 20, 20 }, -1);
					if (isExpanded)
						collapseAll(groupPosition);
					else
						expandAll(groupPosition);
					return false;
				}
			});

			return view;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return hours.get(groupPosition).events.size();
		}

		@Override
		public int getGroupCount() {
			return hours.size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			return hours.get(groupPosition).ID;
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

	public void changeEventStar(Event event, boolean starred, int groupPosition) {
		// update event's star
		if (groupPosition==0) {
			// if in first group, need to find original event
			List<Event> events=ScheduleActivity.dayList.get(event.day).get(
			    event.hour-6).events;
			for (int i=0; i<events.size(); i++) {
				Event temp=events.get(i);
				if (temp.identifier.equalsIgnoreCase(event.identifier)) {
					temp.starred=starred;
					break;
				}
			}
		} else
			event.starred=starred;
		if (starred)
			ScheduleActivity.addStarredEvent(event);
		else
			ScheduleActivity.removeStarredEvent(event.identifier, event.day);

		listAdapter.notifyDataSetChanged();
	}

	public int getTextColor(Event event) {
		if (event.qualify)
			return MyApp.COLOR_QUALIFY;
		else if (event.title.indexOf("Junior")>-1)
			return MyApp.COLOR_JUNIOR;
		else if (event.format.equalsIgnoreCase("Seminar")
		    ||event.format.equalsIgnoreCase("Meeting"))
			return MyApp.COLOR_SEMINAR;
		else if (event.eClass.length()==0)
			return MyApp.COLOR_NON_TOURNAMENT;
		else
			return Color.BLACK;

	}

	public int getTextStyle(Event event) {
		if (event.qualify)
			return Typeface.BOLD;
		else if (event.title.indexOf("Junior")>-1)
			return Typeface.NORMAL;
		else if (event.eClass.length()==0
		    ||event.format.equalsIgnoreCase("Demo"))
			return Typeface.ITALIC;
		else
			return Typeface.NORMAL;

	}
}
