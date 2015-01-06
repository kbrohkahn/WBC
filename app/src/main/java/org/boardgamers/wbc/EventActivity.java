package org.boardgamers.wbc;

import com.kbrohkahn.conventionlibrary.CL_EventActivity;

public class EventActivity extends CL_EventActivity {

  @Override
  protected Class getHelpActivityClass() {
    return HelpActivity.class;
  }
}
