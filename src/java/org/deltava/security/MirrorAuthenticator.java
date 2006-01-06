// Copyright (c) 2004, 2005, 2006 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.security;

import org.deltava.beans.Person;

/**
 * An Authenticator used to mirror data from one authenticator to another. When a user is sucessfully authenticated by
 * the first (&quot;source&quot;) authenticator, the directory name and password are written into the second
 * (&quot;destination&quot;) authenticator.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class MirrorAuthenticator extends MultiAuthenticator {

	/**
	 * Initializes the Authenticator.
	 */
	public MirrorAuthenticator() {
		super(MirrorAuthenticator.class);
	}

	/**
	 * Authenticates the user against the source authenticator.
	 * @param usr the User bean
	 * @param pwd the user's supplied password
	 * @throws SecurityException if authentication fails
	 * @see Authenticator#authenticate(Person, String)
	 */
	public void authenticate(Person usr, String pwd) throws SecurityException {
		_src.authenticate(usr, pwd);
	}

	/**
	 * Returns if the source <i>and</i> destination authenticators contain a particular directory name.
	 * @param directoryName the directory name
	 * @return TRUE if the source authenticator contains
	 * @see org.deltava.security.Authenticator#contains(java.lang.String)
	 */
	public boolean contains(String directoryName) throws SecurityException {
		return _src.contains(directoryName) && _dst.contains(directoryName);
	}

	/**
	 * Updates the user's password in both authenticators. If this operation fails, no guarantee of transaction
	 * atomicity is given.
	 * @param usr the user bean
	 * @param pwd the user's new password
	 * @throws SecurityException if either update operation fails
	 * @see Authenticator#updatePassword(Person, String)
	 */
	public void updatePassword(Person usr, String pwd) throws SecurityException {
		_src.updatePassword(usr, pwd);
		_dst.updatePassword(usr, pwd);
	}

	/**
	 * Adds the user to both authenticators. If this operation fails, no guarantee of transaction
	 * atomicity is given.
	 * @param usr the User bean
	 * @param pwd the user's password
	 * @throws SecurityException if either add operation fails
	 * @see Authenticator#addUser(Person, String)
	 */
	public void addUser(Person usr, String pwd) throws SecurityException {
		_src.addUser(usr, pwd);
		_dst.addUser(usr, pwd);
	}

	/* (non-Javadoc)
	 * @see org.deltava.security.Authenticator#rename(java.lang.String, java.lang.String)
	 */
	public void rename(String oldName, String newName) throws SecurityException {
		_src.rename(oldName, newName);
		_dst.rename(oldName, newName);
	}

	/**
	 * Removes the user from both authenticators.
	 * @see Authenticator#removeUser(String)
	 */
	public void removeUser(String directoryName) throws SecurityException {
		if (_src.contains(directoryName))
			_src.removeUser(directoryName);
		
		if (_dst.contains(directoryName))
			_dst.removeUser(directoryName);
	}
}