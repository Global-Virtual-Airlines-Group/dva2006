// Copyright 2010, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.fb;

import java.time.Instant;

/**
 * An abstract class to store common Facebook object properties. 
 * @author Luke
 * @version 7.0
 * @since 3.4
 */

public abstract class FacebookObject implements java.io.Serializable {
	
	private String _id;
	private Instant _updatedOn;
	
	/**
	 * Creates a new Facebook object.
	 * @param id the ID
	 */
	protected FacebookObject(String id) {
		super();
		_id = id;
	}
	
	/**
	 * Returns the object's Facebook ID.
	 * @return the ID
	 */
	public String getID() {
		return _id;
	}
	
	/**
	 * Returns the date the Facebook object was last updated.
	 * @return the last update date/time
	 */
	public Instant getLastUpdated() {
		return _updatedOn;
	}
	
	/**
	 * Updates the last update time of this Facebook object.
	 * @param dt the date/time the profile was last updated
	 */
	public void setLastUpdated(Instant dt) {
		_updatedOn = dt;
	}
	
	/**
	 * Updates the object's Facebook ID.
	 * @param id the ID
	 */
	public void setID(String id) {
		if (_id != null)
			throw new IllegalStateException("ID is not null");
		
		_id = id;
	}
	
	@Override
	public int hashCode() {
		return _id.hashCode();
	}
}