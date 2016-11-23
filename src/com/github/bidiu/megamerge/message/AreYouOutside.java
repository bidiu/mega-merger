package com.github.bidiu.megamerge.message;

import java.awt.Color;

import com.github.bidiu.megamerge.common.City;

/**
 * Are you outside?
 * 
 * @author sunhe
 * @date Nov 22, 2016
 */
public class AreYouOutside extends AbstractMsgContent {

	public static final Color COLOR = Color.YELLOW;
	
	private City fromCity;
	
	public AreYouOutside(City fromCity) {
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
		return "Are you outside from city " + fromCity + "?";
	}

	@Override
	public Color getColor() {
		return COLOR;
	}

}
