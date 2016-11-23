package com.github.bidiu.megamerge.node;

import static com.github.bidiu.megamerge.Bootstrap.MSG_MANAGER;
import static com.github.bidiu.megamerge.Bootstrap.logger;

import java.util.UUID;

import com.github.bidiu.megamerge.common.City;
import com.github.bidiu.megamerge.common.MessageManager;
import com.github.bidiu.megamerge.message.AreYouOutside;
import com.github.bidiu.megamerge.message.External;
import com.github.bidiu.megamerge.message.Internal;
import com.github.bidiu.megamerge.message.LetUsMerge;
import com.github.bidiu.megamerge.message.MergeMe;
import com.github.bidiu.megamerge.message.MessageContent;
import com.github.bidiu.megamerge.message.Notification;
import com.github.bidiu.megamerge.message.MinLinkWeight;
import com.github.bidiu.megamerge.util.ColorUtils;

import jbotsim.Link;
import jbotsim.Message;
import jbotsim.Node;

/**
 * 
 * @author sunhe
 * @date Nov 20, 2016
 */
public abstract class AbstractMegaMergeNode extends Node {
	
	private boolean initExecuted = false;
	
	protected String uuid;
	
	private City city;
	
	public AbstractMegaMergeNode() {
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
		logger.log(this, "send message to node (ID: " + msg.getSender() + "): " + msg.getContent());
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
	 * 
	 * @author sunhe
	 * @date Nov 22, 2016
	 */
	public abstract void spontaneouslyDo();
	
	@Override
	public void onMessage(Message msg) {
		myReceiveMsg(msg);
		MessageContent msgContent = (MessageContent) msg.getContent();
		Link link = getCommonLinkWith(msg.getSender());
		logger.log(this, "receive message from node (ID: " + msg.getSender() + "): " + msgContent);
		
		if (msgContent instanceof AreYouOutside) {
			onAreYouOutside((AreYouOutside) msgContent, link);
		}
		else if (msgContent instanceof External) {
			onExternal((External) msgContent, link);
		}
		else if (msgContent instanceof Internal) {
			onInternal((Internal) msgContent, link);
		}
		else if (msgContent instanceof LetUsMerge) {
			onLetUsMerge((LetUsMerge) msgContent, link);
		}
		else if (msgContent instanceof MergeMe) {
			onMergeMe((MergeMe) msgContent, link);
		}
		else if (msgContent instanceof Notification) {
			onNotification((Notification) msgContent, link);
		}
		else if (msgContent instanceof MinLinkWeight) {
			onMinLinkWeight((MinLinkWeight) msgContent, link);
		}
		else {
			throw new IllegalStateException();
		}
	}
	
	public abstract void onAreYouOutside(AreYouOutside msg, Link link);
	
	public abstract void onExternal(External msg, Link link);
	
	public abstract void onInternal(Internal msg, Link link);
	
	public abstract void onLetUsMerge(LetUsMerge msg, Link link);
	
	public abstract void onMergeMe(MergeMe msg, Link link);
	
	public abstract void onNotification(Notification msg, Link link);
	
	public abstract void onMinLinkWeight(MinLinkWeight msg, Link link);
	
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
		if (!(obj instanceof AbstractMegaMergeNode)) {
			return false;
		}
		AbstractMegaMergeNode other = (AbstractMegaMergeNode) obj;
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
