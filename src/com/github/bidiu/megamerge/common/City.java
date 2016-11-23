package com.github.bidiu.megamerge.common;

import java.awt.Color;

/**
 * not thread-safe
 * 
 * @author sunhe
 * @date Nov 22, 2016
 */
public class City {
	
	private String name;
	
	private int level;
	
	private Color color;
	
	private boolean downtown;
	
	public City(String name, int level, Color color) {
		this(name, level, color, false);
	}
	
	public City(String name, int level, Color color, boolean downtown) {
		this.name = name;
		this.level = level;
		this.color = color;
		this.downtown = downtown;
	}
	
	/**
	 * Clone the given other city, except for field "downtown" being false.
	 * 
	 * @param otherCity
	 * 		city to be cloned
	 * @author sunhe
	 * @date Nov 22, 2016
	 */
	public City(City otherCity) {
		this(otherCity.getName(), otherCity.getLevel(), otherCity.getColor(), false);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}
	
	public boolean isDowntown() {
		return downtown;
	}

	public void setDowntown(boolean downtown) {
		this.downtown = downtown;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof City)) {
			return false;
		}
		City other = (City) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		}
		else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "{name: " + name + ", level: " + level + "}";
	}
	
}
