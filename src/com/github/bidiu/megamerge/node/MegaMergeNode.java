package com.github.bidiu.megamerge.node;

import static com.github.bidiu.megamerge.Bootstrap.logger;

import com.github.bidiu.megamerge.message.AreYouOutside;
import com.github.bidiu.megamerge.message.External;
import com.github.bidiu.megamerge.message.Internal;
import com.github.bidiu.megamerge.message.LetUsMerge;
import com.github.bidiu.megamerge.message.MergeMe;
import com.github.bidiu.megamerge.message.Notification;
import com.github.bidiu.megamerge.message.MinLinkWeight;

import jbotsim.Link;

/**
 * 
 * @author sunhe
 * @date Nov 22, 2016
 */
public class MegaMergeNode extends AbstractMegaMergeNode {
	
	/*
	 * initial procedure at the very beginning
	 */
	@Override
	public void spontaneouslyDo() {
		logger.debug("spontaneous");
	}

	@Override
	public void onAreYouOutside(AreYouOutside msg, Link link) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onExternal(External msg, Link link) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onInternal(Internal msg, Link link) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLetUsMerge(LetUsMerge msg, Link link) {
		// TODO Auto-generated method stub
		
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
	
}
