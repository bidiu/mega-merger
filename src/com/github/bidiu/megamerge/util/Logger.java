package com.github.bidiu.megamerge.util;

import com.github.bidiu.megamerge.node.AbstractNode;

/**
 * thread-safe
 * 
 * @author sunhe
 * @date Nov 20, 2016
 */
public class Logger {
	
	public static final boolean DEBUG_SWITCH = true;
	
	protected long startTime;
	
	public Logger(long startTime) {
		this.startTime = startTime;
	}
	
	protected double getCurTime() {
		return (System.currentTimeMillis() - startTime) / 1000.0;
	}
	
	public synchronized void log(String content) {
		String s = String.format("[%.2f s] %s", getCurTime(), content);
		System.out.println(s);
	}
	
	public synchronized void log(AbstractNode node, String content) {
		String s = String.format("[%.2f s] [%s]: %s", getCurTime(), node, content);
		System.out.println(s);
	}
	
	public synchronized void error(String content) {
		String s = String.format("[%.2f s] %s", getCurTime(), content);
		System.err.println(s);
	}
	
	public synchronized void error(AbstractNode node, String content) {
		String s = String.format("[%.2f s] [%s]: %s", getCurTime(), node, content);
		System.err.println(s);
	}
	
	public synchronized void debug(String content) {
		if (! DEBUG_SWITCH) {
			return;
		}
		
		String s = String.format("[%.2f s] debug: %s", getCurTime(), content);
		System.out.println(s);
	}
	
}
