package com.example.storein;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
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
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class ItemReview extends ActionBarActivity {

	// Variable
	public static final String TAG = ItemReview.class.getSimpleName();
	protected static String itemId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_item_review);

		itemId = getIntent().getExtras()
				.getString(ParseConstants.KEY_OBJECT_ID);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.item_review, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
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
		// UI Declaration
		ListView mListUsersReview;

		// Variable
		protected ArrayList<HashMap<String, String>> reviewsList = new ArrayList<HashMap<String, String>>();
		public HashMap<String, String> reviewList = new HashMap<String, String>();
		public ArrayList<String> itemsID = new ArrayList<String>();

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_item_review,
					container, false);
			mListUsersReview = (ListView) rootView
					.findViewById(R.id.listUsersReview);

			return rootView;
		}

		@Override
		public void onResume() {
			super.onResume();
			getUsersReview();
		}

		public void getUsersReview() {
			getActivity().setProgressBarIndeterminateVisibility(true);
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
							
							//Get User Information
							ParseUser userInfo = review.getParseUser(ParseConstants.KEY_USER_ID);
							String userName = userInfo.getUsername();
							reviewList.put(ParseConstants.KEY_NAME, userName);

							// Get the review text Information
							String txtReview = review
									.getString(ParseConstants.KEY_REVIEW);

							reviewList
									.put(ParseConstants.KEY_REVIEW, txtReview);

							reviewsList.add(reviewList);
						}
						//Set the list View 
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

		public void setAdapter() {
			String[] keys = { ParseConstants.KEY_NAME,
					ParseConstants.KEY_REVIEW };
			int[] ids = { android.R.id.text1, android.R.id.text2 };

			SimpleAdapter adapter = new SimpleAdapter(getActivity(),
					reviewsList, android.R.layout.simple_list_item_2, keys, ids);

			mListUsersReview.setAdapter(adapter);
		}
	}

}
