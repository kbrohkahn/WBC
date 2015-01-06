package org.boardgamers.wbc;

import com.kbrohkahn.conventionlibrary.CL_NotificationService;

public class NotificationService extends CL_NotificationService {

  @Override
  protected Class getMainActivityClass() {
    return MainActivity.class;
  }
}
