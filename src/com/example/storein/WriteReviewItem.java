package com.example.storein;

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
		getMenuInflater().inflate(R.menu.write_review_item, menu);
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
		// UI Declaration
		Button mBtnSubmit;
		EditText mTxtUserReview;
		RatingBar mRatingBar;

		// Variable
		ParseUser user = ParseUser.getCurrentUser();

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(
					R.layout.fragment_write_review_item, container, false);
			mBtnSubmit = (Button) rootView.findViewById(R.id.btnSubmit);
			mTxtUserReview = (EditText) rootView
					.findViewById(R.id.txtUserReview);
			mRatingBar = (RatingBar) rootView.findViewById(R.id.ratingBar1);

			return rootView;
		}

		@Override
		public void onResume() {
			super.onResume();
			checkExistingUserReview();
		}

		/*
		 * Check Whether user already review the item or not
		 */

		public void checkExistingUserReview() {
			String userId = user.getObjectId();
			ParseQuery<ParseObject> query = ParseQuery
					.getQuery(ParseConstants.TABLE_ITEM_REVIEW);
			query.whereEqualTo(ParseConstants.KEY_USER_ID, userId);
			query.whereEqualTo(ParseConstants.KEY_ITEM_ID, itemId);
			query.countInBackground(new CountCallback() {

				@Override
				public void done(int count, ParseException e) {
					if (e == null) {
						// success
						if (count == 0) {
							// User have not review it yet
							isReviewed = "false";
							onSubmitBtn();
						} else {
							ParseUser temp = ParseUser.getCurrentUser();
							String tempUserId = temp.getObjectId();
							isReviewed = "true";
							ParseQuery<ParseObject> query = ParseQuery
									.getQuery(ParseConstants.TABLE_ITEM_REVIEW);
							query.whereEqualTo(ParseConstants.KEY_USER_ID,
									tempUserId);
							query.whereEqualTo(ParseConstants.KEY_ITEM_ID,
									itemId);
							query.getFirstInBackground(new GetCallback<ParseObject>() {

								@Override
								public void done(ParseObject review,
										ParseException e) {
									if (e == null) {
										mTxtUserReview = (EditText) getActivity()
												.findViewById(
														R.id.txtUserReview);
										mRatingBar = (RatingBar) getActivity()
												.findViewById(R.id.ratingBar1);

										mTxtUserReview.setText(review
												.getString(ParseConstants.KEY_REVIEW));
										mRatingBar.setRating(review
												.getInt(ParseConstants.KEY_RATING));
									}

								}
							});

							onSubmitBtn();
						}
					}

				}
			});
		}

		/*
		 * Submit the review when user click a button
		 */
		public void onSubmitBtn() {
			mBtnSubmit = (Button) getActivity().findViewById(R.id.btnSubmit);
			if (isReviewed == null) {
				checkExistingUserReview();
			}

			if (isReviewed.equals("false")) {
				// Getting User Id
				final String userId = user.getObjectId();

				// Create parse object for store as Pointer
				final ParseObject tempUser = ParseObject.createWithoutData(
						ParseConstants.TABLE_USER, userId);

				// On Click Listener
				mBtnSubmit.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						getActivity().setProgressBarIndeterminateVisibility(
								true);

						String reviewText = mTxtUserReview.getText().toString();
						int rating = Math.round(mRatingBar.getRating());

						ParseObject reviewItem = new ParseObject(
								ParseConstants.TABLE_ITEM_REVIEW);
						reviewItem.put(ParseConstants.KEY_USER_ID, tempUser);
						reviewItem.put(ParseConstants.KEY_ITEM_ID, itemId);
						reviewItem.put(ParseConstants.KEY_REVIEW, reviewText);
						reviewItem.put(ParseConstants.KEY_RATING, rating);
						reviewItem.saveInBackground(new SaveCallback() {

							@Override
							public void done(ParseException e) {
								getActivity()
										.setProgressBarIndeterminateVisibility(
												false);
								if (e == null) {
									// success
									Toast.makeText(getActivity(),
											"Review Saved", Toast.LENGTH_SHORT)
											.show();
									checkExistingUserReview();
									Intent intent = new Intent(getActivity(),
											ItemDetail.class);
									intent.putExtra(
											ParseConstants.KEY_OBJECT_ID,
											itemId);
									startActivity(intent);
								} else {
									Log.e(TAG, e.getMessage());
									AlertDialog.Builder builder = new AlertDialog.Builder(
											getActivity());
									builder.setMessage(e.getMessage())
											.setTitle(R.string.error_title)
											.setPositiveButton(
													android.R.string.ok, null);
									AlertDialog dialog = builder.create();
									dialog.show();
								}

							}
						});

					}
				});
			} else {
				// Getting User Id
				final String userId = user.getObjectId();

				// Create parse object for store as Pointer
				final ParseObject tempUser = ParseObject.createWithoutData(
						ParseConstants.TABLE_USER, userId);
				mBtnSubmit.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						getActivity().setProgressBarIndeterminateVisibility(
								true);
						ParseQuery<ParseObject> query = ParseQuery
								.getQuery(ParseConstants.TABLE_ITEM_REVIEW);
						query.whereEqualTo(ParseConstants.KEY_ITEM_ID, itemId);
						query.whereEqualTo(ParseConstants.KEY_USER_ID, tempUser);
						query.getFirstInBackground(new GetCallback<ParseObject>() {

							@Override
							public void done(ParseObject review,
									ParseException e) {
								getActivity()
										.setProgressBarIndeterminateVisibility(
												false);
								if (e == null) {
									// Success
									String userId = user.getObjectId();
									String reviewText = mTxtUserReview
											.getText().toString();
									int rating = Math.round(mRatingBar
											.getRating());

									review.put(ParseConstants.KEY_USER_ID,
											userId);
									review.put(ParseConstants.KEY_ITEM_ID,
											itemId);
									review.put(ParseConstants.KEY_REVIEW,
											reviewText);
									review.put(ParseConstants.KEY_RATING,
											rating);
									review.saveInBackground(new SaveCallback() {

										@Override
										public void done(ParseException e) {
											if (e == null) {
												Toast.makeText(
														getActivity(),
														"Your Review has been Updated",
														Toast.LENGTH_SHORT)
														.show();
												checkExistingUserReview();
												Intent intent = new Intent(
														getActivity(),
														ItemDetail.class);
												intent.putExtra(
														ParseConstants.KEY_OBJECT_ID,
														itemId);
												startActivity(intent);

											} else {
												Log.e(TAG, e.getMessage());
												AlertDialog.Builder builder = new AlertDialog.Builder(
														getActivity());
												builder.setMessage(
														e.getMessage())
														.setTitle(
																R.string.error_title)
														.setPositiveButton(
																android.R.string.ok,
																null);
												AlertDialog dialog = builder
														.create();
												dialog.show();
											}
										}
									});
								}
							}
						});
					}
				});
			}

		}
	}

}