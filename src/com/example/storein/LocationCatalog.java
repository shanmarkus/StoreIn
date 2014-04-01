package com.example.storein;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

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

		// Fixed Constants
		protected static final int MAX_ITEMS = 5;
		protected ArrayList<HashMap<String, String>> itemsInfo = new ArrayList<HashMap<String, String>>();
		public ArrayList<String> itemsID = new ArrayList<String>();
		public HashMap<String, String> itemInfo = new HashMap<String, String>();

		public void setItemsID(ArrayList<String> itemsID) {
			this.itemsID = itemsID;
		}

		public PlaceholderFragment() {
		}

		@Override
		public void onResume() {
			super.onResume();
			doItemQuery();
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(
					R.layout.fragment_location_catalog, container, false);

			mViewPager = (ViewPager) rootView.findViewById(R.id.view_pager);
			mListItem = (ListView) rootView.findViewById(R.id.listItem);
			ImagePagerAdapter adapter = new ImagePagerAdapter();
			mViewPager.setAdapter(adapter);
			doItemQuery();
			// Setting up on touch click listener

			mViewPager.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					return false;
				}
			});
			return rootView;
		}

		/*
		 * Added Function
		 */

		public void doItemQuery() {
			// ParseObject.registerSubclass(ParseLocationItems.class);
			ParseQuery<ParseObject> query = ParseQuery
					.getQuery(ParseConstants.TABLE_LOCATION_ITEM);
			query.whereEqualTo(ParseConstants.KEY_PLACE_ID, placeID);

			query.findInBackground(new FindCallback<ParseObject>() {

				@Override
				public void done(List<ParseObject> itemsAvailable,
						ParseException e) {
					if (e == null) {
						int itemsAvailableSize = itemsAvailable.size();
						// Dapetin Query
						for (int i = 0; i < itemsAvailableSize; i++) {
							ParseObject itemLocation = itemsAvailable.get(i);
							String temp = itemLocation
									.getString(ParseConstants.KEY_ITEM_ID);
							itemsID.add(temp);
						}
						// Setelah dapet size nya
						Toast.makeText(getActivity(),
								itemsID.size() + "Itmsid size ", Toast.LENGTH_LONG)
								.show();
						for (String id : itemsID) {
							Toast.makeText(getActivity(),
									id,
									Toast.LENGTH_LONG).show();
							ParseQuery<ParseObject> query = ParseQuery
									.getQuery(ParseConstants.TABLE_ITEM);
							query.whereEqualTo(ParseConstants.KEY_OBJECT_ID, id);
							query.findInBackground(new FindCallback<ParseObject>() {

								@Override
								public void done(List<ParseObject> items,
										ParseException e) {
									if (e == null) {
										// success
										ParseObject item = items.get(0);
										String name = item
												.getString(ParseConstants.KEY_NAME);
										Integer rating = item
												.getInt(ParseConstants.KEY_RATING);
										itemInfo.put(ParseConstants.KEY_NAME,
												name);
										itemInfo.put(ParseConstants.KEY_RATING,
												rating.toString());
										itemsInfo.add(itemInfo);

										String[] keys = {
												ParseConstants.KEY_NAME,
												ParseConstants.KEY_RATING };
										int[] ids = { android.R.id.text1,
												android.R.id.text2 };

										SimpleAdapter adapter = new SimpleAdapter(
												getActivity(),
												itemsInfo,
												android.R.layout.simple_list_item_2,
												keys, ids);

										mListItem.setAdapter(adapter);
									} else {
										// failed
										Log.e(TAG, e.getMessage());
										AlertDialog.Builder builder = new AlertDialog.Builder(
												getActivity());
										builder.setMessage(e.getMessage())
												.setTitle(R.string.error_title)
												.setPositiveButton(
														android.R.string.ok,
														null);
										AlertDialog dialog = builder.create();
										dialog.show();
									}
								}
							});
						}
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

		public void getItemsID() {

			Toast.makeText(getActivity(), itemsID.size() + "",
					Toast.LENGTH_SHORT).show();
			for (String id : itemsID) {
				ParseQuery<ParseObject> query = ParseQuery
						.getQuery(ParseConstants.TABLE_ITEM);
				query.whereEqualTo(ParseConstants.KEY_OBJECT_ID, id);
				query.findInBackground(new FindCallback<ParseObject>() {

					@Override
					public void done(List<ParseObject> items, ParseException e) {
						if (e == null) {
							// success
							ParseObject item = items.get(0);
							String name = item
									.getString(ParseConstants.KEY_NAME);
							Integer rating = item
									.getInt(ParseConstants.KEY_RATING);
							HashMap<String, String> itemInfo = new HashMap<String, String>();
							itemInfo.put(ParseConstants.KEY_NAME, name);
							itemInfo.put(ParseConstants.KEY_RATING,
									rating.toString());
							itemsInfo.add(itemInfo);
							Toast.makeText(getActivity(), name,
									Toast.LENGTH_SHORT).show();
							Toast.makeText(getActivity(), rating + "",
									Toast.LENGTH_SHORT).show();
						} else {
							// failed
							Log.e(TAG, e.getMessage());
							AlertDialog.Builder builder = new AlertDialog.Builder(
									getActivity());
							builder.setMessage(e.getMessage())
									.setTitle(R.string.error_title)
									.setPositiveButton(android.R.string.ok,
											null);
							AlertDialog dialog = builder.create();
							dialog.show();
						}
					}
				});
			}

			String[] keys = { ParseConstants.KEY_NAME,
					ParseConstants.KEY_RATING };
			int[] ids = { android.R.id.text1, android.R.id.text2 };

			SimpleAdapter adapter = new SimpleAdapter(getActivity(), itemsInfo,
					android.R.layout.simple_list_item_2, keys, ids);

			mListItem.setAdapter(adapter);

		}

		/*
		 * Image Adapter Class
		 */

		private class ImagePagerAdapter extends PagerAdapter {
			private int[] mImages = new int[] { R.drawable.chiang_mai,
					R.drawable.himeji, R.drawable.petronas_twin_tower,
					R.drawable.ulm };

			@Override
			public int getCount() {
				return mImages.length;
			}

			@Override
			public boolean isViewFromObject(View view, Object object) {
				return view == ((ImageView) object);
			}

			@Override
			public Object instantiateItem(ViewGroup container, int position) {
				Context context = getActivity();
				ImageView imageView = new ImageView(context);
				int padding = context.getResources().getDimensionPixelSize(
						R.dimen.padding_medium);
				imageView.setPadding(padding, padding, padding, padding);
				imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
				imageView.setImageResource(mImages[position]);
				((ViewPager) container).addView(imageView, 0);
				return imageView;
			}

			@Override
			public void destroyItem(ViewGroup container, int position,
					Object object) {
				((ViewPager) container).removeView((ImageView) object);
			}
		}

	}
}
