package com.example.storein;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class PromotionList extends ActionBarActivity {
	// Variables
	public static final String TAG = PromotionList.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_promotion_list);

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
		getMenuInflater().inflate(R.menu.promotion_list, menu);
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
		// UI Variable
		ListView mListPromotions;

		// Variables
		protected String categoryId;
		protected ArrayList<HashMap<String, String>> promotionsInfo = new ArrayList<HashMap<String, String>>();
		public HashMap<String, String> promotionInfo = new HashMap<String, String>();
		protected ArrayList<String> objectsId = new ArrayList<String>();

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_promotion_list,
					container, false);
			categoryId = getActivity().getIntent().getExtras()
					.getString(ParseConstants.KEY_OBJECT_ID);
			return rootView;
		}

		@Override
		public void onResume() {
			super.onResume();
		}

		/*
		 * Added Function
		 */

		// Find the list of the promotion according the PromotionID

		public void doPromotionQuery() {
			ParseQuery<ParseObject> query = ParseQuery
					.getQuery(ParseConstants.TABLE_PROMOTION);
			query.whereEqualTo(ParseConstants.KEY_CATEGORY_ID, categoryId);
			query.findInBackground(new FindCallback<ParseObject>() {
				@Override
				public void done(List<ParseObject> promotions, ParseException e) {
					if (e == null) {
						for (ParseObject promotion : promotions) {
							// put promotion objectId to array list
							String tempObjectId = promotion.getObjectId();
							objectsId.add(tempObjectId);

							// get name of promotion
							final String promoName = promotion
									.getString(ParseConstants.KEY_NAME);

							// find the location(s) of promotion
							String objectId = promotion.getObjectId();
							ParseQuery<ParseObject> query = ParseQuery
									.getQuery(ParseConstants.TABLE_REL_PROMOTION_PLACE);
							query.whereEqualTo(ParseConstants.KEY_PROMOTION_ID,
									objectId);
							ParseQuery<ParseObject> innerQuery = ParseQuery
									.getQuery(ParseConstants.TABLE_PLACE);
							innerQuery.whereMatchesKeyInQuery(
									ParseConstants.KEY_OBJECT_ID,
									ParseConstants.KEY_PLACE_ID, query);

							innerQuery
									.findInBackground(new FindCallback<ParseObject>() {
										@Override
										public void done(
												List<ParseObject> places,
												ParseException e) {
											if (e == null) {
												// success
												for (ParseObject place : places) {
													HashMap<String, String> promotionInfo = new HashMap<String, String>();
													String address = place
															.getString(ParseConstants.KEY_NAME);
													promotionInfo
															.put(ParseConstants.KEY_NAME,
																	promoName);
													promotionInfo
															.put(ParseConstants.KEY_ADDRESS,
																	address);
												}
											} else {
												// failed
											}
										}
									});

						}
					} else {
						// failed
					}

				}
			});
		}
	}

}
