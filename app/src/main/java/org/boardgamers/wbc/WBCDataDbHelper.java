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
 * Created by Kevin on 4/27/2015. Database used for storing all data
 */
public class WBCDataDbHelper extends SQLiteOpenHelper {
  //private final String TAG="Database";

  public static final int DATABASE_VERSION=3;

  public static final String DATABASE_NAME="WBCdata.db";

  public static abstract class EventEntry implements BaseColumns {
    public static final String TABLE_NAME="events";
    public static final String COLUMN_EVENT_ID="e_id";
    public static final String COLUMN_TOURNAMENT_ID="t_id";
    public static final String COLUMN_TITLE="title";
    public static final String COLUMN_DAY="day";
    public static final String COLUMN_HOUR="hour";
    public static final String COLUMN_CLASS="eClass";
    public static final String COLUMN_FORMAT="eFormat";
    public static final String COLUMN_QUALIFY="qualify";
    public static final String COLUMN_DURATION="duration";
    public static final String COLUMN_CONTINUOUS="continuous";
    public static final String COLUMN_TOTAL_DURATION="total_duration";
    public static final String COLUMN_LOCATION="location";
    public static final String COLUMN_NULLABLE="null";
  }

  public static abstract class TournamentEntry implements BaseColumns {
    public static final String TABLE_NAME="tournaments";
    public static final String COLUMN_TOURNAMENT_ID="t_id";
    public static final String COLUMN_TITLE="title";
    public static final String COLUMN_LABEL="label";
    public static final String COLUMN_IS_TOURNAMENT="is_tournament";
    public static final String COLUMN_PRIZE="prize";
    public static final String COLUMN_GM="gm";
    public static final String COLUMN_FINAL_EVENT="final_event";
    public static final String COLUMN_VISIBLE="visible";
    public static final String COLUMN_NULLABLE="null";
  }

  public static abstract class UserEntry implements BaseColumns {
    public static final String TABLE_NAME="user";
    public static final String COLUMN_USER_ID="u_id";
    public static final String COLUMN_NAME="name";
    public static final String COLUMN_EMAIL="email";
    public static final String COLUMN_NULLABLE="null";
  }

  public static abstract class UserEventDataEntry implements BaseColumns {
    public static final String TABLE_NAME="user_event_data";
    public static final String COLUMN_USER_ID="user_id";
    public static final String COLUMN_EVENT_ID="event_id";
    public static final String COLUMN_STARRED="starred";
    public static final String COLUMN_NOTE="note";
    public static final String COLUMN_NULLABLE="null";
  }

  public static abstract class UserTournamentDataEntry implements BaseColumns {
    public static final String TABLE_NAME="user_tournament_data";
    public static final String COLUMN_USER_ID="user_id";
    public static final String COLUMN_TOURNAMENT_ID="tournament_id";
    public static final String COLUMN_FINISH="finish";
    public static final String COLUMN_NULLABLE="null";
  }

  public static abstract class UserCreatedEventEntry implements BaseColumns {
    public static final String TABLE_NAME="user_event";
    public static final String COLUMN_USER_ID="user_id";
    public static final String COLUMN_EVENT_ID="e_id";
    public static final String COLUMN_TITLE="title";
    public static final String COLUMN_DAY="day";
    public static final String COLUMN_HOUR="hour";
    public static final String COLUMN_DURATION="duration";
    public static final String COLUMN_LOCATION="location";
    public static final String COLUMN_NULLABLE="null";
  }

  private static final String SQL_CREATE_EVENT_ENTRIES="CREATE TABLE "+EventEntry.TABLE_NAME+" ("+
      EventEntry._ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+
      EventEntry.COLUMN_EVENT_ID+" INTEGER UNIQUE,"+
      EventEntry.COLUMN_TOURNAMENT_ID+" INTEGER,"+
      EventEntry.COLUMN_TITLE+" TEXT,"+
      EventEntry.COLUMN_DAY+" INTEGER,"+
      EventEntry.COLUMN_HOUR+" REAL,"+
      EventEntry.COLUMN_CLASS+" TEXT,"+
      EventEntry.COLUMN_FORMAT+" TEXT,"+
      EventEntry.COLUMN_QUALIFY+" INTEGER,"+
      EventEntry.COLUMN_DURATION+" REAL,"+
      EventEntry.COLUMN_CONTINUOUS+" INTEGER,"+
      EventEntry.COLUMN_TOTAL_DURATION+" REAL,"+
      EventEntry.COLUMN_LOCATION+" TEXT)";

  private static final String SQL_CREATE_TOURNAMENT_ENTRIES=
      "CREATE TABLE "+TournamentEntry.TABLE_NAME+" ("+
          TournamentEntry._ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+
          TournamentEntry.COLUMN_TOURNAMENT_ID+" INTEGER UNIQUE,"+
          TournamentEntry.COLUMN_TITLE+" TEXT UNIQUE,"+
          TournamentEntry.COLUMN_LABEL+" TEXT,"+
          TournamentEntry.COLUMN_IS_TOURNAMENT+" INTEGER,"+
          TournamentEntry.COLUMN_PRIZE+" INTEGER,"+
          TournamentEntry.COLUMN_GM+" TEXT,"+
          TournamentEntry.COLUMN_FINAL_EVENT+" INTEGER,"+
          TournamentEntry.COLUMN_VISIBLE+" INTEGER)";

  private static final String SQL_CREATE_USER_ENTRIES="CREATE TABLE "+UserEntry.TABLE_NAME+" ("+
      UserEntry._ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+
      UserEntry.COLUMN_USER_ID+" INTEGER UNIQUE,"+
      UserEntry.COLUMN_NAME+" TEXT,"+
      UserEntry.COLUMN_EMAIL+" TEXT)";

  private static final String SQL_CREATE_USER_TOURNAMENT_DATA_ENTRIES=
      "CREATE TABLE "+UserTournamentDataEntry.TABLE_NAME+" ("+
          UserTournamentDataEntry._ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+
          UserTournamentDataEntry.COLUMN_USER_ID+" INTEGER,"+
          UserTournamentDataEntry.COLUMN_TOURNAMENT_ID+" INTEGER,"+
          UserTournamentDataEntry.COLUMN_FINISH+" INTEGER,"+
          "UNIQUE("+UserTournamentDataEntry.COLUMN_USER_ID+","+
          UserTournamentDataEntry.COLUMN_TOURNAMENT_ID+") ON CONFLICT REPLACE)";

  private static final String SQL_CREATE_USER_EVENT_DATA_ENTRIES=
      "CREATE TABLE "+UserEventDataEntry.TABLE_NAME+" ("+
          UserEventDataEntry._ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+
          UserEventDataEntry.COLUMN_USER_ID+" INTEGER,"+
          UserEventDataEntry.COLUMN_EVENT_ID+" INTEGER,"+
          UserEventDataEntry.COLUMN_STARRED+" INTEGER,"+
          UserEventDataEntry.COLUMN_NOTE+" TEXT, "+
          "UNIQUE("+UserEventDataEntry.COLUMN_USER_ID+","+
          UserEventDataEntry.COLUMN_EVENT_ID+") ON CONFLICT REPLACE)";

  private static final String SQL_CREATE_USER_EVENT_ENTRIES=
      "CREATE TABLE "+UserCreatedEventEntry.TABLE_NAME+" ("+
          UserCreatedEventEntry._ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+
          UserCreatedEventEntry.COLUMN_EVENT_ID+" INTEGER,"+
          UserCreatedEventEntry.COLUMN_USER_ID+" INTEGER,"+
          UserCreatedEventEntry.COLUMN_TITLE+" TEXT,"+
          UserCreatedEventEntry.COLUMN_DAY+" INTEGER,"+
          UserCreatedEventEntry.COLUMN_HOUR+" INTEGER,"+
          UserCreatedEventEntry.COLUMN_DURATION+" REAL,"+
          UserCreatedEventEntry.COLUMN_LOCATION+" TEXT)";

  private static final String SQL_DELETE_EVENT_ENTRIES=
      "DROP TABLE IF EXISTS "+EventEntry.TABLE_NAME;

  private static final String SQL_DELETE_TOURNAMENT_ENTRIES=
      "DROP TABLE IF EXISTS "+TournamentEntry.TABLE_NAME;

  private static final String SQL_DELETE_USER_ENTRIES="DROP TABLE IF EXISTS "+UserEntry.TABLE_NAME;

  private static final String SQL_DELETE_USER_EVENT_DATA_ENTRIES=
      "DROP TABLE IF EXISTS "+UserEventDataEntry.TABLE_NAME;

  private static final String SQL_DELETE_USER_TOURNAMENT_DATA_ENTRIES=
      "DROP TABLE IF EXISTS "+UserTournamentDataEntry.TABLE_NAME;

  private static final String SQL_DELETE_USER_EVENT_ENTRIES=
      "DROP TABLE IF EXISTS "+UserCreatedEventEntry.TABLE_NAME;

  public SQLiteDatabase sqLiteDatabase;

  public WBCDataDbHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  public void onCreate(SQLiteDatabase db) {
    db.execSQL(SQL_CREATE_EVENT_ENTRIES);
    db.execSQL(SQL_CREATE_TOURNAMENT_ENTRIES);
    db.execSQL(SQL_CREATE_USER_ENTRIES);
    db.execSQL(SQL_CREATE_USER_EVENT_DATA_ENTRIES);
    db.execSQL(SQL_CREATE_USER_TOURNAMENT_DATA_ENTRIES);
    db.execSQL(SQL_CREATE_USER_EVENT_ENTRIES);
  }

  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    if (oldVersion<newVersion) {
      db.execSQL(SQL_DELETE_EVENT_ENTRIES);
      db.execSQL(SQL_DELETE_TOURNAMENT_ENTRIES);
      db.execSQL(SQL_DELETE_USER_ENTRIES);
      db.execSQL(SQL_DELETE_USER_EVENT_DATA_ENTRIES);
      db.execSQL(SQL_DELETE_USER_TOURNAMENT_DATA_ENTRIES);
      db.execSQL(SQL_DELETE_USER_EVENT_ENTRIES);

      onCreate(db);
    }
  }

  public int getVersion() {
    sqLiteDatabase=super.getReadableDatabase();
    int version=sqLiteDatabase.getVersion();
    sqLiteDatabase.close();
    return version;
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

  public long insertEvent(int eId, int tId, int day, float hour, String title, String eClass,
                          String eFormat, boolean qualify, float duration, boolean continuous,
                          float totalDuration, String location) {
    ContentValues values=new ContentValues();
    values.put(EventEntry.COLUMN_EVENT_ID, eId);
    values.put(EventEntry.COLUMN_TOURNAMENT_ID, tId);
    values.put(EventEntry.COLUMN_TITLE, title);
    values.put(EventEntry.COLUMN_DAY, day);
    values.put(EventEntry.COLUMN_HOUR, hour);
    values.put(EventEntry.COLUMN_CLASS, eClass);
    values.put(EventEntry.COLUMN_FORMAT, eFormat);
    values.put(EventEntry.COLUMN_QUALIFY, qualify ? 1 : 0);
    values.put(EventEntry.COLUMN_DURATION, duration);
    values.put(EventEntry.COLUMN_CONTINUOUS, continuous ? 1 : 0);
    values.put(EventEntry.COLUMN_TOTAL_DURATION, totalDuration);
    values.put(EventEntry.COLUMN_LOCATION, location);

    // Insert the new row, returning the primary key value of the new row
    return sqLiteDatabase.insert(EventEntry.TABLE_NAME, EventEntry.COLUMN_NULLABLE, values);
  }

  public long insertTournament(int tId, String title, String label, boolean isTournament, int prize,
                               String gm, int eventId) {
    ContentValues values=new ContentValues();
    values.put(TournamentEntry.COLUMN_TOURNAMENT_ID, tId);
    values.put(TournamentEntry.COLUMN_TITLE, title);
    values.put(TournamentEntry.COLUMN_LABEL, label);
    values.put(TournamentEntry.COLUMN_IS_TOURNAMENT, isTournament ? 1 : 0);
    values.put(TournamentEntry.COLUMN_PRIZE, prize);
    values.put(TournamentEntry.COLUMN_GM, gm);
    values.put(TournamentEntry.COLUMN_FINAL_EVENT, eventId);
    values.put(TournamentEntry.COLUMN_VISIBLE, 1);

    // Insert the new row, returning the primary key value of the new row
    return sqLiteDatabase
        .insert(TournamentEntry.TABLE_NAME, TournamentEntry.COLUMN_NULLABLE, values);
  }

  public int getNumEvents() {
    Cursor cursor=sqLiteDatabase.rawQuery("SELECT COUNT (*) FROM "+EventEntry.TABLE_NAME, null);
    cursor.moveToFirst();
    int count=cursor.getInt(0);
    cursor.close();

    return count;
  }

  public List<Event> getUserEvents(int uId, String where) {
    String sortOrder=UserCreatedEventEntry.COLUMN_DAY+" ASC, "+UserCreatedEventEntry.COLUMN_HOUR+
        " ASC, "+UserCreatedEventEntry.COLUMN_TITLE+" ASC";

    if (!where.equalsIgnoreCase("")) {
      where+=" AND ";
    }

    where+=UserCreatedEventEntry.TABLE_NAME+"."+UserCreatedEventEntry.COLUMN_USER_ID+"="+
        String.valueOf(uId);

    String query="SELECT * FROM "+UserCreatedEventEntry.TABLE_NAME+" LEFT JOIN "+
        UserEventDataEntry.TABLE_NAME+" ON "+UserCreatedEventEntry.TABLE_NAME+"."+
        UserCreatedEventEntry.COLUMN_EVENT_ID+"="+
        UserEventDataEntry.TABLE_NAME+"."+UserEventDataEntry.COLUMN_EVENT_ID+" WHERE "+
        where+" ORDER BY "+sortOrder;

    //Log.d(TAG, "Full query: "+query);

    Cursor cursor=sqLiteDatabase.rawQuery(query, null);

    String title, location, note;
    int id, day, hour;
    float duration;
    boolean starred;

    List<Event> events=new ArrayList<>();
    Event event;
    cursor.moveToFirst();
    cursor.moveToPrevious();

    while (cursor.moveToNext()) {
      id=cursor.getInt(cursor.getColumnIndexOrThrow(UserCreatedEventEntry.COLUMN_EVENT_ID));
      day=cursor.getInt(cursor.getColumnIndexOrThrow(UserCreatedEventEntry.COLUMN_DAY));
      hour=cursor.getInt(cursor.getColumnIndexOrThrow(UserCreatedEventEntry.COLUMN_HOUR));
      title=cursor.getString(cursor.getColumnIndexOrThrow(UserCreatedEventEntry.COLUMN_TITLE));
      duration=
          cursor.getFloat(cursor.getColumnIndexOrThrow(UserCreatedEventEntry.COLUMN_DURATION));
      location=
          cursor.getString(cursor.getColumnIndexOrThrow(UserCreatedEventEntry.COLUMN_LOCATION));

      starred=cursor.getInt(cursor.getColumnIndexOrThrow(UserEventDataEntry.COLUMN_STARRED))==1;
      note=cursor.getString(cursor.getColumnIndexOrThrow(UserEventDataEntry.COLUMN_NOTE));

      event=new Event(id, MainActivity.USER_EVENT_ID+uId, day, hour, title, "", "", false, duration,
          false, duration, location);

      event.starred=starred;
      event.note=note;

      events.add(event);
    }

    cursor.close();

    return events;
  }

  public List<Event> getAllEvents(int userId) {
    String where="";
    List<Event> events=getUserEvents(userId, where);
    events.addAll(getEvents(userId, where));
    return events;
  }

  public List<Event> getStarredEvents(int userId) {
    String where=UserEventDataEntry.TABLE_NAME+"."+UserEventDataEntry.COLUMN_STARRED+"=1";
    List<Event> events=getUserEvents(userId, where);
    events.addAll(getEvents(userId, where));
    return events;
  }

  public List<Event> getEventsWithNotes(int userId) {
    String where=UserEventDataEntry.TABLE_NAME+"."+UserEventDataEntry.COLUMN_NOTE+"!='' AND "+
        UserEventDataEntry.TABLE_NAME+"."+UserEventDataEntry.COLUMN_NOTE+" IS NOT NULL";
    List<Event> events=getUserEvents(userId, where);
    events.addAll(getEvents(userId, where));
    return events;
  }

  public List<Event> getEventsFromQuery(int userId, String query) {
    String where=UserCreatedEventEntry.TABLE_NAME+"."+UserCreatedEventEntry.COLUMN_TITLE+" LIKE '%"+
        query+"%'";
    List<Event> events=getUserEvents(userId, where);
    where="("+EventEntry.TABLE_NAME+"."+EventEntry.COLUMN_TITLE+" LIKE '%"+query+"%' OR "+
        EventEntry.TABLE_NAME+"."+EventEntry.COLUMN_FORMAT+" LIKE '%"+query+"%')";

    events.addAll(getEvents(userId, where));
    return events;
  }

  public List<Event> getTournamentEvents(int userId, long tournamentId) {
    String where=EventEntry.TABLE_NAME+"."+EventEntry.COLUMN_TOURNAMENT_ID+"="+
        String.valueOf(tournamentId);
    return getEvents(userId, where);
  }

  public Event getEvent(int userId, long eventId) {
    String where=UserCreatedEventEntry.TABLE_NAME+"."+UserCreatedEventEntry.COLUMN_EVENT_ID+"="+
        String.valueOf(eventId);
    List<Event> events=getUserEvents(userId, where);

    where=EventEntry.TABLE_NAME+"."+EventEntry.COLUMN_EVENT_ID+"="+String.valueOf(eventId);
    events.addAll(getEvents(userId, where));

    return events.get(0);
  }

  public List<Event> getEvents(int uId, String whereClause) {
    String sortOrder=EventEntry.COLUMN_DAY+" ASC, "+EventEntry.COLUMN_HOUR+" ASC, "+
        EventEntry.COLUMN_QUALIFY+" ASC, "+EventEntry.COLUMN_TITLE+" ASC";

    String where=whereClause;
    if (!where.equalsIgnoreCase("")) {
      where+=" AND ";
    }

    where+="("+UserEventDataEntry.TABLE_NAME+"."+UserEventDataEntry.COLUMN_USER_ID+"="+
        String.valueOf(uId)+" OR "+UserEventDataEntry.TABLE_NAME+"."+
        UserEventDataEntry.COLUMN_USER_ID+" IS NULL)";

    String query="SELECT * FROM "+EventEntry.TABLE_NAME+" LEFT JOIN "+
        UserEventDataEntry.TABLE_NAME+" ON "+EventEntry.TABLE_NAME+"."+
        EventEntry.COLUMN_EVENT_ID+"="+
        UserEventDataEntry.TABLE_NAME+"."+UserEventDataEntry.COLUMN_EVENT_ID+" WHERE "+
        where+" ORDER BY "+sortOrder;

    //Log.d(TAG, "Full query: "+query);

    Cursor cursor=sqLiteDatabase.rawQuery(query, null);

    String title, eClass, format, location, note;
    int id, tournamentId, day;
    float hour, duration, totalDuration;
    boolean qualify, continuous, starred;

    Event event;
    cursor.moveToFirst();
    cursor.moveToPrevious();

    List<Event> events=new ArrayList<>();
    while (cursor.moveToNext()) {
      id=cursor.getInt(cursor.getColumnIndexOrThrow(EventEntry.COLUMN_EVENT_ID));
      tournamentId=cursor.getInt(cursor.getColumnIndexOrThrow(EventEntry.COLUMN_TOURNAMENT_ID));
      day=cursor.getInt(cursor.getColumnIndexOrThrow(EventEntry.COLUMN_DAY));
      hour=cursor.getFloat(cursor.getColumnIndexOrThrow(EventEntry.COLUMN_HOUR));
      title=cursor.getString(cursor.getColumnIndexOrThrow(EventEntry.COLUMN_TITLE));
      eClass=cursor.getString(cursor.getColumnIndexOrThrow(EventEntry.COLUMN_CLASS));
      format=cursor.getString(cursor.getColumnIndexOrThrow(EventEntry.COLUMN_FORMAT));
      qualify=cursor.getInt(cursor.getColumnIndexOrThrow(EventEntry.COLUMN_QUALIFY))==1;
      duration=cursor.getFloat(cursor.getColumnIndexOrThrow(EventEntry.COLUMN_DURATION));
      continuous=cursor.getInt(cursor.getColumnIndexOrThrow(EventEntry.COLUMN_CONTINUOUS))==1;
      totalDuration=
          cursor.getFloat(cursor.getColumnIndexOrThrow(EventEntry.COLUMN_TOTAL_DURATION));
      location=cursor.getString(cursor.getColumnIndexOrThrow(EventEntry.COLUMN_LOCATION));

      starred=cursor.getInt(cursor.getColumnIndexOrThrow(UserEventDataEntry.COLUMN_STARRED))==1;
      note=cursor.getString(cursor.getColumnIndexOrThrow(UserEventDataEntry.COLUMN_NOTE));

      event=new Event(id, tournamentId, day, hour, title, eClass, format, qualify, duration,
          continuous, totalDuration, location);

      event.starred=starred;
      event.note=note;

      events.add(event);
    }

    cursor.close();

    return events;
  }

  public Cursor getSearchCursor(String query) {
    String sortOrder=TournamentEntry.COLUMN_TITLE+" ASC";
    String selection=TournamentEntry.COLUMN_TITLE+" LIKE '%"+query+"%'";
    return sqLiteDatabase
        .query(TournamentEntry.TABLE_NAME, null, selection, null, null, null, sortOrder);
  }

  public Tournament getTournament(int userId, long tournamentId) {
    String where=TournamentEntry.TABLE_NAME+"."+TournamentEntry.COLUMN_TOURNAMENT_ID+"="+
        String.valueOf(tournamentId);
    List<Tournament> tournaments=getTournaments(userId, where);

    return tournaments.get(0);
  }

  public List<Tournament> getTournamentsWithFinishes(int userId) {
    String where=UserTournamentDataEntry.TABLE_NAME+"."+UserTournamentDataEntry.COLUMN_FINISH+
        "!=0 AND "+UserTournamentDataEntry.TABLE_NAME+"."+
        UserTournamentDataEntry.COLUMN_FINISH+" IS NOT NULL";
    return getTournaments(userId, where);
  }

  public List<Tournament> getAllTournaments(int userId) {
    String where="";
    return getTournaments(userId, where);
  }

  public List<Tournament> getTournaments(int uId, String where) {
    String sortOrder=TournamentEntry.COLUMN_TITLE+" ASC";

    if (!where.equalsIgnoreCase("")) {
      where+=" AND ";
    }
    where+="("+UserTournamentDataEntry.TABLE_NAME+"."+UserTournamentDataEntry.COLUMN_USER_ID+"="+
        String.valueOf(uId)+" OR "+UserTournamentDataEntry.TABLE_NAME+"."+
        UserTournamentDataEntry.COLUMN_USER_ID+" IS NULL)";

    String query="SELECT * FROM "+TournamentEntry.TABLE_NAME+" LEFT JOIN "+
        UserTournamentDataEntry.TABLE_NAME+" ON "+TournamentEntry.TABLE_NAME+"."+
        TournamentEntry.COLUMN_TOURNAMENT_ID+"="+UserTournamentDataEntry.TABLE_NAME+"."+
        UserTournamentDataEntry.COLUMN_TOURNAMENT_ID+" WHERE "+where+" ORDER BY "+sortOrder;

    //Log.d(TAG, "Full query: "+query);

    Cursor cursor=sqLiteDatabase.rawQuery(query, null);

    String title, label, gm;
    int id, prize, finish, finalEventId;
    boolean isTournament, visible;

    List<Tournament> tournaments=new ArrayList<>();
    Tournament tournament;
    if (cursor.moveToFirst()) {
      do {
        id=cursor.getInt(cursor.getColumnIndexOrThrow(TournamentEntry.COLUMN_TOURNAMENT_ID));
        title=cursor.getString(cursor.getColumnIndexOrThrow(TournamentEntry.COLUMN_TITLE));
        label=cursor.getString(cursor.getColumnIndexOrThrow(TournamentEntry.COLUMN_LABEL));
        isTournament=
            cursor.getInt(cursor.getColumnIndexOrThrow(TournamentEntry.COLUMN_IS_TOURNAMENT))==1;
        prize=cursor.getInt(cursor.getColumnIndexOrThrow(TournamentEntry.COLUMN_PRIZE));
        gm=cursor.getString(cursor.getColumnIndexOrThrow(TournamentEntry.COLUMN_GM));
        finalEventId=
            cursor.getInt(cursor.getColumnIndexOrThrow(TournamentEntry.COLUMN_FINAL_EVENT));
        visible=cursor.getInt(cursor.getColumnIndexOrThrow(TournamentEntry.COLUMN_VISIBLE))==1;
        finish=cursor.getInt(cursor.getColumnIndexOrThrow(UserTournamentDataEntry.COLUMN_FINISH));

        tournament=new Tournament(id, title, label, isTournament, prize, gm, finalEventId);
        tournament.finish=finish;
        tournament.visible=visible;

        tournaments.add(tournament);
      } while (cursor.moveToNext());
    }
    cursor.close();
    return tournaments;
  }

  public int getNumUserEvents() {
    Cursor cursor=
        sqLiteDatabase.rawQuery("SELECT COUNT (*) FROM "+UserCreatedEventEntry.TABLE_NAME, null);
    cursor.moveToFirst();
    int count=cursor.getInt(0);
    cursor.close();

    return count;

  }

  public long insertUserEvent(int eId, int uId, String title, int day, int hour, float duration,
                              String location) {
    ContentValues values=new ContentValues();
    values.put(UserCreatedEventEntry.COLUMN_EVENT_ID, eId);
    values.put(UserCreatedEventEntry.COLUMN_USER_ID, uId);
    values.put(UserCreatedEventEntry.COLUMN_TITLE, title);
    values.put(UserCreatedEventEntry.COLUMN_DAY, day);
    values.put(UserCreatedEventEntry.COLUMN_HOUR, hour);
    values.put(UserCreatedEventEntry.COLUMN_DURATION, duration);
    values.put(UserCreatedEventEntry.COLUMN_LOCATION, location);

    // Insert the new row, returning the primary key value of the new row
    return sqLiteDatabase
        .insert(UserCreatedEventEntry.TABLE_NAME, UserCreatedEventEntry.COLUMN_NULLABLE, values);
  }

  public long deleteUserEvent(int uId, int eId) {
    String where=UserCreatedEventEntry.COLUMN_USER_ID+"="+String.valueOf(uId)+" AND "+
        UserCreatedEventEntry.COLUMN_EVENT_ID+"="+String.valueOf(eId);
    return sqLiteDatabase.delete(UserCreatedEventEntry.TABLE_NAME, where, null);
  }

  public long deleteAllUserEvents(int uID) {
    String where=UserCreatedEventEntry.COLUMN_USER_ID+"="+String.valueOf(uID);
    return sqLiteDatabase.delete(UserCreatedEventEntry.TABLE_NAME, where, null);
  }

  public long insertUser(int uId, String name, String email) {
    ContentValues values=new ContentValues();
    values.put(UserEntry.COLUMN_USER_ID, uId);
    values.put(UserEntry.COLUMN_NAME, name);
    values.put(UserEntry.COLUMN_EMAIL, email);

    // Insert the new row, returning the primary key value of the new row
    return sqLiteDatabase.insert(UserEntry.TABLE_NAME, UserEntry.COLUMN_NULLABLE, values);
  }

  public User getUser(int id) {
    List<User> users=getUsers(UserEntry.COLUMN_USER_ID+"="+String.valueOf(id));

    return users.get(0);
  }

  public List<User> getUsers(String where) {
    Cursor cursor=sqLiteDatabase.query(UserEntry.TABLE_NAME, null, where, null, null, null, null);

    List<User> users=new ArrayList<>();
    int id;
    String name, email;
    if (cursor.moveToFirst()) {
      do {
        id=cursor.getInt(cursor.getColumnIndex(UserEntry.COLUMN_USER_ID));
        name=cursor.getString(cursor.getColumnIndex(UserEntry.COLUMN_NAME));
        email=cursor.getString(cursor.getColumnIndex(UserEntry.COLUMN_EMAIL));

        users.add(new User(id, name, email));
      } while (cursor.moveToNext());
    }
    cursor.close();
    return users;
  }

  public long deleteUser(int userId) {
    String where;
    if (userId==-1) {
      where="";
    } else {
      where=UserEntry.COLUMN_USER_ID+"="+String.valueOf(userId);
    }
    return sqLiteDatabase.delete(UserEntry.TABLE_NAME, where, null);
  }

  public long deleteUserData(int userId) {
    String where;

    if (userId==-1) {
      where="";
    } else {
      where=UserEventDataEntry.COLUMN_USER_ID+"="+String.valueOf(userId);
    }
    int result=sqLiteDatabase.delete(UserEventDataEntry.TABLE_NAME, where, null);

    if (userId==-1) {
      where="";
    } else {
      where=UserTournamentDataEntry.COLUMN_USER_ID+"="+String.valueOf(userId);
    }
    result+=sqLiteDatabase.delete(UserTournamentDataEntry.TABLE_NAME, where, null);

    if (userId==-1) {
      where="";
    } else {
      where=UserCreatedEventEntry.COLUMN_USER_ID+"="+String.valueOf(userId);
    }
    result+=sqLiteDatabase.delete(UserCreatedEventEntry.TABLE_NAME, where, null);

    return result;
  }

  public long mergeUserData(int userId) {
    ContentValues values=new ContentValues();

    values.put("user_id", MainActivity.PRIMARY_USER_ID);

    String where=UserEventDataEntry.COLUMN_USER_ID+"="+String.valueOf(userId);
    int result=sqLiteDatabase
        .updateWithOnConflict(UserEventDataEntry.TABLE_NAME, values, where, null,
            SQLiteDatabase.CONFLICT_REPLACE);

    where=UserTournamentDataEntry.COLUMN_USER_ID+"="+String.valueOf(userId);
    result+=sqLiteDatabase
        .updateWithOnConflict(UserTournamentDataEntry.TABLE_NAME, values, where, null,
            SQLiteDatabase.CONFLICT_REPLACE);

    where=UserCreatedEventEntry.COLUMN_USER_ID+"="+String.valueOf(userId);
    result+=sqLiteDatabase.update(UserCreatedEventEntry.TABLE_NAME, values, where, null);

    return result;
  }

  public long updateTournamentsVisible(List<Tournament> tournaments) {
    ContentValues values;
    String where;
    long result=0;
    for (Tournament tournament : tournaments) {

      values=new ContentValues();
      values.put(TournamentEntry.COLUMN_VISIBLE, tournament.visible ? 1 : 0);

      where=TournamentEntry.COLUMN_TOURNAMENT_ID+"="+String.valueOf(tournament.id);

      result+=sqLiteDatabase.update(TournamentEntry.TABLE_NAME, values, where, null);
    }
    return result;
  }

  public long insertUserTournamentData(int userId, int tournamentId, int finish) {
    ContentValues values=new ContentValues();
    values.put(UserTournamentDataEntry.COLUMN_FINISH, finish);

    String where=
        UserTournamentDataEntry.COLUMN_TOURNAMENT_ID+"="+String.valueOf(tournamentId)+" AND "+
            UserTournamentDataEntry.COLUMN_USER_ID+"="+String.valueOf(userId);

    long result=sqLiteDatabase.update(UserTournamentDataEntry.TABLE_NAME, values, where, null);

    if (result==0) {
      values.put(UserTournamentDataEntry.COLUMN_USER_ID, userId);
      values.put(UserTournamentDataEntry.COLUMN_TOURNAMENT_ID, tournamentId);

      result=sqLiteDatabase
          .insert(UserTournamentDataEntry.TABLE_NAME, UserTournamentDataEntry.COLUMN_NULLABLE,
              values);
    }
    return result;
  }

  public long insertUserEventData(int userId, int eventId, boolean starred, String note) {
    ContentValues values=new ContentValues();
    values.put(UserEventDataEntry.COLUMN_STARRED, starred ? 1 : 0);
    values.put(UserEventDataEntry.COLUMN_NOTE, note);

    String where=UserEventDataEntry.COLUMN_EVENT_ID+"="+String.valueOf(eventId)+" AND "+
        UserEventDataEntry.COLUMN_USER_ID+"="+String.valueOf(userId);
    long result=sqLiteDatabase.update(UserEventDataEntry.TABLE_NAME, values, where, null);

    if (result==0) {
      values.put(UserEventDataEntry.COLUMN_USER_ID, userId);
      values.put(UserEventDataEntry.COLUMN_EVENT_ID, eventId);

      result=sqLiteDatabase
          .insert(UserEventDataEntry.TABLE_NAME, UserEventDataEntry.COLUMN_NULLABLE, values);
    }
    return result;
  }

  public long insertUserEventData(int userId, List<Event> changedEvents) {
    Event event;
    long result=0;
    for (int i=0; i<changedEvents.size(); i++) {
      event=changedEvents.get(i);

      result+=insertUserEventData(userId, event.id, event.starred, event.note);
    }
    return result;

  }
}
