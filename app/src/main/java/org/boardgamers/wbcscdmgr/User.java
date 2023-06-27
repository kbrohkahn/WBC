package org.boardgamers.wbcscdmgr;

/**
 * User class for identifying different schedules and their names
 */
class User {
	public final long id;
	public final String name;
	private final String email;

	public User(long i, String n, String e) {
		id = i;
		name = n;
		email = e;
	}
}
