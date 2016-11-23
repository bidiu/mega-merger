package com.github.bidiu.megamerge.message;

import java.awt.Color;

/**
 * 
 * @author sunhe
 * @date Nov 22, 2016
 */
public class MinLinkWeight extends AbstractMsgContent {
	
	/**
	 * Note that I use
	 * 
 	 * 			")"
 	 * 
 	 * as infinite weight, as all normal weights begin with '(' char
	 */
	public static final String INF_WEIGHT = ")";
	
	public static final Color COLOR = Color.ORANGE;
	
	private String minWeight;
	
	public MinLinkWeight(String minWeight) {
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
