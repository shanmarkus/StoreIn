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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass. Activities that
 * contain this fragment must implement the
 * {@link HomeFragment.OnFragmentInteractionListener} interface to handle
 * interaction events. Use the {@link BrowseFragment#newInstance} factory method
 * to create an instance of this fragment.
 * 
 */
public class BrowseFragment extends Fragment {
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";

	// TODO: Rename and change types of parameters
	private String mParam1;
	private String mParam2;

	// UI Variable
	GridView mGridView;
	String[] categories;

	// Other Variables
	public static final String TAG = BrowseFragment.class.getSimpleName();
	protected ArrayList<HashMap<String, String>> categoriesInfo = new ArrayList<HashMap<String, String>>();
	public HashMap<String, String> categoryInfo = new HashMap<String, String>();
	protected ArrayList<String> objectsId = new ArrayList<String>();

	// private OnFragmentInteractionListener mListener;

	// TODO: Rename and change types and number of parameters
	public static BrowseFragment newInstance(String param1, String param2) {
		BrowseFragment fragment = new BrowseFragment();
		Bundle args = new Bundle();
		args.putString(ARG_PARAM1, param1);
		args.putString(ARG_PARAM2, param2);
		fragment.setArguments(args);
		return fragment;
	}

	public BrowseFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			mParam1 = getArguments().getString(ARG_PARAM1);
			mParam2 = getArguments().getString(ARG_PARAM2);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View rootView = inflater.inflate(R.layout.fragment_browse, container,
				false);
		mGridView = (GridView) rootView.findViewById(R.id.gridView);
		getListCategories();
		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		onClickCategoriesList();
	}

	/*
	 * Added Function
	 */

	// Get list of categories
	public void getListCategories() {
		getActivity().setProgressBarIndeterminateVisibility(true);
		ParseQuery<ParseObject> query = ParseQuery
				.getQuery(ParseConstants.TABLE_PROMOTION_CATEGORY);
		query.orderByAscending(ParseConstants.KEY_CREATED_AT);
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> categoryInfos, ParseException e) {
				getActivity().setProgressBarIndeterminateVisibility(false);
				if (e == null) {
					// success
					for (ParseObject category : categoryInfos) {
						HashMap<String, String> categoryInfo = new HashMap<String, String>();
						String categoryId = category.getObjectId();
						String categoryName = category
								.getString(ParseConstants.KEY_NAME);
						categoryInfo.put(ParseConstants.KEY_NAME, categoryName);
						objectsId.add(categoryId);
						categoriesInfo.add(categoryInfo);
					}
					setAdapter();
				} else {
					// failed
					Log.e(TAG, e.getMessage());
					AlertDialog.Builder builder = new AlertDialog.Builder(
							getActivity());
					builder.setMessage(e.getMessage())
							.setTitle(R.string.error_title)
							.setPositiveButton(android.R.string.ok, null);
					AlertDialog dialog = builder.create();
					dialog.show();
				}

			}
		});
	}

	// Setting up the grid view
	public void setAdapter() {
		String[] keys = { ParseConstants.KEY_NAME, ParseConstants.KEY_RATING };
		int[] ids = { android.R.id.text1, android.R.id.text2 };

		SimpleAdapter adapter = new SimpleAdapter(getActivity(),
				categoriesInfo, android.R.layout.simple_list_item_2, keys, ids);

		mGridView.setAdapter(adapter);
	}

	// Add listener to GridView

	public void onClickCategoriesList() {
		mGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String categoryId = objectsId.get(position);
				Intent intent = new Intent(getActivity(), PromotionList.class);
				intent.putExtra(ParseConstants.KEY_OBJECT_ID, categoryId);
				startActivity(intent);
			}
		});
	}
}
