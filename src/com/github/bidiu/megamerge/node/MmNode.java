package com.github.bidiu.megamerge.node;

import static com.github.bidiu.megamerge.Bootstrap.WEIGHT;
import static com.github.bidiu.megamerge.message.MinLinkWeight.INF_WEIGHT;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.github.bidiu.megamerge.common.City;
import com.github.bidiu.megamerge.message.AreYouOutside;
import com.github.bidiu.megamerge.message.External;
import com.github.bidiu.megamerge.message.Internal;
import com.github.bidiu.megamerge.message.LetUsMerge;
import com.github.bidiu.megamerge.message.MergeMe;
import com.github.bidiu.megamerge.message.Notification;
import com.github.bidiu.megamerge.message.Termination;
import com.github.bidiu.megamerge.util.ColorUtils;
import com.github.bidiu.megamerge.util.HashUtils;
import com.github.bidiu.megamerge.message.MinLinkWeight;

import jbotsim.Link;

/**
 * 
 * @author sunhe
 * @date Nov 22, 2016
 */
public class MmNode extends AbstractNode {
	
	public static final String LINK = "LINK";
	
	private MmNode parent;
	private List<MmNode> children = new LinkedList<>();
	
	private List<Link> internalLinks = new LinkedList<>();
	// FIXME refresh upon notification/after sending let's merge
	private Link linkWithMinWeight;
	// FIXME refresh upon unblock
	private Link linkTryingToMerge;
	
	private List<AreYouOutside> blockedAreYouOutside = new LinkedList<>();
	private List<LetUsMerge> blockedLetUsMerge = new LinkedList<>();
	
	// FIXME
	private int weightCounter;
	
	/*
	 * Initial procedure at the very beginning.
	 * I assume that every node at the beginning is a candidate. 
	 */
	@Override
	public void spontaneouslyDo() {
		List<Link> externalLinks = getLinks();
		if (externalLinks.isEmpty()) {
			setCity(City.ELECTED);
		}
		else {
			// current node has other external links
			Link nextLink = Collections.min(externalLinks, new Comparator<Link>() {

				@Override
				public int compare(Link link1, Link link2) {
					String weight1 = (String) link1.getProperty(WEIGHT);
					String weight2 = (String) link2.getProperty(WEIGHT);
					return weight1.compareTo(weight2);
				}
			});
			mySendThrough(nextLink, new AreYouOutside(new City(getCity())));
		}
	}

	/*
	 * Upon receiving 'Outside?' message.
	 */
	@Override
	public void onAreYouOutside(AreYouOutside msg, Link link) {
		City senderCity = msg.getFromCity();
		City myCity = getCity();
		if (myCity.getName().equals(senderCity.getName())) {
			// cities are equal
			mySendThrough(link, new Internal());
		}
		else if (myCity.getLevel() >= senderCity.getLevel()) {
			mySendThrough(link, new External());
		}
		else {
			// do not reply for now
			msg.setProperty(LINK, link);
			blockedAreYouOutside.add(msg);
		}
	}

	/*
	 * Upon receiving 'External' message.
	 */
	@Override
	public void onExternal(External msg, Link link) {
		if (linkWithMinWeight == null || linkWithMinWeight != null && 
				((String) link.getProperty(WEIGHT)).compareTo((String) linkWithMinWeight.getProperty(WEIGHT)) < 0) {
			linkWithMinWeight = link;
		}
		if (weightCounter == children.size()) {
			// already get all weight values from children
			if (parent == null) {
				// I'm parent node
				mySendThrough(linkWithMinWeight, new LetUsMerge(new City(getCity()), uuid));
				if (! internalLinks.contains(linkWithMinWeight)) {
					// I'm the node supposed to send let-us-merge outside
					linkTryingToMerge = linkWithMinWeight;
				}
			}
			else {
				// I'm non-parent node
				mySendTo(parent, new MinLinkWeight((String) link.getProperty(WEIGHT)));
			}
		}
	}

	/*
	 * Upon receiving 'Internal' message
	 */
	@Override
	public void onInternal(Internal msg, Link link) {
		internalLinks.add(link);
		List<Link> externalLinks = getLinks();
		externalLinks.removeAll(internalLinks);
		if (externalLinks.isEmpty()) {
			// current node has no external links
			if (weightCounter == children.size()) {
				// already get all weight values from children
				if (parent == null) {
					// note that since current node has internal links, 
					// and weightCounter == childre.size(), and I'm root, 
					// linkWithMinWeight is impossible to be null here.
					if (INF_WEIGHT.equals((String) linkWithMinWeight.getProperty(WEIGHT))) {
						// reach termination
						setCity(City.ELECTED);
						for (MmNode child : children) {
							mySendTo(child, new Termination());
						}
					}
					else {
						mySendThrough(linkWithMinWeight, new LetUsMerge(new City(getCity()), uuid));
					}
				}
				else if (linkWithMinWeight == null) {
					mySendTo(parent, new MinLinkWeight(INF_WEIGHT));
				}
				else {
					mySendTo(parent, new MinLinkWeight((String) linkWithMinWeight.getProperty(WEIGHT)));
				}
			}
		}
		else {
			// current node has other external links, so keep asking next possible external node
			Link nextLink = Collections.min(externalLinks, new Comparator<Link>() {

				@Override
				public int compare(Link link1, Link link2) {
					String weight1 = (String) link1.getProperty(WEIGHT);
					String weight2 = (String) link2.getProperty(WEIGHT);
					return weight1.compareTo(weight2);
				}
			});
			mySendThrough(nextLink, new AreYouOutside(new City(getCity())));
		}
	}

	/*
	 * Upon receiving 'Let's Merge' message
	 */
	@Override
	public void onLetUsMerge(LetUsMerge msg, Link link) {
		City senderCity = msg.getFromCity();
		int senderLevel = senderCity.getLevel();
		int myLevel = getCity().getLevel();
		
		if (getCity().getName().equals(senderCity.getName())) {
			mySendThrough(linkWithMinWeight, msg);
			if (! internalLinks.contains(linkWithMinWeight)) {
				// I'm the node supposed to send let-us-merge outside
				linkTryingToMerge = linkWithMinWeight;
			}
		}
		else if (myLevel == senderLevel && link.equals(linkTryingToMerge)) {
			// friendly merge
			String weight = (String) link.getProperty(WEIGHT);
			City newCity = new City(WEIGHT, myLevel+1, ColorUtils.random(HashUtils.sToL(weight)));
			String senderUuid = msg.getUuid();
			
			if (uuid.compareTo(senderUuid) < 0) {
				// I am the new downtown
				newCity.setDowntown(true);
				setCity(newCity);
				if (parent != null) {
					children.add(parent);
					parent = null;
				}
				children.add((MmNode) link.getOtherEndpoint(this));
			}
			else {
				// the other is the new downtown
				newCity.setDowntown(false);
				setCity(newCity);
				if (parent != null) {
					children.add(parent);
				}
				parent = (MmNode) link.getOtherEndpoint(this);
			}
			
			// notification of new city
			for (Link internalLink : internalLinks) {
				mySendThrough(internalLink, new Notification(new City(newCity)));
			}
			internalLinks.add(link);
			
			weightCounter = 0;
			linkWithMinWeight = null;
			linkTryingToMerge = null;
			
			List<Link> externalLinks = getLinks();
			externalLinks.removeAll(internalLinks);
			if (! externalLinks.isEmpty()) {
				// current downtown has external links
				Link nextLink = Collections.min(externalLinks, new Comparator<Link>() {

					@Override
					public int compare(Link link1, Link link2) {
						String weight1 = (String) link1.getProperty(WEIGHT);
						String weight2 = (String) link2.getProperty(WEIGHT);
						return weight1.compareTo(weight2);
					}
				});
				mySendThrough(nextLink, new AreYouOutside(new City(getCity())));
			}
			else if (parent != null && children.isEmpty()) {
				mySendTo(parent, new MinLinkWeight(INF_WEIGHT));
			}
		}
		else if (myLevel <= senderLevel) {
			// block the message
			msg.setProperty(LINK, link);
			blockedLetUsMerge.add(msg);
		}
		else if (myLevel > senderLevel) {
			// merge to me
			mySendThrough(link, new MergeMe(new City(getCity())));
			children.add((MmNode) link.getOtherEndpoint(this));
			internalLinks.add(link);
		}
	}

	@Override
	public void onMergeMe(MergeMe msg, Link link) {
		City senderCity = msg.getFromCity();
		setCity(senderCity);
		internalLinks.add(link);
		if (parent != null) {
			children.add(parent);
		}
		parent = (MmNode) link.getOtherEndpoint(this);
		
		// notification of merged city
		for (MmNode child : children) {
			mySendTo(child, new Notification(new City(senderCity), false));
		}
		
		// reset meta data
		weightCounter = 0;
		linkWithMinWeight = null;
		linkTryingToMerge = null;
	}

	@Override
	public void onNotification(Notification msg, Link link) {
		setCity(msg.getNewCity());
		if (parent == null) {
			parent = (MmNode) link.getOtherEndpoint(this);
			children.remove(parent);
		}
		// reset meta data
		weightCounter = 0;
		linkWithMinWeight = null;
		linkTryingToMerge = null;
		
		for (MmNode child : children) {
			mySendTo(child, msg);
		}
		
		if (msg.isFriendlyMerge()) {
			// need to ask minimal weight to outside again
			List<Link> externalLinks = getLinks();
			externalLinks.removeAll(internalLinks);
			if (externalLinks.isEmpty()) {
				// current node has no external links
				if (weightCounter == children.size()) {
					mySendTo(parent, new MinLinkWeight(INF_WEIGHT));
				}
			}
			else {
				// current node has external links
				Link nextLink = Collections.min(externalLinks, new Comparator<Link>() {

					@Override
					public int compare(Link link1, Link link2) {
						String weight1 = (String) link1.getProperty(WEIGHT);
						String weight2 = (String) link2.getProperty(WEIGHT);
						return weight1.compareTo(weight2);
					}
				});
				mySendThrough(nextLink, new AreYouOutside(new City(getCity())));
			}
		}
	}

	@Override
	public void onMinLinkWeight(MinLinkWeight msg, Link link) {
		weightCounter++;
		if (linkWithMinWeight == null || linkWithMinWeight !=null && 
				((String) link.getProperty(WEIGHT)).compareTo((String) linkWithMinWeight.getProperty(WEIGHT)) < 0) {
			linkWithMinWeight = link;
		}
		if (weightCounter == children.size() && internalLinks.size() == getLinks().size()) {
			if (parent == null) {
				if (INF_WEIGHT.equals(msg.getMinWeight())) {
					setCity(City.ELECTED);
					for (MmNode child : children) {
						mySendTo(child, new Termination());
					}
				}
				else {
					mySendThrough(linkWithMinWeight, new LetUsMerge(new City(getCity()), uuid));
				}
			}
			else {
				mySendTo(parent, new MinLinkWeight((String) linkWithMinWeight.getProperty(WEIGHT)));
			}
		}
	}
	
	@Override
	public void onTermination(Termination msg, Link link) {
		setCity(msg.getCity());
		for (MmNode child : children) {
			mySendTo(child, msg);
		}
	}

	@Override
	public void afterProcessingMsg() {
		// TODO Auto-generated method stub
		
	}

}
