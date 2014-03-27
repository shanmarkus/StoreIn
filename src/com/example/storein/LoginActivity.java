package com.example.storein;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends Activity {

	// Values for email and password at the time of the login attempt.
	private String username;
	private String password;

	// UI references.
	private EditText mEmailView;
	private EditText mPasswordView;
	private EditText mSignUpButton;
	private Button mSignInButton;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);

		// Set up the sign up button
		mSignUpButton = (EditText) findViewById(R.id.sign_up_button);
		mSignUpButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(LoginActivity.this,
						SignUpActivity.class);
				startActivity(intent);
			}
		});

		// Set up the login form.
		mEmailView = (EditText) findViewById(R.id.email);
		mPasswordView = (EditText) findViewById(R.id.password);
		mSignInButton = (Button) findViewById(R.id.sign_in_button);

		mSignInButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				username = mEmailView.getText().toString();
				password = mPasswordView.getText().toString();
				
				username = username.trim();
				password = password.trim();
				
				//TODO change to isEmpty and updated to API 8 for froyo
				if (username == "" || password =="") {
					AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
					builder.setMessage(R.string.login_error_message);
					builder.setTitle(R.string.login_error_title);
					builder.setPositiveButton(android.R.string.ok, null);
					AlertDialog dialog = builder.create();
					dialog.show();
				} else {
					//TODO Login successfull
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
