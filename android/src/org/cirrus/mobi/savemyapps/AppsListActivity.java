package org.cirrus.mobi.savemyapps;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;

public class AppsListActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_apps_list);
		
		// savedInstanceState is non-null when there is fragment state
		// saved from previous configurations of this activity
		// (e.g. when rotating the screen from portrait to landscape).
		// In this case, the fragment will automatically be re-added
		// to its container so we don't need to manually add it.
		// For more information, see the Fragments API guide at:
		//
		// http://developer.android.com/guide/components/fragments.html
		//
		if (savedInstanceState == null) {
			// Create the detail fragment and add it to the activity
			// using a fragment transaction.
			AppsListFragment fragment = new AppsListFragment();
			//fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.add(R.id.apps_list_container, fragment).commit();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_apps_list, menu);
		return true;
	}

}
