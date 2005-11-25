package org.deltava.security;

import java.util.*;
import java.io.Serializable;

import org.deltava.beans.*;

/**
 * A singleton class for tracking connected users.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class UserPool implements Serializable {

	private static Map<Object, UserSessionWrapper> _users = new TreeMap<Object, UserSessionWrapper>();

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
	 * Adds a person to the user pool. See the notes for {@link UserPool#removePerson(Person, String)}for an
	 * explanation of why we add the session ID.
	 * @param p the Person to add
	 * @param sessionID the session ID
	 * @see UserPool#removePerson(Person, String)
	 */
	public synchronized static void addPerson(Person p, String sessionID) {
		if (p != null) {
			UserSessionWrapper uw = new UserSessionWrapper(p, sessionID);
			_users.put(p.cacheKey(), uw);
		}
	}

	/**
	 * Removes a person from the user pool, provided that the supplied Session ID matches. This additional check is
	 * performed because this method is usually called from a session lifecycle handler, and a session for a user may
	 * timeout after that same user has launched a new session. In such a circumstance, we do <i>not </i> want to remove
	 * the user from the pool.
	 * @param p the Person to remove
	 * @param sessionID the session ID to match
	 * @see UserPool#addPerson(Person, String)
	 */
	public synchronized static void removePerson(Person p, String sessionID) {

		// Check if the session ID matches the person we wish to remove
		UserSessionWrapper uw = _users.get(p.cacheKey());
		if ((uw != null) && (sessionID.equals(uw.getSessionID())))
			_users.remove(p.cacheKey());
	}

	/**
	 * Queries wether a particular user is currently logged in.
	 * @param id The <i>database ID </i> of the person
	 * @return TRUE if the user is logged in, otherwise FALSE
	 * @see Pilot#getID()
	 */
	public synchronized static boolean contains(int id) {
		return _users.containsKey(new Integer(id));
	}

	/**
	 * Returns all logged in Pilots.
	 * @return a Collection of Pilots
	 * @see UserPool#getUsers()
	 * @see UserPool#getUserNames()
	 */
	public static Collection<Pilot> getPilots() {
		Set<Pilot> results = new HashSet<Pilot>();
		for (Iterator<UserSessionWrapper> i = _users.values().iterator(); i.hasNext();) {
			UserSessionWrapper uw = i.next();
			if (uw.getPerson() instanceof Pilot)
				results.add((Pilot) uw.getPerson());
		}

		return results;
	}

	/**
	 * Returns all logged in Pilots and Applicants.
	 * @return a Collection of Persons
	 * @see UserPool#getPilots()
	 * @see UserPool#getUserNames()
	 */
	public static Collection<Person> getUsers() {
		Set<Person> results = new HashSet<Person>();
		for (Iterator<UserSessionWrapper> i = _users.values().iterator(); i.hasNext();) {
			UserSessionWrapper uw = i.next();
			results.add(uw.getPerson());
		}

		return results;
	}

	/**
	 * Return the names of all logged in Pilots and Applicants.
	 * @return a sorted Collection of names
	 * @see UserPool#getPilots()
	 * @see UserPool#getUsers()
	 */
	public static Collection<String> getUserNames() {
		Set<String> result = new TreeSet<String>();
		for (Iterator i = _users.values().iterator(); i.hasNext();) {
			UserSessionWrapper uw = (UserSessionWrapper) i.next();
			result.add(uw.getPerson().getName());
		}

		return result;
	}

	/**
	 * Clears the user pool.
	 */
	public synchronized static void clear() {
		_users.clear();
	}

	/**
	 * Returns the number of logged-in users.
	 * @return the number of concurrent users
	 */
	public static int getSize() {
		return _users.size();
	}
}