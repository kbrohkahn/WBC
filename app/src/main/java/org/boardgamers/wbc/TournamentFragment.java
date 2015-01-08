package org.boardgamers.wbc;

import com.kbrohkahn.conventionlibrary.CL_TournamentFragment;

public class TournamentFragment extends CL_TournamentFragment {

  @Override
  protected Class getEventActivityClass() {
    return EventActivity.class;
  }

  @Override
  protected String getPreviewLink(String tournament) {
	return "http://boardgamers.org/yearbkex/" + tournament + "pge.htm";
  }

  @Override
  protected String getReportLink(String tournament) {
	return "http://boardgamers.org/yearbook13/" + tournament + "pge.htm";
  }
}
