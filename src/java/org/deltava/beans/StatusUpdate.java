// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.util.Date;

/**
 * A bean to track pilot promotions and general comments.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class StatusUpdate extends DatabaseBean implements Comparable {
	
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
	
	public static final String[] TYPES = {"Comment", "Promotion", "Rank Change", "Added Rating", "Pilot Recognition",
			"Status Change", "Added Security Role", "Removed Rating", "Removed Security Role", "Promotion", "Airline Transfer",
			"Inactivity Notice", "Academy Update", "Pilot Certification", "Senior Captain"};

	private String _firstName;
	private String _lastName;
	private int _type;
	
	private int _authorID;
	private Date _createdOn;
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
		_createdOn = new Date();
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
	
	/**
	 * Returns the first (given) name of the Pilot.
	 * @return the Pilot's first name
	 * @see StatusUpdate#setFirstName(String)
	 */
	public String getFirstName() {
		return _firstName;
	}
	
	/**
	 * Returns the last (family) name of the Pilot.
	 * @return the Pilot's last name
	 * @see StatusUpdate#setLastName(String)
	 */
	public String getLastName() {
		return _lastName;
	}
	
	/**
	 * Returns the database ID of the Pilot creating this Status Update.
	 * @return the database ID
	 * @see StatusUpdate#setAuthorID(int)
	 */
	public int getAuthorID() {
		return _authorID;
	}
	
	/**
	 * Returns the creation date of this update.
	 * @return the date/time this update was created on
	 * @see StatusUpdate#setCreatedOn(Date)
	 */
	public Date getCreatedOn() {
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
	 * Sets the pilot's first (given) name.
	 * @param fName the first name
	 * @see StatusUpdate#getFirstName()
	 */
	public void setFirstName(String fName) {
		_firstName = fName; 
	}
	
	/**
	 * Sets the pilot's last (family) name.
	 * @param lName the last name
	 * @see StatusUpdate#getLastName()
	 */
	public void setLastName(String lName) {
		_lastName = lName;
	}
	
	/**
	 * Sets the date this update was created on.
	 * @param dt the date/time this update was created on
	 * @see StatusUpdate#getCreatedOn()
	 */
	public void setCreatedOn(Date dt) {
		_createdOn = dt;
	}
	
	/**
	 * Sets the database ID of the author of this Status Update.
	 * @param id the database ID
	 * @throws IllegalArgumentException if id is zero or negative
	 * @see StatusUpdate#getAuthorID()
	 */
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
	public int compareTo(Object o2) {
		StatusUpdate su2 = (StatusUpdate) o2;
		return _createdOn.compareTo(su2.getCreatedOn());
	}
}