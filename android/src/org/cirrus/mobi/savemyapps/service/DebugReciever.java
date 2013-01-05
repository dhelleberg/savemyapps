package org.cirrus.mobi.savemyapps.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class DebugReciever extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Toast.makeText(context, action, 2000).show();
		Log.v(">>>>>>", action);
		
	}

}
