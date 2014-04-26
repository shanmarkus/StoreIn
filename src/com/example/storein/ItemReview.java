package com.example.storein;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class ItemReview extends Fragment {
	// TAG
	private static final String TAG = ItemReview.class.getSimpleName();

	// UI Declaration
	ListView mListUsersReview;

	// Variable
	protected ArrayList<HashMap<String, String>> reviewsList = new ArrayList<HashMap<String, String>>();
	public HashMap<String, String> reviewList = new HashMap<String, String>();
	public ArrayList<String> itemsID = new ArrayList<String>();
	protected String itemId;

	public ItemReview() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_item_review,
				container, false);
		
		// Get Variable
		getItemId();

		// Setup UI Variable
		mListUsersReview = (ListView) rootView
				.findViewById(R.id.listUsersReview);

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		getUsersReview();
	}

	/*
	 * Getter for placeId variables
	 */
	public String getItemId() {
		Bundle args = getArguments();
		itemId = args.getString(ParseConstants.KEY_OBJECT_ID);
		return itemId;
	}

	public void getUsersReview() {
		getActivity().setProgressBarIndeterminateVisibility(true);
		if (itemId == null) {
			getItemId();
		}
		ParseQuery<ParseObject> query = ParseQuery
				.getQuery(ParseConstants.TABLE_ITEM_REVIEW);
		query.whereEqualTo(ParseConstants.KEY_ITEM_ID, itemId);
		query.include(ParseConstants.KEY_USER_ID);
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> reviews, ParseException e) {
				getActivity().setProgressBarIndeterminateVisibility(false);
				if (e == null) {
					// success
					for (ParseObject review : reviews) {
						// Setup Hash Map
						HashMap<String, String> reviewList = new HashMap<String, String>();

						// Get User Information
						ParseUser userInfo = review
								.getParseUser(ParseConstants.KEY_USER_ID);
						String userName = userInfo.getUsername();
						reviewList.put(ParseConstants.KEY_NAME, userName);

						// Get the review text Information
						String txtReview = review
								.getString(ParseConstants.KEY_REVIEW);

						reviewList.put(ParseConstants.KEY_REVIEW, txtReview);

						reviewsList.add(reviewList);
					}
					// Set the list View
					setAdapter();
				} else {
					// failed
					parseErrorDialog(e);
				}
			}
		});
	}

	/*
	 * Setup adapter
	 */
	public void setAdapter() {
		String[] keys = { ParseConstants.KEY_NAME, ParseConstants.KEY_REVIEW };
		int[] ids = { android.R.id.text1, android.R.id.text2 };

		SimpleAdapter adapter = new SimpleAdapter(getActivity(), reviewsList,
				android.R.layout.simple_list_item_2, keys, ids);

		mListUsersReview.setAdapter(adapter);
	}

	/*
	 * Debug if ParseException throw the alert dialog
	 */

	protected void parseErrorDialog(ParseException e) {
		Log.e(TAG, e.getMessage());
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(e.getMessage()).setTitle(R.string.error_title)
				.setPositiveButton(android.R.string.ok, null);
		AlertDialog dialog = builder.create();
		dialog.show();
	}
}
