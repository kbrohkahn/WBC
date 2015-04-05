package org.boardgamers.wbc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ServiceStarter extends BroadcastReceiver {

  @Override
  public void onReceive(Context arg0, Intent arg1) {
    Intent intent=new Intent(arg0, NotificationService.class);
    arg0.startService(intent);
  }

}
