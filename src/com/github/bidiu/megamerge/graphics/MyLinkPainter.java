package com.github.bidiu.megamerge.graphics;

import java.awt.Graphics2D;

import jbotsim.Link;
import jbotsim.ui.painting.LinkPainter;

/**
 * Use link's color to represent different kinds of messages. 
 * While there are multiple messages on same link at the same time, 
 * multiple links parallel to each other should be drawn, which is what 
 * this class is doing.
 * 
 * Thread-safe.
 * 
 * @author sunhe
 * @date Nov 20, 2016
 */
public class MyLinkPainter extends LinkPainter {

	@Override
	public void paintLink(Graphics2D arg0, Link arg1) {
		// TODO Auto-generated method stub
		super.paintLink(arg0, arg1);
	}
	
}
