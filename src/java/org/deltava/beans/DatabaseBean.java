// Copyright 2004, 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import org.deltava.util.cache.Cacheable;

/**
 * A common abstract class for beans stored in the database with a numeric primary key.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class DatabaseBean implements java.io.Serializable, Cacheable, Comparable {

    private int _id;
    
 	/**
 	 * Return the database row ID of this bean. <i>This typically will only be called by a DAO</i>
 	 * @return The primary key of the entry in the table in the database that corresponds to this object.
 	 */
    public int getID() {
        return _id;
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
     * Validates a database ID. Used to enforce database ID behavior - that the ID cannot be zero or
     * negative, and it cannot be updated once set.
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
    
    /**
     * Tests for equality by comparing the class and database ID.
     * @param o the object to compare with
     * @return TRUE if the objects have the same class and database ID
     */
    public boolean equals(Object o) {
    	DatabaseBean db2 = (DatabaseBean) o;
        return (compareTo(o) == 0) && (getClass() == db2.getClass()); 
    }
    
    /**
     * Compares two database beans by comparing their IDs.
     * @see Comparable#compareTo(Object)
     */
    public int compareTo(Object o) {
    	DatabaseBean db2 = (DatabaseBean) o;
    	return new Integer(_id).compareTo(new Integer(db2._id));
    }
    
    /**
     * Returns the cache key for use in Form/DAO caches.
     * @return the cache key
     */
    public Object cacheKey() {
        return new Integer(getID());
    }
    
    /**
     * Returns the hash code of the database ID.
     */
    public int hashCode() {
    	return cacheKey().hashCode();
    }
}