package com.example.storein;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

@ParseClassName("Item")
public class ParseItem extends ParseObject {

	public String getID() {
		return getString("objectId");
	}

	public String getName() {
		return getString("name");
	}

	public void setName(String value) {
		put("name", value);
	}

	public String getDescription() {
		return getString("description");
	}

	public void setDescription(String value) {
		put("description", value);
	}

	public Integer getRating() {
		return getInt("rating");
	}

	public static ParseQuery<ParseItem> getQuery() {
		return ParseQuery.getQuery(ParseItem.class);
	}
}
