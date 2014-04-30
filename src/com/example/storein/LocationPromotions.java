package com.example.storein;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class LocationPromotions extends Fragment {
	private static final String TAG = LocationPromotions.class.getSimpleName();

	// UI Variables
	ListView mListPromotion;
	ListView mListItem;

	// Fixed Variables
	private String placeId;
	protected ArrayList<HashMap<String, String>> promotionsInfo = new ArrayList<HashMap<String, String>>();
	public HashMap<String, String> promotionInfo = new HashMap<String, String>();
	public ArrayList<String> promotionsId = new ArrayList<String>();
	public ArrayList<Boolean> promotionsClaimable = new ArrayList<Boolean>();

	protected static final int MAX_ITEMS = 5;
	protected ArrayList<HashMap<String, String>> itemsInfo = new ArrayList<HashMap<String, String>>();
	public HashMap<String, String> itemInfo = new HashMap<String, String>();
	public ArrayList<String> itemsId = new ArrayList<String>();

	// Parse Variable
	ParseObject placeObj;

	public LocationPromotions() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_location_promotions,
				container, false);
		getPlaceID();

		// initiate UI
		mListPromotion = (ListView) rootView.findViewById(R.id.listPromotion);
		mListItem = (ListView) getActivity().findViewById(R.id.listItem);

		return rootView;

	}

	@Override
	public void onResume() {
		super.onResume();
		doPromotionQuery();
		doItemsQuery();
	}

	/*
	 * Clear ArrayList
	 */

	private void clearArrayList() {
		promotionInfo.clear();
		promotionsClaimable.clear();
		promotionsId.clear();
		promotionsInfo.clear();
	}

	/*
	 * Getter for placeId variables
	 */
	public String getPlaceID() {
		Bundle args = getArguments();
		placeId = args.getString(ParseConstants.KEY_OBJECT_ID);

		// Create parse Object
		placeObj = ParseObject.createWithoutData(ParseConstants.TABLE_PLACE,
				placeId);
		return placeId;
	}

	/*
	 * find top promotion list
	 */
	public void doPromotionQuery() {
		getActivity().setProgressBarIndeterminateVisibility(true);

		if (placeObj == null) {
			getPlaceID();
		}
		// Clear array list
		clearArrayList();

		// Do the query

		ParseQuery<ParseObject> query = ParseQuery
				.getQuery(ParseConstants.TABLE_REL_PROMOTION_PLACE);
		query.whereEqualTo(ParseConstants.KEY_PLACE_ID, placeObj);
		query.include(ParseConstants.KEY_PROMOTION_ID);
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> promotions, ParseException e) {
				getActivity().setProgressBarIndeterminateVisibility(false);

				if (e == null) {
					for (ParseObject promo : promotions) {
						// initiate new hash map
						HashMap<String, String> promotionInfo = new HashMap<String, String>();

						// get promotion value
						ParseObject tempPromo = promo
								.getParseObject(ParseConstants.KEY_PROMOTION_ID);
						String promoName = tempPromo
								.getString(ParseConstants.KEY_NAME);
						Boolean claimable = tempPromo
								.getBoolean(ParseConstants.KEY_CLAIMABLE);

						// put to the hash table
						if (claimable == true) {
							promotionInfo.put(ParseConstants.KEY_CLAIMABLE,
									"FLASH DEALS");
						} else {
							Date endDate = tempPromo
									.getDate(ParseConstants.KEY_END_DATE);
							promotionInfo.put(ParseConstants.KEY_CLAIMABLE,
									endDate.toString());
						}
						promotionInfo.put(ParseConstants.KEY_NAME, promoName);
						promotionsInfo.add(promotionInfo);
						promotionsId.add(tempPromo.getObjectId());
						promotionsClaimable.add(claimable);
					}
					setListPromotionAdapter();
					mListPromotion.setOnItemClickListener(promotionSelected);
				} else {
					// failed
					parseErrorDialog(e);
				}

			}
		});
	}

	/*
	 * On Item promotion click Listener
	 */
	OnItemClickListener promotionSelected = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			String promotionId = promotionsId.get(position);
			Boolean claimable = promotionsClaimable.get(position);
			Intent intent = new Intent(getActivity(), PromotionDetail.class);
			intent.putExtra(ParseConstants.KEY_OBJECT_ID, promotionId);
			intent.putExtra(ParseConstants.KEY_CLAIMABLE, claimable);
			intent.putExtra(ParseConstants.KEY_PLACE_ID, placeId);
			startActivity(intent);

		}
	};

	/*
	 * Set Adapter
	 */

	private void setListPromotionAdapter() {
		mListPromotion = (ListView) getActivity().findViewById(
				R.id.listPromotion);
		String[] keys = { ParseConstants.KEY_NAME, ParseConstants.KEY_CLAIMABLE };
		int[] ids = { android.R.id.text1, android.R.id.text2 };

		SimpleAdapter adapter = new SimpleAdapter(getActivity(),
				promotionsInfo, android.R.layout.simple_list_item_2, keys, ids);

		mListPromotion.setAdapter(adapter);
	}

	// ////////////////////////// Items Query

	protected void doItemsQuery() {
		// set progress bar
		getActivity().setProgressBarIndeterminateVisibility(true); 
		itemInfo.clear();
		itemsId.clear();
		itemsInfo.clear();

		if (placeId == null) {
			getPlaceID();
		}

		// Do the rest
		ParseQuery<ParseObject> query = ParseQuery
				.getQuery(ParseConstants.TABLE_REL_PLACE_ITEM);
		query.whereEqualTo(ParseConstants.KEY_PLACE_ID, placeId);
		ParseQuery<ParseObject> innerQuery = ParseQuery
				.getQuery(ParseConstants.TABLE_ITEM);
		innerQuery.whereMatchesKeyInQuery(ParseConstants.KEY_OBJECT_ID,
				ParseConstants.KEY_ITEM_ID, query);

		innerQuery.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> items, ParseException e) {
				getActivity().setProgressBarIndeterminateVisibility(false);
				if (e == null) {
					for (ParseObject item : items) {
						// initiate the hash map for the adapter
						HashMap<String, String> itemInfo = new HashMap<String, String>();

						// get the values
						String objectId = item.getObjectId();
						String name = item.getString(ParseConstants.KEY_NAME);
						Integer rating = item.getInt(ParseConstants.KEY_RATING);

						// put to the adapter
						itemInfo.put(ParseConstants.KEY_NAME, name);
						itemInfo.put(ParseConstants.KEY_RATING,
								rating.toString());
						itemsInfo.add(itemInfo);
						itemsId.add(objectId);

					}
					setListItemAdapter();
					mListItem.setOnItemClickListener(itemSelected);

				} else {
					parseErrorDialog(e);
				}
			}

		});
	}

	/*
	 * On Click Item Listener
	 */

	OnItemClickListener itemSelected = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// Get the Variable
			String objectId = itemsId.get(position);

			// Start intent
			final Intent intent = new Intent(getActivity(),
					ItemInformation.class);
			intent.putExtra(ParseConstants.KEY_OBJECT_ID, objectId);
			startActivity(intent);
		}
	};

	/*
	 * Set Adapter
	 */
	private void setListItemAdapter() {
		mListItem = (ListView) getActivity().findViewById(
				R.id.listItem);
		
		String[] keys = { ParseConstants.KEY_NAME, ParseConstants.KEY_RATING };
		int[] ids = { android.R.id.text1, android.R.id.text2 };

		SimpleAdapter adapter = new SimpleAdapter(getActivity(), itemsInfo,
				android.R.layout.simple_list_item_2, keys, ids);

		mListItem.setAdapter(adapter);
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
