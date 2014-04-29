package com.example.storein;

import java.util.List;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/**
 * A placeholder fragment containing a simple view.
 */
public class LocationDetail extends Fragment {

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
		mLocationNameLabel = (TextView) rootView
				.findViewById(R.id.locationNameLabel);
		mLocationAddressLabel = (TextView) rootView
				.findViewById(R.id.locationAddressLabel);
		mLocationPhoneLabel = (TextView) rootView
				.findViewById(R.id.locationPhoneLabel);
		mLocationRatingBar = (RatingBar) rootView
				.findViewById(R.id.locationRatingBar);

		// Init Function
		doLocationQuery();

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
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
