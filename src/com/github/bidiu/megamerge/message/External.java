package com.github.bidiu.megamerge.message;

import java.awt.Color;

/**
 * 
 * @author sunhe
 * @date Nov 22, 2016
 */
public class External extends AbstractMsgContent {

	public static final Color COLOR = Color.BLUE;
	
	@Override
	public String getInfo() {
		return "Yes, I'm external.";
	}

	@Override
	public Color getColor() {
		return COLOR;
	}

}
