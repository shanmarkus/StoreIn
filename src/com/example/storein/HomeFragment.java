package com.example.storein;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class HomeFragment extends Fragment implements ConnectionCallbacks,
		OnConnectionFailedListener, LocationListener {
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
	Button mButtonRecommend;

	// Fixed Variables
	Date yesterday = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L);
	ArrayList<HashMap<String, String>> userActivities = new ArrayList<HashMap<String, String>>();
	HashMap<String, String> userActivity = new HashMap<String, String>();
	ArrayList<String> promotionsId = new ArrayList<String>();
	private static final double MAX_PlACE_SEARCH_DISTANCE = 10; // 10 Kilometers

	ArrayList<String> recommendationText = new ArrayList<String>();
	ArrayList<String> recommendationId = new ArrayList<String>();

	private String placeId;

	// Parse Constants
	String userId;
	ParseObject currentUser;

	// Location Client
	private LocationClient mLocationClient;
	private LocationManager locationManager;
	private static final LocationRequest REQUEST = LocationRequest.create()
			.setFastestInterval(16) // 16ms = 60fps
			.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	private Location currentLocation;

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

		// Setup Location Client
		mLocationClient = new LocationClient(getActivity(), this, this);

		// Adding Location Manager
		locationManager = (LocationManager) getActivity().getSystemService(
				Context.LOCATION_SERVICE);

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
		mButtonRecommend = (Button) rootView.findViewById(R.id.buttonRecommend);

		mTextRecommendedPromotion = (TextView) rootView
				.findViewById(R.id.textRecommendedPromotion);
		mTextRecommendedPlace = (TextView) rootView
				.findViewById(R.id.textRecommendedPlace);

		setRecommendationUIFalse();

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		// getUserInformation();
		// getUserClaimActivity();
	}

	private void navigateToLogin() {
		Intent intent = new Intent(getActivity(), LoginActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
	}

	/*
	 * Set Recommendation UI to false on the start
	 */

	private void setRecommendationUIFalse() {
		mTextRecommendedPlace.setVisibility(View.INVISIBLE);
		mTextRecommendedPromotion.setVisibility(View.INVISIBLE);

		// Set button become true
		mButtonRecommend.setVisibility(View.VISIBLE);
		mButtonRecommend.setEnabled(true);
	}

	private void setRecommendationUITrue() {
		mTextRecommendedPlace.setVisibility(View.VISIBLE);
		mTextRecommendedPromotion.setVisibility(View.VISIBLE);

		// Set button become false
		mButtonRecommend.setVisibility(View.INVISIBLE);
		mButtonRecommend.setEnabled(false);
	}

	/*
	 * Button Listener
	 */

	OnClickListener buttonRecomend = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// Setup GPS
			if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				Toast.makeText(getActivity(), "GPS is Enabled in your devide",
						Toast.LENGTH_SHORT).show();
			} else {
				showGPSDisabledAlertToUser();
			}
			setRecommendationUIFalse();

			// get user position
			currentLocation = mLocationClient.getLastLocation();
			if (currentLocation != null) {
				getRecomendationPlace();
			}
		}
	};

	/*
	 * Query for finding recommendation
	 */

	private void getRecomendationPlace() {

		ParseGeoPoint location = new ParseGeoPoint(
				currentLocation.getLatitude(), currentLocation.getLongitude());

		// Do the Query
		ParseQuery<ParseObject> query = ParseQuery
				.getQuery(ParseConstants.TABLE_PLACE);
		query.whereWithinKilometers(ParseConstants.KEY_LOCATION, location,
				MAX_PlACE_SEARCH_DISTANCE);
		query.addAscendingOrder(ParseConstants.KEY_TOTAL_CHECK_IN);
		query.getFirstInBackground(new GetCallback<ParseObject>() {

			@Override
			public void done(ParseObject place, ParseException e) {
				if (e == null) {

					String objectId = place.getObjectId();
					placeId = objectId;

					String placeName = place.getString(ParseConstants.KEY_NAME);
					Integer numberCheckIn = place
							.getInt(ParseConstants.KEY_TOTAL_CHECK_IN);
					ParseGeoPoint placeGeoPoint = place
							.getParseGeoPoint(ParseConstants.KEY_LOCATION);
					Location tempPlace = new Location("");
					tempPlace.setLatitude(placeGeoPoint.getLatitude());
					tempPlace.setLongitude(placeGeoPoint.getLongitude());
					// Get the distance
					double tempDistance = currentLocation.distanceTo(tempPlace);

					Integer distance = (int) Math.round(Math.abs(tempDistance));

					String text = "Check Out this "
							+ placeName
							+ " there are "
							+ numberCheckIn
							+ " number of people already check in to this place";
				} else {
					errorAlertDialog(e);
				}
			}
		});
	}

	private void getRecemmendationPromotion() {
		ParseQuery<ParseObject> query = ParseQuery
				.getQuery(ParseConstants.TABLE_REL_PROMOTION_PLACE);
		query.addAscendingOrder(ParseConstants.KEY_TOTAL_CLAIMED);
		query.include(ParseConstants.KEY_PROMOTION_ID);
		query.setLimit(2);
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> promotions, ParseException e) {
				if (e == null) {
					for (ParseObject promotion : promotions) {

						Integer totalPromotionClaim = promotion
								.getInt(ParseConstants.KEY_TOTAL_CLAIMED);

						ParseObject object = promotion
								.getParseObject(ParseConstants.KEY_PROMOTION_ID);
						String objectId = object.getObjectId();
						String objectName = object
								.getString(ParseConstants.KEY_NAME);

						String message = objectName + " is trending, "
								+ totalPromotionClaim + " already claimed";
						recommendationText.add(message);
						recommendationId.add(objectId);
					}
				} else {

				}
			}
		});
	}

	/*
	 * Set Recommendation to textview
	 */

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
	 * Get Recommendation place
	 */

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
					setAdapter();
					mListClaimedPromotion
							.setOnItemClickListener(itemListRecentPromotion);
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

	OnItemClickListener itemListRecentPromotion = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			String objectId = promotionsId.get(position);
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			String title = "Please give this number to claim the reward";
			String message = "Promotion Id " + objectId;
			builder.setTitle(title).setMessage(message)
					.setPositiveButton("Ok", dialogCheckInListener).show();

		}
	};

	/*
	 * Init Alert Dialog
	 */

	DialogInterface.OnClickListener dialogCheckInListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				// Do nothing
				break;
			}
		}
	};

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

	// Check GPS
	private void showGPSDisabledAlertToUser() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				getActivity());
		alertDialogBuilder
				.setMessage(
						"GPS is disabled in your device. Would you like to enable it?")
				.setCancelable(false)
				.setPositiveButton("Goto Settings Page To Enable GPS",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								Intent callGPSSettingIntent = new Intent(
										android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
								startActivity(callGPSSettingIntent);
							}
						});
		alertDialogBuilder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = alertDialogBuilder.create();
		alert.show();
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

	/*
	 * Generated Documentaion
	 * 
	 * @see
	 * com.google.android.gms.location.LocationListener#onLocationChanged(android
	 * .location.Location)
	 */

	private void setUpLocationClientIfNeeded() {
		if (mLocationClient == null) {
			mLocationClient = new LocationClient(getActivity(), this, // ConnectionCallbacks
					this); // OnConnectionFailedListener
		}
	}

	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnected(Bundle arg0) {
		mLocationClient.requestLocationUpdates(REQUEST, this); // LocationListener
		Toast.makeText(getActivity(), "Connected", Toast.LENGTH_SHORT).show();

	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub

	}

}
