package org.boardgamers.wbc;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

public class Summary extends FragmentActivity {
	final static String TAG="Schedule Fragment";

	private ExpandListAdapter listAdapter;
	private ExpandableListView listView;

	private String[] dayStrings;

	private ArrayList<EventGroup> summaryList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.summary);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		String summaryFormat=getIntent().getStringExtra("format");

		// get summary list
		dayStrings=getResources().getStringArray(R.array.days);

		summaryList=new ArrayList<EventGroup>(dayStrings.length);
		for (int i=0; i<dayStrings.length; i++) {
			for (int j=0; j<20; j++) {
				summaryList.add(new EventGroup(j+7, i*24+j+7, new ArrayList<Event>()));
			}
		}

		ArrayList<Event> group;
		Event event;
		for (int i=0; i<ScheduleActivity.dayList.size(); i++) {
			for (int j=0; j<ScheduleActivity.dayList.get(i).size(); j++) {
				group=ScheduleActivity.dayList.get(i).get(j).events;

				for (int k=0; k<group.size(); k++) {
					event=group.get(k);

					if (event.starred&&summaryFormat.equalsIgnoreCase("Starred")
					    ||event.format.equalsIgnoreCase(summaryFormat))
						summaryList.get(i*19+j).events.add(event);
				}
			}
		}

		listAdapter=new ExpandListAdapter(this, summaryList);

		listView=(ExpandableListView) findViewById(R.id.summary_list_view);
		listView.setAdapter(listAdapter);

		expandAll(0);
	}

	public void expandAll(int start) {
		for (int i=start; i<listAdapter.getGroupCount(); i++)
			listView.expandGroup(i);
	}

	public void collapseAll(int start) {
		for (int i=start; i<listAdapter.getGroupCount(); i++)
			listView.collapseGroup(i);
	}

	class ExpandListAdapter extends BaseExpandableListAdapter {
		private final LayoutInflater inflater;

		public ExpandListAdapter(Context c, ArrayList<EventGroup> h) {
			inflater=LayoutInflater.from(c);
		}

		@Override
		public Object getChild(int arg0, int arg1) {
			return summaryList.get(arg0).events.get(arg1);
		}

		@Override
		public long getChildId(int arg0, int arg1) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
		    boolean isLastChild, View view, ViewGroup parent) {
			final Event event=(Event) getChild(groupPosition, childPosition);

			int tColor=MyApp.getTextColor(event);
			int tType=MyApp.getTextStyle(event);

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

			return view;
		}

		@Override
		public int getChildrenCount(int arg0) {
			return summaryList.get(arg0).events.size();
		}

		@Override
		public Object getGroup(int arg0) {
			return summaryList.get(arg0);
		}

		@Override
		public int getGroupCount() {
			return summaryList.size();
		}

		@Override
		public long getGroupId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getGroupView(final int groupPosition,
		    final boolean isExpanded, View view, ViewGroup parent) {
			if (view==null)
				view=inflater.inflate(R.layout.schedule_group, parent, false);

			TextView name=(TextView) view.findViewById(R.id.sg_name);

			SharedPreferences settings=getSharedPreferences(
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
					if (isExpanded)
						listView.collapseGroup(groupPosition);
					else
						listView.expandGroup(groupPosition);
				}
			});

			view.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
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
		public boolean hasStableIds() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isChildSelectable(int arg0, int arg1) {
			// TODO Auto-generated method stub
			return false;
		}

	}

	public void showMapDialog(String room) {
		Intent intent=new Intent(this, Map.class);
		intent.putExtra("room", room);
		startActivity(intent);
	}
}
