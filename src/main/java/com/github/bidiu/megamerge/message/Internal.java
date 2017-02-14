package com.github.bidiu.megamerge.message;

import java.awt.Color;

public class Internal extends AbstractMsgContent {
	
	public static final Color COLOR = Color.PINK;
	
	@Override
	public String getInfo() {
		return "No, I'm internal.";
	}

	@Override
	public Color getColor() {
		return COLOR;
	}

}
