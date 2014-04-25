package com.example.storein;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass. Activities that
 * contain this fragment must implement the
 * {@link HomeFragment.OnFragmentInteractionListener} interface to handle
 * interaction events. Use the {@link CheckInFragment#newInstance} factory
 * method to create an instance of this fragment.
 * 
 */
public class CheckInFragment extends Fragment implements ConnectionCallbacks,
		OnConnectionFailedListener, LocationListener, OnItemClickListener,
		OnMyLocationButtonClickListener {

	protected final static String TAG = CheckInFragment.class.getSimpleName()
			.toString();

	// UI Variable
	ListView mListPlace;

	// Variables
	ArrayList<HashMap<String, String>> placesInfo = new ArrayList<HashMap<String, String>>();
	protected ArrayList<String> placesID = new ArrayList<String>();
	HashMap<String, String> placeInfo = new HashMap<String, String>();

	// Place Constant
	private Location lastLocation = null;
	private Location currentLocation = null;
	private boolean hasSetUpInitialLocation = false;

	// Location Client
	private LocationClient mLocationClient;
	private float radius;

	// Parse Constants
	private static final int MAX_PLACE_SEARCH_RESULTS = 20;
	private static final int MAX_PlACE_SEARCH_DISTANCE = 10; // In KiloMeters

	private static final LocationRequest REQUEST = LocationRequest.create()
			.setFastestInterval(16) // 16ms = 60fps
			.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

	public static CheckInFragment newInstance(String param1, String param2) {
		CheckInFragment fragment = new CheckInFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	public CheckInFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		// Clear array list first
		clearAdapter();

		// set up location listener
		setUpLocationClientIfNeeded();
		mLocationClient.connect();

		// On Click listener
		onListPlaceClickListener();

	}

	@Override
	public void onPause() {
		super.onPause();
		if (mLocationClient != null) {
			mLocationClient.disconnect();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_check_in, container,
				false);

		// Setup Location Client
		mLocationClient = new LocationClient(getActivity(), this, this);

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

	/*
	 * Function Added
	 */

	/*
	 * Clear the array list when resume
	 */

	private void clearAdapter() {
		placesInfo.clear();
		placeInfo.clear();
		placesID.clear();
	}

	/*
	 * Location Query
	 */

	private void doLocationQuery() {
		getActivity().setProgressBarIndeterminateVisibility(true);

		ParseGeoPoint location = new ParseGeoPoint(
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
				getActivity().setProgressBarIndeterminateVisibility(false);
				if (e == null) {
					// success
					if (places.size() > 0) {
						for (ParsePlace place : places) {
							String name = place.getName();
							String address = place.getAddress();
							String id = place.getObjectId();

							// add to the hash map
							HashMap<String, String> placeInfo = new HashMap<String, String>();
							placeInfo.put(ParseConstants.KEY_NAME, name);
							placeInfo.put(ParseConstants.KEY_ADDRESS, address);
							placesInfo.add(placeInfo);

							// add ID
							placesID.add(id);
						}
						setAdapter();
					} else {
						String message = "Sorry there are no promotion near you, please use browse to find other promotion";
						Toast.makeText(getActivity(), message,
								Toast.LENGTH_LONG).show();
					}
				} else {
					errorAlertDialog(e);
				}

			}
		});

	}

	/*
	 * Get user previous location (in Parse)
	 */

	private void initFindPlace() {
		currentLocation = mLocationClient.getLastLocation();

		if (currentLocation != null) {
			doLocationQuery();
		}

	}

	/*
	 * Set Adapter for list view
	 */

	public void setAdapter() {
		String[] keys = { ParseConstants.KEY_NAME, ParseConstants.KEY_ADDRESS };
		int[] ids = { android.R.id.text1, android.R.id.text2 };

		SimpleAdapter adapter = new SimpleAdapter(getActivity(), placesInfo,
				android.R.layout.simple_list_item_2, keys, ids);

		mListPlace = (ListView) getActivity().findViewById(R.id.listPlace);
		mListPlace.setAdapter(adapter);
	}

	/*
	 * Set Listener to the ListView to open other intent
	 */

	public void onListPlaceClickListener() {
		mListPlace = (ListView) getActivity().findViewById(R.id.listPlace);

		mListPlace.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				final String placeID = placesID.get(position);
				final Intent intent = new Intent(getActivity(),
						LocationInformation.class);
				startActivity(intent);
			}
		});
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
	 * Map Functionality
	 */

	private void setUpLocationClientIfNeeded() {
		if (mLocationClient == null) {
			mLocationClient = new LocationClient(getActivity(), this, // ConnectionCallbacks
					this); // OnConnectionFailedListener
		}
	}

	/**
	 * Callback called when connected to GCore. Implementation of
	 * {@link ConnectionCallbacks}.
	 */
	@Override
	public void onConnected(Bundle connectionHint) {
		mLocationClient.requestLocationUpdates(REQUEST, this); // LocationListener
		Toast.makeText(getActivity(), "Connected", Toast.LENGTH_SHORT).show();
		initFindPlace();
	}

	/**
	 * Callback called when disconnected from GCore. Implementation of
	 * {@link ConnectionCallbacks}.
	 */
	@Override
	public void onDisconnected() {
		// Do nothing
	}

	/**
	 * Implementation of {@link OnConnectionFailedListener}.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// Do nothing
	}

	@Override
	public boolean onMyLocationButtonClick() {
		Toast.makeText(
				getActivity().getApplicationContext(),
				"MyLocation button clicked" + currentLocation.getLatitude()
						+ " " + currentLocation.getLongitude(),
				Toast.LENGTH_LONG).show();

		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub

	}

}
