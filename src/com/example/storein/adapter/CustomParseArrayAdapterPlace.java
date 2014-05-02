package com.example.storein.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.storein.ParseConstants;
import com.example.storein.R;
import com.example.storein.model.ParsePlace;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseImageView;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

public class CustomParseArrayAdapterPlace extends ParseQueryAdapter<ParsePlace> {

	Context context;
	static ParseGeoPoint location;
	static Integer MAX_PlACE_SEARCH_DISTANCE;
	static Integer MAX_PLACE_SEARCH_RESULTS;

	public CustomParseArrayAdapterPlace(Context context) {
		super(context, new ParseQueryAdapter.QueryFactory<ParsePlace>() {
			public ParseQuery<ParsePlace> create() {
				ParseQuery<ParsePlace> query = ParseQuery
						.getQuery(ParseConstants.TABLE_PLACE);
				query.whereWithinKilometers(ParseConstants.KEY_LOCATION,
						location, MAX_PlACE_SEARCH_DISTANCE);
				query.orderByAscending(ParseConstants.KEY_NAME);
				query.setLimit(MAX_PLACE_SEARCH_RESULTS);
				return query;
			}
		});
	}

	@Override
	public View getItemView(ParsePlace meal, View convertView, ViewGroup parent) {

		if (convertView == null) {
			convertView = View.inflate(getContext(),
					R.layout.list_place_layout, null);
		}

		super.getItemView(meal, convertView, parent);

		ParseImageView mImagePlaceBackground = (ParseImageView) convertView
				.findViewById(R.id.imagePlaceBackground);
		ParseFile photoFile = meal.getParseFile("image");
		if (photoFile != null) {
			mImagePlaceBackground.setParseFile(photoFile);
			mImagePlaceBackground.loadInBackground(new GetDataCallback() {
				@Override
				public void done(byte[] data, ParseException e) {
					// nothing to do
				}
			});
		}

		TextView mTextPlaceName = (TextView) convertView
				.findViewById(R.id.placeName);
		mTextPlaceName.setText(meal.getName());

		TextView mTextPlaceAddress = (TextView) convertView
				.findViewById(R.id.placeAddress);
		mTextPlaceAddress.setText(meal.getAddress());

		return convertView;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public static void setLocation(ParseGeoPoint location) {
		CustomParseArrayAdapterPlace.location = location;
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