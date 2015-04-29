package org.boardgamers.wbc;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kevin on 4/27/2015.
 * Database used for storing all data
 */
public class WBCDataDbHelper extends SQLiteOpenHelper {
  // TODO
  // If you change the database schema, you must increment the database version.
  public static final int DATABASE_VERSION=1;

  public static final String DATABASE_NAME="WBCdata.db";

  public static abstract class EventEntry implements BaseColumns {
    public static final String TABLE_NAME="events";
    public static final String COLUMN_NAME_ENTRY_ID="id";
    public static final String COLUMN_NAME_ENTRY_TOURNAMENT_ID="tournament_id";
    public static final String COLUMN_NAME_TITLE="title";
    public static final String COLUMN_NAME_DAY="day";
    public static final String COLUMN_NAME_HOUR="hour";
    public static final String COLUMN_NAME_CLASS="eClass";
    public static final String COLUMN_NAME_FORMAT="eFormat";
    public static final String COLUMN_NAME_QUALIFY="qualify";
    public static final String COLUMN_NAME_DURATION="duration";
    public static final String COLUMN_NAME_CONTINUOUS="continuous";
    public static final String COLUMN_NAME_TOTAL_DURATION="total_duration";
    public static final String COLUMN_NAME_LOCATION="location";
    public static final String COLUMN_NAME_STARRED="starred";
    public static final String COLUMN_NAME_NOTE="note";
    public static final String COLUMN_NAME_NULLABLE="null";
  }

  public static abstract class TournamentEntry implements BaseColumns {
    public static final String TABLE_NAME="tournaments";
    public static final String COLUMN_NAME_TITLE="title";
    public static final String COLUMN_NAME_LABEL="label";
    public static final String COLUMN_NAME_IS_TOURNAMENT="is_tournament";
    public static final String COLUMN_NAME_PRIZE="prize";
    public static final String COLUMN_NAME_GM="gm";

    public static final String COLUMN_NAME_FINISH="finish";
    public static final String COLUMN_NAME_VISIBLE="visible";

    public static final String COLUMN_NAME_NULLABLE="null";
  }

  private static final String SQL_CREATE_EVENT_ENTRIES="CREATE TABLE "+EventEntry.TABLE_NAME+" ("+
      EventEntry._ID+" INTEGER PRIMARY KEY,"+
      EventEntry.COLUMN_NAME_ENTRY_ID+" TEXT,"+
      EventEntry.COLUMN_NAME_ENTRY_TOURNAMENT_ID+" INTEGER,"+
      EventEntry.COLUMN_NAME_TITLE+" TEXT,"+
      EventEntry.COLUMN_NAME_DAY+" INTEGER,"+
      EventEntry.COLUMN_NAME_HOUR+" INTEGER,"+
      EventEntry.COLUMN_NAME_CLASS+" TEXT,"+
      EventEntry.COLUMN_NAME_FORMAT+" TEXT,"+
      EventEntry.COLUMN_NAME_QUALIFY+" INTEGER,"+
      EventEntry.COLUMN_NAME_DURATION+" INTEGER,"+
      EventEntry.COLUMN_NAME_CONTINUOUS+" INTEGER,"+
      EventEntry.COLUMN_NAME_TOTAL_DURATION+" INTEGER,"+
      EventEntry.COLUMN_NAME_LOCATION+" TEXT,"+
      EventEntry.COLUMN_NAME_STARRED+" INTEGER,"+
      EventEntry.COLUMN_NAME_NOTE+" TEXT,"+" )";

  private static final String SQL_CREATE_TOURNAMENT_ENTRIES=
      "CREATE TABLE "+TournamentEntry.TABLE_NAME+" ("+
          TournamentEntry._ID+" INTEGER PRIMARY KEY,"+
          TournamentEntry.COLUMN_NAME_TITLE+" TEXT,"+
          TournamentEntry.COLUMN_NAME_LABEL+" TEXT,"+
          TournamentEntry.COLUMN_NAME_IS_TOURNAMENT+" INTEGER,"+
          TournamentEntry.COLUMN_NAME_PRIZE+" INTEGER,"+
          TournamentEntry.COLUMN_NAME_GM+" TEXT,"+
          TournamentEntry.COLUMN_NAME_FINISH+" INTEGER,"+
          TournamentEntry.COLUMN_NAME_VISIBLE+" INTEGER,"+" )";

  private static final String SQL_DELETE_EVENT_ENTRIES=
      "DROP TABLE IF EXISTS "+EventEntry.TABLE_NAME;

  private static final String SQL_DELETE_TOURNAMENT_ENTRIES=
      "DROP TABLE IF EXISTS "+TournamentEntry.TABLE_NAME;

  private WBCDataDbHelper mDbHelper;
  private final Context context;

  public WBCDataDbHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);

    this.context=context;
    mDbHelper=new WBCDataDbHelper(context);
  }

  public void onCreate(SQLiteDatabase db) {
    db.execSQL(SQL_CREATE_EVENT_ENTRIES);
    db.execSQL(SQL_CREATE_TOURNAMENT_ENTRIES);
  }

  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // This database is only a cache for online data, so its upgrade policy is
    // to simply to discard the data and start over
    db.execSQL(SQL_DELETE_EVENT_ENTRIES);
    db.execSQL(SQL_DELETE_TOURNAMENT_ENTRIES);

    onCreate(db);
  }

  public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    onUpgrade(db, oldVersion, newVersion);
  }

  public long insertEvent(String id, long tournamentID, int day, int hour, String title,
                          String eClass, String eFormat, boolean qualify, double duration,
                          boolean continuous, double totalDuration, String location,
                          boolean starred) {
    SQLiteDatabase db=mDbHelper.getWritableDatabase();

    ContentValues values=new ContentValues();
    values.put(EventEntry.COLUMN_NAME_ENTRY_ID, id);
    values.put(EventEntry.COLUMN_NAME_ENTRY_TOURNAMENT_ID, tournamentID);
    values.put(EventEntry.COLUMN_NAME_TITLE, title);
    values.put(EventEntry.COLUMN_NAME_DAY, day);
    values.put(EventEntry.COLUMN_NAME_HOUR, hour);
    values.put(EventEntry.COLUMN_NAME_CLASS, eClass);
    values.put(EventEntry.COLUMN_NAME_FORMAT, eFormat);
    values.put(EventEntry.COLUMN_NAME_QUALIFY, qualify ? 1 : 0);
    values.put(EventEntry.COLUMN_NAME_DURATION, duration);
    values.put(EventEntry.COLUMN_NAME_CONTINUOUS, continuous ? 1 : 0);
    values.put(EventEntry.COLUMN_NAME_TOTAL_DURATION, totalDuration);
    values.put(EventEntry.COLUMN_NAME_LOCATION, location);

    values.put(EventEntry.COLUMN_NAME_NOTE, "");
    values.put(EventEntry.COLUMN_NAME_STARRED, starred ? 1 : 0);

    // Insert the new row, returning the primary key value of the new row
    return db.insert(EventEntry.TABLE_NAME, EventEntry.COLUMN_NAME_NULLABLE, values);

  }

  public long insertTournament(String title, String label, boolean isTournament, int prize,
                               String gm) {
    SQLiteDatabase db=mDbHelper.getWritableDatabase();

    ContentValues values=new ContentValues();
    values.put(TournamentEntry.COLUMN_NAME_TITLE, title);
    values.put(TournamentEntry.COLUMN_NAME_LABEL, label);
    values.put(TournamentEntry.COLUMN_NAME_IS_TOURNAMENT, isTournament ? 1 : 0);
    values.put(TournamentEntry.COLUMN_NAME_PRIZE, prize);
    values.put(TournamentEntry.COLUMN_NAME_GM, gm);

    values.put(TournamentEntry.COLUMN_NAME_FINISH, 0);
    values.put(TournamentEntry.COLUMN_NAME_VISIBLE, 1);

    // Insert the new row, returning the primary key value of the new row
    return db.insert(TournamentEntry.TABLE_NAME, TournamentEntry.COLUMN_NAME_NULLABLE, values);
  }

  /**
   * Search event table for any event with a note
   *
   * @return A list of notes sorted by event title
   */
  public List<String> getAllNotes() {
    SQLiteDatabase db=mDbHelper.getReadableDatabase();

    String[] projection={EventEntry._ID, EventEntry.COLUMN_NAME_TITLE, EventEntry.COLUMN_NAME_NOTE};
    String selection=EventEntry.COLUMN_NAME_NOTE+" IS NOT NULL";
    String sortOrder=EventEntry.COLUMN_NAME_TITLE+" ASC";

    Cursor cursor=
        db.query(EventEntry.TABLE_NAME, projection, selection, null, null, null, sortOrder);

    String title, note;
    List<String> notes=new ArrayList<>();
    cursor.moveToFirst();
    do {
      title=cursor.getString(cursor.getColumnIndexOrThrow(EventEntry.COLUMN_NAME_TITLE));
      note=cursor.getString(cursor.getColumnIndexOrThrow(EventEntry.COLUMN_NAME_NOTE));

      notes.add(title+": "+note);
    } while (cursor.moveToNext());

    cursor.close();
    return notes;
  }

  /**
   * Search tournament table for any event with a finish
   *
   * @return A list of all finishes sorted by tournament title
   */
  public List<String> getAllFinishes() {
    SQLiteDatabase db=mDbHelper.getReadableDatabase();

    String[] projection={TournamentEntry._ID, TournamentEntry.COLUMN_NAME_TITLE,
        TournamentEntry.COLUMN_NAME_FINISH};
    String selection=TournamentEntry.COLUMN_NAME_FINISH+" IS NOT NULL";
    String sortOrder=TournamentEntry.COLUMN_NAME_TITLE+" ASC";

    Cursor cursor=
        db.query(TournamentEntry.TABLE_NAME, projection, selection, null, null, null, sortOrder);

    String title;
    int finish;
    List<String> finishes=new ArrayList<>();
    cursor.moveToFirst();
    do {
      title=cursor.getString(cursor.getColumnIndexOrThrow(TournamentEntry.COLUMN_NAME_TITLE));
      finish=cursor.getInt(cursor.getColumnIndexOrThrow(TournamentEntry.COLUMN_NAME_FINISH));

      finishes.add(title+": "+String.valueOf(finish));
    } while (cursor.moveToNext());

    cursor.close();
    return finishes;
  }

  /**
   * Search tournament table for any event with a finish
   *
   * @return A list of all finishes sorted by tournament title
   */
  public List<Boolean> getAllVisible() {
    SQLiteDatabase db=mDbHelper.getReadableDatabase();

    String[] projection={TournamentEntry._ID, TournamentEntry.COLUMN_NAME_VISIBLE};
    String selection=TournamentEntry.COLUMN_NAME_VISIBLE+" IS 1";
    String sortOrder=TournamentEntry.COLUMN_NAME_TITLE+" ASC";

    Cursor cursor=
        db.query(TournamentEntry.TABLE_NAME, projection, selection, null, null, null, sortOrder);

    boolean visible;
    List<Boolean> visibleTournaments=new ArrayList<>();
    cursor.moveToFirst();
    do {
      visible=cursor.getInt(cursor.getColumnIndexOrThrow(TournamentEntry.COLUMN_NAME_VISIBLE))==1;

      visibleTournaments.add(visible);
    } while (cursor.moveToNext());

    cursor.close();
    return visibleTournaments;
  }

  public List<Event> getStarredEvents() {
    return getEvents(EventEntry.COLUMN_NAME_STARRED+" IS 1");
  }

  public List<Event> getUserEvents() {
    return getEvents(EventEntry.COLUMN_NAME_ENTRY_TOURNAMENT_ID+" IS -1");
  }

  public List<Event> getEvents(String selection) {
    SQLiteDatabase db=mDbHelper.getReadableDatabase();

    // How you want the results sorted in the resulting Cursor
    String sortOrder=EventEntry.COLUMN_NAME_DAY+" ASC "+EventEntry.COLUMN_NAME_HOUR+" ASC "+
        EventEntry.COLUMN_NAME_QUALIFY+"ASC "+EventEntry.COLUMN_NAME_TITLE+" ASC";

    Cursor cursor=db.query(EventEntry.TABLE_NAME, null, selection, null, null, null, sortOrder);

    String title, identifier, eClass, format, location, note;
    int id, tournamentId, day, hour, duration, totalDuration;
    boolean qualify, continuous, starred;

    List<Event> events=new ArrayList<>();
    Event event;
    cursor.moveToFirst();
    do {
      id=cursor.getInt(cursor.getColumnIndexOrThrow(EventEntry._ID));
      identifier=cursor.getString(cursor.getColumnIndexOrThrow(EventEntry.COLUMN_NAME_TITLE));
      tournamentId=
          cursor.getInt(cursor.getColumnIndexOrThrow(EventEntry.COLUMN_NAME_ENTRY_TOURNAMENT_ID));
      day=cursor.getInt(cursor.getColumnIndexOrThrow(EventEntry.COLUMN_NAME_DAY));
      hour=cursor.getInt(cursor.getColumnIndexOrThrow(EventEntry.COLUMN_NAME_HOUR));
      title=cursor.getString(cursor.getColumnIndexOrThrow(EventEntry.COLUMN_NAME_TITLE));
      eClass=cursor.getString(cursor.getColumnIndexOrThrow(EventEntry.COLUMN_NAME_CLASS));
      format=cursor.getString(cursor.getColumnIndexOrThrow(EventEntry.COLUMN_NAME_FORMAT));
      qualify=cursor.getInt(cursor.getColumnIndexOrThrow(EventEntry.COLUMN_NAME_QUALIFY))==1;
      duration=cursor.getInt(cursor.getColumnIndexOrThrow(EventEntry.COLUMN_NAME_DURATION));
      continuous=cursor.getInt(cursor.getColumnIndexOrThrow(EventEntry.COLUMN_NAME_TITLE))==1;
      totalDuration=
          cursor.getInt(cursor.getColumnIndexOrThrow(EventEntry.COLUMN_NAME_TOTAL_DURATION));
      location=cursor.getString(cursor.getColumnIndexOrThrow(EventEntry.COLUMN_NAME_LOCATION));

      starred=cursor.getInt(cursor.getColumnIndexOrThrow(EventEntry.COLUMN_NAME_STARRED))==1;
      note=cursor.getString(cursor.getColumnIndexOrThrow(EventEntry.COLUMN_NAME_NOTE));

      event=new Event(id, identifier, tournamentId, day, hour, title, eClass, format, qualify,
          duration, continuous, totalDuration, location);
      event.starred=starred;
      event.note=note;

      events.add(event);
    } while (cursor.moveToNext());

    cursor.close();
    return events;

  }

  public int updateTournament(long rowId, boolean isTournament, int prize) {
    SQLiteDatabase db=mDbHelper.getReadableDatabase();

    ContentValues values=new ContentValues();
    values.put(TournamentEntry.COLUMN_NAME_IS_TOURNAMENT, isTournament ? 1 : 0);
    values.put(TournamentEntry.COLUMN_NAME_PRIZE, prize);

    String selection=TournamentEntry._ID+" LIKE ?";
    String[] selectionArgs={String.valueOf(rowId)};

    return db.update(TournamentEntry.TABLE_NAME, values, selection, selectionArgs);
  }

  public int updateEventStarred(long rowId, boolean starred) {
    SQLiteDatabase db=mDbHelper.getReadableDatabase();

    ContentValues values=new ContentValues();
    values.put(EventEntry.COLUMN_NAME_STARRED, starred ? 1 : 0);

    String selection=EventEntry._ID+" LIKE ?";
    String[] selectionArgs={String.valueOf(rowId)};

    return db.update(EventEntry.TABLE_NAME, values, selection, selectionArgs);
  }

  public int updateEventNote(long rowId, String note) {
    SQLiteDatabase db=mDbHelper.getReadableDatabase();

    ContentValues values=new ContentValues();
    values.put(EventEntry.COLUMN_NAME_NOTE, note);

    String selection=EventEntry._ID+" LIKE ?";
    String[] selectionArgs={String.valueOf(rowId)};

    return db.update(EventEntry.TABLE_NAME, values, selection, selectionArgs);
  }

  public int startInitialLoad() {
    return new LoadEventsTask().doInBackground(null, null, null);
  }

  /**
   * Load Events Task to load schedule
   */
  public class LoadEventsTask extends AsyncTask<Integer, Integer, Integer> {
    private final static String TAG="Load Events Task";
    private String allChanges;

    @Override protected void onPreExecute() {

      super.onPreExecute();
    }

    @Override
    protected Integer doInBackground(Integer... params) {
      Log.d(TAG, "Starting load");

      // GET PREFERENCES AND THEIR STRINGS
      allChanges="";

      /***** LOAD USER EVENTS *******/

      String identifier, row, eventTitle, eClass, format, gm, tempString, location;
      String[] rowData;
      int index, day, hour, prize, lineNum=0;
      long tournamentID=-1;

      int numTournaments=0, numPreviews=0, numJuniors=0, numSeminars=0;
      double duration, totalDuration;
      boolean continuous, qualify, isTournamentEvent;

      String tournamentTitle, tournamentLabel, shortEventTitle="";
      String change;
      // get events (may need update)

      /***** PARSE SCHEDULE *****/

      Log.d(TAG, "Starting parsing");

      List<String> tournamentTitles=new ArrayList<>();
      // find schedule file
      InputStream is;
      try {
        is=context.getAssets().open("schedule2014.txt");
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

      // parse schedule file
      BufferedReader reader=new BufferedReader(isr);
      try {
        final String preExtraStrings[]=
            {" AFC", " NFC", " FF", " PC", " Circus", " After Action", " Aftermath"};
        final String postExtraStrings[]={" Demo"};

        final String daysForParsing[]=context.getResources().getStringArray(R.array.daysForParsing);

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
              updateTournament(tournamentID, isTournamentEvent, prize);
            }
          } else {
            tournamentID=
                insertTournament(tournamentTitle, tournamentLabel, isTournamentEvent, prize, gm);

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
                // if time passes midnight, next round
                // starts at
                // 9 the next currentDay
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
              Log.d(TAG, "Unknown continuous help: "+eventTitle);
            }
          } else if (continuous) {
            Log.d(TAG, "Non tournament help "+eventTitle+" is cont");
          }

          if (halfPast) {
            eventTitle=eventTitle+" ("+rowData[1]+")";
          }

          identifier=String.valueOf(day*24+hour)+eventTitle;

          insertEvent(identifier, tournamentID, day, hour, eventTitle, eClass, format, qualify,
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
