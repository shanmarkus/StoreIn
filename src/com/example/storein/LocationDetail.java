package com.example.storein;

import java.util.List;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/**
 * A placeholder fragment containing a simple view.
 */
public class LocationDetail extends Fragment implements ConnectionCallbacks,
		OnConnectionFailedListener {

	protected static final String TAG = LocationDetail.class.getSimpleName();
	protected String placeID;

	// UI Variable
	protected ImageView mLocationView;
	protected TextView mLocationNameLabel;
	protected TextView mLocationAddressLabel;
	protected TextView mLocationPhoneLabel;
	protected RatingBar mLocationRatingBar;
	protected Button mLocationCheckIn;

	// Variables
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	LocationClient mLocationClient;
	Location mCurrentLocation;
	protected ParseGeoPoint mCurrentGeoPoint;

	public LocationDetail() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_location_detail,
				container, false);

		// Init bundle variables
		getPlaceID();

		// Setup Location Client
		mLocationClient = new LocationClient(getActivity(), this, this);

		// Setting up the UI
		mLocationNameLabel = (TextView) rootView
				.findViewById(R.id.locationNameLabel);
		mLocationAddressLabel = (TextView) rootView
				.findViewById(R.id.locationAddressLabel);
		mLocationPhoneLabel = (TextView) rootView
				.findViewById(R.id.locationPhoneLabel);
		mLocationRatingBar = (RatingBar) rootView
				.findViewById(R.id.locationRatingBar);
		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		mLocationClient.connect();
		doLocationQuery();
	}

	@Override
	public void onStop() {
		super.onStop();
		mLocationClient.disconnect();
	}

	/*
	 * Setter And Getter
	 */

	public String getPlaceID() {
		Bundle args = getArguments();
		placeID = args.getString(ParseConstants.KEY_OBJECT_ID);
		return placeID;
	}

	/*
	 * Added Function
	 */

	protected void checkUserLastLocation() {
		ParseUser user = ParseUser.getCurrentUser();
		ParseGeoPoint location = user
				.getParseGeoPoint(ParseConstants.KEY_LOCATION);
		if (location == null) {
			Location mlocation = mLocationClient.getLastLocation();
			mCurrentGeoPoint = new ParseGeoPoint(mlocation.getLatitude(),
					mlocation.getLongitude());
			user.put(ParseConstants.KEY_LOCATION, mCurrentGeoPoint);
			user.saveEventually(new SaveCallback() {

				@Override
				public void done(ParseException e) {
					if (e == null) {
						Toast.makeText(getActivity(), "Updated Location",
								Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(getActivity(), "Failed Update Location",
								Toast.LENGTH_SHORT).show();
					}
				}
			});

		} else {
			mCurrentGeoPoint = location;
		}
	}

	/*
	 * Get the query for location details
	 */

	protected void doLocationQuery() {
		if (placeID == null) {
			getPlaceID();
		}
		getActivity().setProgressBarIndeterminateVisibility(true);
		ParseObject.registerSubclass(ParsePlace.class);
		ParseQuery<ParseObject> query = ParseQuery
				.getQuery(ParseConstants.TABLE_PLACE);
		query.whereEqualTo(ParseConstants.KEY_OBJECT_ID, placeID);
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> locationDetail, ParseException e) {
				getActivity().setProgressBarIndeterminateVisibility(false);
				if (e == null) {
					// success
					ParseObject location = locationDetail.get(0);
					String nameLocation = location
							.getString(ParseConstants.KEY_NAME);
					String addressLocation = location
							.getString(ParseConstants.KEY_ADDRESS);
					Integer temp = location.getInt(ParseConstants.KEY_PHONE);
					String phoneLocation = temp.toString();
					Float ratingLocation = (float) location
							.getInt(ParseConstants.KEY_RATING);

					// Setting the information detail
					mLocationNameLabel = (TextView) getActivity().findViewById(
							R.id.locationNameLabel);
					mLocationAddressLabel = (TextView) getActivity()
							.findViewById(R.id.locationAddressLabel);
					mLocationPhoneLabel = (TextView) getActivity()
							.findViewById(R.id.locationPhoneLabel);
					mLocationRatingBar = (RatingBar) getActivity()
							.findViewById(R.id.locationRatingBar);

					mLocationNameLabel.setText(nameLocation);
					mLocationAddressLabel.setText(addressLocation);
					mLocationPhoneLabel.setText(phoneLocation);
					mLocationRatingBar.setRating(ratingLocation);

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
	 * Map Function
	 * 
	 * @see com.google.android.gms.common.GooglePlayServicesClient.
	 * OnConnectionFailedListener
	 * #onConnectionFailed(com.google.android.gms.common.ConnectionResult)
	 */

	@Override
	public void onConnectionFailed(ConnectionResult e) {
		if (e.hasResolution()) {
			try {
				e.startResolutionForResult(getActivity(),
						CONNECTION_FAILURE_RESOLUTION_REQUEST);
			} catch (IntentSender.SendIntentException e1) {
				e1.printStackTrace();
			}
		} else {
			Toast.makeText(getActivity(), e.getErrorCode(), Toast.LENGTH_SHORT)
					.show();
		}

	}

	@Override
	public void onConnected(Bundle arg0) {
		Toast.makeText(getActivity(), "Connected", Toast.LENGTH_SHORT).show();
		// Get current Location
		mCurrentLocation = mLocationClient.getLastLocation();
	}

	@Override
	public void onDisconnected() {
		Toast.makeText(getActivity(), "Disconnected. Please re-connect.",
				Toast.LENGTH_SHORT).show();

	}

}
