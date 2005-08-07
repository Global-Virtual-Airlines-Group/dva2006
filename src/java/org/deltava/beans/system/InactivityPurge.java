// Copyright (c) 2005 Delta Virtual Airlines. All Rights Reserved.
package org.deltava.beans.system;

import java.util.Date;

import org.deltava.beans.DatabaseBean;

/**
 * A system bean to store User Inactivity Purge data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class InactivityPurge extends DatabaseBean implements Comparable {

	private Date _purgeDate;
	private boolean _notified;
	
	/**
	 * Creates a new Inactivity Purge entry.
	 * @param id the Pilot's database ID
	 */
	public InactivityPurge(int id) {
		super();
		setID(id);
	}

	/**
	 * Returns the date the user will be marked Inactive.
	 * @return the date/time
	 * @see InactivityPurge#setPurgeDate(Date)
	 */
	public Date getPurgeDate() {
		return _purgeDate;
	}

	/**
	 * Returns wether the Pilot has been marked of impending inactivity.
	 * @return TRUE if a notification message was sent, otherwise FALSE
	 * @see InactivityPurge#setNotify(boolean)
	 */
	public boolean isNotified() {
		return _notified;
	}
	
	/**
	 * Marks the Pilot as being notified of the impending purge.
	 * @param notified TRUE if the Pilot was notified, otherwise FALS
	 * @see InactivityPurge#isNotified()
	 */
	public void setNotify(boolean notified) {
		_notified = notified;
	}
	
	/**
	 * Updates the date the Pilot will be marked Inactive.
	 * @param dt the date/time the Pilot will be marked inactive
	 * @see InactivityPurge#getPurgeDate()
	 */
	public void setPurgeDate(Date dt) {
		_purgeDate = dt;
	}
	
	/**
	 * Compares two InactivityPurge beans by comparing their Purge Dates.
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(Object o2) {
		InactivityPurge ip2 = (InactivityPurge) o2;
		return _purgeDate.compareTo(ip2.getPurgeDate());
	}
}