package com.example.storein;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

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
	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";

	protected final static String TAG = CheckInFragment.class.getSimpleName()
			.toString();

	// Place Constant
	private String selectedObjectId;
	private Location lastLocation = null;
	private Location currentLocation = null;
	private boolean hasSetUpInitialLocation = false;

	// Map Constant
	private GoogleMap mMap;
	private LocationClient mLocationClient;
	private float radius;
	private float lastRadius;
	private SupportMapFragment fragment;
	private Circle mapCircle;

	// Parse Constants

	private static final float METERS_PER_FEET = 0.3048f;
	private static final int METERS_PER_KILOMETER = 1000;
	private static final double OFFSET_CALCULATION_INIT_DIFF = 1.0;
	private static final float OFFSET_CALCULATION_ACCURACY = 0.01f;
	private static final int MAX_PLACE_SEARCH_RESULTS = 20;
	private static final int MAX_PlACE_SEARCH_DISTANCE = 10; // In KiloMeters

	private static final LocationRequest REQUEST = LocationRequest.create()
			.setFastestInterval(16) // 16ms = 60fps
			.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

	public static CheckInFragment newInstance(String param1, String param2) {
		CheckInFragment fragment = new CheckInFragment();
		Bundle args = new Bundle();
		args.putString(ARG_PARAM1, param1);
		args.putString(ARG_PARAM2, param2);
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
		getActivity().setProgressBarIndeterminateVisibility(true);
		if (mMap == null) {
			mMap = fragment.getMap();
			mMap.setMyLocationEnabled(true);
		}
		setUpLocationClientIfNeeded();
		mLocationClient.connect();
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

	private void doMapQuery() {
		// Get user Location
		ParseGeoPoint location = ParseUser.getCurrentUser().getParseGeoPoint(
				ParseConstants.KEY_LOCATION);

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
					ArrayList<HashMap<String, String>> placesInfo = new ArrayList<HashMap<String, String>>();
					final ArrayList<String> placesID = new ArrayList<String>();

					for (ParsePlace place : places) {
						String name = place.getName();
						String address = place.getAddress();
						String id = place.getObjectId();
						ParseGeoPoint geoPoint = place
								.getParseGeoPoint(ParseConstants.KEY_LOCATION);

						// Adding Marker
						mMap.addMarker(new MarkerOptions().position(
								new LatLng(geoPoint.getLatitude(), geoPoint
										.getLongitude())).title(name));

						// add to the hash map
						HashMap<String, String> placeInfo = new HashMap<String, String>();
						placeInfo.put(ParseConstants.KEY_NAME, name);
						placeInfo.put(ParseConstants.KEY_ADDRESS, address);
						placesInfo.add(placeInfo);

						// add ID
						placesID.add(id);
					}

					String[] keys = { ParseConstants.KEY_NAME,
							ParseConstants.KEY_ADDRESS };
					int[] ids = { android.R.id.text1, android.R.id.text2 };

					SimpleAdapter adapter = new SimpleAdapter(getActivity(),
							placesInfo, android.R.layout.simple_list_item_2,
							keys, ids);

					ListView mListPlace = (ListView) getActivity()
							.findViewById(R.id.listPlace);
					mListPlace.setAdapter(adapter);

					/*
					 * Set Listener to the ListView to open other intent
					 */
					mListPlace
							.setOnItemClickListener(new OnItemClickListener() {
								@Override
								public void onItemClick(AdapterView<?> parent,
										View view, int position, long id) {
									String placeID = placesID.get(position);
									Intent intent = new Intent(getActivity(),
											LocationDetail.class);
									intent.putExtra(
											ParseConstants.KEY_OBJECT_ID,
											placeID);
									startActivity(intent);
								}
							});

				} else {
					// failed
					Log.e(TAG, e.getMessage());
					AlertDialog.Builder builder = new AlertDialog.Builder(
							getActivity());
					builder.setMessage(e.getMessage())
							.setTitle(R.string.error_title)
							.setPositiveButton(android.R.string.ok, null);
					AlertDialog dialog = builder.create();
					dialog.show();
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

	private void locationChecker() {
		// Checking wether the location is change or not
		if (mLocationClient != null && mLocationClient.isConnected()) {
			String msg = "Location = " + mLocationClient.getLastLocation();
			Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Implementation of {@link LocationListener}. Makes the camera always go to
	 * the user location
	 */
	@Override
	public void onLocationChanged(Location location) {

		currentLocation = location;
		lastLocation = location;
		LatLng myLatLng = new LatLng(location.getLatitude(),
				location.getLongitude());
		
		// Not changes
		if (lastLocation != null
				&& geoPointFromLocation(location).distanceInKilometersTo(
						geoPointFromLocation(lastLocation)) < 0.01) {
			doMapQuery();
		}
		if (!hasSetUpInitialLocation) {
			// Zoom to the current location.
			updateZoom(myLatLng);
			hasSetUpInitialLocation = true;
			updateUserLocation(myLatLng);
		}
		// Update map radius indicator
		updateCircle(myLatLng);
	}

	private void updateUserLocation(LatLng myLatLng) {
		// Update User Location to Parse
		ParseGeoPoint temp = new ParseGeoPoint(myLatLng.latitude,
				myLatLng.longitude);
		ParseUser user = ParseUser.getCurrentUser();
		user.put(ParseConstants.KEY_LOCATION, temp);
		user.saveEventually(new SaveCallback() {

			@Override
			public void done(ParseException e) {
				if (e == null) {
					Toast.makeText(getActivity(), "Location Updated",
							Toast.LENGTH_SHORT).show();
					doMapQuery();

				} else {
					Toast.makeText(getActivity(), "Location Not Updated",
							Toast.LENGTH_SHORT).show();
				}

			}
		});
	}

	/**
	 * Callback called when connected to GCore. Implementation of
	 * {@link ConnectionCallbacks}.
	 */
	@Override
	public void onConnected(Bundle connectionHint) {
		mLocationClient.requestLocationUpdates(REQUEST, this); // LocationListener
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
		// Return false so that we don't consume the event and the default
		// behavior still occurs
		// (the camera animates to the user's current position).
		return false;
	}

	/*
	 * Helper method to get the Parse GEO point representation of a location
	 */
	private ParseGeoPoint geoPointFromLocation(Location loc) {
		return new ParseGeoPoint(loc.getLatitude(), loc.getLongitude());
	}

	/*
	 * Displays a circle on the map representing the search radius
	 */
	private void updateCircle(LatLng myLatLng) {
		if (mapCircle == null) {
			mapCircle = mMap.addCircle(new CircleOptions().center(myLatLng)
					.radius(radius * METERS_PER_FEET));
			int baseColor = Color.DKGRAY;
			mapCircle.setStrokeColor(baseColor);
			mapCircle.setStrokeWidth(2);
			mapCircle.setFillColor(Color.argb(50, Color.red(baseColor),
					Color.green(baseColor), Color.blue(baseColor)));
		}
		mapCircle.setCenter(myLatLng);
		mapCircle.setRadius(radius * METERS_PER_FEET); // Convert radius in feet
														// to meters.
	}

	/*
	 * Zooms the map to show the area of interest based on the search radius
	 */
	private void updateZoom(LatLng myLatLng) {
		// Get the bounds to zoom to
		LatLngBounds bounds = calculateBoundsWithCenter(myLatLng);
		// Zoom to the given bounds
		mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 5));

		// Zoom further
		float zoomLevel = 19;
		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng,
				zoomLevel));
	}

	/*
	 * Helper method to calculate the bounds for map zooming
	 */
	public LatLngBounds calculateBoundsWithCenter(LatLng myLatLng) {
		// Create a bounds
		LatLngBounds.Builder builder = LatLngBounds.builder();

		// Calculate east/west points that should to be included
		// in the bounds
		double lngDifference = calculateLatLngOffset(myLatLng, false);
		LatLng east = new LatLng(myLatLng.latitude, myLatLng.longitude
				+ lngDifference);
		builder.include(east);
		LatLng west = new LatLng(myLatLng.latitude, myLatLng.longitude
				- lngDifference);
		builder.include(west);

		// Calculate north/south points that should to be included
		// in the bounds
		double latDifference = calculateLatLngOffset(myLatLng, true);
		LatLng north = new LatLng(myLatLng.latitude + latDifference,
				myLatLng.longitude);
		builder.include(north);
		LatLng south = new LatLng(myLatLng.latitude - latDifference,
				myLatLng.longitude);
		builder.include(south);

		return builder.build();
	}

	/*
	 * Helper method to calculate the offset for the bounds used in map zooming
	 */
	private double calculateLatLngOffset(LatLng myLatLng, boolean bLatOffset) {
		// The return offset, initialized to the default difference
		double latLngOffset = OFFSET_CALCULATION_INIT_DIFF;
		// Set up the desired offset distance in meters
		float desiredOffsetInMeters = radius * METERS_PER_FEET;
		// Variables for the distance calculation
		float[] distance = new float[1];
		boolean foundMax = false;
		double foundMinDiff = 0;
		// Loop through and get the offset
		do {
			// Calculate the distance between the point of interest
			// and the current offset in the latitude or longitude direction
			if (bLatOffset) {
				Location.distanceBetween(myLatLng.latitude, myLatLng.longitude,
						myLatLng.latitude + latLngOffset, myLatLng.longitude,
						distance);
			} else {
				Location.distanceBetween(myLatLng.latitude, myLatLng.longitude,
						myLatLng.latitude, myLatLng.longitude + latLngOffset,
						distance);
			}
			// Compare the current difference with the desired one
			float distanceDiff = distance[0] - desiredOffsetInMeters;
			if (distanceDiff < 0) {
				// Need to catch up to the desired distance
				if (!foundMax) {
					foundMinDiff = latLngOffset;
					// Increase the calculated offset
					latLngOffset *= 2;
				} else {
					double tmp = latLngOffset;
					// Increase the calculated offset, at a slower pace
					latLngOffset += (latLngOffset - foundMinDiff) / 2;
					foundMinDiff = tmp;
				}
			} else {
				// Overshot the desired distance
				// Decrease the calculated offset
				latLngOffset -= (latLngOffset - foundMinDiff) / 2;
				foundMax = true;
			}
		} while (Math.abs(distance[0] - desiredOffsetInMeters) > OFFSET_CALCULATION_ACCURACY);
		return latLngOffset;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub

	}

}
