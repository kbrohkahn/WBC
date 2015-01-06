package org.boardgamers.wbc;

import com.kbrohkahn.conventionlibrary.CL_SettingsActivity;

public class SettingsActivity extends CL_SettingsActivity {

  @Override
  protected Class getNotificationServiceClass() {
    return NotificationService.class;
  }
}
