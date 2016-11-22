package com.github.bidiu.megamerge.msg;

import java.util.List;

import jbotsim.Message;
import jbotsim.Node;

/**
 * 
 * @author sunhe
 * @date Nov 21, 2016
 */
public interface MessageManager {
	
	public List<Message> getToNodeMsgList(Node node);
	
	public List<Message> getFromNodeMsgList(Node node);
	
	public void addMsgToNode(Node node, Message msg);
	
	public void addMsgFromNode(Node node, Message msg);
	
	/**
	 * drop the given message from corresponding message list
	 * 
	 * @param msg
	 * @author sunhe
	 * @date Nov 21, 2016
	 */
	public void receiveMsg(Message msg);	
	
	/**
	 * release resources when link destroyed
	 * 
	 * @author sunhe
	 * @date Nov 21, 2016
	 */
	public void release();
	
}
