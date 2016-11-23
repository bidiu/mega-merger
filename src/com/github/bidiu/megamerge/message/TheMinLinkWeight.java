package com.github.bidiu.megamerge.message;

import java.awt.Color;

/**
 * Note that I use
 * 
 * 			")"
 * 
 * as infinite weight, as all normal weights begin with '(' char
 * 
 * @author sunhe
 * @date Nov 22, 2016
 */
public class TheMinLinkWeight extends AbstractMsgContent {
	
	public static final Color COLOR = Color.ORANGE;
	
	private String minWeight;
	
	public TheMinLinkWeight(String minWeight) {
		this.minWeight = minWeight;
	}
	
	public String getMinWeight() {
		return minWeight;
	}

	public void setMinWeight(String minWeight) {
		this.minWeight = minWeight;
	}

	@Override
	public String getInfo() {
		return "The minimal link weight is " + minWeight + ".";
	}

	@Override
	public Color getColor() {
		return COLOR;
	}

}
