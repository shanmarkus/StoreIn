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
		return convertView;
	}
}