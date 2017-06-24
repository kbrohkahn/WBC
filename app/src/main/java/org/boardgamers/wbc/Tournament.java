package org.boardgamers.wbc;

public class Tournament {

	public final int id;

	public final String title;
	public final String label;
	public final boolean isTournament;

	//	public final int finalEventId;
	public final int prize;
	public final String gm;

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
		this.id = id;

		this.title = t;
		this.label = l;
		this.isTournament = i;

		this.prize = p;
		this.gm = g;
//		this.finalEventId = f;

	}
}
