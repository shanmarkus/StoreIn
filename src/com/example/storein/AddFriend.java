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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.CountCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class AddFriend extends ActionBarActivity {

	private static final String TAG = AddFriend.class.getSimpleName();

	// Intent Extra
	static String friendId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_friend);

		friendId = getIntent().getStringExtra(ParseConstants.KEY_OBJECT_ID);

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

	public static class PlaceholderFragment extends Fragment {

		// UI Variables
		Button mButtonAdd;
		TextView mTextUserName;

		// Fixed variables
		boolean isFriendExist;
		boolean isAlreadyFollow;
		String userId = ParseUser.getCurrentUser().getObjectId();
		ProgressDialog progressDialog;

		// ParseConstant
		ParseObject currentUser = ParseObject.createWithoutData(
				ParseConstants.TABLE_USER, userId);
		ParseObject friendObj;
		ParseImageView mImageViewFriend;

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_add_friend,
					container, false);

			// UI Initiate
			mButtonAdd = (Button) rootView.findViewById(R.id.ButtonAdd);
			mTextUserName = (TextView) rootView.findViewById(R.id.textUserName);
			mImageViewFriend = (ParseImageView) rootView
					.findViewById(R.id.imageViewFriend);

			return rootView;
		}

		@Override
		public void onResume() {
			super.onResume();
			checkFriendId();
		}

		/*
		 * Add Function
		 */

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
		 * Check for friendId
		 */

		private void checkFriendId() {
			initProgressDialog();
			if (friendId == userId) {
				isFriendExist = false;
			} else {
				ParseQuery<ParseUser> query = ParseUser.getQuery();
				query.whereMatches(ParseConstants.KEY_USERNAME, friendId);
				query.getFirstInBackground(new GetCallback<ParseUser>() {

					@Override
					public void done(ParseUser user, ParseException e) {
						if (e == null) {
							if (user != null) {
								isFriendExist = true;
								String friendObjId = user.getObjectId();
								friendObj = ParseObject.createWithoutData(
										ParseConstants.TABLE_USER, friendObjId);

								// Second Function
								checkAlreadyFollow();
							} else {
								progressDialog.dismiss();
								isFriendExist = false;
								Toast.makeText(getActivity(),
										"Friend ID Does not found",
										Toast.LENGTH_SHORT).show();
								Intent intent = new Intent(getActivity(),
										MainActivity.class);
								// intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
								startActivity(intent);
							}
						} else {
							// dismiss the dialog
							progressDialog.dismiss();
							Toast.makeText(getActivity(),
									"Friend ID Does not found",
									Toast.LENGTH_SHORT).show();
							errorAlertDialog(e);
						}
					}
				});
			}
		}

		/*
		 * Check whether this user already follow the checked user
		 */

		private void checkAlreadyFollow() {
			findFriendDetail();
			ParseQuery<ParseObject> query = ParseQuery
					.getQuery(ParseConstants.TABLE_REL_USER_USER);
			query.whereEqualTo(ParseConstants.KEY_USER_ID, currentUser);
			query.whereEqualTo(ParseConstants.KEY_FOLLOWING_ID, friendObj);
			query.countInBackground(new CountCallback() {

				@Override
				public void done(int total, ParseException e) {
					if (e == null) {
						if (total != 0) {
							progressDialog.dismiss();
							isAlreadyFollow = true;
							mButtonAdd.setEnabled(false);
							Toast.makeText(getActivity(),
									"You already follow this user",
									Toast.LENGTH_SHORT).show();
						} else {
							isAlreadyFollow = false;
							// third function
							mButtonAdd.setEnabled(true);
						}
					} else {
						progressDialog.dismiss();
						errorAlertDialog(e);
					}
				}
			});
		}

		/*
		 * the Query
		 */

		private void findFriendDetail() {
			ParseQuery<ParseUser> query = ParseQuery
					.getQuery(ParseConstants.TABLE_USER);
			query.whereEqualTo(ParseConstants.KEY_USERNAME, friendId);
			query.getFirstInBackground(new GetCallback<ParseUser>() {

				@Override
				public void done(ParseUser user, ParseException e) {
					if (e == null) {
						// success
						progressDialog.dismiss();
						ParseFile image = user
								.getParseFile(ParseConstants.KEY_IMAGE);
						String friendName = user
								.getString(ParseConstants.KEY_NAME);
						mTextUserName.setText(friendName);
						mButtonAdd.setOnClickListener(addFriend);
						mImageViewFriend.setParseFile(image);
						// LOADER IMAGE
						mImageViewFriend
								.loadInBackground(new GetDataCallback() {

									@Override
									public void done(byte[] arg0,
											ParseException arg1) {
										// DO nothing
									}
								});

					} else {
						// failed
						progressDialog.dismiss();
						errorAlertDialog(e);
					}
				}
			});

		}

		/*
		 * On Click Listener
		 */

		OnClickListener addFriend = new OnClickListener() {

			@Override
			public void onClick(View v) {
				String userId = ParseUser.getCurrentUser().getObjectId();
				ParseObject tempUser = ParseObject.createWithoutData(
						ParseConstants.TABLE_USER, userId);
				ParseObject object = new ParseObject(
						ParseConstants.TABLE_REL_USER_USER);
				object.put(ParseConstants.KEY_USER_ID, tempUser);
				object.put(ParseConstants.KEY_FOLLOWING_ID, friendObj);
				object.saveInBackground(new SaveCallback() {

					@Override
					public void done(ParseException e) {
						if (e == null) {
							Toast.makeText(getActivity(),
									"Successful Following User",
									Toast.LENGTH_SHORT).show();
							Intent intent = new Intent(getActivity(),
									MainActivity.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
							startActivity(intent);
						} else {
							errorAlertDialog(e);
						}
					}
				});
			}
		};

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
