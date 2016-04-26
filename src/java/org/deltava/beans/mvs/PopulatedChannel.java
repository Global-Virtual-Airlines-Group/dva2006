// Copyright 2011, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.mvs;

import java.util.*;

import org.deltava.beans.Pilot;

/**
 * A bean to store user/warning/channel mappings. This class is unsynchronized and callers
 * should perform their own synchronization to ensure thread safety.
 * @author Luke
 * @version 7.0
 * @since 4.0
 */

public class PopulatedChannel implements java.io.Serializable {
	
	private final Channel _c;
	private final Map<Long, Pilot> _users = new HashMap<Long, Pilot>();

	/**
	 * Initializes the bean.
	 * @param c the Channel
	 */
	public PopulatedChannel(Channel c) {
		super();
		_c = c;
	}

	/**
	 * Returns voice channel information.
	 * @return a Channel
	 */
	public Channel getChannel() {
		return _c; 
	}

	/**
	 * Returns all of the connections in this voice channel.
	 * @return a Collection of Connection IDs
	 */
	public Collection<Long> getConnectionIDs() {
		return new ArrayList<Long>(_users.keySet());
	}
	
	/**
	 * Returns the users in this voice channel.
	 * @return a Collection of Map.Entry objects
	 */
	public Collection<Map.Entry<Long, Pilot>> getEntries() {
		return _users.entrySet();
	}

	/**
	 * Adds a user to the channel.
	 * @param conID the user's connection ID
	 * @param usr the Pilot to add
	 */
	public void add(long conID, Pilot usr) {
		Long key = new Long(conID);
		_users.put(key, usr);
	}
	
	/**
	 * Returns whether a User is in a particular channel.
	 * @param conID the connection ID to check
	 * @return TRUE if the connection is in the channel, otherwise FALSE
	 */
	public boolean contains(long conID) {
		return _users.containsKey(new Long(conID));
	}
	
	/**
	 * Returns whether a User is in a particular channel.
	 * @param usr the Pilot to check
	 * @return TRUE if the Pilot is in the channel, otherwise FALSE
	 */
	public boolean contains(Pilot usr) {
		return _users.containsValue(usr);
	}

	/**
	 * Removes a user from the channel.
	 * @param conID the connection ID of the user
	 * @return TRUE if the user was removed, otherwise FALSE
	 */
	public boolean remove(long conID) {
		return (_users.remove(new Long(conID)) != null);
	}
	
	/**
	 * Returns the number of users in the channel.
	 * @return the number of users
	 */
	public int size() {
		return _users.size();
	}
	
	/**
	 * Returns the roles of all Users in the Channel.
	 * @return a Collection of roles
	 */
	public Collection<String> getRolesPresent() {
		Collection<String> results = new HashSet<String>();
		for (Pilot p : _users.values())
			results.addAll(p.getRoles());
		
		return results;
	}
	
	@Override
	public int hashCode() {
		return _c.getName().hashCode();
	}
	
	@Override
	public String toString() {
		return _c.getName();
	}
	
	@Override
	public boolean equals(Object o) {
		return (o instanceof PopulatedChannel) ? (hashCode() == o.hashCode()) : false;
	}
}