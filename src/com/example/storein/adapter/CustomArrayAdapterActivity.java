package com.example.storein.adapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.Activity;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.storein.R;
import com.example.storein.model.UserActivity;

public class CustomArrayAdapterActivity extends ArrayAdapter<UserActivity> {

	Context context;

	public CustomArrayAdapterActivity(Context context, int textViewResourceId,
			List<UserActivity> activities) {
		super(context, textViewResourceId, activities);
		this.context = context;
	}

	/* private view holder class */
	private class ViewHolder {
		TextView mTextListActivityFriendName;
		TextView mTextlistActivityFriendActivity;

	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		UserActivity record = (UserActivity) getItem(position);

		LayoutInflater mInflater = (LayoutInflater) context
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

		if (convertView == null) {
			convertView = mInflater
					.inflate(R.layout.list_activity_layout, null);
			holder = new ViewHolder();
			holder.mTextListActivityFriendName = (TextView) convertView
					.findViewById(R.id.textListActivityFriendName);
			holder.mTextlistActivityFriendActivity = (TextView) convertView
					.findViewById(R.id.textlistActivityFriendActivity);

			convertView.setTag(holder);
		} else
			holder = (ViewHolder) convertView.getTag();

		// Set Date
		Date temp = record.getCreatedAt();
		String date = new SimpleDateFormat("yyyy-MM-dd").format(temp);

		// show the data from database
		holder.mTextListActivityFriendName.setText(record.getuserName());
		holder.mTextlistActivityFriendActivity.setText(record.getobjectName());

		return convertView;
	}
}