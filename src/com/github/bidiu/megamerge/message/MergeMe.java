package com.github.bidiu.megamerge.message;

import java.awt.Color;

import com.github.bidiu.megamerge.common.City;

public class MergeMe extends AbstractMsgContent {
	
	public static final Color COLOR = Color.WHITE;
	
	private City mergeTo;
	
	public MergeMe(City mergeTo) {
		this.mergeTo = mergeTo;
	}
	
	public City getFromCity() {
		return mergeTo;
	}

	public void setFromCity(City fromCity) {
		this.mergeTo = fromCity;
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
