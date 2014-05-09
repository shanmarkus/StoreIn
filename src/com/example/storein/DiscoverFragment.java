package com.example.storein;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
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
import android.widget.Button;
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
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class DiscoverFragment extends Fragment implements ConnectionCallbacks,
		OnConnectionFailedListener, LocationListener {

	protected final static String TAG = DiscoverFragment.class.getSimpleName()
			.toString();

	// UI Variable
	Button mDiscoverButtonCheckIn;
	Button mDiscoverButtonBrowse;
	Button mDiscoverButtonReccomendation;

	// Variables
	ArrayList<HashMap<String, String>> placesInfo = new ArrayList<HashMap<String, String>>();
	protected ArrayList<String> placesID = new ArrayList<String>();
	HashMap<String, String> placeInfo = new HashMap<String, String>();

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
			.setFastestInterval(16) // 16ms = 60fps
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
		mDiscoverButtonBrowse = (Button) view
				.findViewById(R.id.discoverButtonBrowse);
		mDiscoverButtonCheckIn = (Button) view
				.findViewById(R.id.discoverButtonCheckIn);
		mDiscoverButtonReccomendation = (Button) view
				.findViewById(R.id.discoverButtonDiscover);

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

	private void onClickListener() {
		mDiscoverButtonBrowse.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), BrowseFragment2.class);
				startActivity(intent);
			}
		});

		mDiscoverButtonCheckIn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(),
						CheckInFragment2.class);
				startActivity(intent);
			}
		});
	}

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
							ParseGeoPoint geoPoint = place
									.getParseGeoPoint(ParseConstants.KEY_LOCATION);

							// Adding Marker
							mMap.addMarker(new MarkerOptions().position(
									new LatLng(geoPoint.getLatitude(), geoPoint
											.getLongitude())).title(name));

						}
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

	private void initFindPlace() {
		currentLocation = mLocationClient.getLastLocation();
		if (currentLocation != null) {
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
		mMap.addCircle(new CircleOptions()
				.center(new LatLng(location.getLatitude(), location
						.getLongitude())).radius(100).strokeColor(Color.RED));
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