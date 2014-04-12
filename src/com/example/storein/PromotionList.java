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
		protected String categoriesId;
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
			categoriesId = getActivity().getIntent().getExtras()
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
			query.whereEqualTo(ParseConstants.KEY_CATEGORY, categoriesId);
			query.findInBackground(new FindCallback<ParseObject>() {

				@Override
				public void done(List<ParseObject> promotions, ParseException e) {
					if (e == null) {
						// success
					} else {
						// failed
					}

				}
			});
		}
	}

}
