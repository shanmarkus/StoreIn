package com.example.storein;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.parse.CountCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class WriteReviewItem extends ActionBarActivity {

	// Variable
	public final static String TAG = WriteReviewItem.class.getSimpleName();
	protected static String itemId;
	protected static String isReviewed;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_write_review_item);

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
		Button mBtnSubmit;
		EditText mTxtUserReview;
		RatingBar mRatingBar;

		// Variable
		ParseUser user = ParseUser.getCurrentUser();
		ProgressDialog progressDialog;

		// Getting User Id
		String userId = user.getObjectId();

		// Create parse object for store as Pointer
		final ParseObject tempUser = ParseObject.createWithoutData(
				ParseConstants.TABLE_USER, userId);

		public PlaceholderFragment() {
			// Empty Constructor
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(
					R.layout.fragment_write_review_item, container, false);

			// UI Declaration
			mBtnSubmit = (Button) rootView.findViewById(R.id.btnSubmit);
			mTxtUserReview = (EditText) rootView
					.findViewById(R.id.txtUserReview);
			mRatingBar = (RatingBar) rootView.findViewById(R.id.ratingBar1);

			return rootView;
		}

		@Override
		public void onResume() {
			super.onResume();
			// Re check the variables
			if (userId == null || itemId == null) {
				getUserID();
				getItemId();
			}

			// check user review
			checkExistingUserReview();
		}

		/*
		 * Setter and getter variable
		 */

		protected void getUserID() {
			userId = ParseUser.getCurrentUser().getObjectId();
		}

		protected void getItemId() {
			itemId = getActivity().getIntent().getExtras()
					.getString(ParseConstants.KEY_OBJECT_ID);
		}

		/*
		 * Progress Dialog init
		 */

		private void initProgressDialog() {
			progressDialog = new ProgressDialog(getActivity());
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setMessage("Loading");
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(false);
			progressDialog.show();
		}

		/*
		 * Check Whether user already review the item or not
		 */

		public void checkExistingUserReview() {
			// Set Progress bar
			initProgressDialog();

			// Do the Query
			ParseQuery<ParseObject> query = ParseQuery
					.getQuery(ParseConstants.TABLE_ITEM_REVIEW);
			query.whereEqualTo(ParseConstants.KEY_USER_ID, tempUser);
			query.whereEqualTo(ParseConstants.KEY_ITEM_ID, itemId);
			query.countInBackground(new CountCallback() {

				@Override
				public void done(int count, ParseException e) {
					if (e == null) {
						// success
						if (count == 0) {
							// User have not review it yet
							isReviewed = "false";
							onSubmitBtn(); // listen to the submit button
							progressDialog.dismiss();
						} else {
							isReviewed = "true";
							mBtnSubmit.setEnabled(false); // disable the button
															// first
							fillReview(); // fill the review
							onSubmitBtn(); // listen to the submit button
						}
					} else {
						// throw exception
						parseErrorDialog(e);
					}
				}
			});
		}

		/*
		 * fill the review if user already fill it in database
		 */

		protected void fillReview() {

			// Do the Query
			ParseQuery<ParseObject> query = ParseQuery
					.getQuery(ParseConstants.TABLE_ITEM_REVIEW);
			query.whereEqualTo(ParseConstants.KEY_USER_ID, tempUser);
			query.whereEqualTo(ParseConstants.KEY_ITEM_ID, itemId);
			query.getFirstInBackground(new GetCallback<ParseObject>() {

				@Override
				public void done(ParseObject review, ParseException e) {
					// set progress bar
					progressDialog.dismiss();
					if (e == null) {
						// Set up the view
						mTxtUserReview = (EditText) getActivity().findViewById(
								R.id.txtUserReview);
						mRatingBar = (RatingBar) getActivity().findViewById(
								R.id.ratingBar1);
						mTxtUserReview.setText(review
								.getString(ParseConstants.KEY_REVIEW));
						mRatingBar.setRating(review
								.getInt(ParseConstants.KEY_RATING));
						mBtnSubmit.setEnabled(true);

					} else {
						// throw exception
						parseErrorDialog(e);
					}
				}
			});
		}

		/*
		 * Update review in database
		 */

		protected void updateReview(ParseObject review) {
			// Getting user Input
			String reviewText = mTxtUserReview.getText().toString();
			int rating = Math.round(mRatingBar.getRating());

			// set User input
			review.put(ParseConstants.KEY_USER_ID, tempUser);
			review.put(ParseConstants.KEY_ITEM_ID, itemId);
			review.put(ParseConstants.KEY_REVIEW, reviewText);
			review.put(ParseConstants.KEY_RATING, rating);
			review.saveInBackground(new SaveCallback() {

				@Override
				public void done(ParseException e) {
					progressDialog.dismiss();
					if (e == null) {
						// success
						Toast.makeText(getActivity(), "Review Saved",
								Toast.LENGTH_SHORT).show();
						checkExistingUserReview();
						Intent intent = new Intent(getActivity(),
								ItemInformation.class);
						intent.putExtra(ParseConstants.KEY_OBJECT_ID, itemId);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
						startActivity(intent);
					} else {
						parseErrorDialog(e);
					}

				}
			});
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

		/*
		 * Button on Click Listener
		 */

		// If the user have not reviewed the item yet
		OnClickListener notReviewed = new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Set Progress bar
				initProgressDialog();
				ParseObject review = new ParseObject(
						ParseConstants.TABLE_ITEM_REVIEW);
				updateReview(review);
			}
		};

		// If the user have reviewed the item
		OnClickListener haveReviewed = new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Set Progress bar
				initProgressDialog();

				ParseQuery<ParseObject> query = ParseQuery
						.getQuery(ParseConstants.TABLE_ITEM_REVIEW);
				query.whereEqualTo(ParseConstants.KEY_ITEM_ID, itemId);
				query.whereEqualTo(ParseConstants.KEY_USER_ID, tempUser);
				query.getFirstInBackground(new GetCallback<ParseObject>() {

					@Override
					public void done(ParseObject review, ParseException e) {
						if (e == null) {
							// Success
							updateReview(review);
						}
					}
				});
			}
		};

		/*
		 * Submit the review when user click a button
		 */
		public void onSubmitBtn() {
			mBtnSubmit = (Button) getActivity().findViewById(R.id.btnSubmit);

			if (isReviewed == null) {
				checkExistingUserReview();
			}

			if (isReviewed.equals("false")) {
				mBtnSubmit.setOnClickListener(notReviewed);
			} else {
				mBtnSubmit.setOnClickListener(haveReviewed);
			}
		}
	}

}