// Copyright 2010, 2011, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.hr;

import java.time.Instant;

import org.deltava.beans.*;

/**
 * A bean to store job applicant information.
 * @author Luke
 * @version 7.0
 * @since 3.4
 */

public abstract class ApplicantData extends DatabaseBean implements AuthoredBean, ComboAlias {
	
	private String _firstName;
	private String _lastName;

	private Instant _createdOn;
	private String _body;
	
	/**
	 * Creates the Resume bean.
	 * @param authorID the JobPosting database ID 
	 */
	public ApplicantData(int authorID) {
		super();
		setID(authorID);
	}
	
	/**
	 * Returns the creation date of this profile.
	 * @return the creation date/time
	 */
	public Instant getCreatedOn() {
		return _createdOn;
	}
	
	/**
	 * Returns the profile body.
	 * @return the body
	 */
	public String getBody() {
		return _body;
	}

	/**
	 * Returns the user's first name.
	 * @return the first name
	 */
	public String getFirstName() {
		return _firstName;
	}
	
	/**
	 * Returns the user's last (family) name.
	 * @return the last name
	 */
	public String getLastName() {
		return _lastName;
	}
	
	/**
	 * Returns the user's full name.
	 * @return the first and last names
	 */
	public String getName() {
		StringBuilder buf = new StringBuilder(_firstName);
		buf.append(' ');
		buf.append(_lastName);
		return buf.toString();
	}
	
	@Override
	public int getAuthorID() {
		return getID();
	}
	
	@Override
	public String getComboName() {
		return getName();
	}
	
	@Override
	public String getComboAlias() {
		return getHexID();
	}

	@Override
	public void setAuthorID(int id) {
		setID(id);
	}
	
	/**
	 * Updates the date this profile was updated.
	 * @param dt the creation date/time
	 */
	public void setCreatedOn(Instant dt) {
		_createdOn = dt;
	}

	/**
	 * Updates the profile body.
	 * @param body the body
	 */
	public void setBody(String body) {
		_body = body;
	}

	/**
	 * Updates the user's first name.
	 * @param fName the first name
	 */
	public void setFirstName(String fName) {
		_firstName = fName;
	}

	/**
	 * Updates the user's last (family) name.
	 * @param lName the last name
	 */
	public void setLastName(String lName) {
		_lastName = lName;
	}
}