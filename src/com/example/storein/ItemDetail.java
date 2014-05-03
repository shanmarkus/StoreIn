package com.example.storein;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.CountCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class ItemDetail extends Fragment {
	private static final String TAG = ItemDetail.class.getSimpleName();

	// UI Variable Declaration
	ParseImageView mImageView;
	ImageButton mBtnLoveIt;
	Button mBtnReviewIt;
	TextView mItemTitleLabel;
	TextView mItemDescription;
	TextView mTextItemDetailTotalReward;

	// Fixed Variables
	private Integer totalLoved;

	// Parse Variables
	ParseUser user = ParseUser.getCurrentUser();
	String userId = user.getObjectId();

	// Intent Variables
	protected static String itemId;
	protected static String isLoved;

	public ItemDetail() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_item_detail,
				container, false);

		// Declare UI Variables
		mImageView = (ParseImageView) rootView.findViewById(R.id.imageView);
		mBtnLoveIt = (ImageButton) rootView.findViewById(R.id.btnLoveIt);
		mBtnReviewIt = (Button) rootView.findViewById(R.id.btnReviewIt);
		mItemTitleLabel = (TextView) rootView.findViewById(R.id.itemTitleLabel);
		mItemDescription = (TextView) rootView
				.findViewById(R.id.itemDescriptionLabel);
		mTextItemDetailTotalReward = (TextView) rootView
				.findViewById(R.id.textItemDetailTotalReward);

		// Intent Variables
		// Setup Variable from the previous intents
		getItemId();

		isLoved = getActivity().getIntent().getExtras().getString("isLoved");

		// Find Item Details
		findItemDetail();

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		findAllUI();
		onClickReviewItButton();
		checkLoveButton();
	}

	/*
	 * Getter for placeId variables
	 */
	public String getItemId() {
		Bundle args = getArguments();
		itemId = args.getString(ParseConstants.KEY_OBJECT_ID);
		return itemId;
	}

	/*
	 * Dumb function to protect if the UI variable will return null
	 */

	protected void findAllUI() {
		// Declare UI Variables
		mBtnLoveIt = (ImageButton) getActivity().findViewById(R.id.btnLoveIt);
		mBtnReviewIt = (Button) getActivity().findViewById(R.id.btnReviewIt);
		mItemTitleLabel = (TextView) getActivity().findViewById(
				R.id.itemTitleLabel);
		mItemDescription = (TextView) getActivity().findViewById(
				R.id.itemDescriptionLabel);
	}

	/*
	 * Check whether user already love the item or not
	 */
	public void checkLoveButton() {
		// Set Progress Bar
		getActivity().setProgressBarIndeterminateVisibility(true);
		// Do Query
		ParseQuery<ParseObject> query = ParseQuery
				.getQuery(ParseConstants.TABLE_ITEM_LOVED);
		query.whereEqualTo(ParseConstants.KEY_ITEM_ID, itemId);
		query.whereEqualTo(ParseConstants.KEY_USER_ID, userId);
		query.countInBackground(new CountCallback() {

			@Override
			public void done(int love, ParseException e) {
				// Set Progress Bar
				getActivity().setProgressBarIndeterminateVisibility(false);
				if (e == null) {
					// success
					if (love != 0) {
						isLoved = "true";
						// mBtnLoveIt.setText("Un-Love It");
						onClickLoveItButton();
					} else {
						isLoved = "false";
						// mBtnLoveIt.setText("Love It");
						onClickLoveItButton();
					}
				} else {
					// failed
					parseErrorDialog(e);
				}
			}
		});
	}

	/*
	 * On Click Listener
	 */

	// when user already love the items
	OnClickListener isLovedTrue = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// Create Toast
			debugLoveVar();

			// Set Progress Bar
			getActivity().setProgressBarIndeterminateVisibility(true);
			ParseQuery<ParseObject> query = ParseQuery
					.getQuery(ParseConstants.TABLE_ITEM_LOVED);
			query.whereEqualTo(ParseConstants.KEY_USER_ID, userId);
			query.whereEqualTo(ParseConstants.KEY_ITEM_ID, itemId);
			query.getFirstInBackground(new GetCallback<ParseObject>() {

				@Override
				public void done(ParseObject itemLoved, ParseException e) {
					// Set Progress Bar
					getActivity().setProgressBarIndeterminateVisibility(false);
					if (e == null) {
						try {
							itemLoved.delete();
							checkLoveButton();
							Toast.makeText(getActivity(),
									" UnLove it Parse success",
									Toast.LENGTH_SHORT).show();
							queryTotalLoved();
							onResume();

						} catch (ParseException e1) {
							e1.printStackTrace();
						}

					} else {
						// failed
						parseErrorDialog(e);
					}
				}
			});
		}
	};

	// when user have not love the items
	OnClickListener isLovedFalse = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// create toast
			debugLoveVar();

			// Set Progress Bar
			getActivity().setProgressBarIndeterminateVisibility(true);

			// Do the query
			ParseObject itemLoved = new ParseObject(
					ParseConstants.TABLE_ITEM_LOVED);
			itemLoved.put(ParseConstants.KEY_USER_ID, userId);
			itemLoved.put(ParseConstants.KEY_ITEM_ID, itemId);
			itemLoved.saveInBackground(new SaveCallback() {

				@Override
				public void done(ParseException e) {
					// Set Progress Bar
					getActivity().setProgressBarIndeterminateVisibility(false);
					if (e == null) {
						// Success
						checkLoveButton();
						Toast.makeText(getActivity(), "Love it Parse success",
								Toast.LENGTH_SHORT).show();
						queryTotalLoved();
						onResume();
					} else {
						// failed
						parseErrorDialog(e);
					}

				}
			});
		}
	};

	/*
	 * When user click the Love It Button
	 */
	public void onClickLoveItButton() {
		// Check whether the variables is null or not
		if (isLoved == null) {
			checkLoveButton();
		}

		if (isLoved.equals("false")) {
			// The user HAVE NOT liked it
			mBtnLoveIt.setOnClickListener(isLovedFalse);
		} else if (isLoved.equals("true")) {
			// User ALREADY like the item
			mBtnLoveIt.setOnClickListener(isLovedTrue);
		}
	}

	/*
	 * When user wants to review the item by giving comments and ratings
	 */
	public void onClickReviewItButton() {
		mBtnReviewIt = (Button) getActivity().findViewById(R.id.btnReviewIt);
		mBtnReviewIt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), WriteReviewItem.class);
				intent.putExtra(ParseConstants.KEY_OBJECT_ID, itemId);
				intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				startActivity(intent);
			}
		});

	}

	/*
	 * Find the detail of an item including description and rating
	 */
	public void findItemDetail() {
		// set progress bar
		getActivity().setProgressBarIndeterminate(true);

		// do the query
		ParseQuery<ParseObject> query = ParseQuery
				.getQuery(ParseConstants.TABLE_ITEM);
		query.whereEqualTo(ParseConstants.KEY_OBJECT_ID, itemId);
		query.getFirstInBackground(new GetCallback<ParseObject>() {

			@Override
			public void done(ParseObject item, ParseException e) {
				// set progress bar
				getActivity().setProgressBarIndeterminate(false);
				if (e == null) {
					// success
					findAllUI();
					String title = item.getString(ParseConstants.KEY_NAME);
					String description = item
							.getString(ParseConstants.KEY_DESCRIPTION);
					Integer totalLoved = item
							.getInt(ParseConstants.KEY_TOTAL_LOVED);

					mItemTitleLabel.setText(title);
					mItemDescription.setText(description);
					mTextItemDetailTotalReward.setText(totalLoved + "");
				} else {
					// failed
					parseErrorDialog(e);
				}
			}
		});

	}

	/*
	 * Updating if the user love items
	 */

	private void queryTotalLoved() {
		ParseQuery<ParseObject> query = ParseQuery
				.getQuery(ParseConstants.TABLE_ITEM_LOVED);
		query.whereEqualTo(ParseConstants.KEY_ITEM_ID, itemId);
		query.countInBackground(new CountCallback() {

			@Override
			public void done(int total, ParseException e) {
				if (e == null) {
					// success
					totalLoved = total;
					updateTotalLoved();
				} else {
					// failed
					parseErrorDialog(e);
				}
			}
		});
	}

	private void updateTotalLoved() {
		ParseQuery<ParseObject> query = ParseQuery
				.getQuery(ParseConstants.TABLE_ITEM);
		query.getInBackground(itemId, new GetCallback<ParseObject>() {

			@Override
			public void done(ParseObject item, ParseException e) {
				if (e == null) {
					item.put(ParseConstants.KEY_TOTAL_LOVED, totalLoved);
					Toast.makeText(getActivity(), "updatedTotalLoved" + totalLoved,
							Toast.LENGTH_SHORT).show();
					item.saveInBackground();
				} else {
					parseErrorDialog(e);
				}
			}
		});

	}

	/*
	 * Debug Toast
	 */

	private void debugLoveVar() {
		String tempMessage;
		if (isLoved.equals("false")) {
			tempMessage = "Thank you for the Love";
		} else {
			tempMessage = "UnLove the items";
		}
		Toast.makeText(getActivity(), isLoved, Toast.LENGTH_SHORT).show();
		// Toast Dialog
		Toast.makeText(getActivity(), tempMessage, Toast.LENGTH_SHORT).show();
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

}
