package org.boardgamers.wbc;

import android.content.Intent;

import com.kbrohkahn.conventionlibrary.CL_NotificationService;

public class NotificationService extends CL_NotificationService {
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    mainActivityClass = MainActivity.class;
    return super.onStartCommand(intent, flags, startId);
  }
}
