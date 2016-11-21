package com.github.bidiu.flood.node;

import static com.github.bidiu.megamerge.MegaMergeDemo.logger;

import java.awt.Color;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.bidiu.megamerge.core.State;
import com.github.bidiu.megamerge.node.StatefulNode;

import jbotsim.Message;
import jbotsim.Node;

/**
 * 
 * @author sunhe
 * @date Nov 20, 2016
 */
public class FloodNode extends StatefulNode {
	
	public static final Map<String, State> ALL_STATES;
	
	public static final String STATE_INITIATOR = "initiator";
	public static final String STATE_SLEEPING = "sleeping";
	public static final String STATE_DONE = "done";
	
	static {
		Map<String, State> allStates = new HashMap<>();
		allStates.put("initiator", new State(STATE_INITIATOR, Color.YELLOW));
		allStates.put("sleeping", new State(STATE_SLEEPING, Color.GREEN, true));
		allStates.put("done", new State(STATE_DONE, Color.BLACK));
		ALL_STATES = Collections.unmodifiableMap(allStates);
	}
	
	public FloodNode() {
		super(ALL_STATES);
	}

	@Override
	public void onSelection() {
		setCurState(allStates.get(STATE_INITIATOR));
		mySendAll(new Message());
		logger.log(this, "became initiator, broadcasted to all neighbors");
		setCurState(allStates.get(STATE_DONE));
		logger.log(this, "became done");
	}
	
	@Override
	public void onMessage(Message msg) {
		myReceiveMsg(msg);
		if (getCurState().getName().equals(STATE_DONE)) {
			return;
		}
		
		Node sender = msg.getSender();
		List<Node> neighbors = getNeighbors();
		for (Node neighbor : neighbors) {
			if (! neighbor.equals(sender)) {
				mySendTo(neighbor, msg);
			}
		}
		logger.log(this, "received message, broadcasted to all neighbors but sender(" + sender + ")");
		setCurState(allStates.get(STATE_DONE));
		logger.log(this, "became done");
	}
	
}
