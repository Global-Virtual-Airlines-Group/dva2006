// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2106, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.time.Instant;

/**
 * A bean to track Pilot status updates and comments.
 * @author Luke
 * @version 8.7
 * @since 1.0
 */

public class StatusUpdate extends DatabaseBean implements AuditEntry {
	
	private final UpdateType _type;
	private int _authorID;
	private Instant _createdOn = Instant.now();
	private String _desc;
	
	/**
	 * Creates a new Status Update for a Pilot.
	 * @param pilotID the Pilot Database ID
	 * @param type the UpdateType
	 */
	public StatusUpdate(int pilotID, UpdateType type) {
		super();
		setID(pilotID);
		_type = type;
	}
	
	/**
	 * Returns the status type.
	 * @return the type code
	 */
	public UpdateType getType() {
		return _type;
	}
	
	@Override
	public int getAuthorID() {
		return _authorID;
	}
	
	@Override
	public Instant getDate() {
		return _createdOn;
	}
	
	@Override
	public String getDescription() {
		return _desc;
	}
	
	@Override
	public String getAuditType() {
		return "Pilot";
	}
	
	@Override
	public String getAuditID() {
		return getHexID();
	}
	
	/**
	 * Sets the date this update was created on.
	 * @param dt the date/time this update was created on
	 * @see StatusUpdate#getDate()
	 */
	public void setDate(Instant dt) {
		_createdOn = dt;
	}
	
	@Override
	public void setAuthorID(int id) {
		validateID(_authorID, id);
		_authorID = id;
	}
	
	/**
	 * Sets the description or comments for this status update.
	 * @param desc the description/comments
	 * @see StatusUpdate#getDescription()
	 */
	public void setDescription(String desc) {
		_desc = desc;
	}
	
	/**
	 * Compares this to another status update by comparing the creation date/times.
	 * @see Comparable#compareTo(Object)
	 */
	@Override
	public int compareTo(Object o2) {
		StatusUpdate su2 = (StatusUpdate) o2;
		return _createdOn.compareTo(su2._createdOn);
	}
}