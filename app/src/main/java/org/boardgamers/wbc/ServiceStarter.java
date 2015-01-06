package org.boardgamers.wbc;

import com.kbrohkahn.conventionlibrary.CL_ServiceStarter;

public class ServiceStarter extends CL_ServiceStarter {

  @Override
  protected Class getNotificationServiceClass() {
    return NotificationService.class;
  }
}
