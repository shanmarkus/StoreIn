package com.example.storein;

import java.util.ArrayList;
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
import android.widget.GridView;

import com.example.storein.adapter.CustomArrayAdapterPromotionCategories;
import com.example.storein.model.PromotionCategory;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class BrowseActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_browse_fragment);
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

		// UI Variable
		GridView mGridView;
		String[] categories;

		// Other Variables
		public static final String TAG = BrowseActivity.class.getSimpleName();
		protected ArrayList<String> objectsId = new ArrayList<String>();

		public List<PromotionCategory> promotionCategoryList = new ArrayList<PromotionCategory>();
		public ArrayList<PromotionCategory> promotionCategoryRecord;
		private CustomArrayAdapterPromotionCategories mPromotionCategoryAdapter;

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_browse,
					container, false);
			mGridView = (GridView) rootView.findViewById(R.id.gridView);
			getListCategories();
			return rootView;
		}

		@Override
		public void onResume() {
			super.onResume();
			onClickCategoriesList();
		}

		/*
		 * Added Function
		 */

		// Get list of categories
		public void getListCategories() {
			getActivity().setProgressBarIndeterminateVisibility(true);
			ParseQuery<ParseObject> query = ParseQuery
					.getQuery(ParseConstants.TABLE_PROMOTION_CATEGORY);
			query.orderByAscending(ParseConstants.KEY_CREATED_AT);
			query.findInBackground(new FindCallback<ParseObject>() {

				@Override
				public void done(List<ParseObject> categoryInfos,
						ParseException e) {
					getActivity().setProgressBarIndeterminateVisibility(false);
					if (e == null) {
						// success
						for (ParseObject category : categoryInfos) {
							String categoryId = category.getObjectId();
							String categoryName = category
									.getString(ParseConstants.KEY_NAME);
							PromotionCategory temp = new PromotionCategory();
							temp.setName(categoryName);
							temp.setObjectId(categoryId);
							promotionCategoryList.add(temp);

							objectsId.add(categoryId);
						}
						setAdapter();
					} else {
						errorAlertDialog(e);
					}

				}
			});
		}

		// Setting up the grid view
		public void setAdapter() {
			promotionCategoryRecord = (ArrayList<PromotionCategory>) promotionCategoryList;
			mPromotionCategoryAdapter = new CustomArrayAdapterPromotionCategories(
					getActivity(), R.id.gridView, promotionCategoryRecord);
			mGridView = (GridView) getActivity().findViewById(R.id.gridView);
			mGridView.setAdapter(mPromotionCategoryAdapter);
		}

		// Add listener to GridView

		public void onClickCategoriesList() {
			mGridView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					String categoryId = objectsId.get(position);
					Intent intent = new Intent(getActivity(),
							PromotionList.class);
					intent.putExtra(ParseConstants.KEY_OBJECT_ID, categoryId);
					startActivity(intent);
				}
			});
		}

		/*
		 * Error Dialog Parse
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
