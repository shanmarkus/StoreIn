package com.example.storein;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

@ParseClassName("LocationItems")
public class ParseLocationItems extends ParseObject {

	public String getID() {
		return getString("objectId");
	}

	public String getItemID() {
		return getString("item");
	}

	public static ParseQuery<ParseLocationItems> getQuery() {
		return ParseQuery.getQuery(ParseLocationItems.class);
	}

}
