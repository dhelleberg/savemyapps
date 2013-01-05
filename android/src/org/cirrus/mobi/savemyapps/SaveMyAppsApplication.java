package org.cirrus.mobi.savemyapps;
/**
 *	 This file is part of SaveMyApps
 *
 *   SaveMyApps is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SaveMyApps is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with SaveMyApps.  If not, see <http://www.gnu.org/licenses/>.
 */
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
