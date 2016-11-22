package com.github.bidiu.megamerge.msg;

import java.util.HashMap;
import java.util.Map;

/**
 * not thread-safe
 * 
 * @author sunhe
 * @date Nov 22, 2016
 */
public abstract class AbstractMsgContent implements MessageContent {
	
	private Map<String, Object> properties;
	
	public AbstractMsgContent() {
		properties = new HashMap<>();
	}
	
	@Override
	public Object getProperty(String key) {
		return properties.get(key);
	}
	
	@Override
	public void setProperty(String key, Object value) {
		properties.put(key, value);
	}
	
	@Override
	public String toString() {
		return getInfo();
	}
	
}
