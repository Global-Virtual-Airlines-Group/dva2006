// Copyright 2005, 2006, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security;

import java.util.*;
import java.util.concurrent.*;

import org.deltava.beans.*;
import org.deltava.beans.system.IPAddressInfo;

/**
 * A singleton class for tracking connected and blocked users.
 * @author Luke
 * @version 2.5
 * @since 1.0
 */

public class UserPool {

	private static final ConcurrentMap<Integer, UserSession> _users = new ConcurrentHashMap<Integer, UserSession>();
	private static final Collection<Integer> _blockedUsers = Collections.synchronizedSet(new HashSet<Integer>());
	
	private static int _maxSize;
	private static Date _maxSizeDate;

	// We're a singleton, alone and lonely
	private UserPool() {
		super();
	}

	/**
	 * Adds a person to the user pool. See the notes for {@link UserPool#remove(Person, String)} for an
	 * explanation of why we add the session ID.
	 * @param p the Pilot to add
	 * @param sessionID the session ID
	 * @param userAgent the User-Agent header
	 * @see UserPool#remove(Person, String)
	 */
	public static void add(Pilot p, String sessionID, IPAddressInfo addrInfo, String userAgent) {
		if ((p != null) && (!isBlocked(p))) {
			UserSession uw = new UserSession(p, sessionID, addrInfo, userAgent);
			_users.put(new Integer(p.getID()), uw);
			if (_users.size() >= _maxSize) {
				_maxSize = _users.size();
				_maxSizeDate = new Date();
			}
		}
	}
	
	/**
	 * Locks a user out of the application.
	 * @param p the user to block
	 * @see UserPool#isBlocked(Person)
	 */
	public static void block(Person p) {
		_blockedUsers.add(new Integer(p.getID()));
	}
	
	/**
	 * Unlocks a user from the application.
	 * @param p the Person to block
	 */
	public static void unblock(Person p) {
		_blockedUsers.remove(new Integer(p.getID()));
	}

	/**
	 * Removes a person from the user pool, provided that the supplied Session ID matches. This additional check is
	 * performed because this method is usually called from a session lifecycle handler, and a session for a user may
	 * timeout after that same user has launched a new session. In such a circumstance, we do <i>not</i> want to
	 * remove the user from the pool.
	 * @param p the Person to remove
	 * @param sessionID the session ID to match
	 * @see UserPool#add(Pilot, String, IPAddressInfo, String)
	 */
	public static void remove(Person p, String sessionID) {
		UserSession uw = _users.get(p.cacheKey());
		if ((uw != null) && (sessionID.equals(uw.getSessionID())))
			_users.remove(p.cacheKey());
	}

	/**
	 * Queries whether a particular user is currently logged in.
	 * @param userID The <i>database ID </i> of the person
	 * @return TRUE if the user is logged in, otherwise FALSE
	 * @see Pilot#getID()
	 * @see UserPool#isBlocked(Person)
	 */
	public static boolean contains(int userID) {
		return _users.containsKey(new Integer(userID));
	}
	
	/**
	 * Queries whether a particular user is locked out of the system.
	 * @param usr the Pilot bean
	 * @return TRUE if the user is locked out, otherwise FALSE
	 * @see Pilot#getID()
	 * @see UserPool#contains(int)
	 * @see UserPool#block(Person)
	 */
	public static boolean isBlocked(Person usr) {
		return (usr != null) ? _blockedUsers.contains(new Integer(usr.getID())) : false;
	}

	/**
	 * Returns all logged in Pilots and their browser types.
	 * @return a Map of user agents, keyed by Pilot
	 */
	public static Collection<UserSession> getPilots() {
		return new ArrayList<UserSession>(_users.values());
	}

	/**
	 * Returns the number of logged-in users.
	 * @return the number of concurrent users
	 */
	public static int getSize() {
		return _users.size();
	}
	
	/**
	 * Returns the maximum number of concurrent users.
	 * @return the maxmimum number of concurrent users
	 * @see UserPool#getMaxSizeDate()
	 */
	public static int getMaxSize() {
		return _maxSize;
	}

	/**
	 * Returns the date when the maximum number of concurrent users occurred. 
	 * @return the date/time when the maximum user count was reached
	 * @see UserPool#getMaxSize()
	 */
	public static Date getMaxSizeDate() {
		return _maxSizeDate;
	}
	
	/**
	 * Initializes the maximum size values.
	 * @param maxSize the maximum number of users
	 * @param maxSizeDate the date the maximum concurrent users was reached
	 */
	public static void init(int maxSize, Date maxSizeDate) {
		_maxSize = maxSize;
		_maxSizeDate = maxSizeDate;
	}
}