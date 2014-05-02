package com.example.storein.model;

import com.parse.ParseFile;
import com.parse.ParseGeoPoint;

public class Place {

	String objectId;
	String address;
	String name;
	Integer phone;
	Integer rating;
	Integer totalCheckIn;
	ParseGeoPoint location;
	Boolean isPromotion;
	String category;

	public Place() {

	}

	public Place(String objectId, String address, String name, Integer phone,
			Integer rating, Integer totalCheckIn, ParseGeoPoint location,
			Boolean isPromotion, String category) {
		super();
		this.objectId = objectId;
		this.address = address;
		this.name = name;
		this.phone = phone;
		this.rating = rating;
		this.totalCheckIn = totalCheckIn;
		this.location = location;
		this.isPromotion = isPromotion;
		this.category = category;
	}

	public String getAddress() {
		return address;
	}

	public String getName() {
		return name;
	}

	public Integer getPhone() {
		return phone;
	}

	public Integer getRating() {
		return rating;
	}

	public Integer getTotalCheckIn() {
		return totalCheckIn;
	}

	public ParseGeoPoint getLocation() {
		return location;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPhone(Integer phone) {
		this.phone = phone;
	}

	public void setRating(Integer rating) {
		this.rating = rating;
	}

	public void setTotalCheckIn(Integer totalCheckIn) {
		this.totalCheckIn = totalCheckIn;
	}

	public void setLocation(ParseGeoPoint location) {
		this.location = location;
	}

	public Boolean getIsPromotion() {
		return isPromotion;
	}

	public void setIsPromotion(Boolean isPromotion) {
		this.isPromotion = isPromotion;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

}
