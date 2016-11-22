package com.github.bidiu.megamerge.msg;

import java.awt.Color;

public interface MessageContent {
	
	public String getInfo();
	
	public Object getProperty(String key);
	
	public void setProperty(String key, Object value);
	
	public Color getColor();
	
}
