package com.example.storein;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

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
		Button mButtonStatus;

		// Fixed Variables
		ArrayList<HashMap<String, String>> friendActivities = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> friendActivity = new HashMap<String, String>();

		protected static final String KEY_FRIEND_ACTIVITY = "KEY_FRIEND_ACTIVITY";

		protected String friendId;

		protected String userId = ParseUser.getCurrentUser().getObjectId();
		protected int countThread = 0;

		boolean isFriendExist;
		boolean isFriend;

		ProgressDialog progressDialog;

		Date yesterday = new Date(System.currentTimeMillis() - 24 * 60 * 60
				* 1000L);

		// Parse Variables
		ParseObject currentUser = ParseObject.createWithoutData(
				ParseConstants.TABLE_USER, userId);
		ParseObject friendObj;

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_friend_detail,
					container, false);

			// Intent Extra
			friendId = getActivity().getIntent().getStringExtra(
					ParseConstants.KEY_OBJECT_ID);

			friendObj = ParseObject.createWithoutData(
					ParseConstants.TABLE_USER, friendId);

			mFriendUsername = (TextView) rootView
					.findViewById(R.id.friendUserName);
			mFriendNumberCheckIn = (TextView) rootView
					.findViewById(R.id.friendNumberCheckIn);
			mFriendNumberFollower = (TextView) rootView
					.findViewById(R.id.friendNumberFollower);
			mFriendNumberFollowing = (TextView) rootView
					.findViewById(R.id.friendNumberFollowing);
			mRecentActivity = (ListView) rootView
					.findViewById(R.id.recentActivity);
			mButtonStatus = (Button) rootView.findViewById(R.id.buttonStatus);

			getFriendAllActivity(); // 2 in 1

			return rootView;
		}

		@Override
		public void onResume() {
			super.onResume();
			clearArrayList();
			getFriendInformation(); // 4 in 1
			checkRelation();
		}

		/*
		 * Clear ArrayList
		 */

		private void clearArrayList() {
			friendActivities.clear();
			friendActivity.clear();
		}

		/*
		 * Progress Dialog init
		 */

		private void initProgressDialog() {
			progressDialog = new ProgressDialog(getActivity());
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setMessage("Loading");
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(false);
			progressDialog.show();
		}

		/*
		 * Check Relation between friend and user
		 */

		private void checkRelation() {
			ParseObject tempFriendObj = ParseObject.createWithoutData(
					ParseConstants.TABLE_USER, friendId);

			ParseQuery<ParseObject> query = ParseQuery
					.getQuery(ParseConstants.TABLE_REL_USER_USER);
			query.whereEqualTo(ParseConstants.KEY_USER_ID, currentUser);
			query.whereEqualTo(ParseConstants.KEY_FOLLOWING_ID, tempFriendObj);
			query.countInBackground(new CountCallback() {

				@Override
				public void done(int total, ParseException e) {
					if (e == null) {
						// success
						if (total != 0) {
							mButtonStatus.setText("Unfollow");
							mButtonStatus.setOnClickListener(unFollowFriend);
						} else {
							mButtonStatus.setText("Follow");
							mButtonStatus.setOnClickListener(followFriend);
						}
					} else {
						// failed
						errorAlertDialog(e);
					}
				}
			});
		}

		/*
		 * Action Listener for follow / unfollow friend
		 */

		OnClickListener followFriend = new OnClickListener() {

			@Override
			public void onClick(View v) {
				ParseObject tempFriendObj = ParseObject.createWithoutData(
						ParseConstants.TABLE_USER, friendId);
				ParseObject object = new ParseObject(
						ParseConstants.TABLE_REL_USER_USER);
				object.put(ParseConstants.KEY_USER_ID, currentUser);
				object.put(ParseConstants.KEY_FOLLOWING_ID, tempFriendObj);
				object.saveInBackground(new SaveCallback() {

					@Override
					public void done(ParseException e) {
						if (e == null) {
							// success
							Toast.makeText(getActivity(),
									"Successful Folowing", Toast.LENGTH_SHORT)
									.show();
							// Reload the page
							onResume();
						} else {
							// error
							errorAlertDialog(e);
						}
					}
				});

			}
		};

		OnClickListener unFollowFriend = new OnClickListener() {

			@Override
			public void onClick(View v) {
				ParseObject tempFriendObj = ParseObject.createWithoutData(
						ParseConstants.TABLE_USER, friendId);
				ParseQuery<ParseObject> query = ParseQuery
						.getQuery(ParseConstants.TABLE_REL_USER_USER);
				query.whereEqualTo(ParseConstants.KEY_USER_ID, currentUser);
				query.whereEqualTo(ParseConstants.KEY_FOLLOWING_ID,
						tempFriendObj);
				query.getFirstInBackground(new GetCallback<ParseObject>() {

					@Override
					public void done(ParseObject object, ParseException e) {
						if (e == null) {
							object.deleteInBackground();
							Toast.makeText(getActivity(), "Unfollow the user",
									Toast.LENGTH_SHORT).show();
							onResume();
						} else {
							errorAlertDialog(e);
						}
					}
				});

			}
		};

		/*
		 * Get FriendInformation Information
		 */

		// Get All

		private void getFriendInformation() {
			getFriendName(); // 4 in 1 method
		}

		// Get the name
		private void getFriendName() {
			// Set progress dialog
			initProgressDialog();

			String tempFriend = friendId;

			ParseQuery<ParseUser> query = ParseQuery
					.getQuery(ParseConstants.TABLE_USER);
			query.whereEqualTo(ParseConstants.KEY_OBJECT_ID, tempFriend);
			query.getFirstInBackground(new GetCallback<ParseUser>() {

				@Override
				public void done(ParseUser friend, ParseException e) {
					if (e == null) {
						// success
						String friendName = friend
								.getString(ParseConstants.KEY_NAME);
						mFriendUsername.setText(friendName);
						countThread += 1;
						// Run the second Query
						getFriendCheckIn();
					} else {
						errorAlertDialog(e);
					}
				}
			});
		}

		// Get number of check in
		private void getFriendCheckIn() {
			ParseObject tempFriend = ParseObject.createWithoutData(
					ParseConstants.TABLE_USER, friendId);
			ParseQuery<ParseObject> query = ParseQuery
					.getQuery(ParseConstants.TABLE_ACTV_USER_CHECK_IN_PLACE);
			query.whereEqualTo(ParseConstants.KEY_USER_ID, tempFriend);
			query.countInBackground(new CountCallback() {

				@Override
				public void done(int total, ParseException e) {
					if (e == null) {
						mFriendNumberCheckIn.setText(total + "");
						countThread += 1;
						// Run the third task
						getNumberFollower();
					} else {
						errorAlertDialog(e);
					}
				}
			});
		}

		// Get number of follower
		private void getNumberFollower() {
			ParseObject tempFriendObj = ParseObject.createWithoutData(
					ParseConstants.TABLE_USER, friendId);
			ParseQuery<ParseObject> query = ParseQuery
					.getQuery(ParseConstants.TABLE_REL_USER_USER);
			query.whereEqualTo(ParseConstants.KEY_USER_ID, tempFriendObj);
			query.countInBackground(new CountCallback() {

				@Override
				public void done(int total, ParseException e) {
					if (e == null) {
						// success
						mFriendNumberFollower.setText(total + "");
						countThread += 1;
						// run the forth task
						getNumberFollowing();
					} else {
						// failed
						errorAlertDialog(e);
					}
				}
			});
		}

		// Get number of following
		private void getNumberFollowing() {
			ParseObject tempFriendObj = ParseObject.createWithoutData(
					ParseConstants.TABLE_USER, friendId);
			ParseQuery<ParseObject> query = ParseQuery
					.getQuery(ParseConstants.TABLE_REL_USER_USER);
			query.whereEqualTo(ParseConstants.KEY_FOLLOWING_ID, tempFriendObj);
			query.countInBackground(new CountCallback() {

				@Override
				public void done(int total, ParseException e) {
					progressDialog.dismiss();
					if (e == null) {
						// success
						mFriendNumberFollowing.setText(total + "");
						countThread += 1;
					} else {
						// failed
						errorAlertDialog(e);
					}
				}
			});
		}

		/*
		 * Get User recent Check In Activity
		 */

		private void getFriendAllActivity() {
			getFriendCheckInActivity(); // 2 in 1 method
		}

		private void getFriendCheckInActivity() {

			ParseQuery<ParseObject> query = ParseQuery
					.getQuery(ParseConstants.TABLE_ACTV_USER_CHECK_IN_PLACE);
			query.whereEqualTo(ParseConstants.KEY_USER_ID, friendObj);
			query.whereGreaterThan(ParseConstants.KEY_CREATED_AT, yesterday);
			query.orderByAscending(ParseConstants.KEY_CREATED_AT);
			query.setLimit(5);
			query.include(ParseConstants.KEY_PLACE_ID);

			query.findInBackground(new FindCallback<ParseObject>() {

				@Override
				public void done(List<ParseObject> friendCheckInActivities,
						ParseException e) {
					if (e == null) {
						// success
						for (ParseObject activity : friendCheckInActivities) {
							// Setup Hash map
							HashMap<String, String> friendActivity = new HashMap<String, String>();

							Date date = activity.getCreatedAt();
							String createAt = date.toString();
							ParseObject tempPlace = activity
									.getParseObject(ParseConstants.KEY_PLACE_ID);

							String placeName = tempPlace
									.getString(ParseConstants.KEY_NAME);

							String message = "Check in at " + placeName
									+ " on " + createAt;
							friendActivity.put(KEY_FRIEND_ACTIVITY, message);
							friendActivities.add(friendActivity);
						}

						// Do the second Finding
						getFriendClaimActivity();
					} else {
						// failed
						errorAlertDialog(e);
					}
				}
			});

		}

		/*
		 * get User Claim Promotion Activity
		 */

		private void getFriendClaimActivity() {
			ParseQuery<ParseObject> query = ParseQuery
					.getQuery(ParseConstants.TABLE_ACTV_USER_CLAIM_PROMOTION);
			query.whereGreaterThan(ParseConstants.KEY_CREATED_AT, yesterday);
			query.whereEqualTo(ParseConstants.KEY_USER_ID, friendObj);
			query.orderByAscending(ParseConstants.KEY_CREATED_AT);
			query.setLimit(5);
			query.include(ParseConstants.KEY_PROMOTION_ID);
			query.findInBackground(new FindCallback<ParseObject>() {

				@Override
				public void done(List<ParseObject> friendClaimAcitivities,
						ParseException e) {
					if (e == null) {
						// success
						for (ParseObject activity : friendClaimAcitivities) {
							// Setup Hash map
							HashMap<String, String> friendActivity = new HashMap<String, String>();

							Date date = activity.getCreatedAt();
							String createAt = date.toString();
							ParseObject tempPromotion = activity
									.getParseObject(ParseConstants.KEY_PROMOTION_ID);

							String promotionName = tempPromotion
									.getString(ParseConstants.KEY_NAME);

							String message = "Claim " + promotionName + " on "
									+ createAt;
							friendActivity.put(KEY_FRIEND_ACTIVITY, message);
							friendActivities.add(friendActivity);
						}
						setAdapter();
					} else {
						// failed
						errorAlertDialog(e);
					}

				}
			});
		}

		/*
		 * Setup adapter
		 */
		public void setAdapter() {

			Collections.shuffle(friendActivities);

			String[] keys = { KEY_FRIEND_ACTIVITY, ParseConstants.KEY_REVIEW };
			int[] ids = { android.R.id.text1, android.R.id.text2 };

			SimpleAdapter adapter = new SimpleAdapter(getActivity(),
					friendActivities, android.R.layout.simple_list_item_2,
					keys, ids);

			mRecentActivity.setAdapter(adapter);
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
