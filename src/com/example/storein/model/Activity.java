package com.example.storein.model;

import java.util.Date;

public class Activity {
	String objectId;
	String userId;
	String friendId;
	String type;
	Date createdAt;

	public Activity() {

	}

	public Activity(String objectId, String userId, String friendId,
			String type, Date createdAt) {
		super();
		this.objectId = objectId;
		this.userId = userId;
		this.friendId = friendId;
		this.type = type;
		this.createdAt = createdAt;
	}

	public String getObjectId() {
		return objectId;
	}

	public String getUserId() {
		return userId;
	}

	public String getFriendId() {
		return friendId;
	}

	public String getType() {
		return type;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void setFriendId(String friendId) {
		this.friendId = friendId;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

}
