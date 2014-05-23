package com.example.storein;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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

public class Stash extends ActionBarActivity {

	private final static String TAG = Stash.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stash);

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

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		// UI Declaration
		ListView mStashListClaimedPromotion;

		// Variables
		ArrayList<HashMap<String, String>> claimedPromotionsList = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> claimedPromotionList = new HashMap<String, String>();
		ArrayList<String> claimedPromotionsId = new ArrayList<String>();

		ParseUser user = ParseUser.getCurrentUser();
		String userId;

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_stash,
					container, false);
			// Get variable
			userId = user.getObjectId();
			// Declare UI
			mStashListClaimedPromotion = (ListView) rootView
					.findViewById(R.id.stashListClaimedPromotion);
			return rootView;
		}

		@Override
		public void onResume() {
			super.onResume();
			getAllList(userId);
		}

		/*
		 * Added Function
		 */

		/*
		 * Clear ArrayList
		 */

		private void clearArray() {
			claimedPromotionsId.clear();
			claimedPromotionList.clear();
			claimedPromotionsList.clear();
		}

		/*
		 * Getting all list claimed promotion
		 */

		private void getAllList(String userId) {
			// Clear ArrayList
			clearArray();
			;
			// Do the query
			ParseObject currentUser = ParseObject.createWithoutData(
					ParseConstants.TABLE_USER, userId);
			ParseQuery<ParseObject> query = ParseQuery
					.getQuery(ParseConstants.TABLE_ACTV_USER_CLAIM_PROMOTION);
			query.whereEqualTo(ParseConstants.KEY_USER_ID, currentUser);
			query.whereEqualTo(ParseConstants.KEY_IS_CLAIMED, false);
			query.orderByDescending(ParseConstants.KEY_CREATED_AT);
			query.include(ParseConstants.KEY_PROMOTION_ID);
			query.include(ParseConstants.KEY_PLACE_ID);
			query.findInBackground(new FindCallback<ParseObject>() {

				@Override
				public void done(List<ParseObject> claimedPromotions,
						ParseException e) {
					if (e == null) {
						for (ParseObject claimedPromotion : claimedPromotions) {
							HashMap<String, String> claimedPromotionList = new HashMap<String, String>();

							// Get object
							ParseObject currentPromotion = claimedPromotion
									.getParseObject(ParseConstants.KEY_PROMOTION_ID);
							ParseObject currentLocation = claimedPromotion
									.getParseObject(ParseConstants.KEY_PLACE_ID);

							String namePromotion = currentPromotion
									.getString(ParseConstants.KEY_NAME);
							String namePlace = currentLocation
									.getString(ParseConstants.KEY_NAME);
							String claimedPromotionId = claimedPromotion
									.getObjectId();

							claimedPromotionList.put(
									ParseConstants.KEY_PROMOTION_ID,
									namePromotion);
							claimedPromotionList.put(
									ParseConstants.KEY_PLACE_ID, namePlace);
							claimedPromotionsList.add(claimedPromotionList);

							// Add id
							claimedPromotionsId.add(claimedPromotionId);
						}
						setAdapter();
						mStashListClaimedPromotion
								.setOnItemClickListener(onClaimedPromotionClick);
					} else {
						errorAlertDialog(e);
					}
				}
			});
		}

		/*
		 * Setup adapter
		 */
		public void setAdapter() {

			String[] keys = { ParseConstants.KEY_PROMOTION_ID,
					ParseConstants.KEY_PLACE_ID };
			int[] ids = { android.R.id.text1, android.R.id.text2 };

			SimpleAdapter adapter = new SimpleAdapter(getActivity(),
					claimedPromotionsList, android.R.layout.simple_list_item_2,
					keys, ids);
			mStashListClaimedPromotion.setAdapter(adapter);
		}

		/*
		 * On Item Click Listener
		 */

		OnItemClickListener onClaimedPromotionClick = new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String objectId = claimedPromotionsId.get(position);
				AlertDialog.Builder builder = new AlertDialog.Builder(
						getActivity());
				String title = "Please give this number to claim the reward";
				String message = "Promotion Id " + objectId;
				builder.setTitle(title).setMessage(message)
						.setPositiveButton("Ok", dialogCheckInListener).show();
			}
		};

		/*
		 * Initiate Alert Dialog
		 */

		DialogInterface.OnClickListener dialogCheckInListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					// Do nothing
					break;
				}
			}
		};

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

	}

}
