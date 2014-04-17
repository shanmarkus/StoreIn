package com.example.storein;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
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
import android.widget.Toast;

import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class LocationCatalog extends ActionBarActivity {

	protected static final String TAG = LocationCatalog.class.getSimpleName();
	protected static String placeID;
	protected static String itemID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_location_catalog);
		placeID = getIntent().getExtras().getString(
				ParseConstants.KEY_OBJECT_ID);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.location_catalog, menu);
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
		// User Interface Variable
		ViewPager mViewPager;
		ListView mListItem;
		ListView mListPromotion;

		// Fixed Constants
		protected static final int MAX_ITEMS = 5;
		protected ArrayList<HashMap<String, String>> itemsInfo = new ArrayList<HashMap<String, String>>();
		public HashMap<String, String> itemInfo = new HashMap<String, String>();
		public ArrayList<String> itemsID = new ArrayList<String>();

		protected ArrayList<HashMap<String, String>> promotionsInfo = new ArrayList<HashMap<String, String>>();
		public HashMap<String, String> promotionInfo = new HashMap<String, String>();
		public ArrayList<String> promotionsId = new ArrayList<String>();
		public ArrayList<Boolean> promotionsClaimable = new ArrayList<Boolean>();

		public PlaceholderFragment() {
		}

		@Override
		public void onResume() {
			super.onResume();
			doItemQuery();
			doPromotionQuery();
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(
					R.layout.fragment_location_catalog, container, false);

			mViewPager = (ViewPager) rootView.findViewById(R.id.view_pager);
			mListItem = (ListView) rootView.findViewById(R.id.listItem);
			mListPromotion = (ListView) rootView
					.findViewById(R.id.listPromotion);

			return rootView;
		}

		/*
		 * Added Function
		 */

		/*
		 * find top promotion list
		 */
		public void doPromotionQuery() {
			// Clear the Array
			promotionsInfo.clear();
			promotionsClaimable.clear();
			promotionsId.clear();

			// Do the Rest
			getActivity().setProgressBarIndeterminateVisibility(true);
			ParseObject obj = ParseObject.createWithoutData(
					ParseConstants.TABLE_PLACE, placeID);
			ParseQuery<ParseObject> query = ParseQuery
					.getQuery(ParseConstants.TABLE_REL_PROMOTION_PLACE);
			query.whereEqualTo(ParseConstants.KEY_PLACE_ID, obj);
			query.include(ParseConstants.KEY_PROMOTION_ID);
			query.findInBackground(new FindCallback<ParseObject>() {

				@Override
				public void done(List<ParseObject> promotions, ParseException e) {
					getActivity().setProgressBarIndeterminateVisibility(false);
					if (e == null) {
						Toast.makeText(getActivity(), promotions.size() + " ",
								Toast.LENGTH_SHORT).show();
						for (ParseObject promo : promotions) {
							HashMap<String, String> promotionInfo = new HashMap<String, String>();
							Toast.makeText(getActivity(),
									promotions.size() + " ", Toast.LENGTH_SHORT)
									.show();
							ParseObject tempPromo = promo
									.getParseObject(ParseConstants.KEY_PROMOTION_ID);
							String promoName = tempPromo
									.getString(ParseConstants.KEY_NAME);
							Boolean claimable = tempPromo
									.getBoolean(ParseConstants.KEY_CLAIMABLE);
							if (claimable == true) {
								promotionInfo.put(ParseConstants.KEY_CLAIMABLE,
										"FLASH DEALS");
							} else {
								Date endDate = tempPromo
										.getDate(ParseConstants.KEY_END_DATE);
								promotionInfo.put(ParseConstants.KEY_CLAIMABLE,
										endDate.toString());
							}
							promotionInfo.put(ParseConstants.KEY_NAME,
									promoName);
							promotionsInfo.add(promotionInfo);
							promotionsId.add(tempPromo.getObjectId());
							promotionsClaimable.add(claimable);
						}
						setListPromotionAdapter();
						onPromotionClickListener();
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
		 * On Click Listener for Promotion
		 */

		protected void onPromotionClickListener() {

			mListPromotion.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					String promotionId = promotionsId.get(position);
					Boolean claimable = promotionsClaimable.get(position);
					if (claimable == true) {
						Intent intent = new Intent(getActivity(),
								ClaimPromotion.class);
						intent.putExtra(ParseConstants.KEY_OBJECT_ID,
								promotionId);
						startActivity(intent);
					} else {
						Intent intent = new Intent(getActivity(),
								PromotionDetail.class);
						intent.putExtra(ParseConstants.KEY_OBJECT_ID,
								promotionId);
						startActivity(intent);
					}

				}
			});
		}

		/*
		 * Getting Top Items on a place
		 */

		public void doItemQuery() {
			// Clear the array first
			itemsID.clear();
			itemsInfo.clear();

			// Do the rest
			ParseQuery<ParseObject> query = ParseQuery
					.getQuery(ParseConstants.TABLE_REL_PLACE_ITEM);
			query.whereEqualTo(ParseConstants.KEY_PLACE_ID, placeID);
			ParseQuery<ParseObject> innerQuery = ParseQuery
					.getQuery(ParseConstants.TABLE_ITEM);
			innerQuery.whereMatchesKeyInQuery(ParseConstants.KEY_OBJECT_ID,
					ParseConstants.KEY_ITEM_ID, query);

			innerQuery.findInBackground(new FindCallback<ParseObject>() {

				@Override
				public void done(List<ParseObject> items, ParseException e) {
					getActivity().setProgressBarIndeterminateVisibility(false);
					final ArrayList<String> objectsId = new ArrayList<String>();
					if (e == null) {
						for (ParseObject item : items) {
							String objectId = item.getObjectId();
							String name = item
									.getString(ParseConstants.KEY_NAME);
							Integer rating = item
									.getInt(ParseConstants.KEY_RATING);
							HashMap<String, String> itemInfo = new HashMap<String, String>();
							itemInfo.put(ParseConstants.KEY_NAME, name);
							itemInfo.put(ParseConstants.KEY_RATING,
									rating.toString());
							itemsInfo.add(itemInfo);
							objectsId.add(objectId);

						}
						setListItemAdapter();
						onItemClickListener(objectsId);

					} else {
						e.printStackTrace();
					}
				}

				private void onItemClickListener(
						final ArrayList<String> objectsId) {
					/*
					 * Set On Click Listener on the each Item
					 */
					mListItem.setOnItemClickListener(new OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> parent,
								View view, int position, long id) {
							// Get the Variable
							String userId = ParseUser.getCurrentUser()
									.getObjectId();
							String objectId = objectsId.get(position);

							// Start intent
							final Intent intent = new Intent(getActivity(),
									ItemDetail.class);
							intent.putExtra(ParseConstants.KEY_OBJECT_ID,
									objectId);

							// Query for Checking Loved Items
							ParseQuery<ParseObject> query = ParseQuery
									.getQuery(ParseConstants.TABLE_ITEM_LOVED);
							query.whereEqualTo(ParseConstants.KEY_ITEM_ID,
									objectId);
							query.whereEqualTo(ParseConstants.KEY_USER_ID,
									userId);
							query.countInBackground(new CountCallback() {
								@Override
								public void done(int love, ParseException e) {
									if (e == null) {
										// success
										if (love != 0) {
											Log.d(TAG, "Loved == true");
											intent.putExtra("isLoved", "true");
											startActivity(intent);
										} else {
											intent.putExtra("isLoved", "false");
											Log.d(TAG, "Loved == false");
											startActivity(intent);
										}
									} else {
										// failed
										Log.e(TAG, e.getMessage());
									}
								}
							});

						}
					});
				}
			});
		}

		/*
		 * Adapter setup
		 */
		private void setListItemAdapter() {
			mListItem = (ListView) getActivity().findViewById(R.id.listItem);
			String[] keys = { ParseConstants.KEY_NAME,
					ParseConstants.KEY_RATING };
			int[] ids = { android.R.id.text1, android.R.id.text2 };

			SimpleAdapter adapter = new SimpleAdapter(getActivity(), itemsInfo,
					android.R.layout.simple_list_item_2, keys, ids);

			mListItem.setAdapter(adapter);
		}

		private void setListPromotionAdapter() {
			mListPromotion = (ListView) getActivity().findViewById(
					R.id.listPromotion);
			String[] keys = { ParseConstants.KEY_NAME,
					ParseConstants.KEY_CLAIMABLE };
			int[] ids = { android.R.id.text1, android.R.id.text2 };

			SimpleAdapter adapter = new SimpleAdapter(getActivity(),
					promotionsInfo, android.R.layout.simple_list_item_2, keys,
					ids);

			mListPromotion.setAdapter(adapter);
		}

	}

}
