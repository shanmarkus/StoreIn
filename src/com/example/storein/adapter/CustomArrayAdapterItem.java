package com.example.storein.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.storein.R;
import com.example.storein.model.Item;

public class CustomArrayAdapterItem extends ArrayAdapter<Item> {

	Context context;

	public CustomArrayAdapterItem(Context context, int textViewResourceId,
			List<Item> items) {
		super(context, textViewResourceId, items);
		this.context = context;
	}

	/* private view holder class */
	private class ViewHolder {
		TextView mTextListItemName;
		TextView mTextListItemDescription;
		TextView mTextListItemTotalLoved;

	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		Item record = (Item) getItem(position);

		LayoutInflater mInflater = (LayoutInflater) context
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_item_layout, null);
			holder = new ViewHolder();
			holder.mTextListItemName = (TextView) convertView
					.findViewById(R.id.textListActivityFriendName);
			holder.mTextListItemDescription = (TextView) convertView
					.findViewById(R.id.textlistActivityFriendActivity);
			holder.mTextListItemTotalLoved = (TextView) convertView
					.findViewById(R.id.textItemDetailTotalReward);

			convertView.setTag(holder);
		} else
			holder = (ViewHolder) convertView.getTag();

		// show the data from database
		holder.mTextListItemName.setText(record.getName());
		holder.mTextListItemDescription.setText(record.getDescription());
		holder.mTextListItemTotalLoved.setText(record.getItemLoved() + "");

		return convertView;
	}
}