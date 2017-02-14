package com.github.bidiu.megamerge.util;

public class HashUtils {
	
	/**
	 * Hash text (String) to number (Long), simply by dropping 
	 * all of the non-numeric characters, and parse the result 
	 * to number.
	 * If the result of dropping is empty string, then random number 
	 * (0 ~ 1024) is returned.
	 * 
	 * @author sunhe
	 * @date Nov 22, 2016
	 */
	public static long sToL(String text) {
		String result = text.replaceAll("\\D+", "");
		if (result.length() == 0) {
			return (long)(Math.random() * 1025);
		}
		else {
			return Long.valueOf(result);
		}
	}
	
}
