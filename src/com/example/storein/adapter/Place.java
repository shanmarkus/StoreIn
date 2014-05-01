package com.example.storein.adapter;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

@ParseClassName("Place")
public class Place extends ParseObject {

	public Place() {

	}

	public String getID() {
		return getString("objectId");
	}

	public String getName() {
		return getString("name");
	}

	public String getAddress() {
		return getString("address");
	}

	public void setAddress(String value) {
		put("address", value);
	}

	public void setName(String value) {
		put("name", value);
	}

	public Integer getPhone() {
		return getInt("phone");
	}

	public void setPhone(Integer value) {
		put("phone", value);
	}

	public ParseGeoPoint getLocation() {
		return getParseGeoPoint("location");
	}

	public void setLocation(ParseGeoPoint value) {
		put("location", value);
	}

	public Integer getRating() {
		return getInt("rating");
	}
	
	public ParseFile getImage() {
		return getParseFile("image");
	}

	public void setImage(ParseFile file) {
		put("image", file);
	}

}
