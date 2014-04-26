package com.example.storein;

import java.util.Date;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.CountCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class PromotionDetail extends ActionBarActivity {

	protected static final String TAG = PromotionDetail.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_promotion_detail);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.promotion_detail, menu);
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
		TextView mTxtPromotionTitle;
		TextView mTextPromotionReq;
		TextView mTextPromotionDesc;
		TextView mTextPromotionDuration;
		TextView mTextReward;
		Button mClaimButton;
		TextView mTextFlashDealNumber;

		// Variables Intent
		protected String promotionId;
		protected Boolean claimable;
		protected String placeId;

		// Variables
		protected String claimActivityId;
		protected Integer flashPromoQuota;
		protected String promotionQuotaId;
		protected Integer numberUserStatusAndPromotion;
		protected String userId = ParseUser.getCurrentUser().getObjectId();

		// Promotion Variables
		public String promoTitle;
		public String promoPlace;

		// Progress dialog
		ProgressDialog progressDialog;

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(
					R.layout.fragment_promotion_detail, container, false);

			// Setup UI
			mTxtPromotionTitle = (TextView) rootView
					.findViewById(R.id.txtPromotionTitle);
			mTextPromotionDesc = (TextView) rootView
					.findViewById(R.id.textPromotionDesc);
			mTextPromotionDuration = (TextView) rootView
					.findViewById(R.id.textPromotionDuration);
			mTextPromotionReq = (TextView) rootView
					.findViewById(R.id.textPromotionReq);
			mTextReward = (TextView) rootView.findViewById(R.id.textReward);
			mTextFlashDealNumber = (TextView) rootView
					.findViewById(R.id.textFlashDealNumber);
			mClaimButton = (Button) rootView.findViewById(R.id.claimButton);

			// Do some Function
			getClaimableValue();
			if (claimable == true) {
				checkFlashDeal();
				checkUserAndPromotionStatus();
			}

			return rootView;
		}

		@Override
		public void onResume() {
			super.onResume();
			if(claimable == true){
				checkFlashDeal();
				checkUserAndPromotionStatus();
			}
			findPromotionDetail();
			onClickClaimButton();
	
		}

		/*
		 * Added Functions
		 */

		/*
		 * Setter and Getter
		 */
		protected void getPromotionId() {
			promotionId = (String) getActivity().getIntent().getExtras()
					.get(ParseConstants.KEY_OBJECT_ID);
		}

		protected void getClaimableValue() {
			claimable = getActivity().getIntent().getExtras()
					.getBoolean(ParseConstants.KEY_CLAIMABLE);
		}

		protected void getPlaceId() {
			placeId = (String) getActivity().getIntent().getExtras()
					.get(ParseConstants.KEY_PLACE_ID);
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

		protected void checkFlashDeal() {
			mTextFlashDealNumber = (TextView) getActivity().findViewById(
					R.id.textFlashDealNumber);
			if (claimable == null) {
				getClaimableValue();
			}
			if (claimable == true) {
				getFlashPromotionQuantity();
			} else {
				findPromotionDetail();
				mClaimButton.setEnabled(true);
			}
		}

		/*
		 * Do Query for finding promotion details
		 */

		public void findPromotionDetail() {
			if (promotionId == null) {
				getPromotionId();
			}
			// set progress bar true
			getActivity().setProgressBarIndeterminateVisibility(true);

			// Do the query
			ParseQuery<ParseObject> query = ParseQuery
					.getQuery(ParseConstants.TABLE_PROMOTION);
			query.whereEqualTo(ParseConstants.KEY_OBJECT_ID, promotionId);
			query.getFirstInBackground(new GetCallback<ParseObject>() {

				@Override
				public void done(ParseObject promotion, ParseException e) {
					getActivity().setProgressBarIndeterminateVisibility(false);
					if (e == null) {
						// success

						// Find all the Id
						mTxtPromotionTitle = (TextView) getActivity()
								.findViewById(R.id.txtPromotionTitle);
						mTextPromotionReq = (TextView) getActivity()
								.findViewById(R.id.textPromotionReq);
						mTextPromotionDesc = (TextView) getActivity()
								.findViewById(R.id.textPromotionDesc);
						mTextPromotionDuration = (TextView) getActivity()
								.findViewById(R.id.textPromotionDuration);

						// Get all the important variables
						promoTitle = promotion
								.getString(ParseConstants.KEY_NAME);
						String promoRequirement = promotion
								.getString(ParseConstants.KEY_REQUIREMENT);
						String promoDescription = promotion
								.getString(ParseConstants.KEY_DESCRIPTION);
						Date promoStartDate = promotion
								.getDate(ParseConstants.KEY_START_DATE);
						Date promoEndDate = promotion
								.getDate(ParseConstants.KEY_END_DATE);
						Integer promoPoint = promotion
								.getInt(ParseConstants.KEY_REWARD_POINT);

						// Put all the values
						mTxtPromotionTitle.setText(promoTitle);
						mTextPromotionReq.setText(promoRequirement);
						mTextPromotionDesc.setText(promoDescription);
						mTextPromotionDuration.setText(promoStartDate + " - "
								+ promoEndDate);
						mTextReward.setText(promoPoint.toString());

					} else {
						// failed
						parseErrorDialog(e);
					}

				}
			});
		}

		/*
		 * Check whether this user already claim the flash promotion or not
		 */

		protected void checkUserAndPromotionStatus() {

			if (promotionId == null) {
				getPromotionId();
			}
			ParseObject tempUser = ParseObject.createWithoutData(
					ParseConstants.TABLE_USER, userId);
			ParseObject tempPromo = ParseObject.createWithoutData(
					ParseConstants.TABLE_PROMOTION, promotionId);
			ParseQuery<ParseObject> query = ParseQuery
					.getQuery(ParseConstants.TABLE_ACTV_USER_CLAIM_PROMOTION);
			query.whereEqualTo(ParseConstants.KEY_USER_ID, tempUser);

			query.whereEqualTo(ParseConstants.KEY_PROMOTION_ID, tempPromo);
			query.countInBackground(new CountCallback() {

				@Override
				public void done(int total, ParseException e) {
					if (e == null) {
						// success
						numberUserStatusAndPromotion = total;
						mClaimButton = (Button) getActivity().findViewById(
								R.id.claimButton);
						if (total == 0) {
							mClaimButton.setEnabled(true);
						} else {
							mClaimButton.setEnabled(false);
						}
					} else {
						parseErrorDialog(e);
					}
				}
			});
		}

		/*
		 * if claimable value equal true then find the quantity of the promotion
		 */

		protected void getFlashPromotionQuantity() {
			if (promotionId == null) {
				getPromotionId();
			}
			if (placeId == null) {
				getPlaceId();
			}
			ParseObject promotionObj = ParseObject.createWithoutData(
					ParseConstants.TABLE_PROMOTION, promotionId);
			ParseObject placeObj = ParseObject.createWithoutData(
					ParseConstants.TABLE_PLACE, placeId);
			ParseQuery<ParseObject> flashPromo = ParseQuery
					.getQuery(ParseConstants.TABLE_PROMOTION_QUOTA);
			flashPromo.whereEqualTo(ParseConstants.KEY_PROMOTION_ID,
					promotionObj);
			flashPromo.whereEqualTo(ParseConstants.KEY_PLACE_ID, placeObj);
			flashPromo.getFirstInBackground(new GetCallback<ParseObject>() {

				@Override
				public void done(ParseObject flashPromo, ParseException e) {
					if (e == null) {
						mTextFlashDealNumber = (TextView) getActivity()
								.findViewById(R.id.textFlashDealNumber);
						// success
						Integer quantity = flashPromo
								.getInt(ParseConstants.KEY_QUOTA);
						String tempObjId = flashPromo.getObjectId();
						Toast.makeText(getActivity(), quantity + " ",
								Toast.LENGTH_SHORT).show();
						mTextFlashDealNumber.setText(quantity + "");
						flashPromoQuota = quantity;
						promotionQuotaId = tempObjId;
					} else {
						parseErrorDialog(e);
					}

				}
			});
		}

		/*
		 * Set On Click listener for claim Button and set activity that user has
		 * already claim it
		 */

		public void onClickClaimButton() {
			// first it have to check whether the promotion is left or not
			mClaimButton = (Button) getActivity()
					.findViewById(R.id.claimButton);
			mClaimButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					initProgressDialog();
					if (claimable == true) {
						// if the promotion is flash deal
						if (flashPromoQuota > 0) {
							// update the value first
							checkFlashDeal();
							if (flashPromoQuota > 0) {
								ParseQuery<ParseObject> claimFlashPromo = ParseQuery
										.getQuery(ParseConstants.TABLE_PROMOTION_QUOTA);
								claimFlashPromo.getInBackground(
										promotionQuotaId,
										new GetCallback<ParseObject>() {

											@Override
											public void done(
													ParseObject promotion,
													ParseException e) {
												if (e == null) {
													// success
													flashPromoQuota = flashPromoQuota - 1;
													promotion
															.put(ParseConstants.KEY_QUOTA,
																	flashPromoQuota);
													promotion
															.saveInBackground();
													// save the log
													saveUserClaimActivity();

													// Set to false
													checkUserAndPromotionStatus();
												} else {
													parseErrorDialog(e);
												}
											}
										});
							}
						}
					} else {
						// the promotion is just a ordinary deal
						saveUserClaimActivity();
					}
				}
			});
		}

		/*
		 * Saving Log Function
		 */

		public void saveUserClaimActivity() {
			String userId = ParseUser.getCurrentUser().getObjectId();
			ParseObject tempUserId = ParseObject.createWithoutData(
					ParseConstants.TABLE_USER, userId);
			final ParseObject tempPromotionId = ParseObject.createWithoutData(
					ParseConstants.TABLE_PROMOTION, promotionId);
			ParseObject claimActivity = new ParseObject(
					ParseConstants.TABLE_ACTV_USER_CLAIM_PROMOTION);
			claimActivity.put(ParseConstants.KEY_USER_ID, tempUserId);
			claimActivity.put(ParseConstants.KEY_PROMOTION_ID, tempPromotionId);
			claimActivity.saveInBackground(new SaveCallback() {

				@Override
				public void done(ParseException e) {
					// Dismis the progress bar
					progressDialog.dismiss();
					if (e == null) {
						Toast.makeText(getActivity(),
								"Claim Activity has been saved",
								Toast.LENGTH_SHORT).show();
						claimActivityId = tempPromotionId.getObjectId();
						String message = "Thank you for claiming this promotion, please show this "
								+ "number to the cashier to earn your points "
								+ claimActivityId;
						AlertDialog.Builder builder = new AlertDialog.Builder(
								getActivity());
						builder.setMessage(message)
								.setPositiveButton("Ok",
										dialogClaimClickListener)
								.setNeutralButton("Share",
										dialogClaimClickListener).show();
					} else {
						parseErrorDialog(e);
					}
				}
			});
		}

		/*
		 * Getting objectId of claiming activity to
		 */

		// Dialog Box Action Listener

		DialogInterface.OnClickListener dialogClaimClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					// Do Nothing
					break;

				case DialogInterface.BUTTON_NEUTRAL:
					Intent sendIntent = new Intent();
					sendIntent.setAction(Intent.ACTION_SEND);
					sendIntent.putExtra(Intent.EXTRA_TEXT,
							"Hey I Just Claim a " + promoTitle);
					sendIntent.setType("text/plain");
					startActivity(sendIntent);
					break;
				}
			}
		};

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

}
