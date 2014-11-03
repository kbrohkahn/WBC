package org.boardgamers.wbc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Splash screen so fragments loaded from Main Activity
 */
public class SplashScreen extends Activity {
  private final String TAG = "Splash";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.splash);

    Log.d(TAG, "APP OPEN");

    new LoadEventsTask().execute(null, null, null);
  }

  public void showToast(String string) {
    Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
  }

  public void startMainActivity() {
    startActivity(new Intent(this, MainActivity.class));
    finish();
  }

  /**
   * Load Events Task to load schedule
   */
  public class LoadEventsTask extends AsyncTask<Integer, Integer, Integer> {
    private final static String TAG = "Load Events Task";

    @Override
    protected Integer doInBackground(Integer... params) {
      // SETUP
      MainActivity.dayStrings = getResources().getStringArray(R.array.days);
      MainActivity.dayList = new ArrayList<ArrayList<ArrayList<Event>>>(MainActivity.dayStrings.length);

      ArrayList<ArrayList<Event>> tempList;
      int i = 0;
      while (i < MainActivity.dayStrings.length) {
        tempList = new ArrayList<ArrayList<Event>>();
        for (int j = 0; j < 19; j++) {
          tempList.add(new ArrayList<Event>());
        }
        MainActivity.dayList.add(tempList);
        i++;
      }

      // GET PREFERENCES AND THEIR STRINGS
      SharedPreferences sp = getSharedPreferences(
          getResources().getString(R.string.sp_file_name),
          Context.MODE_PRIVATE);
      String userEventPrefString = getResources().getString(R.string.sp_user_event);
      String starPrefString = getResources().getString(R.string.sp_event_starred);

      MainActivity.allChanges = "";

      /***** LOAD USER EVENTS *******/

      String identifier, row, eventTitle, eClass, format, gm, tempString, location;
      String[] rowData;
      int tournamentID = -1, index, day, hour, prize, lineNum = 0;
      int numTournaments = 0, numPreviews = 0, numJuniors = 0, numSeminars = 0;
      double duration, totalDuration;
      boolean continuous, qualify, isTournamentEvent;

      Event event, tempEvent, prevEvent = null;
      Tournament tournament;
      List<Tournament> tournaments = new ArrayList<Tournament>();
      String tournamentTitle, tournamentLabel, shortEventTitle = "";
      String change;

      for (index = 0; ; index++) {
        row = sp.getString(userEventPrefString + String.valueOf(index), "");
        if (row.equalsIgnoreCase(""))
          break;

        rowData = row.split("~");

        day = Integer.valueOf(rowData[0]);
        hour = Integer.valueOf(rowData[1]);
        eventTitle = rowData[2];
        duration = Double.valueOf(rowData[3]);
        location = rowData[4];

        identifier = String.valueOf(day * 24 + hour) + eventTitle;
        event = new Event(identifier, tournamentID, day, hour, eventTitle, "", "",
            false, duration, false, duration, location);
        event.starred = sp.getBoolean(starPrefString + identifier, false);

        MainActivity.dayList.get(day).get(hour - 6).add(0, event);
        if (event.starred)
          MainActivity.addStarredEvent(event);
      }

      /***** LOAD SHARED EVENTS *******/

      // get events (may need update)
      //int scheduleVersion = sp.getInt(getResources().getString(R.string.sp_2014_schedule_version), -1);
      int newVersion = 13;

      /***** PARSE SCHEDULE *****/

      Log.d(TAG, "Starting parsing");

      // find schedule file
      InputStream is;
      try {
        is = getAssets().open("schedule2014.txt");
      } catch (IOException e2) {
        showToast(
            "ERROR: Could not find schedule file,"
                + "contact dev@boardgamers.org for help.");
        e2.printStackTrace();
        return -1;
      }
      // read schedule file
      InputStreamReader isr;
      try {
        isr = new InputStreamReader(is);
      } catch (IllegalStateException e1) {
        showToast(
            "ERROR: Could not open schedule file,"
                + "contact dev@boardgamers.org for help.");
        e1.printStackTrace();
        return -1;
      }

      // parse schedule file
      BufferedReader reader = new BufferedReader(isr);
      try {
        final String preExtraStrings[] = {" AFC", " NFC", " FF", " PC",
            " Circus", " After Action", " Aftermath"};
        final String postExtraStrings[] = {" Demo"};

        final String daysForParsing[] = getResources()
            .getStringArray(R.array.daysForParsing);


        String line;
        while ((line = reader.readLine()) != null) {
          rowData = line.split("~");

          // currentDay
          tempString = rowData[0];
          for (index = 0; index < daysForParsing.length; index++) {
            if (daysForParsing[index].equalsIgnoreCase(tempString))
              break;
          }
          if (index == -1) {
            Log.d(TAG, "Unknown date: " + rowData[2] + " in " + line);
            index = 0;
          }
          day = index;

          // title
          eventTitle = rowData[2];

          // time
          boolean halfPast = false;
          tempString = rowData[1];
          if (rowData[1].contains(":30")) {
            Log.d(TAG, rowData[2] + " starts at half past");
            tempString = tempString.substring(0, tempString.length() - 3);
            halfPast = true;

          }
          hour = Integer.valueOf(tempString);

          if (rowData.length < 8) {
            prize = 0;
            eClass = "";
            format = "";
            duration = 0;
            continuous = false;
            gm = "";
            location = "";
          } else {
            // prize
            tempString = rowData[3];
            if (tempString.equalsIgnoreCase("") || tempString.equalsIgnoreCase("-"))
              tempString = "0";
            prize = Integer.valueOf(tempString);

            // class
            eClass = rowData[4];

            // format
            format = rowData[5];

            // duration
            if (rowData[6].equalsIgnoreCase("")
                || rowData[6].equalsIgnoreCase("-"))
              duration = 0;
            else
              duration = Double.valueOf(rowData[6]);

            if (duration > .33 && duration < .34)
              duration = .33;

            // continuous
            continuous = rowData[7].equalsIgnoreCase("Y");

            // gm
            gm = rowData[8];

            // location
            location = rowData[9];

          }

          // get tournament title and label and help short name
          tempString = eventTitle;

          // search through extra strings
          for (String eventExtraString : preExtraStrings) {
            index = tempString.indexOf(eventExtraString);
            if (index > -1) {
              tempString = tempString.substring(0, index)
                  + tempString.substring(index + eventExtraString.length());
            }
          }

          // split title in two, first part is tournament title,
          // second is short help title (H1/1)
          isTournamentEvent = eClass.length() > 0;

          if (isTournamentEvent || format.equalsIgnoreCase("Preview")) {
            index = tempString.lastIndexOf(" ");
            shortEventTitle = tempString.substring(index + 1);
            tempString = tempString.substring(0, index);

            if (index == -1) {
              Log.d(TAG, "");
            }
          }

          if (eventTitle.contains("Junior")
              || eventTitle.indexOf("COIN series") == 0
              || format.equalsIgnoreCase("SOG")
              || format.equalsIgnoreCase("Preview")) {
            tournamentTitle = tempString;
            tournamentLabel = "-";

          } else if (isTournamentEvent) {
            for (String eventExtraString : postExtraStrings) {
              index = tempString.indexOf(eventExtraString);
              if (index > -1) {
                tempString = tempString.substring(0, index);
              }
            }

            tournamentLabel = rowData[10];
            tournamentTitle = tempString;
          } else {
            tournamentTitle = tempString;
            tournamentLabel = "";

            if (eventTitle.indexOf("Auction") == 0)
              tournamentTitle = "AuctionStore";

            // search for non tournament main titles
            String[] nonTournamentStrings = {"Open Gaming",
                "Registration", "Vendors Area", "World at War",
                "Wits & Wagers", "Texas Roadhouse BPA Fundraiser",
                "Memoir: D-Day"};
            for (String nonTournamentString : nonTournamentStrings) {
              if (tempString.indexOf(nonTournamentString) == 0) {
                tournamentTitle = nonTournamentString;
                break;
              }
            }
          }

          // check if last 5 in list contains this tournament
          tournamentID = -1;
          for (index = Math.max(0, tournaments.size() - 5); index < tournaments
              .size(); index++) {
            if (tournaments.get(index).title
                .equalsIgnoreCase(tournamentTitle)) {
              tournamentID = index;
              break;
            }
          }

          if (tournamentID > -1) {
            // update existing tournament
            tournament = tournaments.get(tournamentID);
            if (prize > 0)
              tournament.prize = prize;
            if (isTournamentEvent)
              tournament.isTournament = true;

            tournament.gm = gm;
          } else {
            tournamentID = tournaments.size();
            tournament = new Tournament(tournamentID, tournamentTitle,
                tournamentLabel, isTournamentEvent, prize, gm);

            tournament.visible = sp.getBoolean("vis_" + tournamentTitle,
                true);
            tournament.finish = sp.getInt("fin_" + tournamentTitle, 0);

            tournaments.add(tournament);

            if (format.equalsIgnoreCase("Preview"))
              numPreviews++;
            else if (eventTitle.contains("Junior"))
              numJuniors++;
            else if (format.equalsIgnoreCase("Seminar"))
              numSeminars++;
            else if (isTournamentEvent)
              numTournaments++;

          }

          // Log.d(TAG, String.valueOf(tournamentID)+": "+tournamentTitle
          // +";;;E: "+eventTitle);

          totalDuration = duration;
          qualify = false;

          if (isTournamentEvent || format.equalsIgnoreCase("Junior")) {
            if (shortEventTitle.indexOf("SF") == 0) {
              qualify = true;
            } else if (shortEventTitle.indexOf("QF") == 0) {
              qualify = true;
            } else if (shortEventTitle.indexOf("F") == 0) {
              qualify = true;
            } else if (shortEventTitle.indexOf("QF/SF/F") == 0) {
              qualify = true;
              totalDuration *= 3;
            } else if (shortEventTitle.indexOf("SF/F") == 0) {
              qualify = true;
              totalDuration *= 2;
            } else if (continuous && shortEventTitle.indexOf("R") == 0
                && shortEventTitle.contains("/")) {
              int dividerIndex = shortEventTitle.indexOf("/");
              int startRound = Integer.valueOf(shortEventTitle
                  .substring(1, dividerIndex));
              int endRound = Integer.valueOf(shortEventTitle
                  .substring(dividerIndex + 1));

              int currentTime = hour;
              for (int round = 0; round < endRound - startRound; round++) {
                // if time passes midnight, next round
                // starts at
                // 9 the next currentDay
                if (currentTime > 24) {
                  if (currentTime >= 24 + 9)
                    Log.d(TAG, "Event " + eventTitle
                        + " goes past 9");
                  totalDuration += 9 - (currentTime - 24);
                  currentTime = 9;
                }

                totalDuration += duration;
                currentTime += duration;

              }

              if (prevEvent.tournamentID == tournamentID) {
                // update previous help total duration
                tempString = prevEvent.title;

                // search through extra strings
                for (String eventExtraString : preExtraStrings) {
                  index = tempString.indexOf(eventExtraString);
                  if (index > -1) {
                    tempString = tempString.substring(0, index)
                        + tempString.substring(index
                        + eventExtraString.length());
                  }
                }

                index = tempString.lastIndexOf(" ");

                if (index > -1) {
                  shortEventTitle = tempString.substring(index + 1);
                  if (shortEventTitle.indexOf("R") == 0) {
                    dividerIndex = shortEventTitle.indexOf("/");

                    if (dividerIndex == -1)
                      Log.d(TAG, "huh: " + shortEventTitle);
                    else {

                      int prevStartRound = Integer
                          .valueOf(shortEventTitle
                              .substring(1,
                                  dividerIndex));

                      int realNumRounds = startRound
                          - prevStartRound;

                      currentTime = hour;
                      prevEvent.totalDuration = 0;
                      for (int round = 0; round < realNumRounds; round++) {
                        // if time passes midnight, next
                        // round
                        // starts at
                        // 9 the next currentDay
                        if (currentTime > 24) {
                          if (currentTime >= 24 + 9)
                            Log.d(TAG, "Event "
                                + prevEvent.title
                                + " goes past 9");
                          prevEvent.totalDuration += 9 - (currentTime - 24);
                          currentTime = 9;
                        }

                        prevEvent.totalDuration += prevEvent.duration;
                        currentTime += prevEvent.duration;
                      }
                    }

                    List<Event> searchList = MainActivity.dayList
                        .get(prevEvent.day).get(
                            prevEvent.hour - 6);
                    for (Event searchEvent : searchList) {
                      if (searchEvent.identifier
                          .equalsIgnoreCase(prevEvent.identifier))
                        searchEvent.totalDuration = prevEvent.totalDuration;
                    }

                    searchList = MainActivity.dayList.get(
                        prevEvent.day).get(0);
                    for (Event searchEvent : searchList) {
                      if (searchEvent.identifier
                          .equalsIgnoreCase(prevEvent.identifier))
                        searchEvent.totalDuration = prevEvent.totalDuration;
                    }

                    Log.d(TAG,
                        "Event "
                            + prevEvent.title
                            + " duration changed to "
                            + String.valueOf(prevEvent.totalDuration));
                  }
                }
              }
            } else if (continuous) {
              Log.d(TAG, "Unknown continuous help: " + eventTitle);
            }
          } else if (continuous) {
            Log.d(TAG, "Non tournament help " + eventTitle + " is cont");
          }

          if (halfPast)
            eventTitle = eventTitle + " (" + rowData[1] + ")";

          identifier = String.valueOf(day * 24 + hour) + "_" + eventTitle;

          event = new Event(identifier, tournamentID, day, hour,
              eventTitle, eClass, format, qualify, duration,
              continuous, totalDuration, location);

          event.starred = sp.getBoolean(starPrefString + event.identifier,
              false);

          prevEvent = event;

          /********* LOAD INTO DAYLIST *************/
          change = "";

          // TODO add changes here
          /*
           * if (help.title.equalsIgnoreCase("Age of Renaissance H1/3 PC")) { int
           * newDay=5;
           *
           * if (scheduleVersion<0) { change=help.title+": Day changed from "
           * +dayStrings[help.currentDay]+" to " +dayStrings[newDay]; newVersion=0; }
           *
           * help.currentDay=newDay; }
           */

          if (!change.equalsIgnoreCase(""))
            MainActivity.allChanges += "\t" + change + "\n\n";

          // LOAD INTO LIST
          ArrayList<Event> searchList = MainActivity.dayList.get(
              event.day).get(event.hour - 6);
          if (eventTitle.contains("Junior")) {
            index = 0;
            for (; index < searchList.size(); index++) {
              tempEvent = searchList.get(index);
              if (!tempEvent.title.contains("Junior")
                  && tempEvent.tournamentID > 0)
                break;
            }
          } else if (!isTournamentEvent || format.equalsIgnoreCase("Demo")) {
            index = 0;
            for (; index < searchList.size(); index++) {
              tempEvent = searchList.get(index);
              if (tempEvent.eClass.length() > 0
                  && !tempEvent.title.contains("Junior")
                  && !tempEvent.format.equalsIgnoreCase("Demo"))
                break;
            }
          } else if (event.qualify) {
            index = searchList.size();
          } else {
            index = 0;
            for (; index < searchList.size(); index++) {
              tempEvent = searchList.get(index);
              if (tempEvent.qualify)
                break;
            }
          }

          MainActivity.dayList.get(event.day).get(event.hour - 6)
              .add(index, event);

          if (event.starred)
            MainActivity.addStarredEvent(event);

          lineNum++;

        }

        isr.close();
        is.close();
        reader.close();

        MainActivity.NUM_EVENTS = lineNum;

        // save update version
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(
            getResources().getString(R.string.sp_2014_schedule_version),
            newVersion);
        editor.apply();

      } catch (IOException e) {
        showToast(
            "ERROR: Could not parse schedule file,"
                + "contact dev@boardgamers.org for help.");
        e.printStackTrace();
        return -1;
      }

      MainActivity.allTournaments = tournaments;

      Log.d(TAG, "Finished load, " + String.valueOf(tournamentID)
          + " total tournaments and " + String.valueOf(lineNum)
          + " total events");
      Log.d(TAG, "Of total, " + String.valueOf(numTournaments)
          + " are tournaments, " + String.valueOf(numJuniors)
          + " are juniors, " + String.valueOf(numPreviews) + " are previews, "
          + String.valueOf(numSeminars) + " are seminars, ");

      startMainActivity();

      return 1;
    }
  }
}
