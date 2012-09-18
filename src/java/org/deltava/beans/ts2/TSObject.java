// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.ts2;

import java.util.Date;

import org.deltava.beans.DatabaseBean;

/**
 * An abstract class to hold common TeamSpeak 2 object data.
 * @author Luke
 * @version 5.0
 * @since 5.0
 */

public abstract class TSObject extends DatabaseBean {
	
	private Date _createdOn = new Date();

	/**
	 * Returns the date the object was created on.
	 * @return the creation date/time
	 * @see TSObject#setCreatedOn(Date)
	 */
	public Date getCreatedOn() {
		return _createdOn;
	}
	
	/**
	 * Updates the creation date of this Teamspeak object.
	 * @param dt the creation date/time
	 * @see TSObject#getCreatedOn()
	 */
	public void setCreatedOn(Date dt) {
		_createdOn = dt;
	}
}