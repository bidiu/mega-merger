package com.github.bidiu.megamerge;

import com.github.bidiu.flood.node.FloodNode;
import com.github.bidiu.megamerge.util.Logger;

import jbotsim.Node;
import jbotsim.Topology;
import jbotsim.event.SelectionListener;
import jbotsim.ui.JViewer;

/**
 * algorithm bootstrap class
 * 
 * TODO link color
 * TODO adapt StatefulNode
 * TODO how to display uuid
 * TODO restart support
 * 
 * @author sunhe
 * @date Nov 20, 2016
 */
public class MegaMergeDemo implements SelectionListener {
	
	/** in millisecond */
	public static final int CLOCK_INTERVAL = 2000;
	public static final int WINDOW_SIZE_X = 400;
	public static final int WINDOW_SIZE_Y = 300;
	
	public static Logger logger;
	
	private static long startTime;
	private static boolean started = false;
	private static Topology t;
	
	public static void main(String[] args) {
		startTime = System.currentTimeMillis();
		logger = new Logger(startTime);
		MegaMergeDemo demo = new MegaMergeDemo();
		
		t = new Topology(WINDOW_SIZE_X, WINDOW_SIZE_Y);
		t.setDefaultNodeModel(FloodNode.class);
		t.addSelectionListener(demo);
		t.setClockSpeed(CLOCK_INTERVAL);
		t.pause();
		
		JViewer window = new JViewer(t);
		window.setTitle("He Sun - Mega Merge Algorithm");
	}

	/*
	 * callback that starts the algorithm 
	 */
	@Override
	public void onSelection(Node node) {
		if (! started) {
			logger.log("algorithm starts");
			started = true;
			t.resume();
		}
	}
	
}
