// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

import java.util.Date;

import org.deltava.beans.DatabaseBean;

/**
 * A system bean to store User Inactivity Purge data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class InactivityPurge extends DatabaseBean {

	private Date _purgeDate;
	private int _interval;
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
	 * Returns the number of days this entry should be retained for. This should not be confused with the purge date
	 * property, since it is not related. It is designed to give history information when purging a user and is not used
	 * in any programmatic way.
	 * @return the number of days before the user is purged
	 * @see InactivityPurge#setInterval(int)
	 */
	public int getInterval() {
		return _interval;
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
	 * Updates the purge interval.
	 * @param days the number of days this entry will remain active
	 * @see InactivityPurge#getInterval()
	 */
	public void setInterval(int days) {
		_interval = (days < 1) ? 1 : days;
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
		int tmpResult = _purgeDate.compareTo(ip2.getPurgeDate());
		return (tmpResult == 0) ? super.compareTo(o2) : tmpResult;
	}
}