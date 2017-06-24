package org.boardgamers.wbc;

public class Event {
	public final int id;
	public final int tournamentID;

	public final int day;
	public float hour;
	public String title;

	public final String eClass;
	public final String format;
	public final boolean qualify;
	public float duration;
	public final boolean continuous;
	private final float totalDuration;

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