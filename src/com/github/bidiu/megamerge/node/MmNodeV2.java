package com.github.bidiu.megamerge.node;

import static com.github.bidiu.megamerge.Bootstrap.WEIGHT;
import static com.github.bidiu.megamerge.message.MinLinkWeight.INF_WEIGHT;

import java.util.Iterator;
import com.github.bidiu.megamerge.common.City;
import com.github.bidiu.megamerge.message.AreYouOutside;
import com.github.bidiu.megamerge.message.External;
import com.github.bidiu.megamerge.message.Internal;
import com.github.bidiu.megamerge.message.LetUsMerge;
import com.github.bidiu.megamerge.message.MergeMe;
import com.github.bidiu.megamerge.message.MinLinkWeight;
import com.github.bidiu.megamerge.message.Notification;
import com.github.bidiu.megamerge.message.Termination;

import jbotsim.Link;

/**
 * MegaMerger Node
 * 
 * @author sunhe
 * @date Nov 26, 2016
 */
public class MmNodeV2 extends MmHelperNode {

	// done
	@Override
	public void spontaneouslyDo() {
		fireNextAreYouOutside(new Runnable() {
			
			// when no external link
			@Override
			public void run() {
				// reach termination
				whenTermination();
			}
		}, null);
	}

	// done
	@Override
	public void onAreYouOutside(AreYouOutside msg, Link link) {
		City senderCity = msg.getFromCity();
		City myCity = getCity();
		if (myCity.getName().equals(senderCity.getName())) {
			// same city
			// so internal
			mySendThrough(link, new Internal());
			addToInternalLinks(link);
		}
		else if (myCity.getLevel() >= senderCity.getLevel()) {
			mySendThrough(link, new External());
		}
		else {
			// do not reply, block message
			blockAreYouOutside(msg, link);
		}
	}

	@Override
	public void onExternal(External msg, Link link) {
		super.onExternal(msg, link);
		updateLinkWithMinWeight(link, (String) link.getProperty(WEIGHT));
		if (isDoneAsking()) {
			if (parent == null) {
				// since just upon receiving external, termination is impossible
				fireLetUsMerge(new LetUsMerge(new City(getCity()), uuid), new Runnable() {
					
					// when on fringe
					@Override
					public void run() {
						// unblock possible "Let's merge"
						// for some reasons, this MUST be a friendly merge with 
						// the node sending "External"
						unblockLetUsMerge(new Yield<LetUsMerge>() {

							@Override
							public boolean yieldMsg(LetUsMerge blockedMsg, Link blockedLink, Iterator<LetUsMerge> it) {
								if (isFriendlyMerge(blockedMsg, blockedLink)) {
									it.remove();
									whenFriendlyMerge(blockedMsg, blockedLink);
									// the reason to stop unblock is, unblock
									// MergeMe scenario is the task of whenFriendlyMerge
									return false;
								}
								return true;
							}
						});
					}
				});
			}
			else {
				// since just upon receiving external, infinite weight is impossible
				mySendTo(parent, new MinLinkWeight(minWeight));
			}
		}
	}

	@Override
	public void onInternal(Internal msg, Link link) {
		addToInternalLinks(link);
		if (isDoneAsking()) {
			if (parent == null) {
				// I'm root node
				if (isTermination()) {
					whenTermination();
				}
				else {
					fireLetUsMerge(new LetUsMerge(new City(getCity()), uuid), new Runnable() {
						
						// when on fringe
						@Override
						public void run() {
							unblockLetUsMerge(new Yield<LetUsMerge>() {

								@Override
								public boolean yieldMsg(LetUsMerge blockedMsg, Link link, Iterator<LetUsMerge> it) {
									if (isFriendlyMerge(blockedMsg, link)) {
										it.remove();
										whenFriendlyMerge(blockedMsg, link);
										return false;
									}
									return true;
								}
							});
						}
					});
				}
			}
			else {
				// I'm non root node
				mySendTo(parent, new MinLinkWeight(minWeight == null ? INF_WEIGHT : minWeight));
			}
		}
		else {
			fireNextAreYouOutside(null, null);
		}
	}

	@Override
	public void onLetUsMerge(LetUsMerge msg, Link link) {
		City senderCity = msg.getFromCity();
		City myCity = getCity();
		
		if (myCity.getName().equals(senderCity.getName())) {
			// same city, i.e., to relay "Let's merge" inside same city
			// update message with my own UUID
			msg.setUuid(uuid);
			fireLetUsMerge(msg, new Runnable() {
				
				// when on fringe
				@Override
				public void run() {
					// unblock "Let's merge" message -- MUST BE A FRIENDLY MERGE
					unblockLetUsMerge(new Yield<LetUsMerge>() {

						@Override
						public boolean yieldMsg(LetUsMerge blockedMsg, Link link, Iterator<LetUsMerge> it) {
							// blocked message turns out to be friendly merge 
							if (isFriendlyMerge(blockedMsg, link)) {
								// NOTE, remove as soon as possible
								it.remove();
								whenFriendlyMerge(blockedMsg, link);
								return false;
							}
							return true;
						}
					});
				}
			});
		}
		else if (isFriendlyMerge(msg, link)) {
			// is friendly merge
			whenFriendlyMerge(msg, link);
		}
		else if (myCity.getLevel() > senderCity.getLevel()) {
			// merge me
			mySendThrough(link, new MergeMe(new City(myCity), ! isDoneAsking()));
			children.add(getOppositeNode(link));
			addToInternalLinks(link, true);
		}
		else if (myCity.getLevel() == senderCity.getLevel()) {
			// block "Let's merge" message
			blockLetUsMerge(msg, link);
		}
		else {
			throw new IllegalStateException();
		}
	}

	@Override
	public void onMergeMe(MergeMe msg, Link link) {
		whenMergeMe(msg, link);
	}

	@Override
	public void onNotification(Notification msg, Link link) {
		City newCity = msg.getNewCity();
		setCity(newCity);
		
		// FIXME test, flip links
		MmNodeV2 oppositeNode = getOppositeNode(link);
		if (children.contains(oppositeNode)) {
			if (parent != null) {
				children.add(parent);
			}
			parent = oppositeNode;
			children.remove(oppositeNode);
		}
		
		// replay notification to children
		for (MmNodeV2 child : children) {
			mySendTo(child, msg);
		}
		
		resetMeta();
		
		// unblock "let's merge" -- MUST be Merge Me
		unblockLetUsMerge(new Yield<LetUsMerge>() {

			@Override
			public boolean yieldMsg(LetUsMerge blockedMsg, Link link, Iterator<LetUsMerge> it) {
				if (newCity.getLevel() > blockedMsg.getFromCity().getLevel()) {
					it.remove();
					// Note, isToAskMinWeight here
					mySendThrough(link, new MergeMe(newCity, msg.isToAskMinWeight()));
					children.add(getOppositeNode(link));
					addToInternalLinks(link, true);
				}
				return true;
			}
		});
		
		// unblock "Are you outside?" if possible
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
		if (msg.isToAskMinWeight()) {
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

	@Override
	public void onMinLinkWeight(MinLinkWeight msg, Link link) {
		super.onMinLinkWeight(msg, link);
		updateLinkWithMinWeight(link, msg.getMinWeight());
		if (! isDoneAsking()) {
			// wait for other MinLinkWeight and/or External
			return;
		}
		
		if (parent == null) {
			if (isTermination()) {
				whenTermination();
			}
			else {
				fireLetUsMerge(new LetUsMerge(new City(getCity()), uuid), new Runnable() {
					
					// when on fringe
					@Override
					public void run() {
						// unblock "Let's merge" -- MUST be friendly merge
						unblockLetUsMerge(new Yield<LetUsMerge>() {

							@Override
							public boolean yieldMsg(LetUsMerge blockedMsg, Link link, Iterator<LetUsMerge> it) {
								if (isFriendlyMerge(blockedMsg, link)) {
									it.remove();
									whenFriendlyMerge(blockedMsg, link);
									return false;
								}
								return true;
							}
						});
					}
				});
			}
		}
		else {
			mySendTo(parent, new MinLinkWeight(minWeight));
		}
	}

	@Override
	public void onTermination(Termination msg, Link link) {
		whenTermination();
	}

}
