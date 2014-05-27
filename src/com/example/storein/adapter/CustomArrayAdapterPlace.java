package com.example.storein.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
			holder.mTextPlaceAddress = (TextView) convertView
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
			holder.mImagePromotionIcon.setImageResource(R.drawable.icon_star);
		} else {
			// do nothing
		}
		String temp = record.getCategory();
		if (temp.equals("Food and Drink")) {

			Bitmap image = decodeSampledBitmapFromResource(getContext()
					.getResources(), R.drawable.place_restaurant_and_bar, 100,
					100);
			holder.mImagePlaceBackground.setImageBitmap(image);
			holder.mPlaceWrapper
					.setBackgroundResource(R.color.foor_and_bvg_place);
		} else if (temp.equals("Convinience Store")) {
			Bitmap image = decodeSampledBitmapFromResource(getContext()
					.getResources(), R.drawable.place_convenience_store, 100,
					100);
			holder.mImagePlaceBackground.setImageBitmap(image);
			holder.mPlaceWrapper
					.setBackgroundResource(R.color.convenience_store_place);
		} else if (temp.equals("University")) {
			Bitmap image = decodeSampledBitmapFromResource(getContext()
					.getResources(), R.drawable.place_university, 100, 100);
			holder.mImagePlaceBackground.setImageBitmap(image);
			holder.mPlaceWrapper
					.setBackgroundResource(R.color.university_place);
		} else if (temp.equals("Book Store")) {
			Bitmap image = decodeSampledBitmapFromResource(getContext()
					.getResources(), R.drawable.place_bookstore, 100, 100);
			holder.mImagePlaceBackground.setImageBitmap(image);
			holder.mPlaceWrapper.setBackgroundResource(R.color.bookstore_place);
		} else if (temp.equals("Coffee Shop")) {
			Bitmap image = decodeSampledBitmapFromResource(getContext()
					.getResources(), R.drawable.place_coffee_shop, 100, 100);
			holder.mImagePlaceBackground.setImageBitmap(image);
			holder.mPlaceWrapper
					.setBackgroundResource(R.color.coffeeshop_place);
		} else if (temp.equals("Shopping Mall")) {
			Bitmap image = decodeSampledBitmapFromResource(getContext()
					.getResources(), R.drawable.place_shopping_mall, 100, 100);
			holder.mImagePlaceBackground.setImageBitmap(image);
			holder.mPlaceWrapper
					.setBackgroundResource(R.color.shopping_mall_place);
		} else {

		}
		return convertView;
	}

	/*
	 * Added function
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			// Calculate ratios of height and width to requested height and
			// width
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will
			// guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}

	public static Bitmap decodeSampledBitmapFromResource(Resources res,
			int resId, int reqWidth, int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);
	}

}