package org.boardgamers.wbc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class LoadEventsTask extends AsyncTask<Integer, Integer, Integer> {
	private final static String TAG="Load Events Task";

	private String allChanges="";
	private final Context context;
	private final Activity activity;

	private String[] dayStrings;

	public LoadEventsTask(Activity a) {
		activity=a;
		context=a;
	}

	public LoadEventsTask(Context c) {
		context=c;
		activity=null;
	}

	@Override
	protected void onPreExecute() {
		dayStrings=context.getResources().getStringArray(R.array.days);

		ArrayList<EventGroup> temp;
		ScheduleActivity.dayList=new ArrayList<ArrayList<EventGroup>>(
		    dayStrings.length);
		for (int i=0; i<dayStrings.length; i++) {
			temp=new ArrayList<EventGroup>();
			temp.add(new EventGroup(0, i*24, new ArrayList<Event>()));

			for (int j=0; j<18; j++) {
				temp.add(new EventGroup(j+7, i*24+j+7, new ArrayList<Event>()));
			}
			ScheduleActivity.dayList.add(temp);
		}

		super.onPreExecute();
	}

	@Override
	protected Integer doInBackground(Integer... params) {
		final Resources resources=context.getResources();

		SharedPreferences sp=context.getSharedPreferences(
		    resources.getString(R.string.sp_file_name),
		    Context.MODE_PRIVATE);
		String userEventPrefString=resources.getString(R.string.sp_user_event);
		String starPrefString=resources.getString(R.string.sp_event_starred);

		/***** LOAD USER EVENTS *******/

		String identifier, row, temp, eventTitle, eClass, format, gm, tempString, location;
		String[] rowData;
		int tournamentID, index, day, hour, prize, lineNum=0;
		double duration, totalDuration;
		boolean continuous, qualify, isTournamentEvent;

		Event event, tempEvent, prevEvent=null;
		Tournament tournament;

		String tournamentTitle="My Events", tournamentLabel="", shortEventTitle="";
		String change;

		List<Tournament> tournaments=new ArrayList<Tournament>();

		tournamentID=0;
		tournament=new Tournament(tournamentID, tournamentTitle,
		    tournamentLabel, false, 0, "Me");
		tournament.visible=sp.getBoolean("vis_"+tournamentTitle, true);
		tournament.finish=sp.getInt("fin_"+tournamentTitle, 0);

		tournaments.add(tournament);

		for (index=0;; index++) {
			row=sp.getString(userEventPrefString+String.valueOf(index), "");
			if (row.equalsIgnoreCase(""))
				break;

			rowData=row.split("~");

			day=Integer.valueOf(rowData[0]);
			hour=Integer.valueOf(rowData[1]);
			eventTitle=rowData[2];
			duration=Double.valueOf(rowData[3]);
			location=rowData[4];

			identifier=String.valueOf(day*24+hour)+eventTitle;
			event=new Event(identifier, 0, day, hour, eventTitle, "", "",
			    false, duration, false, duration, location);
			event.starred=sp.getBoolean(starPrefString+identifier, false);

			ScheduleActivity.dayList.get(day).get(hour-6).events.add(0, event);
			if (event.starred)
				ScheduleActivity.addStarredEvent(event);
		}

		/***** LOAD SHARED EVENTS *******/

		// get events (may need update)
		int scheduleVersion=sp.getInt(
		    resources.getString(R.string.sp_2014_schedule_version), -1);
		int newVersion=scheduleVersion;

		InputStream is=null;
		InputStreamReader isr=null;
		BufferedReader reader=null;
		String line;

		Log.d(TAG, "Starting parsing");

		/***** PARSE SCHEDULE *****/

		// load schedule file
		try {
			is=context.getAssets().open("schedule2014.txt");
		} catch (IOException e2) {
			Toast.makeText(
			    context,
			    "ERROR: Could not find schedule file,"
			        +"contact dev@boardgamers.org for help.",
			    Toast.LENGTH_LONG).show();
			e2.printStackTrace();
			return -1;
		}
		try {
			isr=new InputStreamReader(is);
		} catch (IllegalStateException e1) {
			Toast.makeText(
			    context,
			    "ERROR: Could not open schedule file,"
			        +"contact dev@boardgamers.org for help.",
			    Toast.LENGTH_LONG).show();
			e1.printStackTrace();
			return -1;
		}

		reader=new BufferedReader(isr);

		int numTournaments=0;
		int numPreviews=0;
		int numJuniors=0;
		int numSeminars=0;

		try {
			final String preExtraStrings[]= { " AFC", " NFC", " FF", " PC",
			    " Circus", " After Action", " Aftermath" };
			final String postExtraStrings[]= { " Demo" };

			final String daysForParsing[]=context.getResources()
			    .getStringArray(R.array.daysForParsing);

			while ((line=reader.readLine())!=null) {
				rowData=line.split("~");

				// day
				tempString=rowData[0];
				for (index=0; index<daysForParsing.length; index++) {
					if (daysForParsing[index].equalsIgnoreCase(tempString))
						break;
				}
				if (index==-1) {
					Log.d(TAG, "Unknown date: "+rowData[2]+" in "+line);
					index=0;
				}
				day=index;

				// title
				eventTitle=rowData[2];

				// time
				boolean halfPast=false;
				tempString=rowData[1];
				if (rowData[1].indexOf(":30")>-1) {
					Log.d(TAG, rowData[2]+" starts at half past");
					tempString=tempString.substring(0, tempString.length()-3);
					halfPast=true;

				}
				hour=Integer.valueOf(tempString);

				if (rowData.length<8) {
					prize=0;
					eClass="";
					format="";
					duration=0;
					continuous=false;
					gm="";
					location="";
				} else {
					// prize
					temp=rowData[3];
					if (temp.equalsIgnoreCase("")||temp.equalsIgnoreCase("-"))
						temp="0";
					prize=Integer.valueOf(temp);

					// class
					eClass=rowData[4];

					// format
					format=rowData[5];

					// duration
					if (rowData[6].equalsIgnoreCase("")
					    ||rowData[6].equalsIgnoreCase("-"))
						duration=0;
					else
						duration=Double.valueOf(rowData[6]);

					if (duration>.33&&duration<.34)
						duration=.33;

					// continuous
					continuous=rowData[7].equalsIgnoreCase("Y");

					// gm
					gm=rowData[8];

					// location
					location=rowData[9];

				}

				// get tournament title and label and event short name
				temp=eventTitle;

				// search through extra strings
				for (String eventExtraString : preExtraStrings) {
					index=temp.indexOf(eventExtraString);
					if (index>-1) {
						temp=temp.substring(0, index)
						    +temp.substring(index+eventExtraString.length());
					}
				}

				// split title in two, first part is tournament title,
				// second is short event title (H1/1)
				isTournamentEvent=eClass.length()>0;

				if (isTournamentEvent||format.equalsIgnoreCase("Preview")) {
					index=temp.lastIndexOf(" ");
					shortEventTitle=temp.substring(index+1);
					temp=temp.substring(0, index);

					if (index==-1) {
						Log.d(TAG, "");
					}
				}

				if (eventTitle.indexOf("Junior")>-1
				    ||eventTitle.indexOf("COIN series")==0
				    ||format.equalsIgnoreCase("SOG")
				    ||format.equalsIgnoreCase("Preview")) {
					tournamentTitle=temp;
					tournamentLabel="-";

				} else if (isTournamentEvent) {
					for (String eventExtraString : postExtraStrings) {
						index=temp.indexOf(eventExtraString);
						if (index>-1) {
							temp=temp.substring(0, index);
						}
					}

					tournamentLabel=rowData[10];
					tournamentTitle=temp;
				} else {
					tournamentLabel="-";
					tournamentTitle=temp;
					tournamentLabel="";

					if (eventTitle.indexOf("Auction")==0)
						tournamentTitle="AuctionStore";

					// search for non tournament main titles
					String[] nonTournamentStrings= { "Open Gaming",
					    "Registration", "Vendors Area", "World at War",
					    "Wits & Wagers", "Texas Roadhouse BPA Fundraiser",
					    "Memoir: D-Day" };
					for (int i=0; i<nonTournamentStrings.length; i++) {
						if (temp.indexOf(nonTournamentStrings[i])==0) {
							tournamentTitle=nonTournamentStrings[i];
							break;
						}
					}
				}

				// check if last 5 in list contains this tournament
				tournamentID=-1;
				for (index=Math.max(0, tournaments.size()-5); index<tournaments
				    .size(); index++) {
					if (tournaments.get(index).title
					    .equalsIgnoreCase(tournamentTitle)) {
						tournamentID=index;
						break;
					}
				}

				if (tournamentID>-1) {
					// update existing tournament
					tournament=tournaments.get(tournamentID);
					if (prize>0)
						tournament.prize=prize;
					if (isTournamentEvent)
						tournament.isTournament=true;

					tournament.gm=gm;
				} else {
					tournamentID=tournaments.size();
					tournament=new Tournament(tournamentID, tournamentTitle,
					    tournamentLabel, isTournamentEvent, prize, gm);

					tournament.visible=sp.getBoolean("vis_"+tournamentTitle,
					    true);
					tournament.finish=sp.getInt("fin_"+tournamentTitle, 0);

					tournaments.add(tournament);

					if (format.equalsIgnoreCase("Preview"))
						numPreviews++;
					else if (eventTitle.indexOf("Junior")>-1)
						numJuniors++;
					else if (format.equalsIgnoreCase("Seminar"))
						numSeminars++;
					else if (isTournamentEvent)
						numTournaments++;

				}

				// Log.d(TAG, String.valueOf(tournamentID)+": "+tournamentTitle
				// +";;;E: "+eventTitle);

				totalDuration=duration;
				qualify=false;

				if (isTournamentEvent||format.equalsIgnoreCase("Junior")) {
					if (shortEventTitle.indexOf("SF")==0) {
						qualify=true;
					} else if (shortEventTitle.indexOf("QF")==0) {
						qualify=true;
					} else if (shortEventTitle.indexOf("F")==0) {
						qualify=true;
					} else if (shortEventTitle.indexOf("QF/SF/F")==0) {
						qualify=true;
						totalDuration*=3;
					} else if (shortEventTitle.indexOf("SF/F")==0) {
						qualify=true;
						totalDuration*=2;
					} else if (continuous&&shortEventTitle.indexOf("R")==0
					    &&shortEventTitle.indexOf("/")>-1) {
						int dividerIndex=shortEventTitle.indexOf("/");
						int startRound=Integer.valueOf(shortEventTitle
						    .substring(1, dividerIndex));
						int endRound=Integer.valueOf(shortEventTitle
						    .substring(dividerIndex+1));

						int currentTime=hour;
						for (int round=0; round<endRound-startRound; round++) {
							// if time passes midnight, next round
							// starts at
							// 9 the next day
							if (currentTime>24) {
								if (currentTime>=24+9)
									Log.d(TAG, "Event "+eventTitle
									    +" goes past 9");
								totalDuration+=9-(currentTime-24);
								currentTime=9;
							}

							totalDuration+=duration;
							currentTime+=duration;

						}

						if (prevEvent.tournamentID==tournamentID) {
							// update previous event total duration
							temp=prevEvent.title;

							// search through extra strings
							for (String eventExtraString : preExtraStrings) {
								index=temp.indexOf(eventExtraString);
								if (index>-1) {
									temp=temp.substring(0, index)
									    +temp.substring(index
									        +eventExtraString.length());
								}
							}

							index=temp.lastIndexOf(" ");

							if (index>-1) {
								shortEventTitle=temp.substring(index+1);
								if (shortEventTitle.indexOf("R")==0) {
									dividerIndex=shortEventTitle.indexOf("/");

									if (dividerIndex==-1)
										Log.d(TAG, "huh: "+shortEventTitle);
									else {

										int prevStartRound=Integer
										    .valueOf(shortEventTitle
										        .substring(1,
										            dividerIndex));

										int realNumRounds=startRound
										    -prevStartRound;

										currentTime=hour;
										prevEvent.totalDuration=0;
										for (int round=0; round<realNumRounds; round++) {
											// if time passes midnight, next
											// round
											// starts at
											// 9 the next day
											if (currentTime>24) {
												if (currentTime>=24+9)
													Log.d(TAG, "Event "
													    +prevEvent.title
													    +" goes past 9");
												prevEvent.totalDuration+=9-(currentTime-24);
												currentTime=9;
											}

											prevEvent.totalDuration+=prevEvent.duration;
											currentTime+=prevEvent.duration;
										}
									}

									List<Event> searchList=ScheduleActivity.dayList
									    .get(prevEvent.day).get(
									        prevEvent.hour-6).events;
									for (int i=0; i<searchList.size(); i++) {
										if (searchList.get(i).identifier
										    .equalsIgnoreCase(prevEvent.identifier))
											searchList.get(i).totalDuration=prevEvent.totalDuration;
									}

									searchList=ScheduleActivity.dayList.get(
									    prevEvent.day).get(0).events;
									for (int i=0; i<searchList.size(); i++) {
										if (searchList.get(i).identifier
										    .equalsIgnoreCase(prevEvent.identifier))
											searchList.get(i).totalDuration=prevEvent.totalDuration;
									}

									Log.d(TAG,
									    "Event "
									        +prevEvent.title
									        +" duration changed to "
									        +String.valueOf(prevEvent.totalDuration));
								}
							}
						}
					} else if (continuous) {
						Log.d(TAG, "Unknown continuous event: "+eventTitle);
					}
				} else if (continuous) {
					Log.d(TAG, "Non tournament event "+eventTitle+" is cont");
				}

				if (halfPast)
					eventTitle=eventTitle+" ("+rowData[1]+")";

				identifier=String.valueOf(day*24+hour)+eventTitle;

				event=new Event(identifier, tournamentID, day, hour,
				    eventTitle, eClass, format, qualify, duration,
				    continuous, totalDuration, location);

				event.starred=sp.getBoolean(starPrefString+event.identifier,
				    false);

				prevEvent=event;

				/********* LOAD INTO DAYLIST *************/
				change="";

				/*
				 * if (event.title.equalsIgnoreCase("Age of Renaissance H1/3 PC")) { int
				 * newDay=5;
				 * 
				 * if (scheduleVersion<0) { change=event.title+": Day changed from "
				 * +dayStrings[event.day]+" to " +dayStrings[newDay]; newVersion=0; }
				 * 
				 * event.day=newDay; }
				 */

				if (!change.equalsIgnoreCase(""))
					allChanges+="\t"+change+"\n\n";

				// LOAD INTO LIST
				ArrayList<Event> searchList=ScheduleActivity.dayList.get(
				    event.day).get(event.hour-6).events;
				if (eventTitle.indexOf("Junior")>-1) {
					index=0;
					for (; index<searchList.size(); index++) {
						tempEvent=searchList.get(index);
						if (tempEvent.title.indexOf("Junior")==-1
						    &&tempEvent.tournamentID>0)
							break;
					}
				} else if (!isTournamentEvent||format.equalsIgnoreCase("Demo")) {
					index=0;
					for (; index<searchList.size(); index++) {
						tempEvent=searchList.get(index);
						if (tempEvent.eClass.length()>0
						    &&tempEvent.title.indexOf("Junior")==-1
						    &&!tempEvent.format.equalsIgnoreCase("Demo"))
							break;
					}
				} else if (event.qualify) {
					index=searchList.size();
				} else {
					index=0;
					for (; index<searchList.size(); index++) {
						tempEvent=searchList.get(index);
						if (tempEvent.qualify)
							break;
					}
				}

				ScheduleActivity.dayList.get(event.day).get(event.hour-6).events
				    .add(index, event);

				if (event.starred)
					ScheduleActivity.addStarredEvent(event);

				lineNum++;

			}

			isr.close();
			is.close();
			reader.close();

			MyApp.NUM_EVENTS=lineNum;

			// save update version
			SharedPreferences.Editor editor=sp.edit();
			editor.putInt(
			    resources.getString(R.string.sp_2014_schedule_version),
			    newVersion);
			editor.commit();

		} catch (IOException e) {

			Toast.makeText(
			    context,
			    "ERROR: Could not parse schedule file,"
			        +"contact dev@boardgamers.org for help.",
			    Toast.LENGTH_LONG).show();
			e.printStackTrace();
			return -1;
		}

		MyApp.allTournaments=tournaments;

		Log.d(TAG, "Finished load, "+String.valueOf(tournamentID)
		    +" total tournaments and "+String.valueOf(lineNum)
		    +" total events");
		Log.d(TAG, "Of total, "+String.valueOf(numTournaments)
		    +" are tournaments, "+String.valueOf(numJuniors)
		    +" are juniors, "+String.valueOf(numPreviews)+" are previews, "
		    +String.valueOf(numSeminars)+" are seminars, ");

		return 1;
	}

	@Override
	protected void onPostExecute(Integer result) {
		if (activity!=null) {
			Intent intent=new Intent(context, ScheduleActivity.class);
			intent.putExtra("changes", allChanges);
			context.startActivity(intent);
			activity.finish();
			super.onPostExecute(result);
		} else {
			NotificationService.checkEvents(context);
		}
	}

}
