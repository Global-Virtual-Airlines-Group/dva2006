// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2106 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.time.Instant;

/**
 * A bean to track pilot promotions and general comments.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class StatusUpdate extends DatabaseBean implements AuthoredBean {
	
	public static final int COMMENT = 0;
	public static final int INTPROMOTION = 1;
	public static final int RANK_CHANGE = 2;
	public static final int RATING_ADD = 3;
	public static final int RECOGNITION = 4;
	public static final int STATUS_CHANGE = 5;
	public static final int SECURITY_ADD = 6;
	public static final int RATING_REMOVE = 7;
	public static final int SECURITY_REMOVE = 8;
	public static final int EXTPROMOTION = 9;
	public static final int AIRLINE_TX = 10;
	public static final int INACTIVITY = 11;
	public static final int ACADEMY = 12;
	public static final int CERT_ADD = 13;
	public static final int SR_CAPTAIN = 14;
	public static final int SUSPENDED = 15;
	public static final int LOA = 16;
	public static final int EXT_AUTH = 17;
	public static final int VOICE_WARN = 18;
	
	public static final String[] TYPES = {"Comment", "Promotion", "Rank Change", "Added Rating", "Pilot Recognition",
			"Status Change", "Added Security Role", "Removed Rating", "Removed Security Role", "Promotion", "Airline Transfer",
			"Inactivity Notice", "Academy Update", "Pilot Certification", "Senior Captain", "Account Suspended", "Leave of Absence",
			"External Authentication", "Voice Warning"};

	private int _type;
	private int _authorID;
	private Instant _createdOn = Instant.now();
	private String _desc;
	
	/**
	 * Creates a new Status Update for a Pilot.
	 * @param pilotID the Pilot Database ID
	 * @param type the update type
	 */
	public StatusUpdate(int pilotID, int type) {
		super();
		setID(pilotID);
		setType(type);
	}
	
	/**
	 * Returns the status type.
	 * @return the type code
	 * @see StatusUpdate#setType(int)
	 * @see StatusUpdate#getTypeName()
	 */
	public int getType() {
		return _type;
	}
	
	/**
	 * Retruns the status type name.
	 * @return the type name
	 * @see StatusUpdate#setType(int)
	 * @see StatusUpdate#getType()
	 */
	public String getTypeName() {
		return TYPES[getType()];
	}
	
	@Override
	public int getAuthorID() {
		return _authorID;
	}
	
	/**
	 * Returns the creation date of this update.
	 * @return the date/time this update was created on
	 * @see StatusUpdate#setCreatedOn(Instant)
	 */
	public Instant getCreatedOn() {
		return _createdOn;
	}
	
	/**
	 * Returns a text description of this status updatae, or the comments.
	 * @return the description or comments
	 * @see StatusUpdate#setDescription(String)
	 */
	public String getDescription() {
		return _desc;
	}
	
	/**
	 * Sets this status update's type.
	 * @param type the type code
	 * @throws IllegalArgumentException if type is negative or invalid
	 * @see StatusUpdate#getType()
	 * @see StatusUpdate#getTypeName()
	 */
	public void setType(int type) {
		if ((type < 0) || (type >= TYPES.length))
			throw new IllegalArgumentException("Invalid Type code - " + type);
		
		_type = type;
	}
	
	/**
	 * Sets the date this update was created on.
	 * @param dt the date/time this update was created on
	 * @see StatusUpdate#getCreatedOn()
	 */
	public void setCreatedOn(Instant dt) {
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
		return _createdOn.compareTo(su2.getCreatedOn());
	}
}