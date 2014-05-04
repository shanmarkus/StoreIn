package com.example.storein;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.example.storein.adapter.CustomArrayAdapterActivity;
import com.example.storein.model.UserActivity;
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
	public List<UserActivity> userActivityList = new ArrayList<UserActivity>();
	public ArrayList<UserActivity> userActivityRecord;
	private CustomArrayAdapterActivity mUserActivityAdapter;

	ProgressDialog progressDialog;

	public UpdatesFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_updates_fragement,
				container, false);

		mListUpdates = (ListView) rootView.findViewById(R.id.listUpdates);
		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		doUpdatesQuery();
	}

	/*
	 * added function
	 */

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
	 * 3 in 1 function It will run getListFriend then getFriendClaimActivity
	 * then get FriendCheckIn
	 */
	private void doUpdatesQuery() {
		getListFriend();
	}

	/*
	 * get list of friends
	 */

	private void getListFriend() {
		// Start progress dialog
		initProgressDialog();
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
					getFriendClaimActivity();
				} else {
					errorAlertDialog(e);
				}
			}
		});
	}

	/*
	 * get friends claim activity
	 */

	private void getFriendClaimActivity() {
		for (String id : friendsId) {
			ParseObject objFriend = ParseObject.createWithoutData(
					ParseConstants.TABLE_USER, id);

			ParseQuery<ParseObject> query = ParseQuery
					.getQuery(ParseConstants.TABLE_ACTV_USER_CLAIM_PROMOTION);
			query.whereEqualTo(ParseConstants.KEY_USER_ID, objFriend);
			query.orderByAscending(ParseConstants.KEY_CREATED_AT);
			query.setLimit(2);
			query.include(ParseConstants.KEY_PROMOTION_ID);
			query.include(ParseConstants.KEY_USER_ID);
			query.findInBackground(new FindCallback<ParseObject>() {

				@Override
				public void done(List<ParseObject> claimAcitivities,
						ParseException e) {
					if (e == null) {
						for (ParseObject claimActivity : claimAcitivities) {
							ParseObject userObj = claimActivity
									.getParseObject(ParseConstants.KEY_USER_ID);
							ParseObject promotionObj = claimActivity
									.getParseObject(ParseConstants.KEY_PROMOTION_ID);

							String tempUserName = userObj
									.getString(ParseConstants.KEY_USERNAME);
							String tempPromotionName = promotionObj
									.getString(ParseConstants.KEY_NAME);
							String tempObjId = claimActivity.getObjectId();
							Date date = claimActivity.getCreatedAt();

							UserActivity tempActivity = new UserActivity();
							tempActivity.setObjectId(tempObjId);
							tempActivity.setuserName(tempUserName);
							tempActivity.setobjectName(tempPromotionName);
							tempActivity.setType("claim");
							tempActivity.setCreatedAt(date);

							userActivityList.add(tempActivity);
						}
					} else {
						errorAlertDialog(e);
					}
				}
			});
		}
		getFriendCheckInActivity();
	}

	/*
	 * Get friends recent Check In
	 */

	private void getFriendCheckInActivity() {
		for (String id : friendsId) {
			ParseObject objFriend = ParseObject.createWithoutData(
					ParseConstants.TABLE_USER, id);

			ParseQuery<ParseObject> query = ParseQuery
					.getQuery(ParseConstants.TABLE_ACTV_USER_CHECK_IN_PLACE);
			query.whereEqualTo(ParseConstants.KEY_USER_ID, objFriend);
			query.orderByAscending(ParseConstants.KEY_CREATED_AT);
			query.setLimit(2);
			query.include(ParseConstants.KEY_PLACE_ID);
			query.include(ParseConstants.KEY_USER_ID);
			query.findInBackground(new FindCallback<ParseObject>() {

				@Override
				public void done(List<ParseObject> checkInActivities,
						ParseException e) {
					if (e == null) {
						for (ParseObject checkInActivity : checkInActivities) {
							// Get 2 object user and place
							ParseObject objActivity = checkInActivity
									.getParseObject(ParseConstants.KEY_PLACE_ID);
							ParseObject objUser = checkInActivity
									.getParseObject(ParseConstants.KEY_USER_ID);

							String activityId = checkInActivity.getObjectId();
							String tempUserName = objUser
									.getString(ParseConstants.KEY_USERNAME);
							String tempPlaceName = objActivity
									.getString(ParseConstants.KEY_NAME);
							Date date = checkInActivity.getCreatedAt();

							UserActivity tempActivity = new UserActivity();
							tempActivity.setObjectId(activityId);
							tempActivity.setuserName(tempUserName);
							tempActivity.setobjectName(tempPlaceName);
							tempActivity.setCreatedAt(date);
							tempActivity.setType("checkIn");

							userActivityList.add(tempActivity);
						}
						setAdapter();
					} else {
						errorAlertDialog(e);
					}
				}
			});
		}
	}

	/*
	 * Set Adapter
	 */

	private void setAdapter() {
		// dismiss the progress dialog
		progressDialog.dismiss();

		Collections.shuffle(userActivityList);
		userActivityRecord = (ArrayList<UserActivity>) userActivityList;
		mUserActivityAdapter = new CustomArrayAdapterActivity(getActivity(),
				R.id.listPromotion, userActivityRecord);
		mListUpdates = (ListView) getActivity().findViewById(R.id.listUpdates);
		mListUpdates.setAdapter(mUserActivityAdapter);
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
