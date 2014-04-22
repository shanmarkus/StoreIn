package com.example.storein;

import java.util.Date;

import android.app.AlertDialog;
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

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(
					R.layout.fragment_promotion_detail, container, false);
			checkFlashDeal();
			return rootView;
		}

		@Override
		public void onResume() {
			super.onResume();
		}

		/*
		 * Added Functions
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

		protected void checkFlashDeal() {
			mTextFlashDealNumber = (TextView) getActivity().findViewById(
					R.id.textFlashDealNumber);
			mTextFlashDealNumber.setVisibility(2);
			if (claimable == null) {
				getClaimableValue();
			}
			if (claimable == true) {
				mTextFlashDealNumber.setVisibility(1);
				getFlashPromotionQuantity();
			} else {
				mTextFlashDealNumber.setVisibility(2);
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
						String promoTitle = promotion
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
		 * if claimable value equal true then find the quantity of the promotion
		 */

		protected void getFlashPromotionQuantity() {

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
						// success
						Integer quantity = flashPromo
								.getInt(ParseConstants.KEY_QUOTA);
						String tempObjId = flashPromo.getObjectId();
						flashPromoQuota = quantity;
						mTextFlashDealNumber.setText(quantity);
						promotionQuotaId = tempObjId;

					} else {
						Toast.makeText(getActivity(), e.getMessage(),
								Toast.LENGTH_SHORT).show();
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

													// show AlertDialog

													String message = "Thank you for claiming this promotion, please show this "
															+ "number to the cashier to earn your points and get the rewards"
															+ claimActivityId;
													AlertDialog.Builder builder = new AlertDialog.Builder(
															getActivity());
													builder.setMessage(message)
															.setPositiveButton(
																	"Ok",
																	dialogClaimClickListener)
															.setNeutralButton(
																	"Share",
																	dialogClaimClickListener)
															.show();
												} else {
													// failed
												}
											}
										});
							}
						}
					} else {
						// the promotion is just a ordinary deal
						saveUserClaimActivity();
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
					}
				}
			});
		}

		/*
		 * Saving Log Function
		 */

		public void saveUserClaimActivity() {
			String userId = ParseUser.getCurrentUser().getObjectId();

			final ParseObject claimActivity = new ParseObject(
					ParseConstants.TABLE_ACTV_USER_CLAIM_PROMOTION);
			claimActivity.put(ParseConstants.KEY_USER_ID, userId);
			claimActivity.put(ParseConstants.KEY_PROMOTION_ID, promotionId);
			claimActivity.saveEventually(new SaveCallback() {

				@Override
				public void done(ParseException e) {
					if (e == null) {
						// Get the objectId for user claim activity
						String tempClaimActivityId = claimActivity
								.getObjectId();
						claimActivityId = tempClaimActivityId;
						Toast.makeText(getActivity(),
								"Claim Activity has been saved",
								Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(getActivity(), e.getMessage(),
								Toast.LENGTH_SHORT).show();
					}
				}
			});
		}

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
							"Hey I Just Claim a promotion");
					sendIntent.setType("text/plain");
					startActivity(sendIntent);
					break;
				}
			}
		};
	}

}
