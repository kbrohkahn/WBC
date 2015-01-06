package org.boardgamers.wbc;

import com.kbrohkahn.conventionlibrary.CL_MainActivity;

public class MainActivity extends CL_MainActivity {

  public void loadClasses() {
    mapActivityClass = MapActivity.class;
    tournamentActivityClass = TournamentActivity.class;
    eventActivityClass = EventActivity.class;
  }

  @Override
  protected Class getHelpActivityClass() {
    return HelpActivity.class;
  }

  @Override
  protected Class getSettingsActivityClass() {
    return SettingsActivity.class;
  }

  @Override
  protected Class getAboutActivityClass() {
    return AboutActivity.class;
  }

  @Override
  protected Class getFilterActivityClass() {
    return FilterActivity.class;
  }

  @Override
  protected Class getSearchActivityClass() {
    return SearchActivity.class;
  }
}
