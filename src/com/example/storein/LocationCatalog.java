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

		public PlaceholderFragment() {
		}

		public ArrayList<HashMap<String, String>> getItemsInfo() {
			return itemsInfo;
		}

		public void setItemsInfo(ArrayList<HashMap<String, String>> itemsInfo) {
			this.itemsInfo = itemsInfo;
		}

		@Override
		public void onResume() {
			super.onResume();

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

			return rootView;
		}

		/*
		 * Added Function
		 */

		public void doItemQuery() {
			// Clear the array first

			itemsID.clear();

			ParseQuery<ParseObject> query = ParseQuery
					.getQuery(ParseConstants.TABLE_LOCATION_ITEM);
			query.whereEqualTo(ParseConstants.KEY_PLACE_ID, placeID);
			try {
				List<ParseObject> items = query.find();
				for (int i = 0; i < items.size(); i++) {
					itemsID.add(items.get(i).getString(
							ParseConstants.KEY_ITEM_ID));
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			/*
			 * Do second Query for the second Table
			 */
			itemInfo.clear();
			itemsInfo.clear();
			for (String id : itemsID) {
				ParseQuery<ParseObject> innerQuery = ParseQuery
						.getQuery(ParseConstants.TABLE_ITEM);
				innerQuery.whereEqualTo(ParseConstants.KEY_OBJECT_ID, id);
				try {
					List<ParseObject> items = innerQuery.find();
					ParseObject item = items.get(0);
					String name = item.getString(ParseConstants.KEY_NAME);
					Integer rating = item.getInt(ParseConstants.KEY_RATING);
					HashMap<String, String> itemInfo = new HashMap<String, String>();
					itemInfo.put(ParseConstants.KEY_NAME, name);
					itemInfo.put(ParseConstants.KEY_RATING, rating.toString());
					itemsInfo.add(itemInfo);

				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
