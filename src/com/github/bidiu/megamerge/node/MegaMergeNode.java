package com.github.bidiu.megamerge.node;

import static com.github.bidiu.megamerge.Bootstrap.logger;

import jbotsim.Message;

/**
 * 
 * @author sunhe
 * @date Nov 22, 2016
 */
public class MegaMergeNode extends AbstractNode {
	
	private boolean initExecuted = false;
	
	/*
	 * Just execute the initial procedure once, i.e., 
	 * spontaneous procedure at the beginning. 
	 * After that, switch to message-driven mode (onMessage method).
	 */
	@Override
	public void onClock() {
		if (initExecuted) {
			super.onClock();
		}
		else {
			spontaneouslyDo();
			initExecuted = true;
		}
	}
	
	/**
	 * initial procedure at the beginning
	 */
	private void spontaneouslyDo() {
		// TODO
	}
	
	
	@Override
	public void onMessage(Message arg0) {
		// TODO 
	}
	
	
	
}
