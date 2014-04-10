package com.example.storein;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.CountCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class ItemDetail extends ActionBarActivity {

	public static final String TAG = ItemDetail.class.getSimpleName();
	protected static String itemId;
	protected static String isLoved;

	// UI Variable Declaration
	Button mBtnLoveIt;
	Button mBtnReviewIt;
	Button mBtnCheckReview;
	TextView mItemTitleLabel;
	TextView mItemDescription;
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_item_detail);
		setSupportProgressBarIndeterminateVisibility(true);
		itemId = getIntent().getExtras()
				.getString(ParseConstants.KEY_OBJECT_ID);

		isLoved = getIntent().getExtras().getString("isLoved");

		mBtnCheckReview = (Button) findViewById(R.id.btnCheckReview);
		mBtnLoveIt = (Button) findViewById(R.id.btnLoveIt);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.item_detail, menu);
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
		// UI Variable Declaration

		Button mBtnLoveIt;
		Button mBtnReviewIt;
		Button mBtnCheckReview;
		TextView mItemTitleLabel;
		TextView mItemDescription;
		ViewPager mViewPager;

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_item_detail,
					container, false);
			mViewPager = (ViewPager) rootView.findViewById(R.id.view_pager);
			mBtnLoveIt = (Button) rootView.findViewById(R.id.btnLoveIt);
			mBtnReviewIt = (Button) rootView.findViewById(R.id.btnReviewIt);

			ImagePagerAdapter adapter = new ImagePagerAdapter();
			mViewPager.setAdapter(adapter);

			return rootView;
		}

		@Override
		public void onResume() {
			super.onResume();
			onClickReviewItButton();
			findItemDetail();
			checkLoveButton();
		}

		/*
		 * Check whether user already love the item or not
		 */
		public void checkLoveButton() {
			mBtnLoveIt = (Button) getActivity().findViewById(R.id.btnLoveIt);
			ParseUser user = ParseUser.getCurrentUser();
			String userId = user.getObjectId();

			ParseQuery<ParseObject> query = ParseQuery
					.getQuery(ParseConstants.TABLE_ITEM_LOVED);
			query.whereEqualTo(ParseConstants.KEY_ITEM_ID, itemId);
			query.whereEqualTo(ParseConstants.KEY_USER_ID, userId);
			query.countInBackground(new CountCallback() {

				@Override
				public void done(int love, ParseException e) {
					getActivity().setProgressBarIndeterminateVisibility(false);
					if (e == null) {
						// success
						if (love != 0) {
							isLoved = "true";
							mBtnLoveIt.setText("Un-Love It");
							onClickLoveItButton();
						} else {
							isLoved = "false";
							mBtnLoveIt.setText("Love It");
							onClickLoveItButton();
						}
					} else {
						// failed
						Log.e(TAG, e.getMessage());
					}
				}
			});
		}

		/*
		 * When user click the Love It Button
		 */
		public void onClickLoveItButton() {

			if (isLoved == null) {
				checkLoveButton();
			}
			checkLoveButton();

			mBtnLoveIt = (Button) getActivity().findViewById(R.id.btnLoveIt);

			if (isLoved.equals("false")) {

				// The user HAVE NOT liked it
				mBtnLoveIt.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						getActivity().setProgressBarIndeterminateVisibility(
								true);
						Toast.makeText(getActivity(), isLoved,
								Toast.LENGTH_SHORT).show();
						// Toast Dialog
						Toast.makeText(getActivity(), "Thank you for the Love",
								Toast.LENGTH_SHORT).show();

						// Get current userId and itemId
						ParseUser user = ParseUser.getCurrentUser();
						String userId = user.getObjectId();

						// Do the query
						ParseObject itemLoved = new ParseObject(
								ParseConstants.TABLE_ITEM_LOVED);
						itemLoved.put(ParseConstants.KEY_USER_ID, userId);
						itemLoved.put(ParseConstants.KEY_ITEM_ID, itemId);
						itemLoved.saveInBackground(new SaveCallback() {

							@Override
							public void done(ParseException e) {
								getActivity()
										.setProgressBarIndeterminateVisibility(
												false);
								if (e == null) {
									// Success
									checkLoveButton();
									Toast.makeText(getActivity(),
											"Love it Parse success",
											Toast.LENGTH_SHORT).show();
									onResume();
								} else {
									// failed
									Log.e(TAG, e.getMessage());
								}

							}
						});

					}
				});
			} else if (isLoved.equals("true")) {
				// User ALREADY like the item
				mBtnLoveIt.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						getActivity().setProgressBarIndeterminateVisibility(
								true);
						// Toast Notification
						Toast.makeText(getActivity(), "Un-Love The item :(",
								Toast.LENGTH_SHORT).show();
						Toast.makeText(getActivity(), isLoved,
								Toast.LENGTH_SHORT).show();

						// get the userId
						ParseUser user = ParseUser.getCurrentUser();
						String userId = user.getObjectId();

						ParseQuery<ParseObject> query = ParseQuery
								.getQuery(ParseConstants.TABLE_ITEM_LOVED);
						query.whereEqualTo(ParseConstants.KEY_USER_ID, userId);
						query.whereEqualTo(ParseConstants.KEY_ITEM_ID, itemId);
						query.getFirstInBackground(new GetCallback<ParseObject>() {

							@Override
							public void done(ParseObject itemLoved,
									ParseException e) {
								getActivity()
										.setProgressBarIndeterminateVisibility(
												false);
								if (e == null) {
									try {
										itemLoved.delete();
										checkLoveButton();
										Toast.makeText(getActivity(),
												" UnLove it Parse success",
												Toast.LENGTH_SHORT).show();
										onResume();

									} catch (ParseException e1) {
										e1.printStackTrace();
									}

								} else {
									// failed
									Log.e(TAG, e.getMessage());
								}
							}
						});
					}
				});
			}
		}

		/*
		 * When user wants to review the item by giving comments and ratings
		 */
		public void onClickReviewItButton() {
			mBtnReviewIt = (Button) getActivity()
					.findViewById(R.id.btnReviewIt);
			mBtnReviewIt.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(getActivity(),
							WriteReviewItem.class);
					intent.putExtra(ParseConstants.KEY_OBJECT_ID, itemId);
					startActivity(intent);
				}
			});

		}

		/*
		 * When user click the Check Review Button
		 */

		public void onClickCheckReviewButton() {
			mBtnCheckReview = (Button) getActivity().findViewById(
					R.id.btnCheckReview);
			mBtnCheckReview.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(getActivity(), ItemReview.class);
					intent.putExtra(ParseConstants.KEY_OBJECT_ID, itemId);
					startActivity(intent);
				}
			});
		}

		/*
		 * Find the detail of an item including description and rating
		 */
		public void findItemDetail() {
			ParseQuery<ParseObject> query = ParseQuery
					.getQuery(ParseConstants.TABLE_ITEM);
			query.whereEqualTo(ParseConstants.KEY_OBJECT_ID, itemId);
			query.getFirstInBackground(new GetCallback<ParseObject>() {

				@Override
				public void done(ParseObject item, ParseException e) {
					getActivity().setProgressBarIndeterminateVisibility(false);
					if (e == null) {
						// success
						mItemTitleLabel = (TextView) getActivity()
								.findViewById(R.id.itemTitleLabel);
						mItemDescription = (TextView) getActivity()
								.findViewById(R.id.itemDescriptionLabel);
						String title = item.getString(ParseConstants.KEY_NAME);
						String description = item
								.getString(ParseConstants.KEY_DESCRIPTION);

						mItemTitleLabel.setText(title);
						mItemDescription.setText(description);
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
		 * Image Adapter Class
		 */

		public class ImagePagerAdapter extends PagerAdapter {
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

				// Go to the gallery page of promotional
				imageView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Log.d(TAG, "MAHO");

					}
				});
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
