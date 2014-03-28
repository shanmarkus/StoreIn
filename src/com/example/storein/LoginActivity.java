package com.example.storein;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class LoginActivity extends Activity {

	// Values for email and password at the time of the login attempt.
	private String username;
	private String password;

	// UI references.
	private EditText mUserNameField;
	private EditText mPasswordField;
	private TextView mSignUpText;
	private Button mLoginButton;

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

		mLoginButton.setOnClickListener(new OnClickListener() {

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
								public void done(ParseUser user,
										ParseException e) {
									setProgressBarIndeterminateVisibility(false);
									if (e == null) {
										Intent intent = new Intent(
												LoginActivity.this,
												MainActivity.class);
										intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
										intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
										startActivity(intent);
									} else {
										AlertDialog.Builder builder = new AlertDialog.Builder(
												LoginActivity.this);
										builder.setMessage(e.getMessage())
												.setTitle(
														R.string.login_error_title)
												.setPositiveButton(
														android.R.string.ok,
														null);
										AlertDialog dialog = builder.create();
										dialog.show();
									}
								}
							});
				}
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}
}
