package com.github.bidiu.megamerge.graphics;

import static com.github.bidiu.megamerge.Bootstrap.MSG_MANAGER;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.List;

import com.github.bidiu.megamerge.msg.MessageContent;
import com.github.bidiu.megamerge.msg.MessageManager;

import jbotsim.Link;
import jbotsim.Message;
import jbotsim.Node;
import jbotsim.ui.painting.LinkPainter;

/**
 * Thread-safe.
 * 
 * @author sunhe
 * @date Nov 20, 2016
 */
public class MyLinkPainter extends LinkPainter {

	private static final int ARROW_WIDTH = 3;
	private static final int ARROR_LENGTH = 10;
	private static final int NODE_RADIUS = 6;
	private static final int OFFSET = 2;

	@Override
	public void paintLink(Graphics2D g2d, Link link) {
		MessageManager msgManager = (MessageManager) link.getProperty(MSG_MANAGER);
		boolean drawn = false;
		boolean dualDirections = false;
		
		List<Node> endpoints = link.endpoints();
		if (!msgManager.getFromNodeMsgList(endpoints.get(0)).isEmpty() && 
				!msgManager.getFromNodeMsgList(endpoints.get(1)).isEmpty()) {
			dualDirections = true;
		}
		
		for (Node srcNode : endpoints) {
			Node destNode = link.getOtherEndpoint(srcNode);
			List<Message> msgList = msgManager.getFromNodeMsgList(srcNode);
			if (msgList.isEmpty()) {
				continue;
			}
			
			MessageContent msgContent = (MessageContent) msgList.get(0).getContent();
			drawStraightLineArrow(g2d, srcNode.getX(), srcNode.getY(), destNode.getX(), destNode.getY(), 
					msgContent.getColor(), link.getWidth(), dualDirections ? OFFSET : 0);
			drawn = true;
		}
		if (! drawn) {
			super.paintLink(g2d, link);
		}
	}

	/**
	 * draw something like this:
	 * 		src ------> dest
	 * 
	 * @author sunhe
	 * @date Nov 21, 2016
	 */
	private void drawStraightLineArrow(Graphics2D g2d, double srcX, double srcY, 
			double destX, double destY, Color color, int width, int offset) {
		if (width == 0) {
			return;
		}
		
		double deltaX = destX - srcX;
		double deltaY = destY - srcY;
		double angle = Math.atan2(deltaY, deltaX);
		int len = (int) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
		
		AffineTransform at1 = AffineTransform.getTranslateInstance(srcX, srcY);
		at1.concatenate(AffineTransform.getRotateInstance(angle));
		AffineTransform at2 = AffineTransform.getRotateInstance(-angle);
		at2.concatenate(AffineTransform.getTranslateInstance(-srcX, -srcY));
		
		g2d.transform(at1);
		g2d.setColor(color);
		g2d.setStroke(new BasicStroke(width));
		g2d.drawLine(0, offset, len, offset);
		g2d.drawLine(len-NODE_RADIUS, offset, len-NODE_RADIUS-ARROR_LENGTH, ARROW_WIDTH+offset);
		g2d.drawLine(len-NODE_RADIUS, offset, len-NODE_RADIUS-ARROR_LENGTH, -ARROW_WIDTH+offset);
		g2d.transform(at2);
	}
	
}
