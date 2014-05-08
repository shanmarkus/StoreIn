package com.example.storein;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.PushService;

public class StoreInApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		Parse.initialize(this, "mgDWPieWTeiy4SRWiefaJ5QFLTlj2XGJwVAVuiJ3",
				"g5j92J8jFzZXupil1EWT2Y0Sbo0ExCKvpdI1bRXV");
		ParseFacebookUtils.initialize("1410606722535641");

		// push notification testing
		PushService.setDefaultPushCallback(this, MainActivity.class);
		
		ParseInstallation.getCurrentInstallation().saveInBackground();
		Parse.setLogLevel(Parse.LOG_LEVEL_VERBOSE);

	}
}
