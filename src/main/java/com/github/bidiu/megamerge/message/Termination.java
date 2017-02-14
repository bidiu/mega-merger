package com.github.bidiu.megamerge.message;

import java.awt.Color;

import com.github.bidiu.megamerge.common.City;

public class Termination extends AbstractMsgContent {
	
	public static final Color COLOR = Color.MAGENTA;
	
	public City getCity() {
		return City.NON_ELECTED;
	}
	
	@Override
	public String getInfo() {
		return "Termination.";
	}

	@Override
	public Color getColor() {
		return COLOR;
	}

}
