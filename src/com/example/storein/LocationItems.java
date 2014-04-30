package com.example.storein;

import java.util.ArrayList;
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

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

/**
 * A placeholder fragment containing a simple view.
 */
public class LocationItems extends Fragment {
	// TAG
	private static final String TAG = LocationDetail.class.getSimpleName();

	// UI Variables
	ListView mListItem;

	// Variables
	protected String placeId;
	protected String userId = ParseUser.getCurrentUser().getObjectId();

	// Fixed Constants
	protected static final int MAX_ITEMS = 5;
	protected ArrayList<HashMap<String, String>> itemsInfo = new ArrayList<HashMap<String, String>>();
	public HashMap<String, String> itemInfo = new HashMap<String, String>();
	public ArrayList<String> itemsId = new ArrayList<String>();

	public LocationItems() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_location_items,
				container, false);

		// UI Declaration
		mListItem = (ListView) getActivity().findViewById(R.id.listItem);

		// get the placeId
		getPlaceID();

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		doItemsQuery();
	}

	/*
	 * Getter for placeId variables
	 */
	public String getPlaceID() {
		Bundle args = getArguments();
		placeId = args.getString(ParseConstants.KEY_OBJECT_ID);
		return placeId;
	}

	/*
	 * query for finding top items in the place
	 */

	protected void doItemsQuery() {
		getActivity().setProgressBarIndeterminateVisibility(true); // set
																	// progress
																	// bar
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
					// setListItemAdapter();
					// mListItem.setOnItemClickListener(itemSelected);

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
