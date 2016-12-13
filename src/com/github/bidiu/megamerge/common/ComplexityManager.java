package com.github.bidiu.megamerge.common;

import static com.github.bidiu.megamerge.Bootstrap.logger;

import com.github.bidiu.megamerge.node.AbstractNode;
import com.github.bidiu.megamerge.node.MmNode;
import com.github.bidiu.megamerge.util.TheoreticalComplexityCalculator;

import jbotsim.Node;
import jbotsim.Topology;
import jbotsim.event.ClockListener;

/**
 * Message complexity manager
 * <p/>
 * Thread-safe.
 * <p/>
 * Singleton.
 * 
 * @author sunhe
 * @date Dec 2, 2016
 */
public class ComplexityManager implements ClockListener {
	
	/** singleton */
	private static ComplexityManager instance;
	
	public synchronized static ComplexityManager getInstance() {
		if (instance == null) {
			instance = new ComplexityManager();
		}
		return instance;
	}
	
	private int clockCnt;
	private int msgCnt;
	
	private Topology t;

	private boolean started;
	private boolean done;
	
	private ComplexityManager() {}
	
	public synchronized ComplexityManager init(Topology t) {
		if (this.t != null) throw new IllegalStateException();
		this.t = t;
		return this;
	}
	
	/**
	 * is algorithm terminated
	 */
	private boolean isTerminated() {
		for (Node node : t.getNodes()) {
			if (node instanceof AbstractNode) {
				City city = ((MmNode) node).getCity();
				if (!city.equals(City.ELECTED) && !city.equals(City.NON_ELECTED)) {
					return false;
				}
			}
		}
		return true;
	}
	
	public synchronized int getClockCnt() {
		return clockCnt;
	}
	
	public synchronized int getMsgCnt() {
		return msgCnt;
	}

	public synchronized boolean isStarted() {
		return started;
	}
	
	public synchronized boolean isDone() {
		return done;
	}

	public synchronized void start() {
		if (started || isTerminated()) throw new IllegalStateException();
		started = true;
	}
	
	public synchronized void messageSent() {
		if (!started || isTerminated()) throw new IllegalStateException();
		msgCnt++;
	}
	
	@Override
	public synchronized void onClock() {
		if (done) return;
		
		done = isTerminated();
		if (!started && done) throw new IllegalStateException();
		
		if (done) {
			int n = t.getNodes().size() - 1;
			int m = t.getLinks().size();
			int useless = 2 * (m - (n - 1));
			int theoretical = TheoreticalComplexityCalculator.calculate(n, m);
			
			logger.error("Nodes: " + n);
			logger.error("Links: " + m);
			logger.error("Average Degree: " + getAverageDegree());
			logger.error("Time Complexity: " + clockCnt);
			System.err.println("======================================================================");
			logger.error(String.format("Real Message Complexity: %d (useless: %d, useful: %d)", 
					msgCnt, useless, msgCnt - useless));
			logger.error(String.format("Theoretical Message Complexity: %d (useless: %d, useful: %d)", 
					theoretical, useless, theoretical - useless));
		}
		else if (started) {
			clockCnt++;
		}
	}
	
	private int getAverageDegree() {
		int totalDegree = 0;
		
		for (Node node : t.getNodes()) {
			if (node instanceof AbstractNode) {
				totalDegree += node.getLinks().size();
			}
		}
		return (int) Math.round((double) totalDegree / (t.getNodes().size()-1));
	}
	
}
