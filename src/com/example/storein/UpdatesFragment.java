package com.example.storein;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * A placeholder fragment containing a simple view.
 */
public class UpdatesFragment extends Fragment {

	// UI Variables
	ListView mListUpdates;

	public UpdatesFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_updates_fragement,
				container, false);
		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();

	}

}
