package com.example.storein;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

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
	TextView mHomeTextClaimedPromotion;
	TextView mHomeTextRewardPoints;
	ImageButton mHomeStashButton;

	// Fixed Variables
	Date yesterday = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L);
	ArrayList<HashMap<String, String>> userActivities = new ArrayList<HashMap<String, String>>();
	HashMap<String, String> userActivity = new HashMap<String, String>();
	ArrayList<String> promotionsId = new ArrayList<String>();

	// Parse Constants
	String userId;
	ParseObject currentUser;

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

		if (ParseUser.getCurrentUser() == null) {
			navigateToLogin();
		} else {
			userId = ParseUser.getCurrentUser().getObjectId();
			currentUser = ParseUser.createWithoutData(
					ParseConstants.TABLE_USER, userId);
		}

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
		mHomeTextClaimedPromotion = (TextView) rootView
				.findViewById(R.id.homeTextClaimedPromotion);
		mHomeTextRewardPoints = (TextView) rootView
				.findViewById(R.id.homeTextRewardPoints);
		mHomeStashButton = (ImageButton) rootView
				.findViewById(R.id.homeStashButton);

		mHomeStashButton.setOnClickListener(stashListener);

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		getUserInformation();
	}

	/*
	 * Navigate to Login
	 */
	private void navigateToLogin() {
		Intent intent = new Intent(getActivity(), LoginActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
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
	 * Get User Information include number of check in, follower, following
	 */

	// Get All

	private void getUserInformation() {
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
					Integer userFollower = user
							.getInt(ParseConstants.KEY_FOLLOWER);
					Integer userFollowing = user
							.getInt(ParseConstants.KEY_FOLLOWING);
					Integer userTotalCheckIn = user
							.getInt(ParseConstants.KEY_TOTAL_CHECK_IN);
					Integer userTotalClaimedPromotion = user
							.getInt(ParseConstants.KEY_TOTAL_CLAIMED_PROMOTION);

					mHomeUserName.setText(userName);
					mHomeNumberFollower.setText(userFollower + "");
					mHomeNumberFollowing.setText(userFollowing + "");
					mHomeNumberCheckIn.setText(userTotalCheckIn + "");
					mHomeTextClaimedPromotion.setText(userTotalClaimedPromotion
							+ "");
					getTotalReward(userId);
				} else {
					progressDialog.dismiss();
					errorAlertDialog(e);
				}
			}
		});
	}

	/*
	 * Get Total Reward Point
	 */

	private void getTotalReward(String userId) {

		ParseObject currentUser = ParseObject.createWithoutData(
				ParseConstants.TABLE_USER, userId);

		ParseQuery<ParseObject> query = ParseQuery
				.getQuery(ParseConstants.TABLE_REL_USER_REWARD);
		query.whereEqualTo(ParseConstants.KEY_USER_ID, currentUser);
		query.getFirstInBackground(new GetCallback<ParseObject>() {

			@Override
			public void done(ParseObject relUserReward, ParseException e) {
				if (e == null) {
					progressDialog.dismiss();
					Integer reward = relUserReward
							.getInt(ParseConstants.KEY_REWARD_POINT);
					mHomeTextRewardPoints.setText(reward + "");
				} else {
					progressDialog.dismiss();
					errorAlertDialog(e);
				}
			}
		});
	}

	/*
	 * Onclick Treasure Listener
	 */

	OnClickListener stashListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(getActivity(), Stash.class);
			startActivity(intent);
		}
	};

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
