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
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class WriteReviewItem extends ActionBarActivity {
	
	// Variable
	public final static String TAG = WriteReviewItem.class.getSimpleName();
	protected static String itemId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(
					R.layout.fragment_write_review_item, container, false);

			return rootView;
		}

		@Override
		public void onResume() {
		}

		public void onSubmitBtn() {
			ParseUser user = ParseUser.getCurrentUser();
			String userId = user.getObjectId();
			String reviewText = mTxtUserReview.getText().toString();
			int rating = Math.round(mRatingBar.getRating());

			ParseObject reviewItem = new ParseObject(
					ParseConstants.TABLE_ITEM_REVIEW);
			reviewItem.put(ParseConstants.KEY_USER_ID, userId);
			reviewItem.put(ParseConstants.KEY_ITEM_ID, itemId);
			reviewItem.put(ParseConstants.KEY_REVIEW, reviewText);
			reviewItem.put(ParseConstants.KEY_RATING, rating);
			reviewItem.saveInBackground(new SaveCallback() {

				@Override
				public void done(ParseException e) {
					if (e == null) {
						// success
						Toast.makeText(getActivity(), "Review Saved",
								Toast.LENGTH_SHORT).show();
						Intent intent = new Intent(getActivity(),
								ItemDetail.class);
						startActivity(intent);
					} else {
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
	}

}
