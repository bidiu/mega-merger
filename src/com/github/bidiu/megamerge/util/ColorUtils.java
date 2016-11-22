package com.github.bidiu.megamerge.util;

import java.awt.Color;

public class ColorUtils {
	
	public static final double TINT_FACTOR = 0.2;
	public static final double SHADE_FACTOR = 0.2;
	
	public static Color tint(Color color) {
		int srcR = color.getRed();
		int srcG = color.getGreen();
		int srcB = color.getBlue();
		
		int r = (int) (srcR + (255-srcR) * TINT_FACTOR);
		int g = (int) (srcG + (255-srcG) * TINT_FACTOR);
		int b = (int) (srcB + (255-srcB) * TINT_FACTOR);
		return new Color(r, g, b);
	}
	
	public static Color shade(Color color) {
		int r = (int) (color.getRed() * (1-SHADE_FACTOR));
		int g = (int) (color.getGreen() * (1-SHADE_FACTOR));
		int b = (int) (color.getBlue() * (1-SHADE_FACTOR));
		return new Color(r, g, b);
	}
	
	public static Color random() {
		int r = (int)(Math.random() * 256);
		int g = (int)(Math.random() * 256);
		int b = (int)(Math.random() * 256);
		return new Color(r, g, b);
	}
	
}
