// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.util.*;

/**
 * A class to support a map of {@link UserData} beans. This class implements Map to allow it to be accessed directly via JSP Expression Language.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class UserDataMap extends HashMap<Integer, UserData> {

	/**
	 * Creates a new, empty UserDataCollection.
	 */
	public UserDataMap() {
		super();
	}

	/**
	 * Creates a new UserDataCollection from a set of Integers containing database IDs.
	 * @param data a Collection of UserData objects
	 * @see UserDataMap#putAll(Map)
	 */
	public UserDataMap(Collection<UserData> data) {
		super();
		for (Iterator<UserData> i = data.iterator(); i.hasNext(); ) {
			UserData ud = i.next();
			put(Integer.valueOf(ud.getID()), ud);
		}
	}

	/**
	 * Returns if the collection contains an entry for a particular user.
	 * @param id the User's database ID
	 * @return TRUE if data for the User is present, otherwise FALSE
	 */
	public boolean contains(int id) {
		return containsKey(Integer.valueOf(id));
	}

	/**
	 * Returns an entry for a particular user.
	 * @param id the User's database ID
	 * @return the UserData bean, or null if not found
	 * @see UserDataMap#get(Object)
	 */
	public UserData get(int id) {
		return get(Integer.valueOf(id));
	}

	/**
	 * Returns all entries present within a particular table.
	 * @param tableName the database table name
	 * @return a Collection of UserData objects
	 * @throws NullPointerException if tableName is null
	 */
	public Collection<UserData> getByTable(String tableName) {
		Collection<UserData> results = new HashSet<UserData>();
		for (Iterator<UserData> i = values().iterator(); i.hasNext();) {
			UserData usr = i.next();
			if (tableName.equals(usr.getDB() + "." + usr.getTable()))
				results.add(usr);
		}

		return results;
	}

	/**
	 * Returns all tables containing Users within this container.
	 * @return a Collection of table names in DB.TABLE format
	 */
	public Collection<String> getTableNames() {
		Collection<String> results = new LinkedHashSet<String>(4);
		for (Iterator<UserData> i = values().iterator(); i.hasNext();) {
			UserData usr = i.next();
			results.add(usr.getDB() + "." + usr.getTable());
		}

		return results;
	}

	/**
	 * Returns all domains containing users within this container.
	 * @return a Collection of domain names
	 */
	public Collection<String> getDomains() {
		Collection<String> results = new LinkedHashSet<String>(4);
		for (Iterator<UserData> i = values().iterator(); i.hasNext();) {
			UserData usr = i.next();
			results.add(usr.getDomain());
		}

		return results;
	}

	/**
	 * Returns the database IDs across all databases for every user within this container.
	 * @return a Collection of database IDs
	 */
	public Collection<Integer> getAllIDs() {
		Collection<Integer> results = new HashSet<Integer>();
		for (Iterator<UserData> i = values().iterator(); i.hasNext();) {
			UserData usr = i.next();
			results.addAll(usr.getIDs());
		}

		return results;
	}

	/**
	 * Dumps the IDs to a string, like a list.
	 */
	@Override
	public String toString() {
		return keySet().toString();
	}
}