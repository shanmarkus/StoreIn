package com.example.storein;

import com.parse.Parse;

import android.app.Application;

public class StoreInApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		Parse.initialize(this, "mgDWPieWTeiy4SRWiefaJ5QFLTlj2XGJwVAVuiJ3",
				"g5j92J8jFzZXupil1EWT2Y0Sbo0ExCKvpdI1bRXV");
	}
}