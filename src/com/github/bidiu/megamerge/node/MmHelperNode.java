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
import com.github.bidiu.megamerge.message.Internal;
import com.github.bidiu.megamerge.message.LetUsMerge;
import com.github.bidiu.megamerge.message.MergeMe;
import com.github.bidiu.megamerge.message.MinLinkWeight;
import com.github.bidiu.megamerge.message.Notification;
import com.github.bidiu.megamerge.message.Termination;
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
	
	
	protected MmNodeV2 parent;
	protected List<MmNodeV2> children = new LinkedList<>();
	
	private List<Link> internalLinks = new LinkedList<>();
	private Link linkTryingToMerge;
	
	protected Link linkWithMinWeight;
	protected String minWeight;
	
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
	
	protected void resetMeta() {
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
	
	protected boolean isTermination() {
		if (parent != null) new IllegalStateException("non-root node cannot invoke this method");
		
		if (isDoneAsking() && (minWeight == null || INF_WEIGHT.equals(minWeight))) {
			return true;
		}
		else {
			return false;
		}
	}
	
	protected void whenTermination() {
		setCity(parent == null ? City.ELECTED : City.NON_ELECTED);
		for (MmNodeV2 child : children) {
			mySendTo(child, new Termination());
		}
	}
	
	/**
	 * is done asking "Are you outside?"
	 */
	protected boolean isDoneAsking() {
		if ((externalRecevied || getExternalLinks().isEmpty()) 
				&& weightCounter == children.size()) {
			return true;
		}
		return false;
	}
	@Override
	public void onExternal(External msg, Link link) {
		externalRecevied = true;
	}
	
	/**
	 * if necessary
	 */
	protected boolean updateLinkWithMinWeight(Link link, String weight) {
		if (linkWithMinWeight == null || weight.compareTo(minWeight) < 0) {
			linkWithMinWeight = link;
			minWeight = weight;
			return true;
		}
		return false;
	}
	
	/**
	 * @param whenNoExternalLink	when no external link
	 * @param afterAsking			after sending "Are you outside?" (note only after successfully asking)
	 */
	protected void fireNextAreYouOutside(Runnable whenNoExternalLink, Runnable afterAsking) {
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
	
	protected void whenMergeMe(MergeMe receivedMsg, Link link) {
		link.setWidth(TREE_PATH_WIDTH);
		City newCity = receivedMsg.getMergeTo();
		setCity(newCity);
		addToInternalLinks(link);
		if (parent != null) {
			children.add(parent);
		}
		parent = getOppositeNode(link);
		resetMeta();
		
		// notification of merging into another city
		for (MmNodeV2 child : children) {
			mySendTo(child, new Notification(new City(newCity), receivedMsg.isToAskMinWeight()));
		}
		
		// unblock "Let's merge" -- MUST be merge me
		unblockLetUsMerge(new Yield<LetUsMerge>() {

			@Override
			public boolean yieldMsg(LetUsMerge blockedMsg, Link link, Iterator<LetUsMerge> it) {
				if (newCity.getLevel() > blockedMsg.getFromCity().getLevel()) {
					it.remove();
					// Note, isToAskMinWeight here
					mySendThrough(link, new MergeMe(newCity, receivedMsg.isToAskMinWeight()));
					children.add(getOppositeNode(link));
					addToInternalLinks(link, true);
				}
				return true;
			}
		});
		
		// unblock "Are you outside?" messages if possible
		unblockAreYouOutside(new Yield<AreYouOutside>() {

			// FIXME double check
			@Override
			public boolean yieldMsg(AreYouOutside blockedMsg, Link link, Iterator<AreYouOutside> it) {
				if (newCity.equals(blockedMsg.getFromCity())) {
					it.remove();
					mySendThrough(link, new Internal());
					addToInternalLinks(link);
				}
				else if (newCity.getLevel() >= blockedMsg.getFromCity().getLevel()) {
					it.remove();
					mySendThrough(link, new External());
				}
				return true;
			}
		});
		
		// ask minWeight of new round if needed
		if (receivedMsg.isToAskMinWeight()) {
			fireNextAreYouOutside(new Runnable() {
				
				// when there's no external link
				@Override
				public void run() {
					if (isDoneAsking()) {
						mySendTo(parent, new MinLinkWeight(INF_WEIGHT));
					}
				}
			}, null);
		}
	}
	
	protected void whenFriendlyMerge(LetUsMerge receivedMsg, Link link) {
		link.setWidth(TREE_PATH_WIDTH);
		MmNodeV2 oppositeNode = getOppositeNode(link);
		int myLevel = getCity().getLevel() + 1;
		String weight = (String) link.getProperty(WEIGHT);
		City newCity = new City(weight, myLevel, ColorUtils.random(HashUtils.sToL(weight)));
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

			// FIXME double check
			@Override
			public boolean yieldMsg(AreYouOutside blockedMsg, Link link, Iterator<AreYouOutside> it) {
				if (newCity.equals(blockedMsg.getFromCity())) {
					it.remove();
					mySendThrough(link, new Internal());
					addToInternalLinks(link);
				}
				else if (myLevel >= blockedMsg.getFromCity().getLevel()) {
					it.remove();
					mySendThrough(link, new External());
				}
				return true;
			}
		});
		
		// at this point, if I am root, I must have at least one child
		fireNextAreYouOutside(new Runnable() {
			
			@Override
			public void run() {
				if (parent != null && isDoneAsking()) {
					mySendTo(parent, new MinLinkWeight(INF_WEIGHT));
				}
			}
		}, null);
	}
	
	@Override
	public void onMinLinkWeight(MinLinkWeight msg, Link link) {
		weightCounter++;
	}
	
	
	//////////////////////////////
	//////////////////////////////
	
	
	// FIXME drop, debug
	@Override
	public void onPreClock() {
		if (containsRepetitive(internalLinks)) {
			System.err.println("internal links of " + this + ": " + internalLinks);
		}
		if (containsRepetitive(children)) {
			System.err.println("children of " + this + ": " + children);
		}
//		if (internalLinks.size() != children.size() + (parent == null ? 0 : 1)) {
//			throw new IllegalStateException(String.format("[%s...] internal links: %d, neighbors: %d", 
//					uuid.substring(0, 6), internalLinks.size(), children.size() + (parent == null ? 0 : 1)));
//		}
	}
	
	// FIXME drop, debug
	private boolean containsRepetitive(List<? extends Object> list) {
		for (int i = 0; i < list.size(); i++) {
			Object ele = list.get(i);
			for (int j = i+1; j < list.size(); j++) {
				if (ele.equals(list.get(j))) {
					return true;
				}
			}
		}
		return false; 
	}
	
}
