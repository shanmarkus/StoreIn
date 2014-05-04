package com.example.storein;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

/**
 * A placeholder fragment containing a simple view.
 */
public class UpdatesFragment extends Fragment {
	private static final String TAG = UpdatesFragment.class.getSimpleName();

	// UI Variables
	ListView mListUpdates;

	// Parse Variables
	String userId = ParseUser.getCurrentUser().getObjectId();
	ParseObject currentUser = ParseObject.createWithoutData(
			ParseConstants.TABLE_USER, userId);

	// Fixed Variables
	ArrayList<String> friendsId = new ArrayList<String>();

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

	/*
	 * added function
	 */

	private void getListFriend() {
		// Clear Array
		friendsId.clear();
		ParseQuery<ParseObject> query = ParseQuery
				.getQuery(ParseConstants.TABLE_REL_USER_USER);
		query.whereEqualTo(ParseConstants.KEY_USER_ID, currentUser);
		query.include(ParseConstants.KEY_FOLLOWING_ID);
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> friends, ParseException e) {
				if (e == null) {
					for (ParseObject obj : friends) {
						ParseObject friend = obj
								.getParseObject(ParseConstants.KEY_FOLLOWING_ID);
						String friendId = friend.getObjectId();
						friendsId.add(friendId);
					}
				} else {
					errorAlertDialog(e);
				}
			}
		});
	}

	/*
	 * Get Recent Check In
	 */

	private void getFriendActivity() {
		ParseQuery<ParseObject> query = ParseQuery
				.getQuery(ParseConstants.TABLE_ACTV_USER_CHECK_IN_PLACE);
		query.whereContainsAll(ParseConstants.KEY_USER_ID, friendsId);
		query.orderByAscending(ParseConstants.KEY_CREATED_AT);
		query.setLimit(10);
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> checkInActivities,
					ParseException e) {
				if (e == null) {

				} else {
					errorAlertDialog(e);
				}
			}
		});
	}

	/*
	 * Error Dialog Parse
	 */
	private void errorAlertDialog(ParseException e) {
		// failed
		Log.e(TAG, e.getMessage());
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(e.getMessage()).setTitle(R.string.error_title)
				.setPositiveButton(android.R.string.ok, null);
		AlertDialog dialog = builder.create();
		dialog.show();
	}
}
