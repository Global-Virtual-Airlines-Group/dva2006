// Copyright 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.murmur;

import org.deltava.beans.DatabaseBean;

/**
 * A bean to store information about a Murmur voice channel. 
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public class Channel extends DatabaseBean {
	
	private int _serverID;
	private final String _name;
	private String _desc;

	/**
	 * Creates the bean.
	 * @param name the channel name 
	 */
	public Channel(String name) {
		super();
		_name = name;
	}
	
	/**
	 * Returns the channel name.
	 * @return the name
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Returns the Murmur virtual server ID.
	 * @return the server ID
	 */
	public int getServerID() {
		return _serverID;
	}
	
	/**
	 * Returns the channel description.
	 * @return the description
	 */
	public String getDescription() {
		return _desc;
	}

	/**
	 * Updates the Murmur virtual server ID.
	 * @param id the server ID
	 * @throws IllegalArgumentException if id is zero or negative
	 */
	public void setServerID(int id) {
		validateID(_serverID, id);
		_serverID = id;
	}

	/**
	 * Updates the channel description.
	 * @param desc the description
	 */
	public void setDescription(String desc) {
		_desc = desc;
	}
}