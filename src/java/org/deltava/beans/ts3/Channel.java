// Copyright 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.ts3;

/**
 * A bean to store data about a Teamspeak 3 Voice channel. 
 * @author Luke
 * @version 6.1
 * @since 6.1
 */

public class Channel {
	
	private String _name;
	private String _topic;
	private String _desc;
	
	private int _maxClients;
	private Codec _codec;

	/**
	 * Creates the bean.
	 * @param name the Channel name
	 */
	public Channel(String name) {
		super();
		_name = name;
	}

	/**
	 * Returns the Channel name.
	 * @return the name
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Returns the Channel topic.
	 * @return the topic
	 */
	public String getTopic() {
		return _topic;
	}
	
	/**
	 * Returns the Channel description.
	 * @return the description
	 */
	public String getDescription() {
		return _desc;
	}
	
	/**
	 * Returns the maximum number of clients in the Channel.
	 * @return the maximum clients
	 */
	public int getMaxClients() {
		return _maxClients;
	}
	
	/**
	 * Returns the Channel codec.
	 * @return the Codec
	 */
	public Codec getCodec() {
		return _codec;
	}
	
	/**
	 * Updates the Channel's topic.
	 * @param t the topic
	 */
	public void setTopic(String t) {
		_topic = t;
	}
	
	/**
	 * Updates the Channel's description.
	 * @param desc the description
	 */
	public void setDescription(String desc) {
		_desc = desc;
	}
	
	/**
	 * Updates the maximum clients for the Channel.
	 * @param maxClients the maxClients
	 */
	public void setMaxClients(int maxClients) {
		_maxClients = Math.max(0, maxClients);
	}
	
	/**
	 * Updates the Channel's voice codec.
	 * @param c the Codec
	 */
	public void setCodec(Codec c) {
		_codec = c;
	}
}