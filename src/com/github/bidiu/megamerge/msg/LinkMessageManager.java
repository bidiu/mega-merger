package com.github.bidiu.megamerge.msg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jbotsim.Link;
import jbotsim.Message;
import jbotsim.Node;

public class LinkMessageManager implements MessageManager {

	private Link link;
	
	private Map<Node, List<Message>> toNodeMsgs;
	
	public LinkMessageManager(Link link) {
		this.link = link;
		toNodeMsgs = new HashMap<>();
		List<Node> endpoints = link.endpoints();
		toNodeMsgs.put(endpoints.get(0), new ArrayList<>());
		toNodeMsgs.put(endpoints.get(1), new ArrayList<>());
	}
	
	@Override
	public List<Message> getToNodeMsgList(Node node) {
		return toNodeMsgs.get(node);
	}

	@Override
	public List<Message> getFromNodeMsgList(Node node) {
		return getToNodeMsgList(link.getOtherEndpoint(node));
	}
	
	@Override
	public void addMsgToNode(Node node, Message msg) {
		List<Message> msgList = toNodeMsgs.get(node);
		msgList.add(msg);
	}

	@Override
	public void addMsgFromNode(Node node, Message msg) {
		addMsgToNode(link.getOtherEndpoint(node), msg);
	}

	@Override
	public void receiveMsg(Message msg) {
		MessageContent msgContent = (MessageContent) msg.getContent();
		for (List<Message> msgList : toNodeMsgs.values()) {
			for (Message msgFromList : msgList) {
				MessageContent msgContentFromList = (MessageContent) msg.getContent();
				if (msgContent == msgContentFromList) {
					// two messages are the same message
					msgList.remove(msgFromList);
					return;
				}
			}
		}
		throw new IllegalStateException();
	}

	@Override
	public void release() {
		// dummy
	}

}
