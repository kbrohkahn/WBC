package org.boardgamers.wbc;

public class Event {
  public long id;
  public String identifier;
  public long tournamentID;

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
  public String note;

  public Event(long id, String identifier, long tournamentID, int day, int hour, String title,
               String eClass, String eFormat, boolean qualify, double duration, boolean continuous,
               double totalDuration, String location) {
    this.id=id;
    this.identifier=identifier;
    this.tournamentID=tournamentID;

    this.day=day;
    this.hour=hour;
    this.title=title;

    this.eClass=eClass;
    this.format=eFormat;
    this.qualify=qualify;
    this.duration=duration;
    this.continuous=continuous;
    this.totalDuration=totalDuration;

    this.location=location;

  }
}