package com.example.storein;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.google.android.gms.internal.er;
import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FriendDetail extends ActionBarActivity {

	private static final String TAG = FriendDetail.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_friend_detail);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.friend_detail, menu);
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

		// UI Variables
		TextView mFriendUsername;
		TextView mFriendNumberCheckIn;
		TextView mFriendNumberFollower;
		TextView mFriendNumberFollowing;
		ListView mRecentActivity;

		// Fixed Variables
		ArrayList<HashMap<String, String>> friendsInfo = new ArrayList<HashMap<String, String>>();
		protected ArrayList<String> friendIds = new ArrayList<String>();
		HashMap<String, String> friendInfo = new HashMap<String, String>();

		protected String friendId;

		protected String userId = ParseUser.getCurrentUser().getObjectId();
		protected int countThread = 0;

		boolean isFriendExist;
		boolean isFriend;

		// Parse Variables
		ParseObject currentUser = ParseObject.createWithoutData(
				ParseConstants.TABLE_USER, userId);
		ParseObject friendObj = ParseObject.createWithoutData(
				ParseConstants.TABLE_USER, friendId);

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_friend_detail,
					container, false);
			friendId = getActivity().getIntent().getStringExtra(
					ParseConstants.KEY_OBJECT_ID);
			return rootView;
		}

		@Override
		public void onResume() {
			super.onResume();
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
		 * Check Relation between friend and user
		 */

		private Boolean checkRelation() {
			ParseQuery<ParseObject> query = ParseQuery
					.getQuery(ParseConstants.TABLE_REL_USER_USER);
			query.whereEqualTo(ParseConstants.KEY_USER_ID, currentUser);
			query.whereEqualTo(ParseConstants.KEY_FOLLOWING_ID, friendObj);
			query.countInBackground(new CountCallback() {

				@Override
				public void done(int total, ParseException e) {
					if (e == null) {
						// success
						if (total != 0) {
							isFriend = true;
						} else {
							isFriend = false;
						}
					} else {
						// failed
						errorAlertDialog(e);
					}
				}
			});
			return isFriend;
		}

		/*
		 * Get FriendInformation Information
		 */

		// Get the name
		private void getFriendName() {

			ParseQuery<ParseUser> query = ParseQuery
					.getQuery(ParseConstants.TABLE_USER);
			query.whereEqualTo(ParseConstants.KEY_OBJECT_ID, friendId);
			query.getFirstInBackground(new GetCallback<ParseUser>() {

				@Override
				public void done(ParseUser friend, ParseException e) {
					if (e == null) {
						// success
						String friendName = friend
								.getString(ParseConstants.KEY_NAME);
						mFriendUsername.setText(friendName);
						countThread += 1;
					} else {
						errorAlertDialog(e);
					}
				}
			});
		}

		// Get number of check in
		private void getFriendCheckIn() {

			ParseQuery<ParseObject> query = ParseQuery
					.getQuery(ParseConstants.TABLE_ACTV_USER_CHECK_IN_PLACE);
			query.whereEqualTo(ParseConstants.KEY_USER_ID, friendId);
			query.countInBackground(new CountCallback() {

				@Override
				public void done(int total, ParseException e) {
					if (e == null) {
						mFriendNumberCheckIn.setText(total + "");
						countThread += 1;
					} else {
						errorAlertDialog(e);
					}
				}
			});
		}

		// Get number of follower
		private void getNumberFollower() {
			ParseQuery<ParseObject> query = ParseQuery
					.getQuery(ParseConstants.TABLE_REL_USER_USER);
			query.whereEqualTo(ParseConstants.KEY_FOLLOWING_ID, friendId);
			query.countInBackground(new CountCallback() {

				@Override
				public void done(int total, ParseException e) {
					if (e == null) {
						// success
						mFriendNumberFollower.setText(total + "");
						countThread += 1;
					} else {
						// failed
						errorAlertDialog(e);
					}
				}
			});
		}

		// Get number of following
		private void getNumberFollowing() {
			ParseQuery<ParseObject> query = ParseQuery
					.getQuery(ParseConstants.TABLE_REL_USER_USER);
			query.whereEqualTo(ParseConstants.KEY_USER_ID, friendId);
			query.countInBackground(new CountCallback() {

				@Override
				public void done(int total, ParseException e) {
					if (e == null) {
						// success
						mFriendNumberFollower.setText(total + "");
						countThread += 1;
					} else {
						// failed
						errorAlertDialog(e);
					}
				}
			});
		}

		/*
		 * Get User recent Activity
		 */

		private void getUserCheckInActivity() {
			Date yesterday = new Date(System.currentTimeMillis() - 24 * 60 * 60
					* 1000L);

			ParseQuery<ParseObject> query = ParseQuery
					.getQuery(ParseConstants.TABLE_ACTV_USER_CHECK_IN_PLACE);
			query.whereGreaterThan(ParseConstants.KEY_CREATED_AT, yesterday);
			query.orderByAscending(ParseConstants.KEY_CREATED_AT);
			query.setLimit(5);
			query.findInBackground(new FindCallback<ParseObject>() {

				@Override
				public void done(List<ParseObject> friendCheckInActivities, ParseException e) {
					if(e == null){
						// 
					}else{
						// failed
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

}
