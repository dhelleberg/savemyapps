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
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.cirrus.mobi.savemyapps.data.App;
import org.cirrus.mobi.savemyapps.service.HostCommService;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class AppsListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<List<App>> {

	private static final String TAG = "SMA/AppsListFragment";

	private static PackageManager mPackageManger;

	private AppsListAdapter mAdapter = null;

	public AppsListFragment() {
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPackageManger = getActivity().getPackageManager();
		getLoaderManager().initLoader(0, null, this);

	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {	
		super.onListItemClick(l, v, position, id);
		
		//testing: kick service
		Intent serviceIntent = new Intent(getActivity().getApplicationContext(), HostCommService.class);
		serviceIntent.putExtra(HostCommService.EXTRA_COMMAND, HostCommService.COMMAND_BACKUP_PACKAGE);
		serviceIntent.putExtra(HostCommService.EXTRA_PARAMS, new String[]{mAdapter.appsList.get(position).packageName});
		getActivity().startService(serviceIntent);
	}



	class AppsListAdapter extends BaseAdapter {

		private List<App> appsList = null;
		private LayoutInflater mLayoutInflater;

		public AppsListAdapter(Context context, List<App> appsList) {
			mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.appsList = appsList;
		}

		public void setAppsList(List<App> apps)
		{
			this.appsList  = apps;
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return this.appsList.size();
		}

		@Override
		public Object getItem(int position) {
			return this.appsList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return this.appsList.get(position).hashCode();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View v = convertView;
			App app = this.appsList.get(position);
			if(v == null)
			{
				v = mLayoutInflater.inflate(R.layout.applications_item, null);				
			}

			TextView appName = (TextView) v.findViewById(R.id.app_name);
			ImageView appIcon = (ImageView) v.findViewById(R.id.app_icon);

			appName.setText(app.name);
			appIcon.setImageDrawable(app.icon);


			//TODO: ViewHolder
			return v;
		}



	}



	public static class AppsListLoader extends AsyncTaskLoader<List<App>>
	{
		private List<App> mAppList;

		public AppsListLoader(Context context) {
			super(context);
		}

		@Override
		public List<App> loadInBackground() {			

			List<ApplicationInfo> appInfo = mPackageManger.getInstalledApplications(PackageManager.GET_DISABLED_COMPONENTS);

			List<App> appList = new ArrayList<App>(appInfo.size());

			for (ApplicationInfo applicationInfo : appInfo) {
				if(BuildConfig.DEBUG)
					Log.d(TAG, "Recieved Appinfo: "+applicationInfo.loadLabel(mPackageManger));

				App app = new App();
				app.name = applicationInfo.loadLabel(mPackageManger).toString();
				app.packageName = applicationInfo.packageName;
				app.icon = applicationInfo.loadIcon(mPackageManger);

				appList.add(app);

			}

			Collections.sort(appList, ALPHA_COMPARATOR);

			return appList;
		}
		/**
		 * Handles a request to start the Loader.
		 */
		@Override 
		protected void onStartLoading() {
			if (mAppList != null) {
				// If we currently have a result available, deliver it
				// immediately.
				deliverResult(mAppList);
			}

			// Start watching for changes in the app data.
			/*if (mPackageObserver == null) {
	            mPackageObserver = new PackageIntentReceiver(this);
	        }*/

			// Has something interesting in the configuration changed since we
			// last built the app list?
			//boolean configChange = mLastConfig.applyNewConfig(getContext().getResources());

			if (takeContentChanged() || mAppList == null){ //|| configChange) {
				// If the data has changed since the last time it was loaded
				// or is not currently available, start a load.
				forceLoad();
			}
		}	

	}



	@Override
	public Loader<List<App>> onCreateLoader(int id, Bundle args) {
		if(BuildConfig.DEBUG)
			Log.v(TAG, "create new loader");
		return new AppsListLoader(getActivity());
	}

	@Override
	public void onLoadFinished(Loader<List<App>> loader, List<App> apps) {
		if(this.mAdapter == null)
		{
			this.mAdapter = new AppsListAdapter(getActivity(), apps);
			setListAdapter(mAdapter);
		}
		else
		{
			this.mAdapter.setAppsList(apps);
		}					
	}

	@Override
	public void onLoaderReset(Loader<List<App>> loader) {
		// TODO Auto-generated method stub

	}


	/**
	 * Perform alphabetical comparison of application entry objects.
	 */
	public static final Comparator<App> ALPHA_COMPARATOR = new Comparator<App>() {
		private final Collator sCollator = Collator.getInstance();
		@Override
		public int compare(App object1, App object2) {
			return sCollator.compare(object1.name, object2.name);
		}
	};


}


