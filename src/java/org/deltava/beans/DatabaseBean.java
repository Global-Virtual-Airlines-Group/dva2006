// Copyright 2004, 2005, 2006, 2007, 2009, 2010, 2012, 2016, 2017, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import org.deltava.util.StringUtils;
import org.deltava.util.cache.Cacheable;

/**
 * A common abstract class for beans stored in the database with a numeric primary key.
 * @author Luke
 * @version 10.2
 * @since 1.0
 */

public abstract class DatabaseBean implements IDBean, Cacheable, Comparable<Object> {

	private int _id;

	@Override
	public int getID() {
		return _id;
	}

	@Override
	public String getHexID() {
		return (_id == 0) ? "" : StringUtils.formatHex(_id);
	}

	/**
	 * Update the database row ID of this bean. <i>This typically will only be called by a DAO</i>
	 * @param id The primary key of the entry in the database that corresponds to this object.
	 * @throws IllegalArgumentException if the database ID is negative
	 * @throws IllegalStateException if we are attempting to change the database ID
	 * @see DatabaseBean#validateID(int, int)
	 */
	public void setID(int id) {
		validateID(_id, id);
		_id = id;
	}

	/**
	 * Validates a database ID. Used to enforce database ID behavior - that the ID cannot be zero or negative, and it cannot be updated once set.
	 * @param oldID the old database ID
	 * @param newID the new database ID
	 * @throws IllegalArgumentException if the database ID is negative
	 * @throws IllegalStateException if we are attempting to change the database ID
	 */
	public static void validateID(int oldID, int newID) throws IllegalArgumentException, IllegalStateException {
		if (newID < 1)
			throw new IllegalArgumentException("Database ID cannot be zero or negative");

		if ((oldID != 0) && (oldID != newID))
			throw new IllegalStateException("Cannot change Datbase ID from " + oldID + " to " + newID);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		return (o instanceof DatabaseBean) && (compareTo(o) == 0) && (getClass() == o.getClass());
	}

	@Override
	public int compareTo(Object o) {
		return Integer.compare(_id, ((IDBean) o).getID());
	}

	@Override
	public Object cacheKey() {
		return Integer.valueOf(getID());
	}

	@Override
	public int hashCode() {
		return _id;
	}
}