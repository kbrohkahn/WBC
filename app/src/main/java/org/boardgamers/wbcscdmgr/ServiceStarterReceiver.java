package org.boardgamers.wbcscdmgr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ServiceStarterReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		String action;
		if ((action = arg1.getAction()) != null && action.equals(Intent.ACTION_BOOT_COMPLETED)) {
			Helpers.scheduleAlarms(arg0);
		}
	}
}
