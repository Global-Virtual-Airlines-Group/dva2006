// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.wsdl;

/**
 * An abstract class to describe WSDL Data Access Objects. 
 * @author Luke
 * @version 2.2
 * @since 2.2
 */

abstract class DAO {
	
	protected String _userID;
	protected String _password;

	/**
	 * Sets the User ID to use.
	 * @param usr the user ID
	 */
	public final void setUser(String usr) {
		_userID = usr;
	}
	
	/**
	 * Sets the password to use.
	 * @param password the password
	 */
	public final void setPassword(String password) {
		_password = password;
	}
}