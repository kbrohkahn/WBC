package org.boardgamers.wbc;

import com.kbrohkahn.conventionlibrary.CL_TournamentFragment;

public class TournamentFragment extends CL_TournamentFragment {

  @Override
  protected Class getEventActivityClass() {
    return EventActivity.class;
  }
}
