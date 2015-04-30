package org.boardgamers.wbc;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SplashScreen extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    setProgressBarIndeterminateVisibility(true);

    setContentView(R.layout.splash);

    //WBCDataDbHelper dbHelper=new WBCDataDbHelper(this);
    //int finish=dbHelper.startInitialLoad();
    new LoadEventsTask(this).execute(0, 0, 0);
  }

  public void taskFinished(int result) {
    if (result>0) {
      finish();
    } else if (result==-1) {
      showToast("ERROR: Could not parse schedule file,"+"contact dev@boardgamers.org for help.");
    } else if (result==-2) {
      showToast("ERROR: Could not find schedule file,"+"contact dev@boardgamers.org for help.");
    } else if (result==-3) {
      showToast("ERROR: Could not open schedule file,"+"contact dev@boardgamers.org for help.");
    }

    finish();
  }

  public void showToast(String string) {
    Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
  }

  /**
   * Load Events Task to load schedule
   */
  public class LoadEventsTask extends AsyncTask<Integer, Integer, Integer> {
    private final static String TAG="Load Events Task";

    private Context context;

    public LoadEventsTask(Context c) {
      context=c;
    }

    @Override protected void onPostExecute(Integer integer) {
      taskFinished(integer);
      super.onPostExecute(integer);
    }

    @Override
    protected Integer doInBackground(Integer... params) {
      Log.d(TAG, "Starting parsing");

      String identifier, eventTitle, eClass, format, gm, tempString, location;
      String[] rowData;
      int index, day, hour, prize, lineNum=0;
      double duration, totalDuration;
      boolean continuous, qualify, isTournamentEvent;

      long tournamentID=-1;
      int numTournaments=0, numPreviews=0, numJuniors=0, numSeminars=0;
      String tournamentTitle, tournamentLabel, shortEventTitle="";
      List<String> tournamentTitles=new ArrayList<>();

      // find schedule file
      InputStream is;
      try {
        is=getAssets().open("schedule2014.txt");
      } catch (IOException e2) {
        e2.printStackTrace();
        return -2;
      }
      // read schedule file
      InputStreamReader isr;
      try {
        isr=new InputStreamReader(is);
      } catch (IllegalStateException e1) {
        e1.printStackTrace();
        return -3;
      }

      WBCDataDbHelper dbHelper=new WBCDataDbHelper(context);
      // parse schedule file
      BufferedReader reader=new BufferedReader(isr);
      try {
        final String preExtraStrings[]=
            {" AFC", " NFC", " FF", " PC", " Circus", " After Action", " Aftermath"};
        final String postExtraStrings[]={" Demo"};

        final String daysForParsing[]=getResources().getStringArray(R.array.daysForParsing);

        String line;
        while ((line=reader.readLine())!=null) {
          rowData=line.split("~");

          // currentDay
          tempString=rowData[0];
          for (index=0; index<daysForParsing.length; index++) {
            if (daysForParsing[index].equalsIgnoreCase(tempString)) {
              break;
            }
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
          if (rowData[1].contains(":30")) {
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
            tempString=rowData[3];
            if (tempString.equalsIgnoreCase("") || tempString.equalsIgnoreCase("-")) {
              tempString="0";
            }
            prize=Integer.valueOf(tempString);

            // class
            eClass=rowData[4];

            // format
            format=rowData[5];

            // duration
            if (rowData[6].equalsIgnoreCase("") || rowData[6].equalsIgnoreCase("-")) {
              duration=0;
            } else {
              duration=Double.valueOf(rowData[6]);
            }

            if (duration>.33 && duration<.34) {
              duration=.33;
            }

            // continuous
            continuous=rowData[7].equalsIgnoreCase("Y");

            // gm
            gm=rowData[8];

            // location
            location=rowData[9];

          }

          // get tournament title and label and help short name
          tempString=eventTitle;

          // search through extra strings
          for (String eventExtraString : preExtraStrings) {
            index=tempString.indexOf(eventExtraString);
            if (index>-1) {
              tempString=tempString.substring(0, index)+
                  tempString.substring(index+eventExtraString.length());
            }
          }

          // split title in two, first part is tournament title,
          // second is short help title (H1/1)
          isTournamentEvent=eClass.length()>0;

          if (isTournamentEvent || format.equalsIgnoreCase("Preview")) {
            index=tempString.lastIndexOf(" ");
            shortEventTitle=tempString.substring(index+1);
            tempString=tempString.substring(0, index);

            if (index==-1) {
              Log.d(TAG, "");
            }
          }

          if (eventTitle.contains("Junior") || eventTitle.indexOf("COIN series")==0 ||
              format.equalsIgnoreCase("SOG") || format.equalsIgnoreCase("Preview")) {
            tournamentTitle=tempString;
            tournamentLabel="";

          } else if (isTournamentEvent) {
            for (String eventExtraString : postExtraStrings) {
              index=tempString.indexOf(eventExtraString);
              if (index>-1) {
                tempString=tempString.substring(0, index);
              }
            }

            tournamentLabel=rowData[10];
            tournamentTitle=tempString;
          } else {
            tournamentTitle=tempString;
            tournamentLabel="";

            if (eventTitle.indexOf("Auction")==0) {
              tournamentTitle="AuctionStore";
            }

            // search for non tournament main titles
            String[] nonTournamentStrings=
                {"Open Gaming", "Registration", "Vendors Area", "World at War", "Wits & Wagers",
                    "Texas Roadhouse BPA Fundraiser", "Memoir: D-Day"};
            for (String nonTournamentString : nonTournamentStrings) {
              if (tempString.indexOf(nonTournamentString)==0) {
                tournamentTitle=nonTournamentString;
                break;
              }
            }
          }

          // check if last 5 in list contains this tournament
          tournamentID=-1;
          for (index=Math.max(0, tournamentTitles.size()-5); index<tournamentTitles.size();
               index++) {
            if (tournamentTitles.get(index).equalsIgnoreCase(tournamentTitle)) {
              tournamentID=index;
              break;
            }
          }

          if (tournamentID>-1) {
            if (prize>0 || isTournamentEvent) {
              dbHelper.updateTournament(tournamentID, isTournamentEvent, prize);
            }
          } else {
            tournamentID=dbHelper
                .insertTournament(tournamentTitle, tournamentLabel, isTournamentEvent, prize, gm);

            tournamentTitles.add(tournamentTitle);

            if (format.equalsIgnoreCase("Preview")) {
              numPreviews++;
            } else if (eventTitle.contains("Junior")) {
              numJuniors++;
            } else if (format.equalsIgnoreCase("Seminar")) {
              numSeminars++;
            } else if (isTournamentEvent) {
              numTournaments++;
            }

          }

          // Log.d(TAG, String.valueOf(tournamentID)+": "+tournamentTitle
          // +";;;E: "+eventTitle);

          totalDuration=duration;
          qualify=false;

          if (isTournamentEvent || format.equalsIgnoreCase("Junior")) {
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
            } else if (continuous && shortEventTitle.indexOf("R")==0 &&
                shortEventTitle.contains("/")) {
              int dividerIndex=shortEventTitle.indexOf("/");
              int startRound=Integer.valueOf(shortEventTitle.substring(1, dividerIndex));
              int endRound=Integer.valueOf(shortEventTitle.substring(dividerIndex+1));

              int currentTime=hour;
              for (int round=0; round<endRound-startRound; round++) {
                // if time passes midnight, next round starts at 9 the next currentDay
                if (currentTime>24) {
                  if (currentTime>=24+9) {
                    Log.d(TAG, "Event "+eventTitle+" goes past 9");
                  }
                  totalDuration+=9-(currentTime-24);
                  currentTime=9;
                }

                totalDuration+=duration;
                currentTime+=duration;

              }

              //              if (prevEvent.tournamentID==tournamentID) {
              //                // update previous help total duration
              //                tempString=prevEvent.title;
              //
              //                // search through extra strings
              //                for (String eventExtraString : preExtraStrings) {
              //                  index=tempString.indexOf(eventExtraString);
              //                  if (index>-1) {
              //                    tempString=tempString.substring(0, index)+
              //                        tempString.substring(index+eventExtraString.length());
              //                  }
              //                }
              //
              //                index=tempString.lastIndexOf(" ");
              //
              //                if (index>-1) {
              //                  shortEventTitle=tempString.substring(index+1);
              //                  if (shortEventTitle.indexOf("R")==0) {
              //                    dividerIndex=shortEventTitle.indexOf("/");
              //
              //                    if (dividerIndex==-1) {
              //                      Log.d(TAG, "huh: "+shortEventTitle);
              //                    } else {
              //
              //                      int prevStartRound=
              //                          Integer.valueOf(shortEventTitle.substring(1, dividerIndex));
              //
              //                      int realNumRounds=startRound-prevStartRound;
              //
              //                      currentTime=hour;
              //                      prevEvent.totalDuration=0;
              //                      for (int round=0; round<realNumRounds; round++) {
              //                        // if time passes midnight, next
              //                        // round
              //                        // starts at
              //                        // 9 the next currentDay
              //                        if (currentTime>24) {
              //                          if (currentTime>=24+9) {
              //                            Log.d(TAG, "Event "+prevEvent.title+" goes past 9");
              //                          }
              //                          prevEvent.totalDuration+=9-(currentTime-24);
              //                          currentTime=9;
              //                        }
              //
              //                        prevEvent.totalDuration+=prevEvent.duration;
              //                        currentTime+=prevEvent.duration;
              //                      }
              //                    }
              //
              //                    Log.d(TAG, "Event "+prevEvent.title+" duration changed to "+
              //                        String.valueOf(prevEvent.totalDuration));
              //                  }
              //                }
              //              }

            } else if (continuous) {
              Log.d(TAG, "Unknown continuous event: "+eventTitle);
            }
          } else if (continuous) {
            Log.d(TAG, "Non tournament event "+eventTitle+" is cont");
          }

          if (halfPast) {
            eventTitle=eventTitle+" ("+rowData[1]+")";
          }

          identifier=String.valueOf(day*24+hour)+eventTitle;

          dbHelper
              .insertEvent(identifier, tournamentID, day, hour, eventTitle, eClass, format, qualify,
                  duration, continuous, totalDuration, location, false);

          //prevEvent=event;

          lineNum++;

        }

        // close streams and number of events
        isr.close();
        is.close();
        reader.close();

        // log statistics
        Log.d(TAG, "Finished load, "+String.valueOf(tournamentID)+" total tournaments and "+
            String.valueOf(lineNum)+" total events");
        Log.d(TAG, "Of total, "+String.valueOf(numTournaments)+" are tournaments, "+
            String.valueOf(numJuniors)+" are juniors, "+String.valueOf(numPreviews)+
            " are previews, "+String.valueOf(numSeminars)+" are seminars, ");

        return lineNum;
      } catch (IOException e) {
        e.printStackTrace();
        return -1;
      }
    }
  }
}
