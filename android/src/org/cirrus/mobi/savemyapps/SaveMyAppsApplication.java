package org.cirrus.mobi.savemyapps;

import org.cirrus.mobi.savemyapps.service.HostCommService;

import android.app.Application;
import android.content.Intent;

public class SaveMyAppsApplication extends Application {
	
	@Override
	public void onCreate() {	
		super.onCreate();
		//kick service to make sure it's running an listening for USB changes and / or creates a socket
		//testing: kick service
		Intent serviceIntent = new Intent(this, HostCommService.class);
		serviceIntent.putExtra(HostCommService.EXTRA_COMMAND, HostCommService.COMMAND_CONNECT);
		startService(serviceIntent);

	}

}
