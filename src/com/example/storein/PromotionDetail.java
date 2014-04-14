package com.example.storein;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
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
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class PromotionDetail extends ActionBarActivity {
	// Variables
	public static final String TAG = PromotionDetail.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_promotion_detail);
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

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.promotion_detail, menu);
		return true;
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

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(
					R.layout.fragment_promotion_detail, container, false);
			mLocationList = (ListView) rootView.findViewById(R.id.LocationsList);
			getPromotionId();

			return rootView;
		}

		@Override
		public void onResume() {
			super.onResume();
			findPromotionDetail();
			findPromotionLocation();
		}

		/*
		 * Added Functions
		 */
		protected void getPromotionId() {
			promotionId = (String) getActivity().getIntent().getExtras()
					.get(ParseConstants.KEY_OBJECT_ID);
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
		 * Do Query to find promotion Location(s)
		 */

		public void findPromotionLocation() {
			if (promotionId == null) {
				getPromotionId();
			}
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
							HashMap<String, String> locationInfo = new HashMap<String, String>();
							ParseObject placeDetail = place
									.getParseObject(ParseConstants.KEY_PLACE_ID);
							String placeName = placeDetail
									.getString(ParseConstants.KEY_NAME);
							String placeAddress = placeDetail
									.getString(ParseConstants.KEY_ADDRESS);
							String objectId = placeDetail.getObjectId();

							locationInfo
									.put(ParseConstants.KEY_NAME, placeName);
							locationInfo.put(ParseConstants.KEY_ADDRESS,
									placeAddress);
							locationsInfo.add(locationInfo);
							objectsId.add(objectId);

						}
						// setup adapter
						setAdapter();

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
