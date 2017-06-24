package org.boardgamers.wbc;

/**
 * User class for identifying different schedules and their names
 */
public class User {
	public final int id;
	public final String name;
	private final String email;

	public User(int i, String n, String e) {
		id = i;
		name = n;
		email = e;
	}
}
