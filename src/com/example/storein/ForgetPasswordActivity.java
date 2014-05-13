package com.example.storein;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;

public class ForgetPasswordActivity extends Activity {
	private static final String TAG = ForgetPasswordActivity.class
			.getSimpleName();

	// UI references.
	private EditText mEmailField;
	private Button mResetButton;

	// Variables
	private String userEmail;
	ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_forget_password);

		// Set up the on click listener
		mResetButton.setOnClickListener(resetButtonListener);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	/*
	 * added function
	 */

	/*
	 * On Click Listener
	 */

	OnClickListener resetButtonListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			userEmail = mEmailField.getText().toString();

			userEmail = userEmail.trim();

			if (userEmail.isEmpty() || userEmail.isEmpty()) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						ForgetPasswordActivity.this);
				builder.setMessage("Field cannot be empty");
				builder.setTitle(R.string.error_title);
				builder.setPositiveButton(android.R.string.ok, null);
				AlertDialog dialog = builder.create();
				dialog.show();
			} else {
				// Login
				setProgressBarIndeterminateVisibility(true);
				ParseUser.requestPasswordResetInBackground(userEmail,
						new RequestPasswordResetCallback() {

							@Override
							public void done(ParseException e) {
								if (e == null) {
									Toast.makeText(
											getApplicationContext(),
											"Password has been send to your email",
											Toast.LENGTH_SHORT).show();
									navigateToLoginActivity();
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

	private void navigateToLoginActivity() {
		Intent intent = new Intent(ForgetPasswordActivity.this,
				LoginActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
	}

	/*
	 * Parse Error handling
	 */

	private void errorAlertDialog(ParseException e) {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				ForgetPasswordActivity.this);
		builder.setMessage(e.getMessage()).setTitle(R.string.login_error_title)
				.setPositiveButton(android.R.string.ok, null);
		AlertDialog dialog = builder.create();
		dialog.show();
	}

}
