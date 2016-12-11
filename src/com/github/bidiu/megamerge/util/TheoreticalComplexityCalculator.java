package com.github.bidiu.megamerge.util;

public class TheoreticalComplexityCalculator {

	protected static double log2(double x) {
	     return Math.log(x) / Math.log(2.0d);
	}
	
	public static int calculate(int n, int m) {
		return (int) Math.round(2*m + 5*n*log2(n) - (n-1));
	}
	
	public static void main(String[] args) {
		System.out.println(calculate(100, 1055));
	}
	
}
