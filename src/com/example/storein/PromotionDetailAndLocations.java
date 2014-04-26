package com.example.storein;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class PromotionDetailAndLocations extends ActionBarActivity {
	// Variables
	public static final String TAG = PromotionDetailAndLocations.class
			.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_promotion_detail_and_location);
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.promotion_detail, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment implements
			ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

		// UI Declaration
		TextView mTxtPromotionTitle;
		TextView mTextPromotionReq;
		TextView mTextPromotionDesc;
		TextView mTextPromotionDuration;
		ListView mLocationList;

		// Variables
		protected String promotionId;
		protected ArrayList<HashMap<String, String>> locationsInfo = new ArrayList<HashMap<String, String>>();
		public HashMap<String, String> locationInfo = new HashMap<String, String>();
		protected ArrayList<String> objectsId = new ArrayList<String>();
		protected ArrayList<ParseGeoPoint> locationsCoordinate = new ArrayList<ParseGeoPoint>();
		protected String placeId;

		// Location Variable
		private Location currentLocation = null;
		private Location placeSelected;
		protected ParseGeoPoint placeCurrentLocation = null;
		protected static final float MAX_DISTANCE = 10000; // 10 Km

		// Location Client
		private LocationClient mLocationClient;
		private static final LocationRequest REQUEST = LocationRequest.create()
				.setFastestInterval(16) // 16ms = 60fps
				.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

		// Progress Bar
		ProgressDialog progressDialog;

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(
					R.layout.fragment_promotion_detail_and_location, container,
					false);
			mLocationList = (ListView) rootView
					.findViewById(R.id.LocationsList);

			// Setup Location Client
			mLocationClient = new LocationClient(getActivity(), this, this);

			// setting up needed function
			getPromotionId();
			onClickLocationList();
			return rootView;
		}

		@Override
		public void onResume() {
			super.onResume();
			mLocationClient.connect();
			findPromotionDetail();
			findPromotionLocation();
		}

		/*
		 * Added Functions
		 */

		/*
		 * Getter PromotionId
		 */
		protected void getPromotionId() {
			promotionId = (String) getActivity().getIntent().getExtras()
					.get(ParseConstants.KEY_OBJECT_ID);
		}

		/*
		 * Clear ArrayList Function
		 */

		private void clearArrayList() {
			locationInfo.clear();
			locationsInfo.clear();
			locationsCoordinate.clear();
			objectsId.clear();
		}

		/*
		 * Progress Dialog initiate
		 */

		private void initProgressDialog() {
			progressDialog = new ProgressDialog(getActivity());
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setMessage("Loading");
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(false);
			progressDialog.show();
		}

		/*
		 * Setting Adapter for Location List
		 */

		public void setAdapter() {
			String[] keys = { ParseConstants.KEY_NAME,
					ParseConstants.KEY_ADDRESS };
			int[] ids = { android.R.id.text1, android.R.id.text2 };

			SimpleAdapter adapter = new SimpleAdapter(getActivity(),
					locationsInfo, android.R.layout.simple_list_item_2, keys,
					ids);

			mLocationList.setAdapter(adapter);
		}

		/*
		 * On Click Listener onListView
		 */

		public void onClickLocationList() {
			mLocationList.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					// Start Progress Dialog
					initProgressDialog();
					placeCurrentLocation = locationsCoordinate.get(position);
					placeId = objectsId.get(position);
					checkUserDistance();
				}
			});
		}

		/*
		 * Do Query to find promotion Location(s)
		 */

		public void findPromotionLocation() {
			clearArrayList();
			if (promotionId == null) {
				getPromotionId();
			}
			locationsInfo.clear();
			// set progress bar true
			getActivity().setProgressBarIndeterminateVisibility(true);

			ParseQuery<ParseObject> query = ParseQuery
					.getQuery(ParseConstants.TABLE_PROMOTION);
			query.whereEqualTo(ParseConstants.KEY_OBJECT_ID, promotionId);

			ParseQuery<ParseObject> innerQuery = ParseQuery
					.getQuery(ParseConstants.TABLE_REL_PROMOTION_PLACE);
			innerQuery.whereMatchesKeyInQuery(ParseConstants.KEY_PROMOTION_ID,
					ParseConstants.KEY_OBJECT_ID, query);
			innerQuery.include(ParseConstants.KEY_PLACE_ID);
			innerQuery.findInBackground(new FindCallback<ParseObject>() {

				@Override
				public void done(List<ParseObject> places, ParseException e) {
					getActivity().setProgressBarIndeterminateVisibility(false);
					if (e == null) {
						// success
						for (ParseObject place : places) {
							// Initiate Hash map
							HashMap<String, String> locationInfo = new HashMap<String, String>();

							// Get Value
							ParseObject placeDetail = place
									.getParseObject(ParseConstants.KEY_PLACE_ID);
							String placeName = placeDetail
									.getString(ParseConstants.KEY_NAME);
							String placeAddress = placeDetail
									.getString(ParseConstants.KEY_ADDRESS);
							String objectId = placeDetail.getObjectId();
							ParseGeoPoint placeCoordinate = placeDetail
									.getParseGeoPoint(ParseConstants.KEY_LOCATION);

							// Add to ArrayList
							locationInfo
									.put(ParseConstants.KEY_NAME, placeName);
							locationInfo.put(ParseConstants.KEY_ADDRESS,
									placeAddress);
							locationsInfo.add(locationInfo);
							objectsId.add(objectId);
							locationsCoordinate.add(placeCoordinate);
						}
						// setup adapter
						setAdapter();

					} else {
						// failed
						errorAlertDialog(e);
					}

				}
			});
		}

		/*
		 * Do Query for finding promotion details
		 */

		public void findPromotionDetail() {
			if (promotionId == null) {
				getPromotionId();
			}
			// set progress bar true
			getActivity().setProgressBarIndeterminateVisibility(true);

			ParseQuery<ParseObject> query = ParseQuery
					.getQuery(ParseConstants.TABLE_PROMOTION);
			query.whereEqualTo(ParseConstants.KEY_OBJECT_ID, promotionId);
			query.getFirstInBackground(new GetCallback<ParseObject>() {

				@Override
				public void done(ParseObject promotion, ParseException e) {
					getActivity().setProgressBarIndeterminateVisibility(false);
					if (e == null) {
						// success

						// Find all the Id
						mTxtPromotionTitle = (TextView) getActivity()
								.findViewById(R.id.txtPromotionTitle);
						mTextPromotionReq = (TextView) getActivity()
								.findViewById(R.id.textPromotionReq);
						mTextPromotionDesc = (TextView) getActivity()
								.findViewById(R.id.textPromotionDesc);
						mTextPromotionDuration = (TextView) getActivity()
								.findViewById(R.id.textPromotionDuration);

						// Get all the important variables
						String promoTitle = promotion
								.getString(ParseConstants.KEY_NAME);
						String promoRequirement = promotion
								.getString(ParseConstants.KEY_REQUIREMENT);
						String promoDescription = promotion
								.getString(ParseConstants.KEY_DESCRIPTION);
						Date promoStartDate = promotion
								.getDate(ParseConstants.KEY_START_DATE);
						Date promoEndDate = promotion
								.getDate(ParseConstants.KEY_END_DATE);

						// Put all the values
						mTxtPromotionTitle.setText(promoTitle);
						mTextPromotionReq.setText(promoRequirement);
						mTextPromotionDesc.setText(promoDescription);
						mTextPromotionDuration.setText(promoStartDate + " - "
								+ promoEndDate);

					} else {
						// failed
						errorAlertDialog(e);
					}

				}
			});
		}

		private void checkUserDistance() {
			if (currentLocation == null) {
				currentLocation = mLocationClient.getLastLocation();
			}

			// Check if user within the range of check in or not by
			// calculating the distance between user and location
			String tempLocation = "";
			placeSelected = new Location(tempLocation);
			placeSelected.setLatitude(placeCurrentLocation.getLatitude());
			placeSelected.setLongitude(placeCurrentLocation.getLongitude());

			float Distance = Math
					.abs(currentLocation.distanceTo(placeSelected));

			progressDialog.dismiss();
			if (Distance > MAX_DISTANCE) {
				// Dismis the progress dialog
				AlertDialog.Builder builder = new AlertDialog.Builder(
						getActivity());
				String message = "Sorry it's seems you too far away to check in to this place, "
						+ " do you want open maps to direction to this location";
				builder.setMessage(message)
						.setPositiveButton("Ok", dialogOpenMaps)
						.setNegativeButton("No", dialogOpenMaps).show();
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						getActivity());
				String message = "Hey you near this location, do you want to check in instead ?";
				builder.setMessage(message)
						.setPositiveButton("Ok", dialogNearLocation)
						.setNegativeButton("No", dialogNearLocation).show();
			}
		}

		/*
		 * Alert Dialog listener
		 */

		// if to far away open maps
		DialogInterface.OnClickListener dialogOpenMaps = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					String uri = String.format(Locale.ENGLISH, "geo:%f,%f",
							placeSelected.getLatitude(),
							placeSelected.getLongitude());
					Intent intent = new Intent(Intent.ACTION_VIEW,
							Uri.parse(uri));
					startActivity(intent);
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					// do nothing
					break;
				}
			}
		};

		DialogInterface.OnClickListener dialogNearLocation = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					saveUserActivity();
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					// do nothing
					break;
				}
			}
		};

		/*
		 * Parse Save User Check In if possible
		 */

		private void saveUserActivity() {
			initProgressDialog();
			String userId = ParseUser.getCurrentUser().getObjectId();
			ParseObject checkInActivity = new ParseObject(
					ParseConstants.TABLE_ACTV_USER_CHECK_IN_PLACE);
			checkInActivity.put(ParseConstants.KEY_USER_ID, userId);
			checkInActivity.put(ParseConstants.KEY_PLACE_ID, placeId);
			checkInActivity.saveInBackground(new SaveCallback() {

				@Override
				public void done(ParseException e) {
					progressDialog.dismiss();
					if (e == null) {
						// success
						Toast.makeText(getActivity(), "Check In Success",
								Toast.LENGTH_SHORT).show();
						Intent intent = new Intent(getActivity(),
								LocationInformation.class);
						intent.putExtra(ParseConstants.KEY_OBJECT_ID, placeId);
						startActivity(intent);
					} else {
						// failed
						errorAlertDialog(e);
					}
				}
			});
		}

		/*
		 * Parse Error Method
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

		@Override
		public void onConnected(Bundle arg0) {
			mLocationClient.requestLocationUpdates(REQUEST, this); // LocationListener
			Toast.makeText(getActivity(), "Connected", Toast.LENGTH_SHORT)
					.show();
			currentLocation = mLocationClient.getLastLocation();
		}

		@Override
		public void onDisconnected() {

		}

		@Override
		public void onLocationChanged(Location arg) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onConnectionFailed(ConnectionResult arg) {
			// TODO Auto-generated method stub

		}
	}

}
