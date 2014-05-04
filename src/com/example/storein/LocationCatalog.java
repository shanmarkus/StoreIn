package com.example.storein;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.example.storein.adapter.CustomArrayAdapterItem;
import com.example.storein.adapter.CustomArrayAdapterPromotion;
import com.example.storein.model.Item;
import com.example.storein.model.Promotion;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class LocationCatalog extends Fragment {
	private static final String TAG = LocationCatalog.class.getSimpleName();

	// UI Variables
	ListView mListPromotion;
	ListView mListItem;

	// Fixed Variables
	private String placeId;

	public List<Promotion> promotionList = new ArrayList<Promotion>();
	public ArrayList<Promotion> promotionRecord;
	private CustomArrayAdapterPromotion mPromotionAdapter;

	protected static final int MAX_ITEMS = 5;

	public List<Item> itemList = new ArrayList<Item>();
	public ArrayList<Item> itemRecord;
	private CustomArrayAdapterItem mItemAdapter;

	// Parse Variable
	ParseObject placeObj;

	public LocationCatalog() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_location_catalog,
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
		if(promotionRecord != null || itemRecord !=null){
			promotionRecord.clear();
			itemRecord.clear();
		}
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
						String promoId = tempPromo.getObjectId();
						String promoName = tempPromo
								.getString(ParseConstants.KEY_NAME);
						Date promoEndDate = tempPromo
								.getDate(ParseConstants.KEY_END_DATE);
						Integer promoReward = tempPromo
								.getInt(ParseConstants.KEY_REWARD_POINT);
						Boolean claimable = tempPromo
								.getBoolean(ParseConstants.KEY_CLAIMABLE);

						// Create new Promotion Object
						Promotion tempPromotion = new Promotion();
						tempPromotion.setObjectId(promoId);
						tempPromotion.setName(promoName);
						tempPromotion.setRewardPoint(promoReward);
						tempPromotion.setClaimable(claimable);
						tempPromotion.setEndDate(promoEndDate);

						promotionList.add(tempPromotion);

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
			String promotionId = promotionList.get(position).getObjectId();
			Boolean claimable = promotionList.get(position).isClaimable();
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
		promotionRecord = (ArrayList<Promotion>) promotionList;
		mPromotionAdapter = new CustomArrayAdapterPromotion(getActivity(),
				R.id.listPromotion, promotionRecord);
		mListPromotion = (ListView) getActivity().findViewById(
				R.id.listPromotion);
		mListPromotion.setAdapter(mPromotionAdapter);
	}

	// ////////////////////////// Items Query

	protected void doItemsQuery() {
		// set progress bar
		getActivity().setProgressBarIndeterminateVisibility(true);

		if (placeId == null) {
			getPlaceID();
		}

		// Do the rest

		ParseQuery<ParseObject> query = ParseQuery
				.getQuery(ParseConstants.TABLE_REL_PLACE_ITEM);
		query.whereEqualTo(ParseConstants.KEY_PLACE_ID, placeObj);
		query.include(ParseConstants.KEY_ITEM_ID);
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> locations, ParseException e) {
				getActivity().setProgressBarIndeterminateVisibility(false);
				if (e == null) {
					for (ParseObject location : locations) {

						ParseObject item = location
								.getParseObject(ParseConstants.KEY_ITEM_ID);
						// get the values
						String objectId = item.getObjectId();
						String tempName = item
								.getString(ParseConstants.KEY_NAME);
						String description = item
								.getString(ParseConstants.KEY_DESCRIPTION);
						Integer totalLoved = item
								.getInt(ParseConstants.KEY_TOTAL_LOVED);

						// create new item
						Item tempItem = new Item();
						tempItem.setName(tempName);
						tempItem.setObjectId(objectId);
						tempItem.setDescription(description);
						tempItem.setItemLoved(totalLoved);

						itemList.add(tempItem);
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
			String objectId = itemList.get(position).getObjectId();

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
		itemRecord = (ArrayList<Item>) itemList;
		mItemAdapter = new CustomArrayAdapterItem(getActivity(), R.id.listItem,
				itemRecord);
		mListItem = (ListView) getActivity().findViewById(R.id.listItem);
		mListItem.setAdapter(mItemAdapter);
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
