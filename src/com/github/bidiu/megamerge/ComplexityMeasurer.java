package com.github.bidiu.megamerge;

import com.github.bidiu.megamerge.common.ComplexityManager;
import com.github.bidiu.megamerge.common.MyLinkResolver;
import com.github.bidiu.megamerge.graphics.MyLinkPainter;
import com.github.bidiu.megamerge.node.MmNode;
import com.github.bidiu.megamerge.util.Logger;
import com.github.bidiu.megamerge.util.RandomNetworkBuilder;

import jbotsim.Node;
import jbotsim.Topology;
import jbotsim.event.ClockListener;
import jbotsim.ui.JViewer;

/**
 * Another type of bootstrap class that measures message complexity in 
 * terms of different settings (how many nodes inside network).
 * 
 * @author sunhe
 * @date Dec 10, 2016
 */
public class ComplexityMeasurer extends Bootstrap {
	
	public static void main(String[] args) {
		startTime = System.currentTimeMillis();
		logger = new Logger(startTime);
		ComplexityMeasurer listener = new ComplexityMeasurer();
		
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
		new RandomNetworkBuilder(200, 200, t).build(t, 10);
		
		JViewer window = new JViewer(t);
		window.getJTopology().setLinkPainter(new MyLinkPainter());
		window.setTitle("He Sun - Mega Merge Algorithm");
	}
	
}
