package com.example.storein;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class BeaconFragment extends Fragment {
	protected static final String TAG = BeaconFragment.class.getSimpleName();

	public static BeaconFragment newInstance(String param1, String param2) {
		BeaconFragment fragment = new BeaconFragment();
		Bundle args = new Bundle();

		fragment.setArguments(args);
		return fragment;
	}

	public BeaconFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {

		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_beacon_fragment,
				container, false);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	/*
	 * Check Bluetooth
	 */

	private void checkBluetooth(BluetoothAdapter mBluetoothAdapter) {
		if (mBluetoothAdapter == null) {
			Toast.makeText(
					getActivity(),
					"Sorry you're device does not have bluetooth to use beacon",
					Toast.LENGTH_SHORT).show();
		} else {
			if (!mBluetoothAdapter.isEnabled()) {
				showBluetoothDisabledAlertToUser();
			}
		}
	}

	/*
	 * Alert Dialog for Bluetooth
	 */
	private void showBluetoothDisabledAlertToUser() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				getActivity());
		alertDialogBuilder
				.setMessage(
						"Bluetooth is disabled in your device. Would you like to enable it?")
				.setCancelable(false)
				.setPositiveButton("Goto Settings Page To Enable Bluetooth",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								Intent callBluetoothSettingIntent = new Intent(
										android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
								startActivity(callBluetoothSettingIntent);
							}
						});
		alertDialogBuilder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = alertDialogBuilder.create();
		alert.show();
	}
}
