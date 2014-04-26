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
		mLocationCheckIn = (Button) rootView.findViewById(R.id.locationCheckIn);

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

	protected void checkDistance() {
		if (mCurrentGeoPoint == null) {
			checkUserLastLocation();
		}
		final Location userLocation = new Location("");
		final Location placeLocation = new Location("");
		userLocation.setLatitude(mCurrentGeoPoint.getLatitude());
		userLocation.setLongitude(mCurrentGeoPoint.getLongitude());

		ParseQuery<ParseObject> query = ParseQuery
				.getQuery(ParseConstants.TABLE_PLACE);
		query.whereEqualTo(ParseConstants.KEY_OBJECT_ID, placeID);
		query.getFirstInBackground(new GetCallback<ParseObject>() {

			@Override
			public void done(ParseObject place, ParseException e) {
				if (e == null) {
					// success
					ParseGeoPoint tempPlaceGeoPoint = place
							.getParseGeoPoint(ParseConstants.KEY_LOCATION);
					placeLocation.setLatitude(tempPlaceGeoPoint.getLatitude());
					placeLocation.setLongitude(tempPlaceGeoPoint.getLongitude());

					// Check the distance between 2 Location in Meters
					float result = Math.abs(userLocation
							.distanceTo(placeLocation));

					// Debug
					Toast.makeText(getActivity(), result + " ",
							Toast.LENGTH_SHORT).show();
					if (result > 10000) {
						// User are not near the location
						mLocationCheckIn = (Button) getActivity().findViewById(
								R.id.locationCheckIn);
						mLocationCheckIn.setEnabled(false);
					} else {
						mLocationCheckIn = (Button) getActivity().findViewById(
								R.id.locationCheckIn);
						mLocationCheckIn.setEnabled(true);
						onCheckInBtnClicked();
					}
				} else {
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

	// add activity records when user check in to a place
	protected void onCheckInBtnClicked() {

		mLocationCheckIn = (Button) getActivity().findViewById(
				R.id.locationCheckIn);
		mLocationCheckIn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String userId = ParseUser.getCurrentUser().getObjectId();
				ParseObject checkInActivity = new ParseObject(
						ParseConstants.TABLE_ACTV_USER_CHECK_IN_PLACE);
				checkInActivity.put(ParseConstants.KEY_USER_ID, userId);
				checkInActivity.put(ParseConstants.KEY_PLACE_ID, placeID);
				checkInActivity.saveInBackground(new SaveCallback() {

					@Override
					public void done(ParseException e) {
						if (e == null) {
							// success
							Toast.makeText(getActivity(), "Check In Success",
									Toast.LENGTH_SHORT).show();
							Intent intent = new Intent(getActivity(),
									LocationCatalog.class);
							intent.putExtra(ParseConstants.KEY_OBJECT_ID,
									placeID);
							startActivity(intent);
						}

						else {
							// failed
							Log.e(TAG, e.getMessage());
							AlertDialog.Builder builder = new AlertDialog.Builder(
									getActivity());
							builder.setMessage(e.getMessage())
									.setTitle(R.string.error_title)
									.setPositiveButton(android.R.string.ok,
											null);
							AlertDialog dialog = builder.create();
							dialog.show();
						}

					}
				});

			}
		});
	}

	/*
	 * Get the query for location details
	 */

	protected void doLocationQuery() {
		if(placeID == null){
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
		// Check The Distance
		//checkDistance();
	}

	@Override
	public void onDisconnected() {
		Toast.makeText(getActivity(), "Disconnected. Please re-connect.",
				Toast.LENGTH_SHORT).show();

	}

}
