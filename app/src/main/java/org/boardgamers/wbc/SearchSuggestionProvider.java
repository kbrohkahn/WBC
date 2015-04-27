package org.boardgamers.wbc;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import java.util.List;

/**
 * Created by Kevin on 4/27/2015.
 */
public class SearchSuggestionProvider extends ContentProvider {
  private List<String> availableSearchStrings;

  public SearchSuggestionProvider() {
    super();
  }

  @Override public Uri insert(Uri uri, ContentValues values) {
    return null;
  }

  @Override public int delete(Uri uri, String selection, String[] selectionArgs) {
    return 0;
  }

  @Override public String getType(Uri uri) {
    return null;
  }

  @Override public Cursor query(Uri uri, String[] projection, String selection,
                                String[] selectionArgs, String sortOrder) {
    return null;
  }

  @Override public int update(Uri uri, ContentValues values, String selection,
                              String[] selectionArgs) {
    return 0;
  }

  @Override public boolean onCreate() {
    //    // get list of available searches
    //    availableSearchStrings=new ArrayList<>();
    //
    //    String[] formats=getStringArray(R.array.search_formats);
    //    availableSearchStrings.addAll(Arrays.asList(formats));
    //
    //    for (int i=0; i<allTournaments.size(); i++) {
    //      availableSearchStrings.add(allTournaments.get(i).title);
    //    }
    //
    return false;
  }
}
