package com.example.storein;

import com.parse.CountCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

public class AddFriend extends ActionBarActivity {

	private static final String TAG = AddFriend.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_friend);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.add_friend, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public static class PlaceholderFragment extends Fragment {

		// Fixed variables
		boolean isFriendExist;
		boolean isAlreadyFollow;
		String userId = ParseUser.getCurrentUser().getObjectId();

		// Intent Extra
		String friendId = getActivity().getIntent().getStringExtra(
				ParseConstants.KEY_OBJECT_ID);

		// ParseConstant
		ParseObject currentUser = ParseObject.createWithoutData(
				ParseConstants.TABLE_USER, userId);
		ParseObject friendObj;

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_add_friend,
					container, false);
			return rootView;
		}

		/*
		 * Check for friendId
		 */

		private Boolean checkFriendId() {
			if (friendId == userId) {
				isFriendExist = false;
			} else {
				ParseQuery<ParseUser> query = ParseUser.getQuery();
				query.whereEqualTo(ParseConstants.KEY_OBJECT_ID, friendId);
				query.countInBackground(new CountCallback() {

					@Override
					public void done(int count, ParseException e) {
						if (e == null) {
							if (count == 0) {
								friendObj = ParseObject.createWithoutData(
										ParseConstants.TABLE_USER, friendId);
								isFriendExist = false;
							} else {
								isFriendExist = true;
							}
						} else {
							errorAlertDialog(e);
						}
					}
				});
			}
			return isFriendExist;
		}

		/*
		 * Check whether this user already follow the checked user
		 */
		
		private Boolean isAlreadyFollow() {

			ParseQuery<ParseObject> query = ParseQuery
					.getQuery(ParseConstants.TABLE_REL_USER_USER);
			query.whereEqualTo(ParseConstants.KEY_USER_ID, currentUser);
			query.whereEqualTo(ParseConstants.KEY_FOLLOWING_ID, friendObj);
			query.countInBackground(new CountCallback() {

				@Override
				public void done(int total, ParseException e) {
					if (e == null) {
						if (total != 0) {
							isAlreadyFollow = true;
						} else {
							isAlreadyFollow = false;
						}
					} else {
						errorAlertDialog(e);
					}
				}
			});
			return isAlreadyFollow();
		}
		
		/*
		 * Add Function
		 */
		
		

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

}
