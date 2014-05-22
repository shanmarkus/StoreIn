package com.example.storein;

import java.io.ByteArrayOutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.widget.EditText;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class EditProfile extends ActionBarActivity {

	private static final String TAG = EditProfile.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_profile);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_profile, menu);
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

		// UI Variables
		protected EditText mUsername;
		protected EditText mPassword;
		protected EditText mRePassword;
		protected EditText mEmail;
		protected EditText mName;
		protected Button mGetImageButton;
		protected Button mSubmitButton;

		// Variables
		ParseUser user = ParseUser.getCurrentUser();
		String userId;
		String username;
		String name;
		String password;
		String rePassword;
		String email;
		public final static int REQ_CODE_PICK_IMAGE = 1;
		Bitmap yourSelectedImage;
		byte[] scaledData;
		ParseFile image;

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_edit_profile,
					container, false);

			// UI Variables
			mUsername = (EditText) rootView.findViewById(R.id.usernameField);
			mRePassword = (EditText) rootView
					.findViewById(R.id.rePasswordField);
			mPassword = (EditText) rootView.findViewById(R.id.passwordField);
			mEmail = (EditText) rootView.findViewById(R.id.emailField);
			mName = (EditText) rootView.findViewById(R.id.nameField);
			mGetImageButton = (Button) rootView
					.findViewById(R.id.editProfileGetImageButton);
			mSubmitButton = (Button) rootView
					.findViewById(R.id.editProfileSubmitButton);

			return rootView;
		}

		@Override
		public void onActivityResult(int requestCode, int resultCode,
				Intent imageReturnedIntent) {
			super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

			switch (requestCode) {
			case REQ_CODE_PICK_IMAGE:
				if (resultCode == Activity.RESULT_OK) {
					Uri selectedImage = imageReturnedIntent.getData();
					String[] filePathColumn = { MediaStore.Images.Media.DATA };

					Cursor cursor = getActivity().getContentResolver().query(
							selectedImage, filePathColumn, null, null, null);
					cursor.moveToFirst();

					int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
					String filePath = cursor.getString(columnIndex);
					cursor.close();

					yourSelectedImage = BitmapFactory.decodeFile(filePath);
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					yourSelectedImage.compress(Bitmap.CompressFormat.JPEG, 100,
							bos);

					scaledData = bos.toByteArray();
					userId = user.getObjectId();
					image = new ParseFile(userId + ".jpg", scaledData);
					image.saveInBackground();
					Toast.makeText(getActivity(), "image ready",
							Toast.LENGTH_SHORT).show();
				}
			}
		}

		@Override
		public void onResume() {
			super.onResume();
			getUserInformation();
			onClickFunction();
		}

		/*
		 * Added Function
		 */

		/*
		 * On Click Function
		 */

		private void onClickFunction() {
			mGetImageButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					getImage();
				}
			});

			mSubmitButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					updateUserInformation();
				}
			});
		}

		/*
		 * Getter Image Setting
		 */

		private void getImage() {
			Intent i = new Intent(
					Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(i, REQ_CODE_PICK_IMAGE);
		}

		/*
		 * submit new value
		 */

		private void updateUserInformation() {
			if (userId == null) {
				userId = user.getObjectId();
			}

			ParseQuery<ParseUser> query = ParseUser.getQuery();
			query.getInBackground(userId, new GetCallback<ParseUser>() {

				@Override
				public void done(ParseUser user, ParseException e) {
					if (e == null) {
						// get Value
						username = mUsername.getText().toString();
						name = mName.getText().toString();
						email = mEmail.getText().toString();
						password = mPassword.getText().toString();
						rePassword = mRePassword.getText().toString();

						// Check Image
						if (image == null) {
							Toast.makeText(
									getActivity(),
									"You have not put any image for your profile picture",
									Toast.LENGTH_SHORT).show();
						}

						// Check Password
						if (password.equals(rePassword)) {
							user.setUsername(username);
							user.setPassword(password);
							user.setEmail(email);
							user.put(ParseConstants.KEY_NAME, name);
							user.put(ParseConstants.KEY_IMAGE, image);
							user.saveInBackground(new SaveCallback() {

								@Override
								public void done(ParseException e) {
									if (e == null) {
										Toast.makeText(getActivity(),
												"Save Successful",
												Toast.LENGTH_SHORT).show();
										Intent intent = new Intent(
												getActivity(),
												MainActivity.class);
										startActivity(intent);

									} else {
										errorAlertDialog(e);
									}
								}
							});
						} else {
							Toast.makeText(getActivity(), "Password not Match",
									Toast.LENGTH_SHORT).show();
						}
					} else {
						errorAlertDialog(e);
					}
				}

			});
		}

		/*
		 * get user information
		 */

		private void getUserInformation() {
			if (userId == null) {
				userId = user.getObjectId();
			}

			ParseQuery<ParseUser> query = ParseUser.getQuery();
			query.getInBackground(userId, new GetCallback<ParseUser>() {

				@Override
				public void done(ParseUser user, ParseException e) {
					if (e == null) {
						// Get Values
						username = user.getUsername();
						name = user.getString(ParseConstants.KEY_NAME);
						email = user.getEmail();

						// Set to the UI
						mName.setText(name);
						mUsername.setText(username);
						mEmail.setText(email);
					} else {
						errorAlertDialog(e);
					}
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
