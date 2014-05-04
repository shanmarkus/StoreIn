package com.example.storein.model;

import java.util.Date;

import com.parse.ParseFile;

public class Promotion {
	String objectId;
	String categoryId;
	String name;
	Date startDate;
	Date endDate;
	String requirement;
	boolean claimable;
	Integer rewardPoint;
	ParseFile image;

	public Promotion() {

	}

	public Promotion(String objectId, String categoryId, String name,
			Date startDate, Date endDate, String requirement,
			boolean claimable, Integer rewardPoint, ParseFile image) {
		super();
		this.objectId = objectId;
		this.categoryId = categoryId;
		this.name = name;
		this.startDate = startDate;
		this.endDate = endDate;
		this.requirement = requirement;
		this.claimable = claimable;
		this.rewardPoint = rewardPoint;
		this.image = image;
	}

	public String getObjectId() {
		return objectId;
	}

	public String getCategoryId() {
		return categoryId;
	}

	public String getName() {
		return name;
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public String getRequirement() {
		return requirement;
	}

	public boolean isClaimable() {
		return claimable;
	}

	public Integer getRewardPoint() {
		return rewardPoint;
	}

	public ParseFile getImage() {
		return image;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public void setRequirement(String requirement) {
		this.requirement = requirement;
	}

	public void setClaimable(boolean claimable) {
		this.claimable = claimable;
	}

	public void setRewardPoint(Integer rewardPoint) {
		this.rewardPoint = rewardPoint;
	}

	public void setImage(ParseFile image) {
		this.image = image;
	}

}
