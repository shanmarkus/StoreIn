package com.example.storein;

import org.json.JSONArray;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/**
 * A placeholder fragment containing a simple view.
 */
public class LocationDetail extends Fragment {

	protected static final String TAG = LocationDetail.class.getSimpleName();
	protected String placeID;

	// UI Variable
	protected ParseImageView mLocationView;
	protected TextView mLocationNameLabel;
	protected TextView mLocationAddressLabel;
	protected TextView mLocationPhoneLabel;
	protected TextView mLocationTotalCheckIn;
	protected TextView mLocationOpeningHour;
	protected RatingBar mLocationRatingBar;

	// Variables
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

		// Setting up the UI
		mLocationView = (ParseImageView) rootView
				.findViewById(R.id.locationView);
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
					ParseFile image = location
							.getParseFile(ParseConstants.KEY_IMAGE);
					String nameLocation = location
							.getString(ParseConstants.KEY_NAME);
					String addressLocation = location
							.getString(ParseConstants.KEY_ADDRESS);
					Integer temp = location.getInt(ParseConstants.KEY_PHONE);
					String phoneLocation = temp.toString();
					Float ratingLocation = (float) location
							.getInt(ParseConstants.KEY_RATING);
					Integer totalCheckIn = location.getInt(ParseConstants.KEY_TOTAL_CHECK_IN);
					JSONArray array = location.getJSONArray(ParseConstants.KEY_OPERATIONAL_HOUR);
					String tempOpeningHour = array.toString();

					mLocationNameLabel.setText(nameLocation);
					mLocationAddressLabel.setText(addressLocation);
					mLocationPhoneLabel.setText(phoneLocation);
					mLocationRatingBar.setRating(ratingLocation);
					mLocationTotalCheckIn.setText(totalCheckIn+"");
					mLocationOpeningHour.setText(tempOpeningHour);

					mLocationView.setParseFile(image);
					mLocationView.loadInBackground(new GetDataCallback() {
						
						@Override
						public void done(byte[] arg0, ParseException arg1) {
							// DO Nothing
							
						}
					});
				} else {
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
