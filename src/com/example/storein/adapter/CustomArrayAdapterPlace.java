package com.example.storein.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.storein.ParseConstants;
import com.example.storein.R;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

public class CustomArrayAdapterPlace extends ParseQueryAdapter<Place> {
	static ParseGeoPoint location;
	static Integer MAX_PlACE_SEARCH_DISTANCE;
	static Integer MAX_PLACE_SEARCH_RESULTS;

	public CustomArrayAdapterPlace(Context context) {
		super(context, new ParseQueryAdapter.QueryFactory<Place>() {
			public ParseQuery<Place> create() {
				// Here we can configure a ParseQuery to display
				// only top-rated meals.
				ParseObject.registerSubclass(Place.class);
				ParseQuery<Place> query = new ParseQuery<Place>(
						ParseConstants.TABLE_PLACE);
				query.whereWithinKilometers(ParseConstants.KEY_LOCATION,
						location, MAX_PlACE_SEARCH_DISTANCE);
				query.orderByAscending(ParseConstants.KEY_NAME);
				query.setLimit(MAX_PLACE_SEARCH_RESULTS);
				return query;
			}
		});
	}

	Context context;

	@Override
	public View getItemView(Place meal, View v, ViewGroup parent) {

		if (v == null) {
			v = View.inflate(getContext(), R.layout.list_place_layout, null);
		}

		super.getItemView(meal, v, parent);

		ParseImageView mImagePlaceBackground = (ParseImageView) v
				.findViewById(R.id.imagePlaceBackground);
		ParseFile photoFile = meal.getImage();
		if (photoFile != null) {
			mImagePlaceBackground.setParseFile(photoFile);
			mImagePlaceBackground.loadInBackground(new GetDataCallback() {
				@Override
				public void done(byte[] data, ParseException e) {
					// nothing to do
				}
			});
		}

		TextView mTextPlaceName = (TextView) v.findViewById(R.id.placeName);
		mTextPlaceName.setText(meal.getName());
		TextView mTextPlaceAddress = (TextView) v
				.findViewById(R.id.placeAddress);
		mTextPlaceAddress.setText(meal.getAddress());
		return v;
	}

	public static void setLocation(ParseGeoPoint location) {
		CustomArrayAdapterPlace.location = location;
	}

	public static void setMAX_PlACE_SEARCH_DISTANCE(
			Integer mAX_PlACE_SEARCH_DISTANCE) {
		MAX_PlACE_SEARCH_DISTANCE = mAX_PlACE_SEARCH_DISTANCE;
	}

	public static void setMAX_PLACE_SEARCH_RESULTS(
			Integer mAX_PLACE_SEARCH_RESULTS) {
		MAX_PLACE_SEARCH_RESULTS = mAX_PLACE_SEARCH_RESULTS;
	}

}