// Copyright 2005, 2006, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.util.*;

import org.deltava.util.CollectionUtils;

/**
 * A class to support a map of {@link UserData} beans. This class implements Map to allow it to be accessed directly via
 * JSP Expression Language.
 * @author Luke
 * @version 2.5
 * @since 1.0
 */

public class UserDataMap implements Map<Integer, UserData> {

	private final Map<Integer, UserData> _entries = new HashMap<Integer, UserData>();

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
		putAll(CollectionUtils.createMap(data, "ID"));
	}

	/**
	 * Adds an entry to the map.
	 * @param id the object key (is discared)
	 * @param usr the UserData object
	 * @return the entry
	 */
	public UserData put(Integer id, UserData usr) {
		return _entries.put(new Integer(usr.getID()), usr);
	}

	/**
	 * Adds a collection of UserData objects to the container.
	 * @param data a Collection of UserData objects
	 */
	@SuppressWarnings("unchecked")
	public void putAll(Map data) {
		_entries.putAll(data);
	}

	/**
	 * Returns if the collection contains an entry for a particular user.
	 * @param id the User's database ID
	 * @return TRUE if data for the User is present, otherwise FALSE
	 */
	public boolean contains(int id) {
		return _entries.containsKey(new Integer(id));
	}

	/**
	 * Returns an entry for a particular user.
	 * @param id the User's database ID
	 * @return the UserData bean, or null if not found
	 * @see UserDataMap#get(int)
	 */
	public UserData get(Object id) {
		return _entries.get(id);
	}

	/**
	 * Returns an entry for a particular user.
	 * @param id the User's database ID
	 * @return the UserData bean, or null if not found
	 * @see UserDataMap#get(Object)
	 */
	public UserData get(int id) {
		return _entries.get(new Integer(id));
	}

	/**
	 * Returns all entries present within a particular table.
	 * @param tableName the database table name
	 * @return a Collection of UserData objects
	 * @throws NullPointerException if tableName is null
	 */
	public Collection<UserData> getByTable(String tableName) {
		Collection<UserData> results = new HashSet<UserData>();
		for (Iterator<UserData> i = _entries.values().iterator(); i.hasNext();) {
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
		for (Iterator<UserData> i = _entries.values().iterator(); i.hasNext();) {
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
		for (Iterator<UserData> i = _entries.values().iterator(); i.hasNext();) {
			UserData usr = i.next();
			results.add(usr.getDomain());
		}

		return results;
	}

	/**
	 * Returns the database IDs across all databases for every user within this container.
	 * @return a Collection of database IDs
	 * @see UserDataMap#getIDs()
	 */
	public Collection<Integer> getAllIDs() {
		Collection<Integer> results = new HashSet<Integer>(_entries.size());
		for (Iterator<UserData> i = _entries.values().iterator(); i.hasNext();) {
			UserData usr = i.next();
			results.addAll(usr.getIDs());
		}

		return results;
	}

	/**
	 * Returns the size of the collection.
	 * @return the number of entries
	 */
	public int size() {
		return _entries.size();
	}

	/**
	 * Removes an entry from the Map.
	 * @return the removed UserData entry
	 */
	public UserData remove(Object obj) {
		return _entries.remove(obj);
	}

	/**
	 * Checks if the Map is empty.
	 * @return TRUE if the map is empty, otherwise FALSE
	 */
	public boolean isEmpty() {
		return _entries.isEmpty();
	}

	public Collection<UserData> values() {
		return new LinkedHashSet<UserData>(_entries.values());
	}

	/**
	 * Clears the map.
	 */
	public void clear() {
		_entries.clear();
	}

	/**
	 * <i>NOT IMPLEMENTED</i>
	 * @throws UnsupportedOperationException always
	 */
	public boolean containsKey(Object obj) {
		throw new UnsupportedOperationException();
	}

	/**
	 * <i>NOT IMPLEMENTED</i>
	 * @throws UnsupportedOperationException always
	 */
	public boolean containsValue(Object obj) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns the map's entries.
	 * @return a Set of MapEntry objects
	 */
	public Set<Map.Entry<Integer, UserData>> entrySet() {
		return _entries.entrySet();
	}

	/**
	 * Returns the database IDs contained within the map.
	 * @return a Collection of Integers
	 * @see UserDataMap#keySet()
	 * @see UserDataMap#getAllIDs()
	 */
	public Collection<Integer> getIDs() {
		return _entries.keySet();
	}

	/**
	 * Returns the database IDs contained within the map.
	 * @return a Set of Integers
	 * @see UserDataMap#getIDs()
	 */
	public Set<Integer> keySet() {
		return _entries.keySet();
	}

	/**
	 * Dumps the IDs to a string, like a list.
	 */
	public String toString() {
		return _entries.keySet().toString();
	}
}