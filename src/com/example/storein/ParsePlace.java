package com.example.storein;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

@ParseClassName("Place")
public class ParsePlace extends ParseObject {
	public String getName() {
		return getString("name");
	}
	
	public String getAddress(){
		return getString("address");
	}
	
	public void setAddress(String value){
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
	
	public Integer getRating(){
		return getInt("rating");
	}
	
	

	public static ParseQuery<ParsePlace> getQuery() {
		return ParseQuery.getQuery(ParsePlace.class);
	}
}
