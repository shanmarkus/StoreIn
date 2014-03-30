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

public class LocationDetail extends ActionBarActivity {

	public static final String TAG = LocationDetail.class.getSimpleName();
	protected static String placeID;

	// UI Variable
	protected ImageView mLocationView;
	protected TextView mLocationNameLabel;
	protected TextView mLocationAddressLabel;
	protected TextView mLocationPhoneLabel;
	protected RatingBar mLocationRatingBar;
	protected Button mLocationCheckIn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_location_detail);
		placeID = getIntent().getExtras().getString(
				ParseConstants.KEY_OBJECT_ID);

		// Setting up the UI
		mLocationNameLabel = (TextView) findViewById(R.id.locationNameLabel);
		mLocationAddressLabel = (TextView) findViewById(R.id.locationAddressLabel);
		mLocationPhoneLabel = (TextView) findViewById(R.id.locationPhoneLabel);
		mLocationRatingBar = (RatingBar) findViewById(R.id.locationRatingBar);
		mLocationCheckIn = (Button) findViewById(R.id.locationCheckIn);

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
		doLocationQuery();
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

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_location_detail,
					container, false);
			
			
			//Button
			Button btn = (Button) rootView.findViewById(R.id.locationCheckIn);
			btn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(getActivity(), LocationCatalog.class);
					intent.putExtra(ParseConstants.KEY_OBJECT_ID, placeID);
					startActivity(intent);	
				}
			});
			return rootView;
		}
	}

	/*
	 * Added Function
	 */

	/*
	 * Get the query for location details
	 */

	protected void doLocationQuery() {
		ParseObject.registerSubclass(ParsePlace.class);
		ParseQuery<ParseObject> query = ParseQuery
				.getQuery(ParseConstants.TABLE_PLACE);
		query.whereEqualTo(ParseConstants.KEY_OBJECT_ID, placeID);
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> locationDetail, ParseException e) {
				setProgressBarIndeterminateVisibility(false);
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
					mLocationNameLabel = (TextView) findViewById(R.id.locationNameLabel);
					mLocationAddressLabel = (TextView) findViewById(R.id.locationAddressLabel);
					mLocationPhoneLabel = (TextView) findViewById(R.id.locationPhoneLabel);
					mLocationRatingBar = (RatingBar) findViewById(R.id.locationRatingBar);

					mLocationNameLabel.setText(nameLocation);
					mLocationAddressLabel.setText(addressLocation);
					mLocationPhoneLabel.setText(phoneLocation);
					mLocationRatingBar.setRating(ratingLocation);

				} else {
					// failed
					Log.e(TAG, e.getMessage());
					AlertDialog.Builder builder = new AlertDialog.Builder(
							LocationDetail.this);
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
