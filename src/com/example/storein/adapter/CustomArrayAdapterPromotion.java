package com.example.storein.adapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.storein.R;
import com.example.storein.model.Promotion;

public class CustomArrayAdapterPromotion extends ArrayAdapter<Promotion> {

	Context context;

	public CustomArrayAdapterPromotion(Context context, int textViewResourceId,
			List<Promotion> promotions) {
		super(context, textViewResourceId, promotions);
		this.context = context;
	}

	/* private view holder class */
	private class ViewHolder {
		TextView mTextListPromotionName;
		TextView mTextListPromotionDuration;
		TextView mTextListTotalReward;
		TextView mTextTitleFlash;

	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		Promotion record = (Promotion) getItem(position);

		LayoutInflater mInflater = (LayoutInflater) context
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_promotion_layout,
					null);
			holder = new ViewHolder();
			holder.mTextListPromotionName = (TextView) convertView
					.findViewById(R.id.textListPromotionName);
			holder.mTextListPromotionDuration = (TextView) convertView
					.findViewById(R.id.textListPromotionDuration);
			holder.mTextListTotalReward = (TextView) convertView
					.findViewById(R.id.textListItemTotalReward);
			holder.mTextTitleFlash = (TextView) convertView
					.findViewById(R.id.textListPromotionTitleFlash);

			convertView.setTag(holder);
		} else
			holder = (ViewHolder) convertView.getTag();
		
		// Set Date 
		Date temp = record.getEndDate();
		String date = new SimpleDateFormat("yyyy-MM-dd").format(temp);
		
		// show the data from database
		holder.mTextListPromotionName.setText(record.getName());
		holder.mTextListPromotionDuration.setText(date);
		holder.mTextListTotalReward.setText(record.getRewardPoint()+"");
		if (record.isClaimable()== true) {
			holder.mTextTitleFlash.setVisibility(View.VISIBLE);
		} else {
			holder.mTextTitleFlash.setVisibility(View.INVISIBLE);
		}

		return convertView;
	}
}