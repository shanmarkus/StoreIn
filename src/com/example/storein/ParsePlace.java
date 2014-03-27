package com.example.storein;

import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class ParsePlace extends ParseObject {
	public String getText() {
		return getString("name");
	}

	public void setText(String value) {
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
	
	public Integer getRating(){
		return getInt("rating");
	}
	
	

	public static ParseQuery<ParsePlace> getQuery() {
		return ParseQuery.getQuery(ParsePlace.class);
	}
}
