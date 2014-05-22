package com.example.storein;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class DiscoverFragment extends Fragment implements ConnectionCallbacks,
		OnConnectionFailedListener, LocationListener {

	protected final static String TAG = DiscoverFragment.class.getSimpleName()
			.toString();

	// UI Variable
	ImageButton mDiscoverButtonCheckIn;
	ImageButton mDiscoverButtonBrowse;
	ImageButton mDiscoverButtonRecommendation;

	// Variables
	ArrayList<HashMap<String, String>> placesInfo = new ArrayList<HashMap<String, String>>();
	protected ArrayList<String> placesID = new ArrayList<String>();
	HashMap<String, String> placeInfo = new HashMap<String, String>();
	ProgressDialog progressDialog;

	private String placeId;

	// Place Constant
	private Location currentLocation = null;

	// Map Constant
	private GoogleMap mMap;
	private LocationClient mLocationClient;
	private SupportMapFragment fragment;

	// Parse Constants

	private static final int MAX_PLACE_SEARCH_RESULTS = 20;
	private static final int MAX_PlACE_SEARCH_DISTANCE = 10; // In KiloMeters

	private static final LocationRequest REQUEST = LocationRequest.create()
			.setFastestInterval(1000) // 16ms = 60fps
			.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

	public static DiscoverFragment newInstance(String param1, String param2) {
		DiscoverFragment fragment = new DiscoverFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	public DiscoverFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mMap == null) {
			mMap = fragment.getMap();
			mMap.setMyLocationEnabled(true);
		}
		mMap.clear();
		setUpLocationClientIfNeeded();
		mLocationClient.connect();
		onClickListener();

	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().setProgressBarIndeterminateVisibility(false);
		if (mLocationClient != null) {
			mLocationClient.disconnect();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_discover, container,
				false);

		// Declare UI
		mDiscoverButtonBrowse = (ImageButton) view
				.findViewById(R.id.discoverButtonBrowse);
		mDiscoverButtonCheckIn = (ImageButton) view
				.findViewById(R.id.discoverButtonCheckIn);
		mDiscoverButtonRecommendation = (ImageButton) view
				.findViewById(R.id.discoverButtonReccomendation);

		// Adding Location Manager
		LocationManager locationManager = (LocationManager) getActivity()
				.getSystemService(Context.LOCATION_SERVICE);

		// Setup GPS
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			Toast.makeText(getActivity(), "GPS is Enabled in your devide",
					Toast.LENGTH_SHORT).show();
		} else {
			showGPSDisabledAlertToUser();
		}

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		FragmentManager fm = getChildFragmentManager();
		fragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
		if (fragment == null) {
			fragment = SupportMapFragment.newInstance();
			fm.beginTransaction().replace(R.id.map, fragment).commit();
		}
	}

	/*
	 * Function Added
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
	 * On Click Listener
	 */

	private void onClickListener() {
		mDiscoverButtonBrowse.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), BrowseActivity.class);
				startActivity(intent);
			}
		});

		mDiscoverButtonCheckIn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), CheckInActivity.class);
				startActivity(intent);
			}
		});

		mDiscoverButtonRecommendation.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				initProgressDialog();
				Random random = new Random();
				Integer rand = random.nextInt(2);
				Toast.makeText(getActivity(), rand + "", Toast.LENGTH_SHORT)
						.show();
				if (rand == 0) {
					getRecomendationPlace();
				} else {
					if (placesID.size() != 0) {
						rand = random.nextInt(placesID.size());
						placeId = placesID.get(rand);
						getRecemmendationPromotion(placeId);
					} else {
						getRecomendationPlace();
					}
				}
			}
		});
	}

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
		query.orderByDescending(ParseConstants.KEY_TOTAL_CHECK_IN);
		query.getFirstInBackground(new GetCallback<ParseObject>() {

			@Override
			public void done(ParseObject place, ParseException e) {
				if (e == null) {
					progressDialog.dismiss();

					placeId = place.getObjectId();

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

					AlertDialog.Builder builder = new AlertDialog.Builder(
							getActivity());
					builder.setMessage(text)
							.setPositiveButton("Check In",
									dialogRecomendationListener)
							.setNegativeButton("Cancel",
									dialogRecomendationListener).show();

				} else {
					progressDialog.dismiss();
					errorAlertDialog(e);
				}
			}
		});
	}

	private void getRecemmendationPromotion(String targetPlaceId) {

		ParseObject currentPlace = ParseObject.createWithoutData(
				ParseConstants.TABLE_PLACE, targetPlaceId);
		ParseQuery<ParseObject> query = ParseQuery
				.getQuery(ParseConstants.TABLE_REL_PROMOTION_PLACE);
		query.whereEqualTo(ParseConstants.KEY_PLACE_ID, currentPlace);
		query.orderByDescending(ParseConstants.KEY_TOTAL_CLAIMED);
		query.include(ParseConstants.KEY_PROMOTION_ID);
		query.include(ParseConstants.KEY_PLACE_ID);
		query.getFirstInBackground(new GetCallback<ParseObject>() {

			@Override
			public void done(ParseObject promotion, ParseException e) {
				if (e == null) {
					progressDialog.dismiss();
					Integer totalPromotionClaim = promotion
							.getInt(ParseConstants.KEY_TOTAL_CLAIMED);

					ParseObject object = promotion
							.getParseObject(ParseConstants.KEY_PROMOTION_ID);
					ParseObject targetPlace = promotion
							.getParseObject(ParseConstants.KEY_PLACE_ID);

					// get the place Id
					placeId = targetPlace.getObjectId();
					String objectName = object
							.getString(ParseConstants.KEY_NAME);

					String message = objectName + " is trending, "
							+ totalPromotionClaim
							+ " already claimed, Hurry and "
							+ "go for Check In to grab this promotion";

					AlertDialog.Builder builder = new AlertDialog.Builder(
							getActivity());
					builder.setMessage(message)
							.setPositiveButton("Check In",
									dialogRecomendationListener)
							.setNegativeButton("Cancel",
									dialogRecomendationListener).show();
				} else {
					errorAlertDialog(e);
					progressDialog.dismiss();
				}
			}
		});
	}

	private void doLocationQuery() {
		getActivity().setProgressBarIndeterminateVisibility(true);

		final ParseGeoPoint location = new ParseGeoPoint(
				currentLocation.getLatitude(), currentLocation.getLongitude());

		// Do the Query
		ParseObject.registerSubclass(ParsePlace.class);
		ParseQuery<ParsePlace> query = ParsePlace.getQuery();
		query.whereWithinKilometers(ParseConstants.KEY_LOCATION, location,
				MAX_PlACE_SEARCH_DISTANCE);
		query.orderByAscending(ParseConstants.KEY_NAME);
		query.setLimit(MAX_PLACE_SEARCH_RESULTS);
		query.findInBackground(new FindCallback<ParsePlace>() {

			@Override
			public void done(List<ParsePlace> places, ParseException e) {
				if (e == null) {
					getActivity().setProgressBarIndeterminateVisibility(false);
					// success
					if (places.size() > 0) {
						for (ParsePlace place : places) {
							String objectId = place.getObjectId();
							String name = place.getName();
							ParseGeoPoint geoPoint = place
									.getParseGeoPoint(ParseConstants.KEY_LOCATION);

							// Adding to ArrayList
							placesID.add(objectId);
							// Adding Marker
							mMap.addMarker(new MarkerOptions().position(
									new LatLng(geoPoint.getLatitude(), geoPoint
											.getLongitude())).title(name));

						}
					} else {
						getActivity().setProgressBarIndeterminateVisibility(
								false);
						String message = "Sorry there are no promotion near you, please use browse to find other promotion";
						Toast.makeText(getActivity(), message,
								Toast.LENGTH_LONG).show();
					}
					// Save user last location
					saveUserLocation(location);
				} else {
					errorAlertDialog(e);
				}

			}
		});

	}

	/*
	 * Saving User Location
	 */

	private void saveUserLocation(final ParseGeoPoint userGeoPoint) {
		ParseQuery<ParseUser> query = ParseUser.getQuery();
		query.getInBackground(ParseUser.getCurrentUser().getObjectId(),
				new GetCallback<ParseUser>() {

					@Override
					public void done(ParseUser user, ParseException e) {
						if (e == null) {
							user.put(ParseConstants.KEY_LOCATION, userGeoPoint);
							user.saveEventually();
						} else {
							errorAlertDialog(e);
						}
					}
				});
	}

	private void initFindPlace() {
		currentLocation = mLocationClient.getLastLocation();
		if (currentLocation != null) {
			// Adding Maps
			mMap.addCircle(new CircleOptions()
					.center(new LatLng(currentLocation.getLatitude(),
							currentLocation.getLongitude())).radius(100)
					.strokeColor(Color.RED));

			// Do the Query
			doLocationQuery();
		}

	}

	/*
	 * Error Dialog
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
	 * Check GPS Function
	 */

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
	 * Alert Dialog For Check In
	 */

	DialogInterface.OnClickListener dialogRecomendationListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				saveUserActivity(placeId);
				break;

			case DialogInterface.BUTTON_NEGATIVE:
				// do nothing
				break;
			}
		}
	};

	/*
	 * Parse Save User Check In if possible
	 */

	private void saveUserActivity(final String placeID) {
		initProgressDialog();
		String userId = ParseUser.getCurrentUser().getObjectId();

		ParseObject tempUser = ParseObject.createWithoutData(
				ParseConstants.TABLE_USER, userId);
		ParseObject tempPlace = ParseObject.createWithoutData(
				ParseConstants.TABLE_PLACE, placeID);

		ParseObject checkInActivity = new ParseObject(
				ParseConstants.TABLE_ACTV_USER_CHECK_IN_PLACE);
		checkInActivity.put(ParseConstants.KEY_USER_ID, tempUser);
		checkInActivity.put(ParseConstants.KEY_PLACE_ID, tempPlace);
		checkInActivity.saveInBackground(new SaveCallback() {

			@Override
			public void done(ParseException e) {
				progressDialog.dismiss();
				if (e == null) {
					// success
					Toast.makeText(getActivity(), "Check In Success",
							Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(getActivity(),
							LocationInformation.class);
					intent.putExtra(ParseConstants.KEY_OBJECT_ID, placeID);
					startActivity(intent);
				} else {
					// failed
					errorAlertDialog(e);
				}
			}
		});
	}

	/*
	 * Map Functionality
	 */

	private void setUpLocationClientIfNeeded() {
		if (mLocationClient == null) {
			mLocationClient = new LocationClient(getActivity(), this, // ConnectionCallbacks
					this); // OnConnectionFailedListener
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		currentLocation = location;
		LatLng latLng = new LatLng(location.getLatitude(),
				location.getLongitude());
		CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng,
				16);
		mMap.animateCamera(cameraUpdate);

	}

	@Override
	public void onConnected(Bundle connectionHint) {
		mLocationClient.requestLocationUpdates(REQUEST, this); // LocationListener
		initFindPlace();

	}

	@Override
	public void onDisconnected() {
		// Do nothing
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// Do nothing
	}

}