package org.boardgamers.wbc;

import com.kbrohkahn.conventionlibrary.CL_TournamentActivity;

public class TournamentActivity extends CL_TournamentActivity {

  @Override
  protected Class getHelpActivityClass() {
    return HelpActivity.class;
  }
}
