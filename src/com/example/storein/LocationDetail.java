package com.example.storein;

import java.util.List;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class LocationDetail extends ActionBarActivity {

	// Variables
	public static final String TAG = LocationDetail.class.getSimpleName();
	protected static String placeID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_location_detail);
		placeID = getIntent().getExtras().getString(
				ParseConstants.KEY_OBJECT_ID);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.location_detail, menu);
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		setSupportProgressBarIndeterminateVisibility(true);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		// UI Variable
		protected ImageView mLocationView;
		protected TextView mLocationNameLabel;
		protected TextView mLocationAddressLabel;
		protected TextView mLocationPhoneLabel;
		protected RatingBar mLocationRatingBar;
		protected Button mLocationCheckIn;

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_location_detail,
					container, false);

			// Setting up the UI
			mLocationNameLabel = (TextView) rootView
					.findViewById(R.id.locationNameLabel);
			mLocationAddressLabel = (TextView) rootView
					.findViewById(R.id.locationAddressLabel);
			mLocationPhoneLabel = (TextView) rootView
					.findViewById(R.id.locationPhoneLabel);
			mLocationRatingBar = (RatingBar) rootView
					.findViewById(R.id.locationRatingBar);
			mLocationCheckIn = (Button) rootView
					.findViewById(R.id.locationCheckIn);

			return rootView;
		}

		@Override
		public void onResume() {
			super.onResume();
			doLocationQuery();
		}

		/*
		 * Added Function
		 */

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
							} else {
								// failed
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
			getActivity().setProgressBarIndeterminateVisibility(true);
			ParseObject.registerSubclass(ParsePlace.class);
			ParseQuery<ParseObject> query = ParseQuery
					.getQuery(ParseConstants.TABLE_PLACE);
			query.whereEqualTo(ParseConstants.KEY_OBJECT_ID, placeID);
			query.findInBackground(new FindCallback<ParseObject>() {

				@Override
				public void done(List<ParseObject> locationDetail,
						ParseException e) {
					getActivity().setProgressBarIndeterminateVisibility(false);
					if (e == null) {
						// success
						ParseObject location = locationDetail.get(0);
						String nameLocation = location
								.getString(ParseConstants.KEY_NAME);
						String addressLocation = location
								.getString(ParseConstants.KEY_ADDRESS);
						Integer temp = location
								.getInt(ParseConstants.KEY_PHONE);
						String phoneLocation = temp.toString();
						Float ratingLocation = (float) location
								.getInt(ParseConstants.KEY_RATING);

						// Setting the information detail
						mLocationNameLabel = (TextView) getActivity()
								.findViewById(R.id.locationNameLabel);
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

	}

}
