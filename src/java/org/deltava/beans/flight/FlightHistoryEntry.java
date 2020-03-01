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

	private final HistoryType _type;
	private final Instant _createdOn;
	private int _authorID;
	private String _desc;
	
	/**
	 * Creates the bean.
	 * @param id the FlightReport database ID
	 * @param t the HistoryType
	 * @param authorID the author's database ID 
	 * @param createdOn the creation date/time
	 * @param desc the update message
	 */
	public FlightHistoryEntry(int id, HistoryType t, int authorID, Instant createdOn, String desc) {
		if (id > 0) setID(id);
		_createdOn = createdOn;
		_type = t;
		_authorID = authorID;
		_desc = desc;
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
	
	/**
	 * Returns the type of status update.
	 * @return the HistoryType
	 */
	public HistoryType getType() {
		return _type;
	}

	@Override
	public int getAuthorID() {
		return _authorID;
	}

	@Override
	public void setAuthorID(int id) {
		_authorID = id;
	}
	
	@Override
	public int compareTo(Object o) {
		FlightHistoryEntry fe2 = (FlightHistoryEntry) o;
		int tmpResult = _createdOn.compareTo(fe2._createdOn);
		return (tmpResult == 0) ? Integer.compare(getID(), fe2.getID()) : tmpResult;
	}
}