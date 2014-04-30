package com.example.storein;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.ParseObject;
import com.parse.ParseUser;

public class HomeFragment extends Fragment {
	protected static final String TAG = HomeFragment.class.getSimpleName();

	// UI Variables
	ImageView mHomeUserName;
	TextView mHomeNumberCheckIn;
	TextView mHomeNumberFollower;
	TextView mHomeNumberFollowing;
	TextView mTextRecommendedPlace;
	TextView mTextRecommendedPromotion;
	ListView mListClaimedPromotion;
	
	// Fixed Variables
	String userId = ParseUser.getCurrentUser().getObjectId();
	ParseObject currentUser = ParseUser.createWithoutData(ParseConstants.TABLE_USER, userId);
	

	public static HomeFragment newInstance(String param1, String param2) {
		HomeFragment fragment = new HomeFragment();
		Bundle args = new Bundle();

		fragment.setArguments(args);
		return fragment;
	}

	public HomeFragment() {
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
		View rootView = inflater.inflate(R.layout.fragment_home, container,
				false);

		// UI Declaration
		mHomeNumberCheckIn = (TextView) rootView.findViewById(R.id.homeNumberCheckIn);
		mHomeNumberFollower = (TextView) rootView.findViewById(R.id.homeNumberFollower);
		mHomeNumberCheckIn = (TextView) rootView.findViewById(R.id.homeNumberCheckIn);
		mHomeUserName = (ImageView) rootView.findViewById(R.id.homeUserName);
		mListClaimedPromotion = (ListView) rootView
				.findViewById(R.id.listClaimedPromotion);
		mTextRecommendedPromotion = (TextView) rootView
				.findViewById(R.id.textRecommendedPromotion);
		mTextRecommendedPlace = (TextView) rootView
				.findViewById(R.id.textRecommendedPlace);

		return rootView;
	}
	
	/*
	 * Get User Information 
	 * include number of check in, follower, following
	 */
	
	

}
