// Copyright 2010, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.fb;

/**
 * A bean to store information about a user's Facebook profile.
 * @author Luke
 * @version 7.0
 * @since 3.4
 */

public class ProfileInfo extends FacebookObject {

	private String _fName;
	private String _lName;
	private String _eMail;
	
	private boolean _verified;
	
	public ProfileInfo(String id) {
		super(id);
	}
	
	/**
	 * Returns the user's first name.
	 * @return the first name
	 */
	public String getFirstName() {
		return _fName;
	}
	
	/**
	 * Returns the user's last (family) name.
	 * @return the last name
	 */
	public String getLastName() {
		return _lName;
	}
	
	/**
	 * Return the user's e-mail address.
	 * @return the address
	 */
	public String getEMail() {
		return _eMail;
	}
	
	/**
	 * Returns the user's full name.
	 * @return the first and last names
	 */
	public String getName() {
		StringBuilder buf = new StringBuilder(_fName);
		buf.append(' ');
		buf.append(_lName);
		return buf.toString();
	}
	
	/**
	 * Returns whether Facebook has verified the profile.
	 * @return TRUE if verified, otherwise FALSE
	 */
	public boolean getVerified() {
		return _verified;
	}
	
	/**
	 * Updates the user's first name.
	 * @param fName the first name
	 */
	public void setFirstName(String fName) {
		_fName = fName;
	}
	
	/**
	 * Updates the user's last name.
	 * @param lName the last name
	 */
	public void setLastName(String lName) {
		_lName = lName;
	}
	
	/**
	 * Updates the user's e-mail address.
	 * @param addr the e-mail address
	 */
	public void setEMail(String addr) {
		_eMail = addr;
	}
	
	/**
	 * Sets this Facebook profile as verified.
	 * @param isVerified TRUE if the profile is verified, otherwise FALSE
	 */
	public void setVerified(boolean isVerified) {
		_verified = isVerified;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder("FB-");
		buf.append(getID());
		buf.append('-');
		buf.append(_fName);
		buf.append(' ');
		buf.append(_lName);
		return buf.toString();
	}
}