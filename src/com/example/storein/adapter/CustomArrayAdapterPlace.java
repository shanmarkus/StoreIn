package com.example.storein.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.storein.R;
import com.example.storein.model.Place;

public class CustomArrayAdapterPlace extends ArrayAdapter<Place> {

	Context context;

	public CustomArrayAdapterPlace(Context context, int textViewResourceId,
			List<Place> items) {
		super(context, textViewResourceId, items);
		this.context = context;
	}

	/* private view holder class */
	private class ViewHolder {
		TextView mTextPlaceName;
		TextView mTextPlaceAddress;
		ImageView mImagePromotionIcon;
		ImageView mImagePlaceBackground;
		RelativeLayout mPlaceWrapper;

	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		Place record = (Place) getItem(position);

		LayoutInflater mInflater = (LayoutInflater) context
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_place_layout, null);
			holder = new ViewHolder();
			holder.mTextPlaceName = (TextView) convertView
					.findViewById(R.id.placeName);
			holder.mTextPlaceAddress =  (TextView) convertView
					.findViewById(R.id.placeAddress);
			holder.mImagePromotionIcon = (ImageView) convertView
					.findViewById(R.id.imagePromotionIcon);
			holder.mImagePlaceBackground = (ImageView) convertView
					.findViewById(R.id.imagePlaceBackground);
			holder.mPlaceWrapper = (RelativeLayout) convertView
					.findViewById(R.id.placeWrapper);

			convertView.setTag(holder);
		} else
			holder = (ViewHolder) convertView.getTag();

		// show the data from database
		holder.mTextPlaceName.setText(record.getName() + "");
		holder.mTextPlaceAddress.setText(record.getAddress());
		if (record.getIsPromotion() == true) {
			holder.mImagePromotionIcon
					.setImageResource(R.drawable.icon_star);
		} else {
			// do nothing
		}
		String temp = record.getCategory();
		if (temp.equals("Food and Drink")) {
			holder.mImagePlaceBackground
					.setImageResource(R.drawable.place_restaurant_and_bar);
			holder.mPlaceWrapper
					.setBackgroundResource(R.color.foor_and_bvg_place);
		} else if (temp.equals("Convinience Store")) {
			holder.mImagePlaceBackground
					.setImageResource(R.drawable.place_convenience_store);
			holder.mPlaceWrapper
					.setBackgroundResource(R.color.convenience_store_place);
		} else if (temp.equals("University")) {
			holder.mImagePlaceBackground
					.setImageResource(R.drawable.place_university);
			holder.mPlaceWrapper
					.setBackgroundResource(R.color.university_place);
		} else if (temp.equals("Book Store")) {
			holder.mImagePlaceBackground
					.setImageResource(R.drawable.place_bookstore);
			holder.mPlaceWrapper.setBackgroundResource(R.color.bookstore_place);
		} else if (temp.equals("Coffee Shop")) {
			holder.mImagePlaceBackground
					.setImageResource(R.drawable.place_coffee_shop);
			holder.mPlaceWrapper
					.setBackgroundResource(R.color.coffeeshop_place);
		} else if (temp.equals("Shopping Mall")) {
			holder.mImagePlaceBackground
					.setImageResource(R.drawable.place_shopping_mall);
			holder.mPlaceWrapper
					.setBackgroundResource(R.color.shopping_mall_place);
		} else {

		}
		return convertView;
	}
}