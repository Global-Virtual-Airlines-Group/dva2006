// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.hr;

/**
 * A bean to store job applicant information.
 * @author Luke
 * @version 3.4
 * @since 3.4
 */

public class Profile extends ApplicantData {
	
	private boolean _autoReuse;
	
	/**
	 * Creates a generic profile from a Job posting Application.
	 * @param a the Application bean
	 */
	public Profile(Application a) {
		super(a.getAuthorID());
		setCreatedOn(a.getCreatedOn());
		setBody(a.getBody());
		setFirstName(a.getFirstName());
		setLastName(a.getLastName());
	}
	
	/**
	 * Creates the Resume bean.
	 * @param authorID the author's database ID 
	 */
	public Profile(int authorID) {
		super(authorID);
	}
	
	/**
	 * Returns whether this profile should be automatically submitted for future opportunities.
	 * @return TRUE if automatically submitted, otherwise FALSE
	 */
	public boolean getAutoReuse() {
		return _autoReuse;
	}
	
	@Override
	public int getAuthorID() {
		return getID();
	}
	
	@Override
	public void setAuthorID(int id) {
		setID(id);
	}
	
	/**
	 * Updates whether this profile should be automatically submitted for future opportunities.
	 * @param autoReuse TRUE if automatically submitted, otherwise FALSE
	 */
	public void setAutoReuse(boolean autoReuse) {
		_autoReuse = autoReuse;
	}
}