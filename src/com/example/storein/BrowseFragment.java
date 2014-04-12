package com.example.storein;

import java.util.ArrayList;
import java.util.HashMap;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;

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
	protected ArrayList<HashMap<String, String>> categoriesInfo = new ArrayList<HashMap<String, String>>();
	public HashMap<String, String> categoryInfo = new HashMap<String, String>();

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

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		setAdapter();
	}

	/*
	 * Added Function
	 */

	// Setting up the grid view
	public void setAdapter() {
		categories = getResources().getStringArray(R.array.category_label);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1, categories);

		mGridView.setAdapter(adapter);
	}

}
