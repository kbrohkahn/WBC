package org.boardgamers.wbc;

public class Tournament {

  public int id;

  public String title;
  public String label;
  public boolean isTournament;

  public int prize;
  public String gm;

  public boolean visible;
  public int finish;

  /**
   * @param id - unique int
   * @param t  - tournament name
   * @param l  - label
   * @param i  - boolean, whether tournament is tournament
   * @param p  - prize
   * @param g  - game manager
   */
  public Tournament(int id, String t, String l, boolean i, int p, String g) {
    this.id=id;

    this.title=t;
    this.label=l;
    this.isTournament=i;

    this.prize=p;
    this.gm=g;

  }
}
