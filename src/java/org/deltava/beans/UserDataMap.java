// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2016, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A class to support a map of {@link UserData} beans. This class implements Map to allow it to be accessed directly via JSP Expression Language.
 * @author Luke
 * @version 9.0
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
		data.forEach(ud -> put(Integer.valueOf(ud.getID()), ud));
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
		return values().stream().filter(usr -> tableName.equals(usr.getDBTable())).collect(Collectors.toSet());
	}

	/**
	 * Returns all tables containing Users within this container.
	 * @return a Collection of table names in DB.TABLE format
	 */
	public Collection<String> getTableNames() {
		return values().stream().map(UserData::getDBTable).collect(Collectors.toSet());
	}

	/**
	 * Returns all domains containing users within this container.
	 * @return a Collection of domain names
	 */
	public Collection<String> getDomains() {
		return values().stream().map(UserData::getDomain).collect(Collectors.toSet());
	}

	/**
	 * Returns the database IDs across all databases for every user within this container.
	 * @return a Collection of database IDs
	 */
	public Collection<Integer> getAllIDs() {
		return values().stream().flatMap(ud -> ud.getIDs().stream()).collect(Collectors.toSet());
	}

	@Override
	public String toString() {
		return keySet().toString();
	}
}