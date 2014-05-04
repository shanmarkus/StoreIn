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
		TextView mTextlistActivityDate;

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
			holder.mTextlistActivityDate = (TextView) convertView
					.findViewById(R.id.textlistActivityDate);

			convertView.setTag(holder);
		} else
			holder = (ViewHolder) convertView.getTag();

		// Set Date
		Date temp = record.getCreatedAt();
		String date = new SimpleDateFormat("yyyy-MM-dd").format(temp);

		// show the data from database
		holder.mTextListActivityFriendName.setText(record.getuserName());
		String type = record.getType();
		String objectName = record.getobjectName();
		String message;
		if (type.equals("claim")) {
			message = "I just claimed " + objectName;
		} else {
			message = "I just check in at " + objectName;
		}
		holder.mTextlistActivityFriendActivity.setText(message);
		holder.mTextlistActivityDate.setText(date);

		return convertView;
	}
}