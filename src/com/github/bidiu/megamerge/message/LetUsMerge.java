package com.github.bidiu.megamerge.message;

import java.awt.Color;

import com.github.bidiu.megamerge.common.City;

public class LetUsMerge extends AbstractMsgContent {

	public static final Color COLOR = Color.RED;
	
	private City fromCity;
	
	/** sender's UUID */
	private String uuid;
	
	public LetUsMerge(City fromCity, String uuid) {
		this.fromCity = fromCity;
		this.uuid = uuid;
	}
	
	public City getFromCity() {
		return fromCity;
	}

	public void setFromCity(City fromCity) {
		this.fromCity = fromCity;
	}
	
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Override
	public String getInfo() {
		return "Let's merge, from city " + fromCity + ".";
	}

	@Override
	public Color getColor() {
		return COLOR;
	}
	
}
