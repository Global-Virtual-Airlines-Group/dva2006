// Copyright 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.ts2;

import java.time.Instant;

import org.deltava.beans.DatabaseBean;

/**
 * An abstract class to hold common TeamSpeak 2 object data.
 * @author Luke
 * @version 7.0
 * @since 5.0
 */

public abstract class TSObject extends DatabaseBean {
	
	private Instant _createdOn = Instant.now();

	/**
	 * Returns the date the object was created on.
	 * @return the creation date/time
	 * @see TSObject#setCreatedOn(Instant)
	 */
	public Instant getCreatedOn() {
		return _createdOn;
	}
	
	/**
	 * Updates the creation date of this Teamspeak object.
	 * @param dt the creation date/time
	 * @see TSObject#getCreatedOn()
	 */
	public void setCreatedOn(Instant dt) {
		_createdOn = dt;
	}
}