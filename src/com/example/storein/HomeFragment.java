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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class HomeFragment extends Fragment {
	protected static final String TAG = HomeFragment.class.getSimpleName();
	ProgressDialog progressDialog;

	// UI Variables
	TextView mHomeUserName;
	TextView mHomeNumberCheckIn;
	TextView mHomeNumberFollower;
	TextView mHomeNumberFollowing;
	TextView mTextRecommendedPlace;
	TextView mTextRecommendedPromotion;
	ListView mListClaimedPromotion;

	// Fixed Variables
	Date yesterday = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L);
	ArrayList<HashMap<String, String>> userActivities = new ArrayList<HashMap<String, String>>();
	HashMap<String, String> userActivity = new HashMap<String, String>();
	ArrayList<String> promotionsId = new ArrayList<String>();

	// Parse Constants
	String userId = ParseUser.getCurrentUser().getObjectId();
	ParseObject currentUser = ParseUser.createWithoutData(
			ParseConstants.TABLE_USER, userId);

	public static HomeFragment newInstance(String param1, String param2) {
		HomeFragment fragment = new HomeFragment();
		Bundle args = new Bundle();

		fragment.setArguments(args);
		return fragment;
	}

	public HomeFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {

		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_home, container,
				false);

		// UI Declaration
		mHomeNumberCheckIn = (TextView) rootView
				.findViewById(R.id.homeNumberCheckIn);
		mHomeNumberFollower = (TextView) rootView
				.findViewById(R.id.homeNumberFollower);
		mHomeNumberFollowing = (TextView) rootView
				.findViewById(R.id.homeNumberFollowing);
		mHomeNumberCheckIn = (TextView) rootView
				.findViewById(R.id.homeNumberCheckIn);
		mHomeUserName = (TextView) rootView.findViewById(R.id.homeUserName);
		mListClaimedPromotion = (ListView) rootView
				.findViewById(R.id.listClaimedPromotion);
		mTextRecommendedPromotion = (TextView) rootView
				.findViewById(R.id.textRecommendedPromotion);
		mTextRecommendedPlace = (TextView) rootView
				.findViewById(R.id.textRecommendedPlace);

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		getUserInformation();
	}

	/*
	 * Progress Dialog initiate
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
	 * Get Recent Claim Promotion
	 */

	private void getUserClaimActivity() {
		ParseQuery<ParseObject> query = ParseQuery
				.getQuery(ParseConstants.TABLE_ACTV_USER_CLAIM_PROMOTION);
		query.whereGreaterThan(ParseConstants.KEY_CREATED_AT, yesterday);
		query.whereEqualTo(ParseConstants.KEY_USER_ID, currentUser);
		query.orderByAscending(ParseConstants.KEY_CREATED_AT);
		query.orderByAscending(ParseConstants.KEY_CLAIMABLE);
		query.include(ParseConstants.KEY_PROMOTION_ID);
		query.include(ParseConstants.KEY_PLACE_ID);
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> userClaimAcitivities,
					ParseException e) {
				if (e == null) {
					// success
					for (ParseObject activity : userClaimAcitivities) {
						// Setup Hash map
						HashMap<String, String> friendActivity = new HashMap<String, String>();

						String promotionId = activity.getObjectId();

						ParseObject tempPromotion = activity
								.getParseObject(ParseConstants.KEY_PROMOTION_ID);
						ParseObject tempPlace = activity
								.getParseObject(ParseConstants.KEY_PLACE_ID);

						String promotionName = tempPromotion
								.getString(ParseConstants.KEY_NAME);
						String promotionPlace = tempPlace
								.getString(ParseConstants.KEY_NAME);

						userActivity
								.put(ParseConstants.KEY_NAME, promotionName);
						userActivity.put(ParseConstants.KEY_LOCATION,
								promotionPlace);
						userActivities.add(friendActivity);
						promotionsId.add(promotionId);
					}
					// setAdapter();
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

		String[] keys = { ParseConstants.KEY_NAME, ParseConstants.KEY_LOCATION };
		int[] ids = { android.R.id.text1, android.R.id.text2 };

		SimpleAdapter adapter = new SimpleAdapter(getActivity(),
				userActivities, android.R.layout.simple_list_item_2, keys, ids);

		mListClaimedPromotion.setAdapter(adapter);
	}
	
	/*
	 * On Click Listener
	 */

	/*
	 * Get User Information include number of check in, follower, following
	 */

	// Get All

	private void getUserInformation() {
		getFriendName(); // 4 in 1 method
	}

	// Get the name
	private void getFriendName() {
		// Set progress dialog
		initProgressDialog();

		ParseQuery<ParseUser> query = ParseQuery
				.getQuery(ParseConstants.TABLE_USER);
		query.whereEqualTo(ParseConstants.KEY_OBJECT_ID, userId);
		query.getFirstInBackground(new GetCallback<ParseUser>() {

			@Override
			public void done(ParseUser user, ParseException e) {
				if (e == null) {
					// success
					String userName = user.getString(ParseConstants.KEY_NAME);
					mHomeUserName.setText(userName);
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
		// ParseObject tempFriend = ParseObject.createWithoutData(
		// ParseConstants.TABLE_USER, friendId);

		ParseQuery<ParseObject> query = ParseQuery
				.getQuery(ParseConstants.TABLE_ACTV_USER_CHECK_IN_PLACE);
		query.whereEqualTo(ParseConstants.KEY_USER_ID, currentUser);
		query.countInBackground(new CountCallback() {

			@Override
			public void done(int total, ParseException e) {
				if (e == null) {
					mHomeNumberCheckIn.setText(total + "");
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
		// ParseObject tempFriendObj = ParseObject.createWithoutData(
		// ParseConstants.TABLE_USER, friendId);

		ParseQuery<ParseObject> query = ParseQuery
				.getQuery(ParseConstants.TABLE_REL_USER_USER);
		query.whereEqualTo(ParseConstants.KEY_USER_ID, currentUser);
		query.countInBackground(new CountCallback() {

			@Override
			public void done(int total, ParseException e) {
				if (e == null) {
					// success
					mHomeNumberFollower.setText(total + "");
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
		// ParseObject tempFriendObj = ParseObject.createWithoutData(
		// ParseConstants.TABLE_USER, friendId);

		ParseQuery<ParseObject> query = ParseQuery
				.getQuery(ParseConstants.TABLE_REL_USER_USER);
		query.whereEqualTo(ParseConstants.KEY_FOLLOWING_ID, currentUser);
		query.countInBackground(new CountCallback() {
			@Override
			public void done(int total, ParseException e) {
				progressDialog.dismiss();
				if (e == null) {
					// success
					mHomeNumberFollowing.setText(total + "");
				} else {
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
