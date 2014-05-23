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

import com.example.storein.R;
import com.example.storein.model.PromotionCategory;

public class CustomArrayAdapterPromotionCategories extends
		ArrayAdapter<PromotionCategory> {

	Context context;

	public CustomArrayAdapterPromotionCategories(Context context,
			int textViewResourceId, List<PromotionCategory> promotions) {
		super(context, textViewResourceId, promotions);
		this.context = context;
	}

	/* private view holder class */
	private class ViewHolder {
		ImageView mImageView;
		TextView mTextView;

	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		PromotionCategory record = (PromotionCategory) getItem(position);

		LayoutInflater mInflater = (LayoutInflater) context
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

		if (convertView == null) {
			convertView = mInflater.inflate(
					R.layout.list_browse_category_layout, null);
			holder = new ViewHolder();
			holder.mImageView = (ImageView) convertView
					.findViewById(R.id.imageView);
			holder.mTextView = (TextView) convertView
					.findViewById(R.id.textView);

			convertView.setTag(holder);
		} else
			holder = (ViewHolder) convertView.getTag();

		// Set Date
		String tempName = record.getName();
		String objectID = record.getObjectId();

		// show the data from database
		holder.mTextView.setText(tempName);
		if (objectID.equals("KcrrXPZQGf")) {
			// Appareal
			holder.mImageView
					.setBackgroundResource(R.drawable.categories_apparel);
		} else if (objectID.equals("KHBlp64sKs")) {
			// Automotive
			holder.mImageView
					.setBackgroundResource(R.drawable.categories_automotive);
		} else if (objectID.equals("TwwYAAywnW")) {
			// Babies and Kids
			holder.mImageView
					.setBackgroundResource(R.drawable.categories_babies_and_kids);
		} else if (objectID.equals("Q6NN9YrPPR")) {
			// Books and MAgazine
			holder.mImageView
					.setBackgroundResource(R.drawable.categories_books_n_mag);
		} else if (objectID.equals("wzvWaypOuG")) {
			// College and Education
			holder.mImageView
					.setBackgroundResource(R.drawable.categories_college_and_education);
		} else if (objectID.equals("7i6Yz26mov")) {
			// DeptStore
			holder.mImageView
					.setBackgroundResource(R.drawable.categories_dept_store);
		} else if (objectID.equals("gIniMnWYgI")) {
			// Electronics
			holder.mImageView
					.setBackgroundResource(R.drawable.categories_electronics);
		} else if (objectID.equals("gHzn2Iysvi")) {
			// Green Living
			holder.mImageView
					.setBackgroundResource(R.drawable.categories_green_living);
		} else if (objectID.equals("XiG6NAxhfT")) {
			// Health and Beauty
			holder.mImageView
					.setBackgroundResource(R.drawable.categories_health_n_beauty);
		} else if (objectID.equals("wf7RMebx3R")) {
			// Hobby and Collectible
			holder.mImageView
					.setBackgroundResource(R.drawable.categories_hobby_and_collectible);
		} else if (objectID.equals("CIZ0gkEoFz")) {
			// House and Home
			holder.mImageView
					.setBackgroundResource(R.drawable.categories_house_n_home);
		} else if (objectID.equals("3C3lyL5EvU")) {
			// Jewellery
			holder.mImageView
					.setBackgroundResource(R.drawable.categories_jewelery);
		} else if (objectID.equals("A2NpI9sMm2")) {
			// Movies and Music
			holder.mImageView
					.setBackgroundResource(R.drawable.categories_movies);
		} else if (objectID.equals("vhge6Dn79J")) {
			// Outdoors
			holder.mImageView
					.setBackgroundResource(R.drawable.categories_outdoor);
		} else if (objectID.equals("7Ji88UPqiE")) {
			// Restaurant
			holder.mImageView
					.setBackgroundResource(R.drawable.categories_resto_n_bar);
		} else if (objectID.equals("rngzWZq5HE")) {
			// Restaurant
			holder.mImageView
					.setBackgroundResource(R.drawable.categories_grocery);
		} else if (objectID.equals("so6LBEfC0g")) {
			// Travel
			holder.mImageView
					.setBackgroundResource(R.drawable.categories_travel);
		} else {
			// Blank
		}

		return convertView;
	}
}