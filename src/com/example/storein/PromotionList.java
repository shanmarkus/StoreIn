package com.example.storein;

import java.util.ArrayList;
import java.util.HashMap;
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
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

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
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
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
			mListPromotions = (ListView) rootView
					.findViewById(R.id.listPromotions);
			categoryId = getActivity().getIntent().getExtras()
					.getString(ParseConstants.KEY_OBJECT_ID);
			return rootView;
		}

		@Override
		public void onResume() {
			super.onResume();
			if (promotionsInfo.size() == 0) {
				doPromotionQuery();
			}
			onClickPromotionListener();
		}

		/*
		 * Added Function
		 */

		// Find the list of the promotion according the PromotionID

		public void doPromotionQuery() {
			getActivity().setProgressBarIndeterminateVisibility(true);
			ParseQuery<ParseObject> query = ParseQuery
					.getQuery(ParseConstants.TABLE_PROMOTION);
			query.whereEqualTo(ParseConstants.KEY_CATEGORY_ID, categoryId);
			// Set only the general Promotion
			query.whereEqualTo(ParseConstants.KEY_CLAIMABLE, false);

			ParseQuery<ParseObject> innerQuery = ParseQuery
					.getQuery(ParseConstants.TABLE_REL_PROMOTION_PLACE);
			innerQuery.whereMatchesKeyInQuery(ParseConstants.KEY_PROMOTION_ID,
					ParseConstants.KEY_OBJECT_ID, query);
			innerQuery.include(ParseConstants.KEY_PROMOTION_ID);
			innerQuery.include(ParseConstants.KEY_PLACE_ID);

			innerQuery.findInBackground(new FindCallback<ParseObject>() {

				@Override
				public void done(List<ParseObject> results, ParseException e) {
					getActivity().setProgressBarIndeterminateVisibility(false);
					if (e == null) {
						for (ParseObject result : results) {
							HashMap<String, String> promotionInfo = new HashMap<String, String>();

							ParseObject promotion = result
									.getParseObject(ParseConstants.KEY_PROMOTION_ID);
							ParseObject place = result
									.getParseObject(ParseConstants.KEY_PLACE_ID);
							String promotionName = promotion
									.getString(ParseConstants.KEY_NAME);
							String placeName = place
									.getString(ParseConstants.KEY_NAME);
							String objectId = promotion.getObjectId();

							promotionInfo.put(ParseConstants.KEY_NAME,
									promotionName);
							promotionInfo.put(ParseConstants.KEY_ADDRESS,
									placeName);
							objectsId.add(objectId);
							promotionsInfo.add(promotionInfo);

						}

						// set adapter
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

		// Setting adapter for List

		public void setAdapter() {
			String[] keys = { ParseConstants.KEY_NAME,
					ParseConstants.KEY_ADDRESS };
			int[] ids = { android.R.id.text1, android.R.id.text2 };

			SimpleAdapter adapter = new SimpleAdapter(getActivity(),
					promotionsInfo, android.R.layout.simple_list_item_2, keys,
					ids);

			mListPromotions.setAdapter(adapter);
		}

		// Set on Click Listener

		public void onClickPromotionListener() {
			mListPromotions.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					String objectId = objectsId.get(position);
					Intent intent = new Intent(getActivity(),
							PromotionDetailAndLocations.class);
					intent.putExtra(ParseConstants.KEY_OBJECT_ID, objectId);
					startActivity(intent);
				}
			});
		}
	}

}
