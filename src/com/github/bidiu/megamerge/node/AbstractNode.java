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
import com.github.bidiu.megamerge.message.Termination;
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
public abstract class AbstractNode extends Node {
	
	private boolean initExecuted = false;
	
	protected String uuid;
	
	private City city;
	
	private boolean cityChanged;
	
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
		cityChanged = true;
	}
	
	public boolean isCityChanged() {
		return cityChanged;
	}
	
	public void mySendAll(Object content) {
		mySendAll(new Message(content));
	}
	
	public void mySendAll(Message msg) {
		for (Node node : getNeighbors()) {
			mySendTo(node, msg);
		}
	}
	
	public void mySendThrough(Link link, Object content) {
		mySendThrough(link, new Message(content));
	}
	
	public void mySendThrough(Link link, Message msg) {
		mySendTo(link.getOtherEndpoint(this), msg);
	}
	
	public void mySendTo(Node node, Object content) {
		mySendTo(node, new Message(content));
	}
	
	public void mySendTo(Node node, Message msg) {
		logger.log(this, "send message to node (ID: " + node + "): " + msg.getContent());
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
		cityChanged = false;
		MessageContent msgContent = (MessageContent) msg.getContent();
		Link link = getCommonLinkWith(msg.getSender());
		Class<?> msgClazz = null;
		
		if (msgContent instanceof AreYouOutside) {
			msgClazz = AreYouOutside.class;
			onAreYouOutside((AreYouOutside) msgContent, link);
		}
		else if (msgContent instanceof External) {
			msgClazz = External.class;
			onExternal((External) msgContent, link);
		}
		else if (msgContent instanceof Internal) {
			msgClazz = Internal.class;
			onInternal((Internal) msgContent, link);
		}
		else if (msgContent instanceof LetUsMerge) {
			msgClazz = LetUsMerge.class;
			onLetUsMerge((LetUsMerge) msgContent, link);
		}
		else if (msgContent instanceof MergeMe) {
			msgClazz = MergeMe.class;
			onMergeMe((MergeMe) msgContent, link);
		}
		else if (msgContent instanceof Notification) {
			msgClazz = Notification.class;
			onNotification((Notification) msgContent, link);
		}
		else if (msgContent instanceof MinLinkWeight) {
			msgClazz = MinLinkWeight.class;
			onMinLinkWeight((MinLinkWeight) msgContent, link);
		}
		else if (msgContent instanceof Termination) {
			msgClazz = Termination.class;
			onTermination((Termination) msgContent, link);
		}
		else {
			throw new IllegalStateException();
		}
		afterProcessingMsg(msgClazz);
	}
	
	public abstract void onAreYouOutside(AreYouOutside msg, Link link);
	
	public abstract void onExternal(External msg, Link link);
	
	public abstract void onInternal(Internal msg, Link link);
	
	public abstract void onLetUsMerge(LetUsMerge msg, Link link);
	
	public abstract void onMergeMe(MergeMe msg, Link link);
	
	public abstract void onNotification(Notification msg, Link link);
	
	public abstract void onMinLinkWeight(MinLinkWeight msg, Link link);
	
	public abstract void onTermination(Termination msg, Link link);
	
	public void afterProcessingMsg(Class<?> msgClazz) {
		// dummy
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
