// Copyright (c) 2004, 2005 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.security;

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
	 * @param directoryName the user's directory name
	 * @param pwd the user's supplied password
	 * @throws SecurityException if authentication fails
	 * @see Authenticator#authenticate(String, String)
	 */
	public void authenticate(String directoryName, String pwd) throws SecurityException {
		_src.authenticate(directoryName, pwd);
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
	 * @param directoryName the user's directory name
	 * @param pwd the user's new password
	 * @throws SecurityException if either update operation fails
	 * @see Authenticator#updatePassword(String, String)
	 */
	public void updatePassword(String directoryName, String pwd) throws SecurityException {
		_src.updatePassword(directoryName, pwd);
		_dst.updatePassword(directoryName, pwd);
	}

	/**
	 * Adds the user to both authenticators. If this operation fails, no guarantee of transaction
	 * atomicity is given.
	 * @param directoryName the user's directory name
	 * @param pwd the user's password
	 * @throws SecurityException if either add operation fails
	 * @see Authenticator#addUser(String, String)
	 */
	public void addUser(String directoryName, String pwd) throws SecurityException {
		_src.addUser(directoryName, pwd);
		_dst.addUser(directoryName, pwd);
	}

	/**
	 *  Adds the user to both authenticators.
	 * @param directoryName the user's directory name
	 * @param pwd the user's password
	 * @param userID an alias for the user
	 * @see org.deltava.security.Authenticator#addUser(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void addUser(String directoryName, String pwd, String userID) throws SecurityException {
		_src.addUser(directoryName, pwd, userID);
		_dst.addUser(directoryName, pwd, userID);
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