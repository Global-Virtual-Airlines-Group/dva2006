// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security;

import java.util.*;

import org.deltava.beans.*;

/**
 * A singleton class for tracking connected and blocked users.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class UserPool {

	private static final Map<Integer, UserSessionWrapper> _users = new TreeMap<Integer, UserSessionWrapper>();
	private static final Collection<Integer> _blockedUsers = Collections.synchronizedSet(new HashSet<Integer>());
	
	private static int _maxSize;
	private static Date _maxSizeDate;

	// We're a singleton, alone and lonely
	private UserPool() {
	}

	private static class UserSessionWrapper {

		private String _sessionID;
		private Person _p;

		public UserSessionWrapper(Person p, String sessionID) {
			super();
			_p = p;
			_sessionID = sessionID;
		}

		public Person getPerson() {
			return _p;
		}

		public String getSessionID() {
			return _sessionID;
		}
	}

	/**
	 * Adds a person to the user pool. See the notes for {@link UserPool#remove(Person, String)} for an
	 * explanation of why we add the session ID.
	 * @param p the Person to add
	 * @param sessionID the session ID
	 * @see UserPool#remove(Person, String)
	 */
	public synchronized static void add(Person p, String sessionID) {
		if ((p != null) && (!isBlocked(p))) {
			UserSessionWrapper uw = new UserSessionWrapper(p, sessionID);
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
	 * Removes a person from the user pool, provided that the supplied Session ID matches. This additional check is
	 * performed because this method is usually called from a session lifecycle handler, and a session for a user may
	 * timeout after that same user has launched a new session. In such a circumstance, we do <i>not</i> want to
	 * remove the user from the pool.
	 * @param p the Person to remove
	 * @param sessionID the session ID to match
	 * @see UserPool#add(Person, String)
	 */
	public synchronized static void remove(Person p, String sessionID) {

		// Check if the session ID matches the person we wish to remove
		UserSessionWrapper uw = _users.get(p.cacheKey());
		if ((uw != null) && (sessionID.equals(uw.getSessionID())))
			_users.remove(p.cacheKey());
	}

	/**
	 * Queries wether a particular user is currently logged in.
	 * @param userID The <i>database ID </i> of the person
	 * @return TRUE if the user is logged in, otherwise FALSE
	 * @see Pilot#getID()
	 * @see UserPool#isBlocked(Person)
	 */
	public synchronized static boolean contains(int userID) {
		return _users.containsKey(new Integer(userID));
	}
	
	/**
	 * Queries wether a particular user is locked out of the system.
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
	 * Returns all logged in Pilots.
	 * @return a Collection of Pilots
	 */
	public static synchronized Collection<Pilot> getPilots() {
		Collection<Pilot> results = new LinkedHashSet<Pilot>();
		for (Iterator<UserSessionWrapper> i = _users.values().iterator(); i.hasNext();) {
			UserSessionWrapper uw = i.next();
			if (uw.getPerson() instanceof Pilot)
				results.add((Pilot) uw.getPerson());
		}

		return results;
	}

	/**
	 * Returns the number of logged-in users.
	 * @return the number of concurrent users
	 */
	public static synchronized int getSize() {
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
}