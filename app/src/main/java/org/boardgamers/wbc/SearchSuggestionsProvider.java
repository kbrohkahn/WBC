package org.boardgamers.wbc;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class SearchSuggestionsProvider extends ContentProvider {
  private final String TAG="Content Provider";

  private final String PROVIDER_NAME="org.boardgamers.wbc.SearchSuggestionsProvider";
  private final String TABLE_NAME="tournaments";
  private final String URL="content://"+PROVIDER_NAME+"/"+TABLE_NAME;
  private final Uri CONTENT_URI=Uri.parse(URL);
  private final int CODE=1;

  private final String[] SEARCH_SUGGEST_COLUMNS=
      {BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2,
          SearchManager.SUGGEST_COLUMN_ICON_1};
  private UriMatcher uriMatcher;

  private WBCDataDbHelper dbHelper;

  @Override
  public boolean onCreate() {
    dbHelper=new WBCDataDbHelper(getContext());
    dbHelper.getReadableDatabase();

    uriMatcher=new UriMatcher(UriMatcher.NO_MATCH);
    uriMatcher.addURI(PROVIDER_NAME, TABLE_NAME, CODE);

    return true;
  }

  @Override
  public Uri insert(Uri uri, ContentValues arg1) {
    return null;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                      String sortOrder) {
    Log.d(TAG, selectionArgs[0]);
    Log.d(TAG, uri.getLastPathSegment().toLowerCase());
    Log.d(TAG, String.valueOf(uriMatcher.match(uri)));
    switch (uriMatcher.match(uri)) {
      case CODE:

        // TODO
        break;
      default:
        break;
    }

    MatrixCursor matrixCursor=new MatrixCursor(SEARCH_SUGGEST_COLUMNS);

    Cursor c=dbHelper.getSearchCursor("title LIKE '"+selectionArgs[0]+"'");

    String title, label;
    int id;
    while (c.moveToNext()) {
      id=c.getInt(c.getColumnIndexOrThrow(WBCDataDbHelper.TournamentEntry._ID));
      title=c.getString(c.getColumnIndexOrThrow(WBCDataDbHelper.TournamentEntry.COLUMN_NAME_TITLE));
      label=c.getString(c.getColumnIndexOrThrow(WBCDataDbHelper.TournamentEntry.COLUMN_NAME_LABEL));

      matrixCursor.addRow(new Object[] {id, title, label, 0});
    }

    return matrixCursor;

  }

  @Override
  public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    return 0;
  }

  @Override
  public String getType(Uri uri) {
    return null;
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    return 0;
  }

}
