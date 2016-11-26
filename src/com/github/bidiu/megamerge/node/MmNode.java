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
import com.github.bidiu.megamerge.message.Notification;
import com.github.bidiu.megamerge.message.Termination;
import com.github.bidiu.megamerge.util.ColorUtils;
import com.github.bidiu.megamerge.util.HashUtils;
import com.github.bidiu.megamerge.message.MinLinkWeight;

import jbotsim.Link;

/**
 * Bugs:
 * 0. analyze console
 * 1. blocked let's-merge now is friendly merge
 * 
 * @author sunhe
 * @date Nov 22, 2016
 */
public class MmNode extends AbstractNode {
	
	public static final String LINK = "LINK";
	public static final int TREE_PATH_WIDTH = 5;
	
	private MmNode parent;
	private List<MmNode> children = new LinkedList<>();
	
	private List<Link> internalLinks = new LinkedList<>();
	// FIXME refresh upon notification/after sending let's merge
	private Link linkWithMinWeight;
	private String minWeight;
	// FIXME refresh upon unblock
	private Link linkTryingToMerge;
	
	private List<AreYouOutside> blockedAreYouOutside = new LinkedList<>();
	private List<LetUsMerge> blockedLetUsMerge = new LinkedList<>();
	
	// FIXME
	private int weightCounter;
	private boolean externalRecevied;
	
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
		externalRecevied = true;
		if (linkWithMinWeight == null || linkWithMinWeight != null && 
				((String) link.getProperty(WEIGHT)).compareTo(minWeight) < 0) {
			linkWithMinWeight = link;
			minWeight = (String) link.getProperty(WEIGHT);
		}
		if (weightCounter == children.size()) {
			// already get all weight values from children
			if (parent == null) {
				// I'm parent node
				mySendThrough(linkWithMinWeight, new LetUsMerge(new City(getCity()), uuid));
				if (! internalLinks.contains(linkWithMinWeight)) {
					// I'm the node supposed to send let-us-merge outside
					// In other words, try to merge through the link just asked with outside
					linkTryingToMerge = linkWithMinWeight;
					
					// N1
					// unblock let's merge messages if possible
					Iterator<LetUsMerge> it = blockedLetUsMerge.iterator();
					while (it.hasNext()) {
						LetUsMerge blockedMsg = it.next();
						Link blockedLink = (Link) blockedMsg.getProperty(LINK);
						if (linkWithMinWeight.equals(blockedLink)) {
							// N1: friendly merge
							// N1: for some reasons, must be friendly merge
							// N1: here parameter link and linkWithMinWeight are the same link
							int myLevel = getCity().getLevel();
							int senderLevel = blockedMsg.getFromCity().getLevel();
							
							// redundant check
							if (myLevel != senderLevel) {
								throw new IllegalStateException();
							}
							it.remove();
							
							/*
							 * following copy from onLetUsMerge
							 */
							blockedLink.setWidth(TREE_PATH_WIDTH);
							String weight = (String) blockedLink.getProperty(WEIGHT);
							City newCity = new City(weight, myLevel+1, ColorUtils.random(HashUtils.sToL(weight)));
							String senderUuid = blockedMsg.getUuid();
							
							if (uuid.compareTo(senderUuid) < 0) {
								// I am the new downtown
								newCity.setDowntown(true);
								setCity(newCity);
								if (parent != null) {
									children.add(parent);
									parent = null;
								}
								children.add((MmNode) blockedLink.getOtherEndpoint(this));
							}
							else {
								// the other is the new downtown
								newCity.setDowntown(false);
								setCity(newCity);
								if (parent != null) {
									children.add(parent);
								}
								parent = (MmNode) blockedLink.getOtherEndpoint(this);
							}
							
							// notification of new city
							for (Link internalLink : internalLinks) {
								mySendThrough(internalLink, new Notification(new City(newCity)));
							}
							internalLinks.add(blockedLink);
							
							// reset
							weightCounter = 0;
							linkWithMinWeight = null;
							linkTryingToMerge = null;
							externalRecevied = false;
							minWeight = null;
							
							// unblock let's-merge messages if possible
							Iterator<LetUsMerge> it2 = blockedLetUsMerge.iterator();
							while (it2.hasNext()) {
								LetUsMerge blockedMsg2 = it2.next();
								if (getCity().getLevel() > blockedMsg2.getFromCity().getLevel()) {
									Link blockedLink2 = (Link) blockedMsg2.getProperty(LINK);
									mySendThrough(blockedLink2, new MergeMe(new City(getCity()), true));
									children.add((MmNode) blockedLink2.getOtherEndpoint(this));
									internalLinks.add(blockedLink2);
									it2.remove();
								}
							}
							
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
					}
				}
			}
			else {
				// I'm non-parent node
				mySendTo(parent, new MinLinkWeight(minWeight));
			}
		}
	}

	/*
	 * Upon receiving 'Internal' message
	 */
	@Override
	public void onInternal(Internal msg, Link link) {
		if (! internalLinks.contains(link)) {
			internalLinks.add(link);
		}
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
					if (INF_WEIGHT.equals(minWeight)) {
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
					mySendTo(parent, new MinLinkWeight(minWeight));
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
		
		// TODO debug
		System.err.println(this + ": " + getCity().getName() + ", " + senderCity.getName());
		if (getCity().getName().equals(senderCity.getName())) {
			// internal relay let-us-merge
			msg.setUuid(uuid);
			mySendThrough(linkWithMinWeight, msg);
			if (! internalLinks.contains(linkWithMinWeight)) {
				// I'm the node supposed to send let-us-merge outside
				linkTryingToMerge = linkWithMinWeight;
			}
			
			// FIXME what if already block a let-us-merge from the other or another!
		}
		// TODO updated for MERGE~
		else if (myLevel == senderLevel && linkTryingToMerge != null && link.equals(linkTryingToMerge)) {
			// friendly merge
			link.setWidth(TREE_PATH_WIDTH);
			String weight = (String) link.getProperty(WEIGHT);
			City newCity = new City(weight, myLevel+1, ColorUtils.random(HashUtils.sToL(weight)));
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
			
			// reset
			weightCounter = 0;
			linkWithMinWeight = null;
			linkTryingToMerge = null;
			externalRecevied = false;
			minWeight = null;
			
			// unblock let's-merge messages if possible
			Iterator<LetUsMerge> it = blockedLetUsMerge.iterator();
			while (it.hasNext()) {
				LetUsMerge blockedMsg = it.next();
				if (getCity().getLevel() > blockedMsg.getFromCity().getLevel()) {
					Link blockedLink = (Link) blockedMsg.getProperty(LINK);
					mySendThrough(blockedLink, new MergeMe(new City(getCity()), true));
					children.add((MmNode) blockedLink.getOtherEndpoint(this));
					internalLinks.add(blockedLink);
					it.remove();
				}
			}
			
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
				// FIXME
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
			mySendThrough(link, new MergeMe(
					new City(getCity()), !(weightCounter == children.size() && 
							(internalLinks.size() == getLinks().size() || externalRecevied))
				)
			);
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
		link.setWidth(TREE_PATH_WIDTH);
		
		// notification of merged city
		for (MmNode child : children) {
			mySendTo(child, new Notification(new City(senderCity), msg.isToAskMinWeight()));
		}
		
		// reset meta data
		weightCounter = 0;
		linkWithMinWeight = null;
		linkTryingToMerge = null;
		externalRecevied = false;
		minWeight = null;
		
		// unblock let's merge messages if possible
		Iterator<LetUsMerge> it = blockedLetUsMerge.iterator();
		while (it.hasNext()) {
			LetUsMerge blockedMsg = it.next();
			if (getCity().getLevel() > blockedMsg.getFromCity().getLevel()) {
				Link blockedLink = (Link) blockedMsg.getProperty(LINK);
				mySendThrough(blockedLink, new MergeMe(new City(getCity()), msg.isToAskMinWeight()));
				children.add((MmNode) blockedLink.getOtherEndpoint(this));
				internalLinks.add(blockedLink);
				it.remove();
			}
		}
		
		// unblock are-you-outside messages if possible
		Iterator<AreYouOutside> it2 = blockedAreYouOutside.iterator();
		while (it2.hasNext()) {
			AreYouOutside blockedMsg = it2.next();
			Link blockedLink = (Link) blockedMsg.getProperty(LINK);
			if (getCity().equals(blockedMsg.getFromCity())) {
				mySendThrough(blockedLink, new Internal());
				if (! internalLinks.contains(blockedLink)) {
					internalLinks.add(blockedLink);
				}
				it2.remove();
			}
			else if (getCity().getLevel() >= blockedMsg.getFromCity().getLevel()) {
				mySendThrough(blockedLink, new External());
				it2.remove();
			}
		}
		
		if (msg.isToAskMinWeight()) {
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
	public void onNotification(Notification msg, Link link) {
		setCity(msg.getNewCity());
		if (parent == null) {
			parent = (MmNode) link.getOtherEndpoint(this);
			children.remove(parent);
		}
		
		// relay notification to children
		for (MmNode child : children) {
			mySendTo(child, msg);
		}
		
		// reset meta data
		weightCounter = 0;
		linkWithMinWeight = null;
		linkTryingToMerge = null;
		externalRecevied = false;
		minWeight = null;
		
		// unblock let's merge messages if possible
		Iterator<LetUsMerge> it = blockedLetUsMerge.iterator();
		while (it.hasNext()) {
			LetUsMerge blockedMsg = it.next();
			if (getCity().getLevel() > blockedMsg.getFromCity().getLevel()) {
				Link blockedLink = (Link) blockedMsg.getProperty(LINK);
				mySendThrough(blockedLink, new MergeMe(new City(getCity()), msg.isToAskMinWeight()));
				children.add((MmNode) blockedLink.getOtherEndpoint(this));
				if (internalLinks.contains(blockedLink)) {
					throw new IllegalStateException();
				}
				internalLinks.add(blockedLink);
				it.remove();
			}
		}
		
		// unblock are-you-outside messages if possible
		Iterator<AreYouOutside> it2 = blockedAreYouOutside.iterator();
		while (it2.hasNext()) {
			AreYouOutside blockedMsg = it2.next();
			Link blockedLink = (Link) blockedMsg.getProperty(LINK);
			if (getCity().equals(blockedMsg.getFromCity())) {
				mySendThrough(blockedLink, new Internal());
				internalLinks.add(blockedLink);
				it2.remove();
			}
			else if (getCity().getLevel() >= blockedMsg.getFromCity().getLevel()) {
				mySendThrough(blockedLink, new External());
				it2.remove();
			}
		}
		
		if (msg.isToAskMinWeight()) {
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
				msg.getMinWeight().compareTo(minWeight) < 0) {
			linkWithMinWeight = link;
			minWeight = msg.getMinWeight();
		}
//		if (weightCounter == children.size() && internalLinks.size() == getLinks().size()) {
//			if (parent == null) {
//				if (INF_WEIGHT.equals(msg.getMinWeight())) {
//					setCity(City.ELECTED);
//					for (MmNode child : children) {
//						mySendTo(child, new Termination());
//					}
//				}
//				else {
//					mySendThrough(linkWithMinWeight, new LetUsMerge(new City(getCity()), uuid));
//				}
//			}
//			else {
//				mySendTo(parent, new MinLinkWeight((String) linkWithMinWeight.getProperty(WEIGHT)));
//			}
//		}
		/*
		 * till the end of function is version with N2
		 */
		
		
		if (weightCounter == children.size() && 
				(internalLinks.size() == getLinks().size() || externalRecevied)) {
			if (parent == null) {
				if (INF_WEIGHT.equals(minWeight)) {
					setCity(City.ELECTED);
					for (MmNode child : children) {
						mySendTo(child, new Termination());
					}
				}
				else {
					mySendThrough(linkWithMinWeight, new LetUsMerge(new City(getCity()), uuid));
					
					/*
					 * 
					 */
					if (! internalLinks.contains(linkWithMinWeight)) {
						// I'm the node supposed to send let-us-merge outside
						// In other words, try to merge through the link just asked with outside
						linkTryingToMerge = linkWithMinWeight;
						
						// N1
						// unblock let's merge messages if possible
						Iterator<LetUsMerge> it = blockedLetUsMerge.iterator();
						while (it.hasNext()) {
							LetUsMerge blockedMsg = it.next();
							Link blockedLink = (Link) blockedMsg.getProperty(LINK);
							if (linkWithMinWeight.equals(blockedLink)) {
								// N1: friendly merge
								// N1: for some reasons, must be friendly merge
								// N1: here parameter link and linkWithMinWeight are the same link
								int myLevel = getCity().getLevel();
								int senderLevel = blockedMsg.getFromCity().getLevel();
								
								// redundant check
								if (myLevel != senderLevel) {
									throw new IllegalStateException();
								}
								it.remove();
								
								/*
								 * following copy from onLetUsMerge
								 */
								blockedLink.setWidth(TREE_PATH_WIDTH);
								String weight = (String) blockedLink.getProperty(WEIGHT);
								City newCity = new City(weight, myLevel+1, ColorUtils.random(HashUtils.sToL(weight)));
								String senderUuid = blockedMsg.getUuid();
								
								if (uuid.compareTo(senderUuid) < 0) {
									// I am the new downtown
									newCity.setDowntown(true);
									setCity(newCity);
									if (parent != null) {
										children.add(parent);
										parent = null;
									}
									children.add((MmNode) blockedLink.getOtherEndpoint(this));
								}
								else {
									// the other is the new downtown
									newCity.setDowntown(false);
									setCity(newCity);
									if (parent != null) {
										children.add(parent);
									}
									parent = (MmNode) blockedLink.getOtherEndpoint(this);
								}
								
								// notification of new city
								for (Link internalLink : internalLinks) {
									mySendThrough(internalLink, new Notification(new City(newCity)));
								}
								internalLinks.add(blockedLink);
								
								// reset
								weightCounter = 0;
								linkWithMinWeight = null;
								linkTryingToMerge = null;
								externalRecevied = false;
								minWeight = null;
								
								// unblock let's-merge messages if possible
								Iterator<LetUsMerge> it2 = blockedLetUsMerge.iterator();
								while (it2.hasNext()) {
									LetUsMerge blockedMsg2 = it2.next();
									if (getCity().getLevel() > blockedMsg2.getFromCity().getLevel()) {
										Link blockedLink2 = (Link) blockedMsg2.getProperty(LINK);
										mySendThrough(blockedLink2, new MergeMe(new City(getCity()), true));
										children.add((MmNode) blockedLink2.getOtherEndpoint(this));
										internalLinks.add(blockedLink2);
										it2.remove();
									}
								}
								
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
						}
					}
				}
			}
			else {
				mySendTo(parent, new MinLinkWeight(minWeight));
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
	
	// TODO debug
	@Override
	public void onPreClock() {
		if (containsRepetitive(internalLinks)) {
			System.err.println("internal links of " + this + ": " + internalLinks);
		}
		if (containsRepetitive(children)) {
			System.err.println("children of " + this + ": " + children);
		}
	}
	
	// TODO debug
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