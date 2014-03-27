package com.example.storein;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

//Parse Import

public class SignUpActivity extends ActionBarActivity {
	private final static String TAG = SignUpActivity.class.getSimpleName();

	protected EditText mUsername;
	protected EditText mPassword;
	protected EditText mEmail;
	protected Button mSignUpButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_sign_up);

		mUsername = (EditText) findViewById(R.id.usernameField);
		mPassword = (EditText) findViewById(R.id.passwordField);
		mEmail = (EditText) findViewById(R.id.emailField);
		mSignUpButton = (Button) findViewById(R.id.signupButton);

		mSignUpButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String username = mUsername.getText().toString();
				String password = mPassword.getText().toString();
				String email = mEmail.getText().toString();

				username = username.trim();
				password = password.trim();
				email = email.trim();

				// TODO Updated to isEmpty and API 8 for Froyo
				if (username == "" || password == "" || email == "") {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							SignUpActivity.this);
					builder.setMessage(R.string.signup_error_message)
							.setTitle(R.string.signup_error_title)
							.setPositiveButton(android.R.string.ok, null);
					AlertDialog dialog = builder.create();
					dialog.show();
				} else {
					setProgressBarIndeterminateVisibility(true);
					// Create new User !!

					ParseUser user = new ParseUser();
					user.setUsername(username);
					user.setEmail(email);
					user.setPassword(password);

					user.signUpInBackground(new SignUpCallback() {

						@Override
						public void done(ParseException e) {
							setProgressBarIndeterminateVisibility(false);
							if (e == null) {
								Intent intent = new Intent(SignUpActivity.this,
										MainActivity.class);
								intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
								startActivity(intent);
							} else {
								// Add Alert Dialog
								AlertDialog.Builder builder = new AlertDialog.Builder(
										SignUpActivity.this);
								builder.setMessage(e.getMessage())
										.setTitle(R.string.signup_error_title)
										.setPositiveButton(android.R.string.ok,
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
}
