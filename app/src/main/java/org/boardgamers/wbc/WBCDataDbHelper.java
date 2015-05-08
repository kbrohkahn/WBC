package org.boardgamers.wbc;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

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
    public static final String COLUMN_NAME_FINAL_EVENT="final_event";
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
      EventEntry.COLUMN_NAME_NOTE+" TEXT"+" )";

  private static final String SQL_CREATE_TOURNAMENT_ENTRIES=
      "CREATE TABLE "+TournamentEntry.TABLE_NAME+" ("+
          TournamentEntry._ID+" INTEGER PRIMARY KEY,"+
          TournamentEntry.COLUMN_NAME_TITLE+" TEXT,"+
          TournamentEntry.COLUMN_NAME_LABEL+" TEXT,"+
          TournamentEntry.COLUMN_NAME_IS_TOURNAMENT+" INTEGER,"+
          TournamentEntry.COLUMN_NAME_PRIZE+" INTEGER,"+
          TournamentEntry.COLUMN_NAME_GM+" TEXT,"+
          TournamentEntry.COLUMN_NAME_FINAL_EVENT+" INTEGER,"+
          TournamentEntry.COLUMN_NAME_FINISH+" INTEGER,"+
          TournamentEntry.COLUMN_NAME_VISIBLE+" INTEGER"+" )";

  private static final String SQL_DELETE_EVENT_ENTRIES=
      "DROP TABLE IF EXISTS "+EventEntry.TABLE_NAME;

  private static final String SQL_DELETE_TOURNAMENT_ENTRIES=
      "DROP TABLE IF EXISTS "+TournamentEntry.TABLE_NAME;

  public SQLiteDatabase sqLiteDatabase;

  public WBCDataDbHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  public void onCreate(SQLiteDatabase db) {
    db.execSQL(SQL_CREATE_EVENT_ENTRIES);
    db.execSQL(SQL_CREATE_TOURNAMENT_ENTRIES);
  }

  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL(SQL_DELETE_EVENT_ENTRIES);
    db.execSQL(SQL_DELETE_TOURNAMENT_ENTRIES);

    onCreate(db);
  }

  @Override
  public SQLiteDatabase getWritableDatabase() {
    sqLiteDatabase=super.getWritableDatabase();
    return sqLiteDatabase;
  }

  @Override
  public SQLiteDatabase getReadableDatabase() {
    sqLiteDatabase=super.getReadableDatabase();
    return sqLiteDatabase;
  }

  @Override
  public synchronized void close() {
    sqLiteDatabase.close();
    super.close();
  }

  public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    onUpgrade(db, oldVersion, newVersion);
  }

  public long insertEvent(String identifier, long tournamentID, int day, int hour, String title,
                          String eClass, String eFormat, boolean qualify, double duration,
                          boolean continuous, double totalDuration, String location,
                          boolean starred) {
    ContentValues values=new ContentValues();
    values.put(EventEntry.COLUMN_NAME_ENTRY_ID, identifier);
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
    return sqLiteDatabase.insert(EventEntry.TABLE_NAME, EventEntry.COLUMN_NAME_NULLABLE, values);
  }

  public long insertTournament(String title, String label, boolean isTournament, int prize,
                               String gm, int eventId) {
    ContentValues values=new ContentValues();
    values.put(TournamentEntry.COLUMN_NAME_TITLE, title);
    values.put(TournamentEntry.COLUMN_NAME_LABEL, label);
    values.put(TournamentEntry.COLUMN_NAME_IS_TOURNAMENT, isTournament ? 1 : 0);
    values.put(TournamentEntry.COLUMN_NAME_PRIZE, prize);
    values.put(TournamentEntry.COLUMN_NAME_GM, gm);
    values.put(TournamentEntry.COLUMN_NAME_FINAL_EVENT, eventId);

    values.put(TournamentEntry.COLUMN_NAME_FINISH, 0);
    values.put(TournamentEntry.COLUMN_NAME_VISIBLE, 1);

    // Insert the new row, returning the primary key value of the new row
    return sqLiteDatabase
        .insert(TournamentEntry.TABLE_NAME, TournamentEntry.COLUMN_NAME_NULLABLE, values);
  }

  public long deleteTournamentEvents(long id) {
    String where=EventEntry.COLUMN_NAME_ENTRY_TOURNAMENT_ID+"="+String.valueOf(id);
    return sqLiteDatabase.delete(EventEntry.TABLE_NAME, where, null);
  }

  public long deleteEvent(long id) {
    String where=EventEntry._ID+"="+String.valueOf(id);
    return sqLiteDatabase.delete(EventEntry.TABLE_NAME, where, null);
  }

  public int getNumEvents() {
    Cursor cursor=sqLiteDatabase.rawQuery("SELECT COUNT (*) FROM "+EventEntry.TABLE_NAME, null);
    cursor.moveToFirst();
    int count=cursor.getInt(0);
    cursor.close();

    return count;
  }

  /**
   * Search tournament table for any event with a finish
   *
   * @return A list of all finishes sorted by tournament title
   */
  public List<Boolean> getAllVisible() {
    String[] projection={TournamentEntry._ID, TournamentEntry.COLUMN_NAME_VISIBLE};
    String selection=TournamentEntry.COLUMN_NAME_VISIBLE+"=1";
    String sortOrder=TournamentEntry.COLUMN_NAME_TITLE+" ASC";

    Cursor cursor=sqLiteDatabase
        .query(TournamentEntry.TABLE_NAME, projection, selection, null, null, null, sortOrder);

    boolean visible;
    List<Boolean> visibleTournaments=new ArrayList<>();
    if (cursor.moveToFirst()) {
      do {
        visible=cursor.getInt(cursor.getColumnIndexOrThrow(TournamentEntry.COLUMN_NAME_VISIBLE))==1;

        visibleTournaments.add(visible);
      } while (cursor.moveToNext());
    }

    cursor.close();
    return visibleTournaments;
  }

  public List<Event> getAllEvents() {
    return getEvents(null);
  }

  public List<Event> getStarredEvents() {
    return getEvents(EventEntry.COLUMN_NAME_STARRED+"=1");
  }

  public List<Event> getTournamentEvents(long id) {
    return getEvents(EventEntry.COLUMN_NAME_ENTRY_TOURNAMENT_ID+"="+String.valueOf(id));
  }

  /**
   * Runs getEvents, selecting events where note is not empty string
   *
   * @return A list of notes sorted by event title
   */
  public List<Event> getEventsWithNotes() {
    return getEvents(EventEntry.COLUMN_NAME_NOTE+"!=''");
  }

  /**
   * Runs getEvents, selecting events where id = id
   *
   * @param id - the id to search for
   * @return - a single event with id of id, or null if no event exists
   */
  public Event getEvent(long id) {
    String where=EventEntry._ID+"="+String.valueOf(id);
    List<Event> events=getEvents(where);

    if (events.size()==0) {
      return null;
    } else {
      return events.get(0);
    }
  }

  /**
   * Used in search, runs getEvents on query user searched for. Searches event title and event format
   *
   * @param query - string user searched for
   * @return - a list of events containing query, or null if no event exists
   */
  public List<Event> getEventsFromQuery(String query) {
    String where=EventEntry.COLUMN_NAME_TITLE+" LIKE '%"+query+"%' OR "+
        EventEntry.COLUMN_NAME_FORMAT+" LIKE '%"+query+"%'";
    return getEvents(where);
  }

  public List<Event> getEvents(String selection) {
    String sortOrder=EventEntry.COLUMN_NAME_DAY+" ASC, "+EventEntry.COLUMN_NAME_HOUR+" ASC, "+
        EventEntry.COLUMN_NAME_QUALIFY+" ASC, "+EventEntry.COLUMN_NAME_TITLE+" ASC";

    Cursor cursor=
        sqLiteDatabase.query(EventEntry.TABLE_NAME, null, selection, null, null, null, sortOrder);

    String title, identifier, eClass, format, location, note;
    int id, tournamentId, day, hour, duration, totalDuration;
    boolean qualify, continuous, starred;

    List<Event> events=new ArrayList<>();
    Event event;
    cursor.moveToFirst();
    cursor.moveToPrevious();

    while (cursor.moveToNext()) {
      id=cursor.getInt(cursor.getColumnIndexOrThrow(EventEntry._ID));
      identifier=cursor.getString(cursor.getColumnIndexOrThrow(EventEntry.COLUMN_NAME_ENTRY_ID));
      tournamentId=
          cursor.getInt(cursor.getColumnIndexOrThrow(EventEntry.COLUMN_NAME_ENTRY_TOURNAMENT_ID));
      day=cursor.getInt(cursor.getColumnIndexOrThrow(EventEntry.COLUMN_NAME_DAY));
      hour=cursor.getInt(cursor.getColumnIndexOrThrow(EventEntry.COLUMN_NAME_HOUR));
      title=cursor.getString(cursor.getColumnIndexOrThrow(EventEntry.COLUMN_NAME_TITLE));
      eClass=cursor.getString(cursor.getColumnIndexOrThrow(EventEntry.COLUMN_NAME_CLASS));
      format=cursor.getString(cursor.getColumnIndexOrThrow(EventEntry.COLUMN_NAME_FORMAT));
      qualify=cursor.getInt(cursor.getColumnIndexOrThrow(EventEntry.COLUMN_NAME_QUALIFY))==1;
      duration=cursor.getInt(cursor.getColumnIndexOrThrow(EventEntry.COLUMN_NAME_DURATION));
      continuous=cursor.getInt(cursor.getColumnIndexOrThrow(EventEntry.COLUMN_NAME_CONTINUOUS))==1;
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
    }

    cursor.close();
    return events;
  }

  public Cursor getSearchCursor(String selection) {
    String sortOrder=TournamentEntry.COLUMN_NAME_TITLE+" ASC";
    return sqLiteDatabase
        .query(TournamentEntry.TABLE_NAME, null, selection, null, null, null, sortOrder);
  }

  public Tournament getTournament(long id) {
    String where=TournamentEntry._ID+"="+String.valueOf(id);
    List<Tournament> tournaments=getTournaments(where);

    if (tournaments.size()==0) {
      return null;
    } else {
      return tournaments.get(0);
    }
  }

  /**
   * Search tournament table for any event with a finish
   *
   * @return A list of all finishes sorted by tournament title
   */
  public List<Tournament> getTournamentsWithFinishes() {
    String where=TournamentEntry.COLUMN_NAME_FINISH+"!=0";
    return getTournaments(where);
  }

  public List<Tournament> getTournaments(String selection) {
    Cursor cursor=getSearchCursor(selection);

    String title, label, gm;
    int id, prize, finish, finalEventId;
    boolean isTournament, visible;

    List<Tournament> tournaments=new ArrayList<>();
    Tournament tournament;
    if (cursor.moveToFirst()) {
      do {
        id=cursor.getInt(cursor.getColumnIndexOrThrow(TournamentEntry._ID));
        title=cursor.getString(cursor.getColumnIndexOrThrow(TournamentEntry.COLUMN_NAME_TITLE));
        label=cursor.getString(cursor.getColumnIndexOrThrow(TournamentEntry.COLUMN_NAME_LABEL));
        isTournament=
            cursor.getInt(cursor.getColumnIndexOrThrow(TournamentEntry.COLUMN_NAME_IS_TOURNAMENT))==
                1;
        prize=cursor.getInt(cursor.getColumnIndexOrThrow(TournamentEntry.COLUMN_NAME_PRIZE));
        gm=cursor.getString(cursor.getColumnIndexOrThrow(TournamentEntry.COLUMN_NAME_GM));
        finalEventId=cursor.getInt(cursor.getColumnIndexOrThrow(TournamentEntry.COLUMN_NAME_FINAL_EVENT));
        finish=cursor.getInt(cursor.getColumnIndexOrThrow(TournamentEntry.COLUMN_NAME_FINISH));
        visible=cursor.getInt(cursor.getColumnIndexOrThrow(TournamentEntry.COLUMN_NAME_VISIBLE))==1;

        tournament=new Tournament(id, title, label, isTournament, prize, gm, finalEventId);
        tournament.finish=finish;
        tournament.visible=visible;

        tournaments.add(tournament);
      } while (cursor.moveToNext());
    }
    cursor.close();
    return tournaments;
  }

  public int updateTournamentFinish(long rowId, int finish) {
    ContentValues values=new ContentValues();
    values.put(TournamentEntry.COLUMN_NAME_FINISH, finish);

    String selection=TournamentEntry._ID+" LIKE ?";
    String[] selectionArgs={String.valueOf(rowId)};

    return sqLiteDatabase.update(TournamentEntry.TABLE_NAME, values, selection, selectionArgs);
  }

  public int updateEventStarred(long rowId, boolean starred) {
    ContentValues values=new ContentValues();
    values.put(EventEntry.COLUMN_NAME_STARRED, starred ? 1 : 0);

    String selection=EventEntry._ID+" LIKE ?";
    String[] selectionArgs={String.valueOf(rowId)};

    return sqLiteDatabase.update(EventEntry.TABLE_NAME, values, selection, selectionArgs);
  }

  public int updateEventNote(long rowId, String note) {
    ContentValues values=new ContentValues();
    values.put(EventEntry.COLUMN_NAME_NOTE, note);

    String selection=EventEntry._ID+" LIKE ?";
    String[] selectionArgs={String.valueOf(rowId)};

    return sqLiteDatabase.update(EventEntry.TABLE_NAME, values, selection, selectionArgs);
  }
}
