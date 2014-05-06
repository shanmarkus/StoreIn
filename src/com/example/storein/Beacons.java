package com.example.storein;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

public class Beacons extends Activity {

	private static final String TAG = Beacons.class.getSimpleName();

	// Beacon Manager
	private static final String ESTIMOTE_PROXIMITY_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
	private static final Region ALL_ESTIMOTE_BEACONS = new Region("regionId",
			ESTIMOTE_PROXIMITY_UUID, null, null);
	private BeaconManager beaconManager = new BeaconManager(this);
	private static List<Beacon> currentBeacons;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_beacon_manager);

		beaconManager.setRangingListener(new BeaconManager.RangingListener() {

			@Override
			public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {
				currentBeacons = beacons;
				Log.d(TAG, "Ranged beacons: " + beacons);
			}
		});
	}

	@Override
	public void onStart() {
		super.onStart();
		beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
			@Override
			public void onServiceReady() {
				try {
					beaconManager.startRanging(ALL_ESTIMOTE_BEACONS);
				} catch (RemoteException e) {
					Log.e(TAG, "Cannot start ranging", e);
				}
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		test();
	}

	@Override
	public void onStop() {
		super.onStop();
		try {
			beaconManager.stopRanging(ALL_ESTIMOTE_BEACONS);
		} catch (RemoteException e) {
			Log.e(TAG, "Cannot stop but it does not matter now", e);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.beacon_manager, menu);
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

	/*
	 * Added Function
	 */

	public void test() {
		for (Beacon beacon : currentBeacons) {
			Toast.makeText(this, beacon.getName(), Toast.LENGTH_SHORT).show();
		}
	}

}
