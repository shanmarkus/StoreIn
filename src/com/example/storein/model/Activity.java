package com.example.storein.model;

import java.util.Date;

public class Activity {
	String objectId;
	String userName;
	String objectName;
	String type;
	Date createdAt;

	public Activity() {

	}

	public Activity(String objectId, String userName, String objectName,
			String type, Date createdAt) {
		super();
		this.objectId = objectId;
		this.userName = userName;
		this.objectName = objectName;
		this.type = type;
		this.createdAt = createdAt;
	}

	public String getObjectId() {
		return objectId;
	}

	public String getuserName() {
		return userName;
	}

	public String getobjectName() {
		return objectName;
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

	public void setuserName(String userName) {
		this.userName = userName;
	}

	public void setobjectName(String objectName) {
		this.objectName = objectName;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

}
