package com.github.bidiu.megamerge.node;

import static com.github.bidiu.megamerge.Bootstrap.WEIGHT;
import static com.github.bidiu.megamerge.message.MinLinkWeight.INF_WEIGHT;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.github.bidiu.megamerge.common.City;
import com.github.bidiu.megamerge.message.AreYouOutside;
import com.github.bidiu.megamerge.message.External;
import com.github.bidiu.megamerge.message.LetUsMerge;
import com.github.bidiu.megamerge.message.MergeMe;
import com.github.bidiu.megamerge.message.Notification;
import com.github.bidiu.megamerge.util.ColorUtils;
import com.github.bidiu.megamerge.util.HashUtils;

import jbotsim.Link;

/**
 * @author sunhe
 * @date Nov 25, 2016
 */
public abstract class MmHelperNode extends AbstractNode {
	
	public static interface Yield<T> {
		/**
		 * @param blockedMsg		yielded message
		 * @param link				the link from which the message was received
		 * @return					whether yield should go on
		 */
		public boolean yieldMsg(T blockedMsg, Link link, Iterator<T> it);
	}
	
	private static final int TREE_PATH_WIDTH = 5;
	private static final String LINK = "LINK";
	
	
	private MmNodeV2 parent;
	private List<MmNodeV2> children = new LinkedList<>();
	
	private List<Link> internalLinks = new LinkedList<>();
	private Link linkTryingToMerge;
	
	private Link linkWithMinWeight;
	private String minWeight;
	
	private int weightCounter;
	private boolean externalRecevied;
	
	private List<AreYouOutside> blockedAreYouOutside = new LinkedList<>();
	private List<LetUsMerge> blockedLetUsMerge = new LinkedList<>();
	
	//////////////////////////////
	//////////////////////////////
	
	private List<Link> getExternalLinks() {
		List<Link> externalLinks = getLinks();
		externalLinks.removeAll(internalLinks);
		return externalLinks;
	}
	
	private void resetMeta() {
		weightCounter = 0;
		linkWithMinWeight = null;
		linkTryingToMerge = null;
		externalRecevied = false;
		minWeight = null;
	}
	
	protected MmNodeV2 getOppositeNode(Link link) {
		return (MmNodeV2) link.getOtherEndpoint(this);
	}
	
	//////////////////////////////
	//////////////////////////////
	
	/**
	 * @param whenNoExternalLink	when no external link
	 * @param afterAsking			after sending "Are you outside?" (note only after successfully asking)
	 */
	protected void fireAreYouOutside(Runnable whenNoExternalLink, Runnable afterAsking) {
		List<Link> externalLinks = getExternalLinks();
		if (externalLinks.isEmpty()) {
			// no external link
			// execute callback
			if (whenNoExternalLink != null) whenNoExternalLink.run();
		}
		else {
			// has external link
			Link nextLink = Collections.min(externalLinks, new Comparator<Link>() {

				@Override
				public int compare(Link link1, Link link2) {
					String weight1 = (String) link1.getProperty(WEIGHT);
					String weight2 = (String) link2.getProperty(WEIGHT);
					return weight1.compareTo(weight2);
				}
			});
			mySendThrough(nextLink, new AreYouOutside(new City(getCity())));
			// execute callback
			if (afterAsking != null) afterAsking.run();
		}
	}
	
	protected void addToInternalLinks(Link link) {
		addToInternalLinks(link, false);
	}
	
	protected void addToInternalLinks(Link link, boolean exceptionOnExist) {
		if (internalLinks.contains(link)) {
			if (exceptionOnExist) throw new IllegalStateException();
		}
		else {
			internalLinks.add(link);
		}
	}
	
	/**
	 * @param link		the link from which the message is received
	 */
	protected void blockAreYouOutside(AreYouOutside msg, Link link) {
		msg.setProperty(LINK, link);
		blockedAreYouOutside.add(msg);
	}
	
	protected void unblockAreYouOutside(Yield<AreYouOutside> yield) {
		Iterator<AreYouOutside> it = blockedAreYouOutside.iterator();
		while (it.hasNext()) {
			AreYouOutside blockedMsg = it.next();
			if (! yield.yieldMsg(blockedMsg, (Link) blockedMsg.getProperty(LINK), it)) break;
		}
	}
	
	/**
	 * @param link		the link from which the message is received
	 */
	protected void blockLetUsMerge(LetUsMerge msg, Link link) {
		msg.setProperty(LINK, link);
		blockedLetUsMerge.add(msg);
	}
	
	protected void unblockLetUsMerge(Yield<LetUsMerge> yield) {
		Iterator<LetUsMerge> it = blockedLetUsMerge.iterator();
		while (it.hasNext()) {
			LetUsMerge blockedMsg = it.next();
			if (! yield.yieldMsg(blockedMsg, (Link) blockedMsg.getProperty(LINK), it)) break;
		}
	}
	
	/**
	 * @param whenOnFringe		when current node is on fringe
	 */
	protected void fireLetUsMerge(LetUsMerge msg, Runnable whenOnFringe) {
		mySendThrough(linkWithMinWeight, msg);
		if (! internalLinks.contains(linkWithMinWeight)) {
			// current node is on fringe
			linkTryingToMerge = linkWithMinWeight;
			whenOnFringe.run();
		}
	}
	
	protected boolean isFriendlyMerge(LetUsMerge receivedMsg, Link link) {
		City senderCity = receivedMsg.getFromCity();
		int senderLevel = senderCity.getLevel();
		int myLevel = getCity().getLevel();
		
		return myLevel == senderLevel && linkTryingToMerge != null && link.equals(linkTryingToMerge);
	}
	
	protected void whenFriendlyMerge(LetUsMerge receivedMsg, Link link) {
		link.setWidth(TREE_PATH_WIDTH);
		MmNodeV2 oppositeNode = getOppositeNode(link);
		int myLevel = getCity().getLevel();
		String weight = (String) link.getProperty(WEIGHT);
		City newCity = new City(weight, myLevel+1, ColorUtils.random(HashUtils.sToL(weight)));
		String senderUuid = receivedMsg.getUuid();
		
		if (uuid.compareTo(senderUuid) < 0) {
			// I am new downtown
			newCity.setDowntown(true);
			if (parent != null) {
				children.add(parent);
				parent = null;
			}
			children.add(oppositeNode);
		}
		else {
			// I am NOT new downtown
			newCity.setDowntown(false);
			if (parent != null) {
				children.add(parent);
			}
			parent = oppositeNode;
		}
		setCity(newCity);
		
		// notify children new city (level + 1)
		for (Link internalLink : internalLinks) {
			mySendThrough(internalLink, new Notification(new City(newCity), true));
		}
		
		// add to internal links after notification
		addToInternalLinks(link, true);
		resetMeta();
		
		// unblock "Let's merge" -- MUST BE MERGE ME
		unblockLetUsMerge(new Yield<LetUsMerge>() {

			@Override
			public boolean yieldMsg(LetUsMerge blockedMsg, Link link, Iterator<LetUsMerge> it) {
				if (myLevel > blockedMsg.getFromCity().getLevel()) {
					// merge me
					it.remove();
					mySendThrough(link, new MergeMe(newCity, true));
					children.add(getOppositeNode(link));
					addToInternalLinks(link, true);
				}
				return true;
			}
		});
		
		// unblock "Are you outside?"
		unblockAreYouOutside(new Yield<AreYouOutside>() {

			@Override
			public boolean yieldMsg(AreYouOutside blockedMsg, Link link, Iterator<AreYouOutside> it) {
				if (myLevel >= blockedMsg.getFromCity().getLevel()) {
					it.remove();
					mySendThrough(link, new External());
				}
				return true;
			}
		});
		
		// at this point, if I am root, I must have at least one child
		fireAreYouOutside(new Runnable() {
			
			@Override
			public void run() {
				if (parent != null && children.isEmpty()) {
					mySendTo(parent, INF_WEIGHT);
				}
			}
		}, null);
	}
	
}
