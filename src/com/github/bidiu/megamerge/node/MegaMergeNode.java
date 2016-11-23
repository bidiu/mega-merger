package com.github.bidiu.megamerge.node;

import static com.github.bidiu.megamerge.Bootstrap.logger;
import static com.github.bidiu.megamerge.Bootstrap.WEIGHT;
import static com.github.bidiu.megamerge.message.MinLinkWeight.INF_WEIGHT;

import java.awt.Color;
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
import com.github.bidiu.megamerge.util.ColorUtils;
import com.github.bidiu.megamerge.util.HashUtils;
import com.github.bidiu.megamerge.message.MinLinkWeight;

import jbotsim.Link;

/**
 * 
 * @author sunhe
 * @date Nov 22, 2016
 */
public class MegaMergeNode extends AbstractMegaMergeNode {
	
	public static final String LINK = "LINK";
	
	private MegaMergeNode parent;
	private List<MegaMergeNode> children = new LinkedList<>();
	
	private List<Link> internalLinks;
	// FIXME
	private Link linkWithMinWeight;
	// FIXME
	private Link linkOnWhichLetUsMergeSent;
	
	private List<AreYouOutside> blockedAreYouOutside = new LinkedList<>();
	private List<LetUsMerge> blockedLetUsMerge = new LinkedList<>();
	
	/*
	 * Initial procedure at the very beginning.
	 * I assume that every node at the beginning is a candidate. 
	 */
	@Override
	public void spontaneouslyDo() {
		getLinks().get(0).setColor(Color.ORANGE);
		
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
		linkWithMinWeight = link;
		// FIXME
		mySendTo(parent, new MinLinkWeight((String) link.getProperty(WEIGHT)));
	}

	/*
	 * Upon receiving 'Internal' message
	 */
	@Override
	public void onInternal(Internal msg, Link link) {
		internalLinks.add(link);
		List<Link> externalLinks = getLinks();
		externalLinks.remove(internalLinks);
		if (externalLinks.isEmpty()) {
			// FIXME current node have no external links
			mySendTo(parent, new MinLinkWeight(INF_WEIGHT));
		}
		else {
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
		
		if (myLevel == senderLevel && linkOnWhichLetUsMergeSent.equals(link)) {
			// friendly merge
			
			String weight = (String) link.getProperty(WEIGHT);
			City newCity = new City(WEIGHT, myLevel+1, ColorUtils.random(HashUtils.sToL(weight)));
			String senderUuid = msg.getUuid();
			
			if (uuid.compareTo(senderUuid) < 0) {
				// I am the new downtown
				newCity.setDowntown(true);
				// TODO
			}
			else {
				// the other is the new downtown
				newCity.setDowntown(false);
				parent = (MegaMergeNode) link.getOtherEndpoint(this);
			}
			setCity(newCity);
			// notification of new city
			for (Link internalLink : internalLinks) {
				mySendThrough(internalLink, new Notification(new City(newCity)));
			}
			internalLinks.add(link);
			
		}
		else if (myLevel <= senderLevel) {
			// block the message
			msg.setProperty(LINK, link);
			blockedLetUsMerge.add(msg);
		}
		else if (myLevel > senderLevel) {
			// merge to me
			
		}
	}

	@Override
	public void onMergeMe(MergeMe msg, Link link) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNotification(Notification msg, Link link) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMinLinkWeight(MinLinkWeight msg, Link link) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterProcessingMsg() {
		// TODO Auto-generated method stub
		
	}
	
}
