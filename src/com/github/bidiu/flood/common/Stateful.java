package com.github.bidiu.flood.common;

import java.util.Map;

/**
 * 
 * @author sunhe
 * @date Nov 20, 2016
 */
public interface Stateful {
	
	/**
	 * get all possible states
	 * 
	 * @return
	 * @author sunhe
	 * @date Nov 20, 2016
	 */
	public Map<String, State> getAllStates();
	
	/**
	 * get current state
	 * 
	 * @return
	 * @author sunhe
	 * @date Nov 20, 2016
	 */
	public State getCurState();
	
	/**
	 * set current state
	 * 
	 * @param state
	 * @return
	 * @author sunhe
	 * @date Nov 20, 2016
	 */
	public void setCurState(State state);
	
}
