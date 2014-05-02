package com.example.storein.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.storein.R;
import com.example.storein.model.Place;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;

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
		ParseImageView mImagePlaceBackground;

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
			holder.mTextPlaceAddress = (TextView) convertView
					.findViewById(R.id.placeAddress);
			holder.mImagePromotionIcon = (ImageView) convertView
					.findViewById(R.id.imagePromotionIcon);
			holder.mImagePlaceBackground = (ParseImageView) convertView
					.findViewById(R.id.imagePlaceBackground);

			convertView.setTag(holder);
		} else
			holder = (ViewHolder) convertView.getTag();

		// show the data from database
		holder.mTextPlaceName.setText(record.getName() + "");
		holder.mTextPlaceAddress.setText(record.getAddress());
		Toast.makeText(getContext(), record.getIsPromotion() + " ",
				Toast.LENGTH_SHORT).show();
		if (record.getIsPromotion() == true) {
			holder.mImagePromotionIcon
					.setImageResource(R.drawable.promotion_icon);
		} else {
			// do nothing
		}

		return convertView;
	}
}