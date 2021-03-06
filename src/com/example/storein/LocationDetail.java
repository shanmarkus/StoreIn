package com.example.storein;

import org.json.JSONArray;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.CountCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseImageView;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.PushService;

/**
 * A placeholder fragment containing a simple view.
 */
public class LocationDetail extends Fragment {

	protected static final String TAG = LocationDetail.class.getSimpleName();
	protected String placeID;

	// UI Variable
	protected ParseImageView mLocationView;
	protected Button mLocationDetailFollowButton;
	protected TextView mLocationNameLabel;
	protected TextView mLocationAddressLabel;
	protected TextView mLocationPhoneLabel;
	protected TextView mLocationTotalCheckIn;
	protected TextView mLocationOpeningHour;
	protected RatingBar mLocationRatingBar;

	// Variables
	protected ParseGeoPoint mCurrentGeoPoint;
	String nameLocation;

	public LocationDetail() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_location_detail,
				container, false);

		// Init bundle variables
		getPlaceID();

		// Setting up the UI
		mLocationView = (ParseImageView) rootView
				.findViewById(R.id.locationView);
		mLocationDetailFollowButton = (Button) rootView
				.findViewById(R.id.locationDetailFollowButton);
		mLocationNameLabel = (TextView) rootView
				.findViewById(R.id.locationNameLabel);
		mLocationAddressLabel = (TextView) rootView
				.findViewById(R.id.locationAddressLabel);
		mLocationPhoneLabel = (TextView) rootView
				.findViewById(R.id.locationPhoneLabel);
		mLocationTotalCheckIn = (TextView) rootView
				.findViewById(R.id.textTotalCheckIn);
		mLocationRatingBar = (RatingBar) rootView
				.findViewById(R.id.locationRatingBar);
		mLocationOpeningHour = (TextView) rootView
				.findViewById(R.id.textOpeningHour);

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		// Init Function
		doLocationQuery();
	}

	@Override
	public void onStop() {
		super.onStop();
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

	/*
	 * Get the button follow value
	 */

	private void findButtonValue(String nameLocation) {
		if (placeID == null) {
			getPlaceID();
		}
		// Get Channels
		String tempTenants = nameLocation.replaceAll("\\p{Z}", "");
		tempTenants = tempTenants + placeID;

		ParseInstallation instal = ParseInstallation.getCurrentInstallation();
		String id = instal.getObjectId();
		ParseQuery<ParseInstallation> pushQuery = ParseInstallation.getQuery();
		pushQuery.whereEqualTo("objectId", id);
		pushQuery.whereEqualTo("channels", tempTenants);
		pushQuery.countInBackground(new CountCallback() {

			@Override
			public void done(int total, ParseException e) {
				if (e == null) {
					if (total == 0) {
						mLocationDetailFollowButton.setText("Follow");
						mLocationDetailFollowButton
								.setOnClickListener(followTenant);
					} else {
						mLocationDetailFollowButton.setText("Un-Follow");
						mLocationDetailFollowButton
								.setOnClickListener(unFollowTenant);
					}
				} else {
					parseErrorDialog(e);
				}
			}
		});
	}

	/*
	 * On Click Listener for following the tenant
	 */

	OnClickListener followTenant = new OnClickListener() {

		@Override
		public void onClick(View v) {
			String tempTenants = nameLocation.replaceAll("\\p{Z}", "");
			tempTenants = tempTenants + placeID;
			PushService.subscribe(getActivity(), tempTenants,
					MainActivity.class);
			findButtonValue(nameLocation);
			onResume();
		}
	};

	OnClickListener unFollowTenant = new OnClickListener() {

		@Override
		public void onClick(View v) {
			String tempTenants = nameLocation.replaceAll("\\p{Z}", "");
			tempTenants = tempTenants + placeID;
			PushService.unsubscribe(getActivity(), tempTenants);
			findButtonValue(nameLocation);
			onResume();
		}
	};

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
		query.getFirstInBackground(new GetCallback<ParseObject>() {

			@Override
			public void done(ParseObject location, ParseException e) {
				if (e == null) {
					getActivity().setProgressBarIndeterminateVisibility(false);
					ParseFile image = location
							.getParseFile(ParseConstants.KEY_IMAGE);
					nameLocation = location.getString(ParseConstants.KEY_NAME);
					String addressLocation = location
							.getString(ParseConstants.KEY_ADDRESS);
					Integer temp = location.getInt(ParseConstants.KEY_PHONE);
					String phoneLocation = temp.toString();
					Float ratingLocation = (float) location
							.getInt(ParseConstants.KEY_RATING);
					Integer totalCheckIn = location
							.getInt(ParseConstants.KEY_TOTAL_CHECK_IN);
					JSONArray array = location
							.getJSONArray(ParseConstants.KEY_OPERATIONAL_HOUR);
					String tempOpeningHour = array.toString();

					mLocationNameLabel.setText(nameLocation);
					mLocationAddressLabel.setText(addressLocation);
					mLocationPhoneLabel.setText(phoneLocation);
					mLocationRatingBar.setRating(ratingLocation);
					mLocationTotalCheckIn.setText(totalCheckIn + "");
					mLocationOpeningHour.setText(tempOpeningHour);

					mLocationView.setParseFile(image);

					// get value of the button
					findButtonValue(nameLocation);

					// IMAGE LOADER

					mLocationView.loadInBackground(new GetDataCallback() {

						@Override
						public void done(byte[] arg0, ParseException arg1) {
							// DO Nothing
						}
					});
				} else {
					getActivity().setProgressBarIndeterminateVisibility(false);
					parseErrorDialog(e);
				}

			}
		});

	}

	/*
	 * Debug Parse Error
	 */

	private void parseErrorDialog(ParseException e) {
		Log.e(TAG, e.getMessage());
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(e.getMessage()).setTitle(R.string.error_title)
				.setPositiveButton(android.R.string.ok, null);
		AlertDialog dialog = builder.create();
		dialog.show();
	}

}
