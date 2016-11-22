package com.github.bidiu.megamerge.common;

import static com.github.bidiu.megamerge.Bootstrap.WEIGHT;

import java.util.Comparator;
import java.util.List;

import jbotsim.Link;

public class LinkComparator implements Comparator<Link> {

	@SuppressWarnings("unchecked")
	@Override
	public int compare(Link linkA, Link linkB) {
		List<String> weightA = (List<String>) linkA.getProperty(WEIGHT);
		String firstComponentA = weightA.get(0);
		List<String> weightB = (List<String>) linkB.getProperty(WEIGHT);
		String firstComponentB = weightB.get(0);
		
		int result = firstComponentA.compareTo(firstComponentB);
		if (result != 0) {
			return result;
		}
		
		String secondComponentA = weightA.get(1);
		String secondComponentB = weightB.get(1);
		return secondComponentA.compareTo(secondComponentB);
	}

}
