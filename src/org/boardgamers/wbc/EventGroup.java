package org.boardgamers.wbc;

import java.util.ArrayList;

public class EventGroup {
	public final int ID;
	public int hour;
	public ArrayList<Event> events;

	public EventGroup(int i, int h, ArrayList<Event> e) {
		ID=i;
		hour=h;
		events=e;
	}
}
