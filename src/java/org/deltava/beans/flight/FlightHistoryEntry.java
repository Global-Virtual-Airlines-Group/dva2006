// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import java.time.Instant;

import org.deltava.beans.*;

/**
 *  A bean to store Flight Report status updates.
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public class FlightHistoryEntry extends DatabaseBean implements AuthoredBean {

	private final Instant _createdOn;
	private int _authorID;
	private String _desc;
	
	/**
	 * Creates the bean.
	 * @param id the FlightReport database ID
	 * @param createdOn the creation date/time
	 */
	public FlightHistoryEntry(int id, Instant createdOn) {
		setID(id);
		_createdOn = createdOn;
	}
	
	/**
	 * Returns the creation date of this status update.
	 * @return the creation date/time
	 */
	public Instant getCreatedOn() {
		return _createdOn;
	}
	
	/**
	 * Returns the description of this status update.
	 * @return the description
	 */
	public String getDescription() {
		return _desc;
	}

	@Override
	public int getAuthorID() {
		return _authorID;
	}

	@Override
	public void setAuthorID(int id) {
		validateID(_authorID, id);
		_authorID = id;
	}

	/**
	 * Updates the status entry description.
	 * @param desc the description
	 */
	public void setDescription(String desc) {
		_desc = desc;
	}
	
	@Override
	public int compareTo(Object o) {
		FlightHistoryEntry fe2 = (FlightHistoryEntry) o;
		int tmpResult = _createdOn.compareTo(fe2._createdOn);
		return (tmpResult == 0) ? Integer.compare(getID(), fe2.getID()) : tmpResult;
	}
}