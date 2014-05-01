package com.example.storein.model;

import com.parse.ParseFile;
import com.parse.ParseGeoPoint;

public class Place {

	String address;
	String name;
	Integer phone;
	Integer rating;
	Integer totalCheckIn;
	ParseGeoPoint location;
	ParseFile image;

	public Place() {

	}

	public Place(String address, String name, Integer phone, Integer rating,
			Integer totalCheckIn, ParseGeoPoint location, ParseFile image) {
		super();
		this.address = address;
		this.name = name;
		this.phone = phone;
		this.rating = rating;
		this.totalCheckIn = totalCheckIn;
		this.location = location;
		this.image = image;
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

	public ParseFile getImage() {
		return image;
	}

	public void setImage(ParseFile image) {
		this.image = image;
	}

}
