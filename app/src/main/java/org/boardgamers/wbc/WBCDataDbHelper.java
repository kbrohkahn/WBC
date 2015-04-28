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
          TournamentEntry.COLUMN_NAME_GM+" TEXT,"+" )";

  private static final String SQL_DELETE_EVENT_ENTRIES=
      "DROP TABLE IF EXISTS "+EventEntry.TABLE_NAME;

  private static final String SQL_DELETE_TOURNAMENT_ENTRIES=
      "DROP TABLE IF EXISTS "+TournamentEntry.TABLE_NAME;

  private WBCDataDbHelper mDbHelper;

  public WBCDataDbHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);

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

  public long insertEvent(String id, int tournamentID, int day, int hour, String title,
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

  public List<Event> getEvents(String selection, String[] selectionArgs) {
    SQLiteDatabase db=mDbHelper.getReadableDatabase();

    // Define a projection that specifies which columns from the database
    // you will actually use after this query.
    String[] projection={EventEntry._ID, EventEntry.COLUMN_NAME_ENTRY_ID,
        EventEntry.COLUMN_NAME_ENTRY_TOURNAMENT_ID, EventEntry.COLUMN_NAME_TITLE,
        EventEntry.COLUMN_NAME_DAY, EventEntry.COLUMN_NAME_HOUR, EventEntry.COLUMN_NAME_CLASS,
        EventEntry.COLUMN_NAME_FORMAT, EventEntry.COLUMN_NAME_QUALIFY,
        EventEntry.COLUMN_NAME_DURATION, EventEntry.COLUMN_NAME_CONTINUOUS,
        EventEntry.COLUMN_NAME_TOTAL_DURATION, EventEntry.COLUMN_NAME_LOCATION,
        EventEntry.COLUMN_NAME_NOTE, EventEntry.COLUMN_NAME_STARRED};

    // How you want the results sorted in the resulting Cursor
    String sortOrder=EventEntry.COLUMN_NAME_TITLE+" ASC";

    Cursor cursor=db.query(EventEntry.TABLE_NAME, projection, selection, selectionArgs, null, null,
        sortOrder);

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

  public int updateEventStarred(int rowId, boolean starred) {
    SQLiteDatabase db=mDbHelper.getReadableDatabase();

    ContentValues values=new ContentValues();
    values.put(EventEntry.COLUMN_NAME_STARRED, starred ? 1 : 0);

    String selection=EventEntry.COLUMN_NAME_ENTRY_ID+" LIKE ?";
    String[] selectionArgs={String.valueOf(rowId)};

    return db.update(EventEntry.TABLE_NAME, values, selection, selectionArgs);
  }

  public int updateEventNote(int rowId, String note) {
    SQLiteDatabase db=mDbHelper.getReadableDatabase();

    ContentValues values=new ContentValues();
    values.put(EventEntry.COLUMN_NAME_NOTE, note);

    String selection=EventEntry.COLUMN_NAME_ENTRY_ID+" LIKE ?";
    String[] selectionArgs={String.valueOf(rowId)};

    return db.update(EventEntry.TABLE_NAME, values, selection, selectionArgs);
  }
}
