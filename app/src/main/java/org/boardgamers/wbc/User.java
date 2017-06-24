package org.boardgamers.wbc;

/**
 * User class for identifying different schedules and their names
 */
public class User {
	public int id;
	public String name;
	private String email;

	public User(int i, String n, String e) {
		id = i;
		name = n;
		email = e;
	}
}
