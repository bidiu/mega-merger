package com.github.bidiu.megamerge.message;

import java.awt.Color;

import com.github.bidiu.megamerge.common.City;

/**
 * Also ask the minimum
 * 
 * @author sunhe
 * @date Nov 22, 2016
 */
public class Notification extends AbstractMsgContent {

	public static final Color COLOR = Color.GREEN;
	
	private City newCity;
	
	private boolean toAskMinWeight;
	
	public Notification(City newCity) {
		this(newCity, true);
	}
	
	public Notification(City newCity, boolean toAskMinWeight) {
		this.toAskMinWeight = toAskMinWeight;
	}
	
	public City getNewCity() {
		return newCity;
	}

	public void setNewCity(City newCity) {
		this.newCity = newCity;
	}
	
	public boolean isToAskMinWeight() {
		return toAskMinWeight;
	}

	public void setToAskMinWeight(boolean toAskMinWeight) {
		this.toAskMinWeight = toAskMinWeight;
	}

	@Override
	public String getInfo() {
		return "Notification of new city " + newCity + ".";
	}

	@Override
	public Color getColor() {
		return COLOR;
	}
	
	
}
