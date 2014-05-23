package com.example.storein.model;

public class PromotionCategory {
	String objectId;
	String name;

	public PromotionCategory() {

	}

	public PromotionCategory(String objectId, String name) {
		this.objectId = objectId;
		this.name = name;
	}

	public String getObjectId() {
		return objectId;
	}

	public String getName() {
		return name;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public void setName(String name) {
		this.name = name;
	}

}
