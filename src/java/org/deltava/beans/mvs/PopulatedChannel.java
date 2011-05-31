// Copyright 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.mvs;

import java.util.*;

import org.deltava.beans.Pilot;

/**
 * A bean to store user/channel mappings. This class is unsynchronized and callers
 * should perform their own synchronization to ensure thread safety.
 * @author Luke
 * @version 4.0
 * @since 4.0
 */

public class PopulatedChannel implements java.io.Serializable {
	
	private Channel _c;
	private final Collection<Pilot> _users = new LinkedHashSet<Pilot>();

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
	 * Returns the users in this voice channel.
	 * @return a Collection of Pilot beans
	 */
	public Collection<Pilot> getUsers() {
		return _users;
	}

	/**
	 * Adds a user to the channel.
	 * @param usr the Pilot to add
	 */
	public void add(Pilot usr) {
		_users.add(usr);
	}
	
	/**
	 * Returns whether a User is in a particular channel.
	 * @param usr the Pilot to check
	 * @return TRUE if the Pilot is in the channel, otherwise FALSE
	 */
	public boolean contains(Pilot usr) {
		return _users.contains(usr);
	}

	/**
	 * Removes a user from the channel.
	 * @param usr the Pilot to remove
	 * @returnn TRUE if the user was removed, otherwise FALSE
	 */
	public boolean remove(Pilot usr) {
		return _users.remove(usr);
	}
	
	/**
	 * Returns the number of users in the channel.
	 * @return the number of users
	 */
	public int size() {
		return _users.size();
	}
}