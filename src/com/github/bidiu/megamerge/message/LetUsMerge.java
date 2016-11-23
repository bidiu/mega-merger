package com.github.bidiu.megamerge.message;

import java.awt.Color;

import com.github.bidiu.megamerge.common.City;

public class LetUsMerge extends AbstractMsgContent {

	public static final Color COLOR = Color.RED;
	
	private City fromCity;
	
	public LetUsMerge(City fromCity) {
		this.fromCity = fromCity;
	}
	
	public City getFromCity() {
		return fromCity;
	}

	public void setFromCity(City fromCity) {
		this.fromCity = fromCity;
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
