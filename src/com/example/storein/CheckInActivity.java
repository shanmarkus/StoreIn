package com.example.storein;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
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
import android.widget.Toast;

import com.example.storein.adapter.CustomArrayAdapterPlace;
import com.example.storein.model.Place;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class CheckInActivity extends ActionBarActivity {
	protected final static String TAG = CheckInActivity.class.getSimpleName()
			.toString();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_check_in_fragment);
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_edit_profile) {
			Intent intent = new Intent(this, EditProfile.class);
			startActivity(intent);
		}
		// Log out menu item
		else if (id == R.id.action_logout) {
			ParseUser.logOut();
			Intent intent = new Intent(this, LoginActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unbindDrawables(findViewById(R.id.container));
		System.gc();
	}

	/*
	 * Unbind resource to remove memmory alocation
	 */

	private void unbindDrawables(View view) {
		if (view.getBackground() != null) {
			view.getBackground().setCallback(null);
		}
		if (view instanceof ViewGroup) {
			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
				unbindDrawables(((ViewGroup) view).getChildAt(i));
			}
			((ViewGroup) view).removeAllViews();
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment implements
			ConnectionCallbacks, OnConnectionFailedListener, LocationListener,
			OnMyLocationButtonClickListener {

		// UI Variable
		ListView mListPlace;

		// Variables

		public List<Place> placeRecord = new ArrayList<Place>();
		public ArrayList<Place> placesItem;
		private CustomArrayAdapterPlace mAdapter;

		private String placeName;
		private String placeID;
		private ProgressDialog progressDialog;

		// Place Constant
		private Location currentLocation = null;

		// Location Client
		private LocationClient mLocationClient;

		// Parse Constants
		private static final int MAX_PLACE_SEARCH_RESULTS = 20;
		private static final float MAX_PlACE_SEARCH_DISTANCE = 0.5f; // In
																		// Kilometers

		private static final LocationRequest REQUEST = LocationRequest.create()
				.setFastestInterval(16) // 16ms = 60fps
				.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.fragment_check_in, container,
					false);

			// Setup Location Client
			mLocationClient = new LocationClient(getActivity(), this, this);

			// Adding Location Manager
			LocationManager locationManager = (LocationManager) getActivity()
					.getSystemService(Context.LOCATION_SERVICE);

			// Setup GPS
			if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				Toast.makeText(getActivity(), "GPS is Enabled in your devide",
						Toast.LENGTH_SHORT).show();
			} else {
				showGPSDisabledAlertToUser();
			}
			return view;
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
		}

		@Override
		public void onResume() {
			super.onResume();
			// Clear array list first
			clearAdapter();

			// set up location listener
			setUpLocationClientIfNeeded();
			mLocationClient.connect();

			// On Click listener
			onListPlaceClickListener();

		}

		@Override
		public void onPause() {
			super.onPause();
			if (mLocationClient != null) {
				mLocationClient.disconnect();
			}
		}

		/*
		 * Function Added
		 */

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
		 * Clear the array list when resume
		 */

		private void clearAdapter() {
			placeRecord.clear();
		}

		/*
		 * Location Query
		 */

		private void doLocationQuery() {

			// Clear ArrayList
			clearAdapter();

			getActivity().setProgressBarIndeterminateVisibility(true);

			ParseGeoPoint location = new ParseGeoPoint(
					currentLocation.getLatitude(),
					currentLocation.getLongitude());

			// Do the Query
			ParseObject.registerSubclass(ParsePlace.class);
			ParseQuery<ParsePlace> query = ParsePlace.getQuery();
			query.whereWithinKilometers(ParseConstants.KEY_LOCATION, location,
					MAX_PlACE_SEARCH_DISTANCE);
			query.orderByAscending(ParseConstants.KEY_NAME);
			query.setLimit(MAX_PLACE_SEARCH_RESULTS);
			query.include(ParseConstants.KEY_CATEGORY);
			query.findInBackground(new FindCallback<ParsePlace>() {

				@Override
				public void done(List<ParsePlace> places, ParseException e) {
					getActivity().setProgressBarIndeterminateVisibility(false);
					if (e == null) {
						// success
						if (places.size() > 0) {
							for (ParsePlace place : places) {
								placeName = place.getName();
								String address = place.getAddress();
								String id = place.getObjectId();

								ParseObject category = place
										.getParseObject(ParseConstants.KEY_CATEGORY);
								String placeCategory = category
										.getString(ParseConstants.KEY_NAME);

								JSONArray promotions = place
										.getJSONArray(ParseConstants.KEY_LIST_PROMOTION);

								int totalPromotion = promotions.length();

								// add to the place
								Place temp = new Place();
								temp.setName(placeName);
								temp.setAddress(address);
								temp.setCategory(placeCategory);
								temp.setObjectId(id);
								if (totalPromotion != 0) {
									temp.setIsPromotion(true);
								} else {
									temp.setIsPromotion(false);
								}

								// add to list
								placeRecord.add(temp);
							}
							setCustomAdapter();
						} else {
							String message = "Sorry there are no promotion near you, please use browse to find other promotion";
							Toast.makeText(getActivity(), message,
									Toast.LENGTH_LONG).show();
						}
					} else {
						errorAlertDialog(e);
					}

				}
			});
		}

		/*
		 * Get user previous location (in Parse)
		 */

		private void initFindPlace() {
			currentLocation = mLocationClient.getLastLocation();
			if (currentLocation != null) {
				doLocationQuery();
			} else {
				Toast.makeText(getActivity(), "Cannot get user Location",
						Toast.LENGTH_SHORT).show();
			}

		}

		/*
		 * Set Adapter for list view
		 */

		public void setCustomAdapter() {

			placesItem = (ArrayList<Place>) placeRecord;
			mAdapter = new CustomArrayAdapterPlace(getActivity(),
					R.id.listPlace, placesItem);
			mListPlace = (ListView) getActivity().findViewById(R.id.listPlace);
			mListPlace.setAdapter(mAdapter);
		}

		/*
		 * Set Listener to the ListView to open other intent
		 */

		public void onListPlaceClickListener() {
			mListPlace = (ListView) getActivity().findViewById(R.id.listPlace);

			mListPlace.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					placeID = placesItem.get(position).getObjectId();
					AlertDialog.Builder builder = new AlertDialog.Builder(
							getActivity());
					placeName = placesItem.get(position).getName();
					String message = "Check In at " + placeName;
					builder.setMessage(message)
							.setPositiveButton("Ok", dialogCheckInListener)
							.setNeutralButton("Share", dialogCheckInListener)
							.setNegativeButton("No", dialogCheckInListener)
							.show();
				}
			});
		}

		/*
		 * Alert Dialog For Check In
		 */

		DialogInterface.OnClickListener dialogCheckInListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					saveUserActivity();
					break;

				case DialogInterface.BUTTON_NEUTRAL:
					Intent sendIntent = new Intent();
					sendIntent.setAction(Intent.ACTION_SEND);
					sendIntent.putExtra(Intent.EXTRA_TEXT,
							"Hey I Just Check In at " + placeName
									+ " Using StoreIn - Available on GooglePlay");
					sendIntent.setType("text/plain");
					startActivity(sendIntent);
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

			ParseObject tempUser = ParseObject.createWithoutData(
					ParseConstants.TABLE_USER, userId);
			ParseObject tempPlace = ParseObject.createWithoutData(
					ParseConstants.TABLE_PLACE, placeID);

			ParseObject checkInActivity = new ParseObject(
					ParseConstants.TABLE_ACTV_USER_CHECK_IN_PLACE);
			checkInActivity.put(ParseConstants.KEY_USER_ID, tempUser);
			checkInActivity.put(ParseConstants.KEY_PLACE_ID, tempPlace);
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
						intent.putExtra(ParseConstants.KEY_OBJECT_ID, placeID);
						startActivity(intent);
					} else {
						// failed
						errorAlertDialog(e);
					}
				}
			});
		}

		/*
		 * Error Dialog
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

		/*
		 * Check GPS Function
		 */

		// Check GPS
		private void showGPSDisabledAlertToUser() {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					getActivity());
			alertDialogBuilder
					.setMessage(
							"GPS is disabled in your device. Would you like to enable it?")
					.setCancelable(false)
					.setPositiveButton("Goto Settings Page To Enable GPS",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									Intent callGPSSettingIntent = new Intent(
											android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
									startActivity(callGPSSettingIntent);
								}
							});
			alertDialogBuilder.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
			AlertDialog alert = alertDialogBuilder.create();
			alert.show();
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

		/**
		 * Callback called when connected to GCore. Implementation of
		 * {@link ConnectionCallbacks}.
		 */
		@Override
		public void onConnected(Bundle connectionHint) {
			mLocationClient.requestLocationUpdates(REQUEST, this); // LocationListener
			Toast.makeText(getActivity(), "Connected", Toast.LENGTH_SHORT)
					.show();
			initFindPlace();
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

			return false;
		}

		@Override
		public void onLocationChanged(Location e) {

		}
	}

}
