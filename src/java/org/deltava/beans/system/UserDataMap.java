// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.system;

import java.util.*;

import org.deltava.util.CollectionUtils;

/**
 * A class to support a map of {@link UserData} beans. This class implements Map to allow it to be accessed
 * directly via JSP Expression Language.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class UserDataMap implements java.io.Serializable, Map {

	private Map<Integer, UserData> _entries;

	/**
	 * Creates a new, empty UserDataCollection.
	 */
	public UserDataMap() {
		super();
		_entries = new HashMap<Integer, UserData>();
	}

	/**
	 * Creates a new UserDataCollection from a set of Integers containing database IDs.
	 * @param data a Collection of UserData objects
	 * @see UserDataMap#putAll(Map)
	 */
	public UserDataMap(Collection<UserData> data) {
		this();
		putAll(CollectionUtils.createMap(data, "ID"));
	}

	/**
	 * Adds an entry to the map.
	 * @param obj the object key (is discared)
	 * @param usr the UserData object
	 * @return the entry
	 */
	public Object put(Object obj, Object usr) {
		return _entries.put(new Integer(((UserData) usr).getID()), (UserData) usr);
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
	public Object get(Object id) {
		return _entries.get(id);
	}
	
	/**
	 * Returns an entry for a particular user.
	 * @param id the User's database ID
	 * @return the UserData bean, or null if not found
	 * @see UserDataMap#get(Object)
	 */
	public Object get(int id) {
		return _entries.get(new Integer(id));
	}
	
	/**
	 * Returns all entries present within a particular table.
	 * @param tableName the database table name
	 * @return a Collection of UserData objects
	 * @throws NullPointerException if tableName is null
	 */
	public Collection<UserData> getByTable(String tableName) {

		Set<UserData> results = new HashSet<UserData>();
		for (Iterator i = _entries.values().iterator(); i.hasNext();) {
			UserData usr = (UserData) i.next();
			String usrTable = usr.getDB() + "." + usr.getTable();
			if (tableName.equals(usrTable))
				results.add(usr);
		}

		return results;
	}

	/**
	 * Returns all tables containing Users within this container.
	 * @return a Collection of table names in DB.TABLE format
	 */
	public Collection<String> getTableNames() {

		Set<String> results = new HashSet<String>();
		for (Iterator i = _entries.values().iterator(); i.hasNext();) {
			UserData usr = (UserData) i.next();
			results.add(usr.getDB() + "." + usr.getTable());
		}

		return results;
	}
	
	/**
	 * Utility method to query wether a table is a Pilot or Applicant table.
	 * @param tableName the table name, in either TABLE or DB.TABLE format
	 * @return TRUE if the table should be queried by a Pilot DAO, otherwise FALSE
	 */
	public static boolean isPilotTable(String tableName) {
	   if ((tableName != null) && (tableName.indexOf('.') != -1))
	      tableName = tableName.substring(tableName.indexOf('.') + 1);
	   
	   return "PILOTS".equals(tableName);
	}

	/**
	 * Returns the size of the collection.
	 * @return the number of entries
	 */
	public int size() {
		return _entries.size();
	}
	
	/**
	 * Removes an entry from the Map. <i>NOT IMPLEMENTED</i>
	 * @throws UnsupportedOperationException always 
	 */
	public Object remove(Object obj) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Checks if the Map is empty.
	 * @return TRUE if the map is empty, otherwise FALSE
	 */
	public boolean isEmpty() {
		return _entries.isEmpty();
	}

	public Collection<UserData> values() {
		return new HashSet<UserData>(_entries.values());
	}
	
	/**
	 * <i>NOT IMPLEMENTED</i>
	 * @throws UnsupportedOperationException always
	 */
	public void clear() {
		throw new UnsupportedOperationException();
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
	 * <i>NOT IMPLEMENTED</i>
	 * @throws UnsupportedOperationException always
	 */
	public Set entrySet() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * <i>NOT IMPLEMENTED</i>
	 * @throws UnsupportedOperationException always
	 */
	public Set keySet() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Dumps the IDs to a string, like a list.
	 */
	public String toString() {
	   return _entries.keySet().toString();
	}
}