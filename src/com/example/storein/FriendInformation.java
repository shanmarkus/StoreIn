package com.example.storein;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class FriendInformation extends Fragment {
	protected static final String TAG = FriendInformation.class.getSimpleName();

	// UI Declaration
	EditText mTextFindFriend;
	ImageButton mSearchButton;
	ListView mListFriends;

	// Fixed Variables
	ArrayList<HashMap<String, String>> friendsInfo = new ArrayList<HashMap<String, String>>();
	protected ArrayList<String> friendIds = new ArrayList<String>();
	HashMap<String, String> friendInfo = new HashMap<String, String>();

	String userId = ParseUser.getCurrentUser().getObjectId();

	// Parse Variables
	ParseObject currentUser = ParseObject.createWithoutData(
			ParseConstants.TABLE_USER, userId);

	public static HomeFragment newInstance(String param1, String param2) {
		HomeFragment fragment = new HomeFragment();
		Bundle args = new Bundle();

		fragment.setArguments(args);
		return fragment;
	}

	public FriendInformation() {
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
		View rootView = inflater.inflate(R.layout.fragment_friend_information,
				container, false);

		// UI Declaration
		mListFriends = (ListView) rootView.findViewById(R.id.listFriend);
		mSearchButton = (ImageButton) rootView.findViewById(R.id.SearchButton);
		mTextFindFriend = (EditText) rootView.findViewById(R.id.textFindFriend);

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		findUserFollowing();
		searchButtonClicked();
	}

	/*
	 * Clear ArrayList
	 */

	private void clearArrayList() {
		friendsInfo.clear();
		friendInfo.clear();
		friendIds.clear();
	}

	/*
	 * Find List of Friend
	 */

	protected void findUserFollowing() {
		getActivity().setProgressBarIndeterminateVisibility(true);
		// Clear ArrayList
		clearArrayList();

		// Do the Query
		ParseQuery<ParseObject> query = ParseQuery
				.getQuery(ParseConstants.TABLE_REL_USER_USER);
		query.whereEqualTo(ParseConstants.KEY_USER_ID, currentUser);
		query.include(ParseConstants.KEY_FOLLOWING_ID);
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> friends, ParseException e) {
				getActivity().setProgressBarIndeterminateVisibility(false);
				Toast.makeText(getActivity(), friends.size() + " ",
						Toast.LENGTH_SHORT).show();
				if (e == null) {
					for (ParseObject friend : friends) {
						// setting the hash map for adapter
						HashMap<String, String> friendInfo = new HashMap<String, String>();

						// get the following list
						ParseObject tempFriend = friend
								.getParseObject(ParseConstants.KEY_FOLLOWING_ID);
						String friendName = tempFriend
								.getString(ParseConstants.KEY_NAME);
						String friendId = tempFriend.getObjectId();

						// add to array list
						friendInfo.put(ParseConstants.KEY_NAME, friendName);
						friendsInfo.add(friendInfo);
						friendIds.add(friendId);
					}
					// Set the adapter
					setAdapter();
					// On Item click listener
					friendOnClicked();
				} else {
					errorAlertDialog(e);
				}
			}
		});
	}

	/*
	 * When user tap friend direct it to the friend information
	 */

	private void friendOnClicked() {
		mListFriends.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String tempFriendId = friendIds.get(position);
				Intent intent = new Intent(getActivity(), FriendDetail.class);
				intent.putExtra(ParseConstants.KEY_OBJECT_ID, tempFriendId);
				startActivity(intent);

			}
		});
	}

	/*
	 * On searchButton clicked function
	 */

	private void searchButtonClicked() {
		mSearchButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// find friend
				findFriend();
			}
		});
	}

	/*
	 * Find Friend query
	 */

	private void findFriend() {
		String friendId = mTextFindFriend.getText().toString();
		if (friendId.isEmpty()) {
			// Create notification
			Toast.makeText(getActivity(), "Sorry Friend Id cannot be empty",
					Toast.LENGTH_SHORT).show();
		}

		// Send to other activity to find friend
		Intent intent = new Intent(getActivity(), FriendDetail.class);
		intent.putExtra(ParseConstants.KEY_OBJECT_ID, friendId);
		startActivity(intent);
	}

	/*
	 * Setup adapter
	 */
	public void setAdapter() {
		String[] keys = { ParseConstants.KEY_NAME, ParseConstants.KEY_REVIEW };
		int[] ids = { android.R.id.text1, android.R.id.text2 };

		SimpleAdapter adapter = new SimpleAdapter(getActivity(), friendsInfo,
				android.R.layout.simple_list_item_2, keys, ids);

		mListFriends.setAdapter(adapter);
	}

	/*
	 * Error Dialog Parse
	 */
	private void errorAlertDialog(ParseException e) {
		// failed
		Log.e(TAG, e.getMessage());
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(e.getMessage()).setTitle(R.string.error_title)
				.setPositiveButton(android.R.string.ok, null);
		AlertDialog dialog = builder.create();
		dialog.show();
	}

}
