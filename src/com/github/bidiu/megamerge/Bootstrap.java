package com.github.bidiu.megamerge;

import com.github.bidiu.megamerge.common.ComplexityManager;
import com.github.bidiu.megamerge.common.LinkMessageManager;
import com.github.bidiu.megamerge.common.MessageManager;
import com.github.bidiu.megamerge.common.MyLinkResolver;
import com.github.bidiu.megamerge.graphics.MyLinkPainter;
import com.github.bidiu.megamerge.node.MmNode;
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
 * @author sunhe
 * @date Nov 20, 2016
 */
public class Bootstrap implements SelectionListener, ConnectivityListener {
	
	/** in millisecond */
	public static final int CLOCK_INTERVAL = 700;
	public static final int WINDOW_SIZE_X = 480;
	public static final int WINDOW_SIZE_Y = 300;
	public static final String MSG_MANAGER = "MSG_MANAGER";
	public static final String WEIGHT = "WEIGHT";
	
	public static Logger logger;
	
	private static long startTime;
	private static boolean started = false;
	private static Topology t;
	
	public static void main(String[] args) {
		startTime = System.currentTimeMillis();
		logger = new Logger(startTime);
		printDirections();
		Bootstrap listener = new Bootstrap();
		
		t = new Topology(WINDOW_SIZE_X, WINDOW_SIZE_Y);
		t.setDefaultNodeModel(MmNode.class);
		t.setLinkResolver(new MyLinkResolver());
		t.addSelectionListener(listener);
		t.addConnectivityListener(listener);
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
		t.addClockListener(ComplexityManager.getInstance().init(t));
		t.setClockSpeed(CLOCK_INTERVAL);
		t.pause();
		
		JViewer window = new JViewer(t);
		window.getJTopology().setLinkPainter(new MyLinkPainter());
		window.setTitle("He Sun - Mega Merge Algorithm");
	}
	
	public static void printDirections() {
		logger.log("Directions");
		logger.log("1. Click left mouse button to add nodes on canvas.");
		logger.log("2. When two nodes are close enough to each other, there will a link between them.");
		logger.log("3. You can drag any node to a different position.");
		logger.log("4. Click middle (scroll) button on any node to start the algorithm");
	}

	/*
	 * callback that starts the algorithm 
	 */
	@Override
	public void onSelection(Node node) {
		if (! started) {
			logger.log("algorithm starts");
			started = true;
			ComplexityManager.getInstance().start();
			t.resume();
		}
	}

	@Override
	public void onLinkAdded(Link link) {
		link.setProperty(MSG_MANAGER, new LinkMessageManager(link));
		
		// use string "(UUID, UUID)" as weight of every link
		String weight = "(" + link.endpoint(0).toString().substring(0, 6) + "..., "
				+ link.endpoint(1).toString().substring(0, 6) + "...)";
		link.setProperty(WEIGHT, weight);
	}

	@Override
	public void onLinkRemoved(Link link) {
		((MessageManager) link.getProperty(MSG_MANAGER)).release();
	}
	
}
