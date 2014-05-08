package com.example.storein;

import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

public class LoginActivity extends Activity {
	private static final String TAG = LoginActivity.class.getSimpleName();

	// Values for email and password at the time of the login attempt.
	private String username;
	private String password;

	// UI references.
	private EditText mUserNameField;
	private EditText mPasswordField;
	private TextView mSignUpText;
	private Button mLoginButton;
	private Button mLoginWithFacebook;

	// Variables
	ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_login);

		// Set up the sign up button
		mSignUpText = (TextView) findViewById(R.id.signUpText);
		mSignUpText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(LoginActivity.this,
						SignUpActivity.class);
				startActivity(intent);
			}
		});

		// Set up the login form.
		mUserNameField = (EditText) findViewById(R.id.usernameField);
		mPasswordField = (EditText) findViewById(R.id.passwordField);
		mLoginButton = (Button) findViewById(R.id.loginButton);
		mLoginWithFacebook = (Button) findViewById(R.id.buttonLoginFacebook);

		mLoginWithFacebook.setOnClickListener(loginWithFacebookListener);
		mLoginButton.setOnClickListener(normalLogin);

		ParseUser currentUser = ParseUser.getCurrentUser();
		if (currentUser != null && ParseFacebookUtils.isLinked(currentUser)) {
			navigateToMainActivity();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
	}

	/*
	 * added function
	 */

	/*
	 * Normal Login Function
	 */

	OnClickListener normalLogin = new OnClickListener() {

		@Override
		public void onClick(View v) {
			username = mUserNameField.getText().toString();
			password = mPasswordField.getText().toString();

			username = username.trim();
			password = password.trim();

			if (username.isEmpty() || password.isEmpty()) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						LoginActivity.this);
				builder.setMessage(R.string.login_error_message);
				builder.setTitle(R.string.login_error_title);
				builder.setPositiveButton(android.R.string.ok, null);
				AlertDialog dialog = builder.create();
				dialog.show();
			} else {
				// Login
				setProgressBarIndeterminateVisibility(true);
				ParseUser.logInInBackground(username, password,
						new LogInCallback() {

							@Override
							public void done(ParseUser user, ParseException e) {
								setProgressBarIndeterminateVisibility(false);
								if (e == null) {
									navigateToMainActivity();
								} else {
									errorAlertDialog(e);
								}
							}
						});
			}
		}
	};

	/*
	 * Navigate to main Activity function
	 */

	private void navigateToMainActivity() {
		Intent intent = new Intent(LoginActivity.this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
	}

	/*
	 * Login With Facebook Function
	 */

	private void loginWithFacebook() {

		List<String> permissions = Arrays.asList("basic_info", "user_about_me",
				"user_relationships", "user_birthday", "user_location");
		ParseFacebookUtils.logIn(permissions, this, new LogInCallback() {
			@Override
			public void done(ParseUser user, ParseException e) {
				if (e == null) {
					if (user == null) {
						Log.d(TAG, "user has cancelled the facebook Login");
					} else if (user.isNew()) {
						Log.d(TAG,
								"User signed up and logged in through Facebook!");
						navigateToMainActivity();
					} else {
						Log.d(TAG, "User logged in through Facebook!");
						navigateToMainActivity();
					}
				} else {
					errorAlertDialog(e);
				}

			}
		});
	}

	OnClickListener loginWithFacebookListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			loginWithFacebook();
		}
	};

	/*
	 * Parse Error handling
	 */

	private void errorAlertDialog(ParseException e) {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				LoginActivity.this);
		builder.setMessage(e.getMessage()).setTitle(R.string.login_error_title)
				.setPositiveButton(android.R.string.ok, null);
		AlertDialog dialog = builder.create();
		dialog.show();
	}

}
