package org.boardgamers.wbc;

import com.kbrohkahn.conventionlibrary.CL_MainActivity;

public class MainActivity extends CL_MainActivity {

  @Override
  public void loadClasses() {
    mapActivityClass = MapActivity.class;
    aboutActivityClass = AboutActivity.class;
    helpActivityClass = HelpActivity.class;
    filterActivityClass = FilterActivity.class;
    settingsActivityClass = SettingsActivity.class;
    searchActivityClass = SearchActivity.class;
    tournamentActivityClass = TournamentActivity.class;
    eventActivityClass = EventActivity.class;
    notificationServiceClass = NotificationService.class;
  }
}
