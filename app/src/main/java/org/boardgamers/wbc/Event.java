package org.boardgamers.wbc;

public class Event {
	public int id;
	public int tournamentID;

	public int day;
	public float hour;
	public String title;

	public String eClass;
	public String format;
	public boolean qualify;
	public float duration;
	public boolean continuous;
	private float totalDuration;

	public String location;

	// user variables
	public boolean starred;
	public String note;

	public Event(int id, int tournamentID, int day, float hour, String title, String eClass,
				 String eFormat, boolean qualify, float duration, boolean continuous,
				 float totalDuration, String location) {
		this.id = id;
		this.tournamentID = tournamentID;

		this.day = day;
		this.hour = hour;
		this.title = title;

		this.eClass = eClass;
		this.format = eFormat;
		this.qualify = qualify;
		this.duration = duration;
		this.continuous = continuous;
		this.totalDuration = totalDuration;

		this.location = location;

	}
}