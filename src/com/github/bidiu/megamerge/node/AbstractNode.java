package com.github.bidiu.megamerge.node;

import static com.github.bidiu.megamerge.Bootstrap.MSG_MANAGER;

import java.util.UUID;

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
public abstract class AbstractNode extends Node {
	
	protected String uuid;
	
	private City city;
	
	public AbstractNode() {
		uuid = UUID.randomUUID().toString();
		setCity(new City(uuid.substring(0, 6)+"...", 1, ColorUtils.random(), true));
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
		if (!(obj instanceof AbstractNode)) {
			return false;
		}
		AbstractNode other = (AbstractNode) obj;
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
