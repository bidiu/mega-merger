package com.github.bidiu.megamerge.core;

import java.awt.Color;

/**
 * immutable
 * 
 * @author sunhe
 * @date Nov 20, 2016
 */
public class State {
	
	/** state's name */
	private String name;
	
	/** node's color */
	private Color color;
	
	/** is initial state */
	private boolean initial;
	
	public State(String name, Color color) {
		this(name, color, false);
	}
	
	/**
	 * @param initial
	 * 		is initial state
	 * @author sunhe
	 * @date Nov 20, 2016
	 */
	public State(String name, Color color, boolean initial) {
		this.name = name;
		this.color = color;
		this.initial = initial;
	}
	
	public String getName() {
		return name;
	}

	public Color getColor() {
		return color;
	}
	
	public boolean isInitial() {
		return initial;
	}

	@Override
	public String toString() {
		return name;
	}
	
}
