package com.github.bidiu.megamerge;

import java.util.ArrayList;
import java.util.List;

import com.github.bidiu.flood.node.FloodNode;
import com.github.bidiu.megamerge.common.LinkMessageManager;
import com.github.bidiu.megamerge.common.MessageManager;
import com.github.bidiu.megamerge.graphics.MyLinkPainter;
import com.github.bidiu.megamerge.node.AbstractNode;
import com.github.bidiu.megamerge.util.Logger;

import jbotsim.Link;
import jbotsim.Node;
import jbotsim.Topology;
import jbotsim.event.ClockListener;
import jbotsim.event.ConnectivityListener;
import jbotsim.event.SelectionListener;
import jbotsim.ui.JViewer;

/**
 * algorithm bootstrap class
 * 
 * TODO ID utility
 * TODO drop Stateful from AbstractNode
 * TODO use HSL color system
 * TODO restart support
 * 
 * Constraints:
 * 1. Every time a message sent, it has to be copied
 * 2. Message.equals MUST compare address
 * 3. When call JBotsim's send API, wrapper Message class is copied when receiving.
 * 
 * @author sunhe
 * @date Nov 20, 2016
 */
public class Bootstrap implements SelectionListener, ConnectivityListener {
	
	/** in millisecond */
	public static final int CLOCK_INTERVAL = 3000;
	public static final int WINDOW_SIZE_X = 800;
	public static final int WINDOW_SIZE_Y = 600;
	public static final String MSG_MANAGER = "MSG_MANAGER";
	public static final String WEIGHT = "WEIGHT";
	
	public static Logger logger;
	
	private static long startTime;
	private static boolean started = false;
	private static Topology t;
	
	public static void main(String[] args) {
		startTime = System.currentTimeMillis();
		logger = new Logger(startTime);
		Bootstrap demo = new Bootstrap();
		
		t = new Topology(WINDOW_SIZE_X, WINDOW_SIZE_Y);
		t.setDefaultNodeModel(FloodNode.class);
		t.addSelectionListener(demo);
		t.addConnectivityListener(demo);
		// force redraw call every clock by moving an invisible node
		t.addClockListener(new ClockListener() {
			
			private Node dummyNode;
			
			@Override
			public void onClock() {
				if (dummyNode == null) {
					dummyNode = new Node();
					dummyNode.setDirection(-0.75 * Math.PI);
					t.addNode(-100.0, 100.0, dummyNode);
				}
				dummyNode.move();
			}
		});
		t.setClockSpeed(CLOCK_INTERVAL);
		t.pause();
		
		JViewer window = new JViewer(t);
		window.getJTopology().setLinkPainter(new MyLinkPainter());
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

	@Override
	public void onLinkAdded(Link link) {
		link.setProperty(MSG_MANAGER, new LinkMessageManager(link));
		List<String> weight = new ArrayList<>();
		weight.add(((AbstractNode) link.endpoint(0)).getUuid());
		weight.add(((AbstractNode) link.endpoint(1)).getUuid());
		link.setProperty(WEIGHT, weight);
	}

	@Override
	public void onLinkRemoved(Link link) {
		((MessageManager) link.getProperty(MSG_MANAGER)).release();
	}
	
}
