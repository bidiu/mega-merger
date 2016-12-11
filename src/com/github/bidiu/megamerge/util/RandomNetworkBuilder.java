package com.github.bidiu.megamerge.util;

import com.github.bidiu.megamerge.node.MmNode;

import jbotsim.Topology;

/**
 * Random network builder
 * <p/>
 * Thread safe
 * 
 * @author sunhe
 * @date Dec 10, 2016
 */
public class RandomNetworkBuilder {
	
	/**
	 * thread-safe
	 */
	protected static class Centralization {
		
		public static Centralization dummyCent = new Centralization(0, 0, 0, 0);
		public static Centralization getDummyCent() {
			return dummyCent;
		}
		
		private int deltaX;
		private int deltaY;
		
		public Centralization(int xSize, int ySize, Topology t) {
			this(xSize, ySize, t.getWidth(), t.getHeight());
		}
		public Centralization(int xSize, int ySize, int xWindowSize, int yWindowSize) {
			if (xSize > xWindowSize || ySize > yWindowSize) throw new IllegalStateException("cannot centralize");
			
			deltaX = (xWindowSize - xSize) / 2;
			deltaY = (yWindowSize - ySize) / 2;
		}
		
		public synchronized int transformX(int x) {
			return x + deltaX;
		}
		public synchronized int transformY(int y) {
			return y + deltaY;
		}
	}
	
	
	protected int xLowerThreshold;
	protected int xUpperThreshold;
	protected int yLowerThreshold;
	protected int yUpperThreshold;
	
	/**
	 * By default, designated area is located at upper-left location, 
	 * when smaller than the window size.
	 */
	public RandomNetworkBuilder(int xSize, int ySize) {
		this(xSize, ySize, null);
	}
	
	/**
	 * Coordinates will be centralized.
	 * xSize and ySize MUST be bigger than the real window size.
	 */
	public RandomNetworkBuilder(int xSize, int ySize, Topology t) {
		Centralization cent = (
				t == null ? 
				Centralization.getDummyCent() :
				new Centralization(xSize, ySize, t)
		);
		
		xLowerThreshold = cent.transformX(0);
		xUpperThreshold = cent.transformX(xSize);
		yLowerThreshold = cent.transformY(0);
		yUpperThreshold = cent.transformY(ySize);
	}
	
	/*
	 * getters
	 */
	public synchronized int getxLowerThreshold() {
		return xLowerThreshold;
	}
	public synchronized int getxUpperThreshold() {
		return xUpperThreshold;
	}
	public synchronized int getyLowerThreshold() {
		return yLowerThreshold;
	}
	public synchronized int getyUpperThreshold() {
		return yUpperThreshold;
	}


	/**
	 * @param lowerThreshold
	 * 		inclusive
	 * @param upperThreshold
	 * 		inclusive
	 */
	protected int getRandIndex(int lowerThreshold, int upperThreshold) {
		return (int) (Math.random() * (upperThreshold - lowerThreshold + 1) + lowerThreshold);
	}
	
	public synchronized MmNode getMmNode() {
		MmNode node = new MmNode();
		node.setLocation(getRandIndex(xLowerThreshold, xUpperThreshold), getRandIndex(yLowerThreshold, yUpperThreshold));
		return node;
	}
	
	public synchronized Topology build(Topology t, int n) {
		for (int i = 0; i < n; i++) {
			t.addNode(getMmNode());
		}
		return t;
	}
	
}
