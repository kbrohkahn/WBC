package org.boardgamers.wbc;

public class Event {

	public String identifier;
	public int tournamentID;

	public int day;
	public int hour;
	public String title;

	public String eClass;
	public String format;
	public boolean qualify;
	public double duration;
	public boolean continuous;
	public double totalDuration;

	public String location;

	// user variables
	public boolean starred;

	public Event(String i, int tID, int da, int h, String t, String cl,
			String f, boolean q, double du, boolean co, double td, String l) {
		identifier=i;
		tournamentID=tID;

		day=da;
		hour=h;
		title=t;

		eClass=cl;
		format=f;
		qualify=q;
		duration=du;
		continuous=co;
		totalDuration=td;

		location=l;

	}
}
