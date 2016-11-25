package com.github.bidiu.megamerge.message;

import java.awt.Color;

import com.github.bidiu.megamerge.common.City;

public class MergeMe extends AbstractMsgContent {
	
	public static final Color COLOR = Color.WHITE;
	
	private City mergeTo;
	
	private boolean toAskMinWeight;
	
	public MergeMe(City mergeTo) {
		this(mergeTo, false);
	}
	
	public MergeMe(City mergeTo, boolean toAskMinWeight) {
		this.mergeTo = mergeTo;
		this.toAskMinWeight = toAskMinWeight;
	}
	
	public City getFromCity() {
		return mergeTo;
	}

	public void setFromCity(City fromCity) {
		this.mergeTo = fromCity;
	}
	
	public City getMergeTo() {
		return mergeTo;
	}

	public void setMergeTo(City mergeTo) {
		this.mergeTo = mergeTo;
	}

	public boolean isToAskMinWeight() {
		return toAskMinWeight;
	}

	public void setToAskMinWeight(boolean toAskMinWeight) {
		this.toAskMinWeight = toAskMinWeight;
	}

	@Override
	public String getInfo() {
		return "Merge me, I'm in city " + mergeTo + ".";
	}

	@Override
	public Color getColor() {
		return COLOR;
	}

}
