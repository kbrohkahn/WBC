package org.boardgamers.wbc;

public class Event {
	public final long id;
	public final long tournamentID;

	public final int day;
	public final float hour;
	public String title;

	public final String eClass;
	public final String format;
	public final boolean qualify;
	public final float duration;
	public final boolean continuous;
	private final float totalDuration;

	public final String location;

	// user variables
	public boolean starred;
	public String note;

	public Event(long id, long tournamentID, int day, float hour, String title, String eClass,
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