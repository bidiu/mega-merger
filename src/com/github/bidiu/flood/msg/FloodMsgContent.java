package com.github.bidiu.flood.msg;

import java.awt.Color;

import com.github.bidiu.megamerge.msg.AbstractMsgContent;

public class FloodMsgContent extends AbstractMsgContent {
	
	public static final Color COLOR = Color.RED;

	@Override
	public String getInfo() {
		return "Flood Broadcast Message";
	}
	
	@Override
	public Color getColor() {
		return COLOR;
	}

}
