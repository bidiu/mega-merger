package com.github.bidiu.flood.node;

import static com.github.bidiu.megamerge.Bootstrap.MSG_MANAGER;

import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import com.github.bidiu.flood.common.State;
import com.github.bidiu.flood.common.Stateful;
import com.github.bidiu.megamerge.common.City;
import com.github.bidiu.megamerge.common.MessageManager;
import com.github.bidiu.megamerge.util.ColorUtils;

import jbotsim.Link;
import jbotsim.Message;
import jbotsim.Node;

/**
 * 
 * @author sunhe
 * @date Nov 20, 2016
 */
public abstract class StatefulNode extends Node implements Stateful {
	
	protected Map<String, State> allStates;
	
	protected String uuid = UUID.randomUUID().toString();
	
	protected City city;
	
	public StatefulNode(Map<String, State> allStates) {
		this.allStates = allStates;
		for (Entry<String, State> entry : allStates.entrySet()) {
			if (entry.getValue().isInitial()) {
				setCurState(entry.getValue());
				break;
			}
		}
		if (getState() == null) {
			throw new IllegalStateException("node has no initial state assigned");
		}
	}
	
	@Override
	public Map<String, State> getAllStates() {
		return allStates;
	}
	
	@Override
	public State getCurState() {
		return (State) getState();
	}

	@Override
	public void setCurState(State state) {
		setState(state);
		setColor(state.getColor());
	}

	public String getUuid() {
		return uuid;
	}
	
	public City getCity() {
		return city;
	}
	
	public void setCity(City city) {
		this.city = city;
		if (city.isDowntown()) {
			setColor(ColorUtils.shade(city.getColor()));
		}
		else {
			setColor(ColorUtils.tint(city.getColor()));
		}
	}
	
	public void mySendAll(Object content) {
		mySendAll(new Message(content));
	}
	
	public void mySendAll(Message msg) {
		for (Node node : getNeighbors()) {
			mySendTo(node, msg);
		}
	}
	
	public void mySendTo(Node node, Object content) {
		mySendTo(node, new Message(content));
	}
	
	public void mySendTo(Node node, Message msg) {
		Link link = getCommonLinkWith(node);
		((MessageManager) link.getProperty(MSG_MANAGER)).addMsgToNode(node, msg);
		send(node, msg);
	}
	
	public Message myReceiveMsg(Message msg) {
		Node sender = msg.getSender();
		Link link = getCommonLinkWith(sender);
		((MessageManager) link.getProperty(MSG_MANAGER)).receiveMsg(msg);
		return msg;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof StatefulNode)) {
			return false;
		}
		StatefulNode other = (StatefulNode) obj;
		if (uuid == null) {
			if (other.uuid != null) {
				return false;
			}
		}
		else if (!uuid.equals(other.uuid)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return uuid.substring(0, 6) + "...";
	}
	
}
