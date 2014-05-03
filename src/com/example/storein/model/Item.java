package com.example.storein.model;

import com.parse.ParseFile;

public class Item {
	String objectId;
	String name;
	Integer rating;
	String description;
	Integer itemLoved;
	ParseFile image;

	public Item() {

	}

	public Item(String objectId, String name, Integer rating,
			String description, Integer itemLoved, ParseFile image) {
		super();
		this.objectId = objectId;
		this.name = name;
		this.rating = rating;
		this.description = description;
		this.itemLoved = itemLoved;
		this.image = image;
	}

	public String getName() {
		return name;
	}

	public Integer getRating() {
		return rating;
	}

	public String getDescription() {
		return description;
	}

	public Integer getItemLoved() {
		return itemLoved;
	}

	public ParseFile getImage() {
		return image;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setRating(Integer rating) {
		this.rating = rating;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setItemLoved(Integer itemLoved) {
		this.itemLoved = itemLoved;
	}

	public void setImage(ParseFile image) {
		this.image = image;
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

}
