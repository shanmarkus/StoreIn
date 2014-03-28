package com.example.storein;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass. Activities that
 * contain this fragment must implement the
 * {@link HomeFragment.OnFragmentInteractionListener} interface to handle
 * interaction events. Use the {@link CheckInFragment#newInstance} factory
 * method to create an instance of this fragment.
 * 
 */
public class CheckInFragment extends Fragment implements ConnectionCallbacks,
		OnConnectionFailedListener, LocationListener,
		OnMyLocationButtonClickListener {
	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";

	protected final static String TAG = CheckInFragment.class.getSimpleName();

	// Place Constant
	private String selectedObjectId;
	private Location lastLocation = null;
	private Location currentLocation = null;
	private boolean hasSetUpInitialLocation = false;

	// Map Constant
	private GoogleMap mMap;
	private LocationClient mLocationClient;
	private final Map<String, Marker> mapMarkers = new HashMap<String, Marker>();
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
	private static final int MAX_PlACE_SEARCH_DISTANCE = 100;

	private static final LocationRequest REQUEST = LocationRequest.create()
			.setInterval(5000) // 5 seconds
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
	public void onSaveInstanceState(Bundle outState) {
		outState.putString("WORKAROUND_FOR_BUG_19917_KEY",
				"WORKAROUND_FOR_BUG_19917_VALUE");
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mMap == null) {
			mMap = fragment.getMap();
			mMap.setMyLocationEnabled(true);
			mMap.setOnMyLocationButtonClickListener(this);
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
		Location myLoc = mLocationClient.getLastLocation();
		final ParseGeoPoint myPoint = new ParseGeoPoint(myLoc.getLatitude(),
				myLoc.getLongitude());
		ParseQuery<ParsePlace> mapQuery = ParsePlace.getQuery();

		mapQuery.whereWithinKilometers("location", myPoint,
				MAX_PlACE_SEARCH_DISTANCE);

		mapQuery.include("name");
		mapQuery.orderByDescending("createdAt");
		mapQuery.setLimit(MAX_PLACE_SEARCH_RESULTS);
		mapQuery.findInBackground(new FindCallback<ParsePlace>() {

			@Override
			public void done(List<ParsePlace> objects, ParseException e) {
				// Initial Hash tag to keep the places
				Set<String> toKeep = new HashSet<String>();
				for (ParsePlace place : objects) {
					toKeep.add(place.getObjectId());
					Marker oldMarker = mapMarkers.get(place.getObjectId());
					MarkerOptions markerOpts = new MarkerOptions()
							.position(new LatLng(place.getLocation()
									.getLatitude(), place.getLocation()
									.getLongitude()));
					// Setting up Boundaries
					if (place.getLocation().distanceInKilometersTo(myPoint) > radius
							* METERS_PER_FEET / METERS_PER_KILOMETER) {
						// Set up an out-of-range marker
					} else {
						// Set up an in-range marker
					}
					// Add Marker to Maps
					Marker marker = ((SupportMapFragment) getFragmentManager()
							.findFragmentById(R.id.map)).getMap().addMarker(
							markerOpts);
					mapMarkers.put(place.getObjectId(), marker);

					if (place.getObjectId().equals(selectedObjectId)) {
						marker.showInfoWindow();
						selectedObjectId = null;
					}
				}
				cleanUpMarkers(toKeep);
			}
		});
	}

	private void cleanUpMarkers(Set<String> markersToKeep) {
		for (String objId : new HashSet<String>(mapMarkers.keySet())) {
			if (!markersToKeep.contains(objId)) {
				Marker marker = mapMarkers.get(objId);
				marker.remove();
				mapMarkers.get(objId).remove();
				mapMarkers.remove(objId);
			}
		}
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
		if (lastLocation != null
				&& geoPointFromLocation(location).distanceInKilometersTo(
						geoPointFromLocation(lastLocation)) < 0.01) {
			// If the location hasn't changed by more than 10 meters, ignore it.
			return;
		}
		lastLocation = location;
		LatLng myLatLng = new LatLng(location.getLatitude(),
				location.getLongitude());
		if (!hasSetUpInitialLocation) {
			// Zoom to the current location.
			updateZoom(myLatLng);
			hasSetUpInitialLocation = true;
		}
		// Update map radius indicator
		updateCircle(myLatLng);
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

}
