// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security;

/**
 * A bean containing data stored in the User Token cookie. 
 * @author Luke
 * @version 12.4
 * @since 12.4
 */

public class UserCookieData extends SecurityCookieData {
	
	private final String _firstName;
	private final String _lastName;

	/**
	 * Creates the bean.
	 * @param userID the user ID
	 * @param fName the user's given name
	 * @param lName the user's family name
	 */
	public UserCookieData(String userID, String fName, String lName) {
		super(userID);
		_firstName = fName;
		_lastName = lName;
	}

	/**
	 * Returns the user's given name.
	 * @return the name
	 */
	public String getFirstName() {
		return _firstName;
	}
	
	/**
	 * Returns the user's family name.
	 * @return the name
	 */
	public String getLastName() {
		return _lastName;
	}
}