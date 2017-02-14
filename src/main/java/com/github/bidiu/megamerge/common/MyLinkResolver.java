package com.github.bidiu.megamerge.common;

import jbotsim.LinkResolver;
import jbotsim.Node;

public class MyLinkResolver extends LinkResolver {
	
	public static final int DEFAULT_THRESHOLD = 120;

	protected int threshold;
	
	public MyLinkResolver() {
		this(DEFAULT_THRESHOLD);
	}
	
	public MyLinkResolver(int threshold) {
		this.threshold = threshold;
	}
	
	private int getDistance(double x1, double y1, double x2, double y2) {
		double deltaX = Math.abs(x1 - x2);
		double deltaY = Math.abs(y1 - y2);
		return (int) Math.round(Math.sqrt(deltaX * deltaX + deltaY * deltaY));
	}
	
	@Override
	public boolean isHeardBy(Node node1, Node node2) {
		return getDistance(node1.getX(), node1.getY(), node2.getX(), node2.getY()) <= threshold;
	}
	
}
