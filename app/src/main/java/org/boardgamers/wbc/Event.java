package org.boardgamers.wbc;

import com.kbrohkahn.conventionlibrary.CL_Event;

public class Event extends CL_Event {

  public Event(String i, int tID, int da, int h, String t, String cl,
               String f, boolean q, double du, boolean co, double td, String l) {
    super(i, tID, da, h, t, cl, f, q, du, co, td, l);
  }
}
