// Copyright 2004, 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security;

import java.util.*;

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
	 * Creates the Authenticator.
	 */
	public MirrorAuthenticator() {
		super(MirrorAuthenticator.class);
	}
	
	/**
	 * Initializes the Authenticator.
	 * @param propsFile the name of the proeprties file to load
	 * @throws SecurityException if an error occurs
	 */
	public void init(String propsFile) throws SecurityException {
		init(propsFile, "mirror");
	}

	/**
	 * Authenticates the user against the source authenticator. This then synchronizes credentials
	 * information with the destination authenticator.
	 * @param usr the User bean
	 * @param pwd the user's supplied password
	 * @throws SecurityException if authentication fails
	 * @see Authenticator#authenticate(Person, String)
	 * @see MultiAuthenticator#sync(Person, String)
	 */
	public void authenticate(Person usr, String pwd) throws SecurityException {
		_src.authenticate(usr, pwd);
		sync(usr, pwd);
	}

	/**
	 * Returns if the source <i>and</i> destination authenticators contain a particular directory name.
	 * @param usr the user bean
	 * @return TRUE if the source authenticator contains the user, otherwise FALSE
	 * @see org.deltava.security.Authenticator#contains(Person)
	 */
	public boolean contains(Person usr) throws SecurityException {
		return _src.contains(usr);
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
		setConnection(_src);
		_src.updatePassword(usr, pwd);
		clearConnection(_src);
		for (Iterator<Authenticator> i = _dst.iterator(); i.hasNext(); ) {
			Authenticator dst = i.next();
			setConnection(dst);
			dst.updatePassword(usr, pwd);
			clearConnection(dst);
		}
	}

	/**
	 * Adds the user to all authenticators. If this operation fails, no guarantee of transaction
	 * atomicity is given.
	 * @param usr the User bean
	 * @param pwd the user's password
	 * @throws SecurityException if either add operation fails
	 * @see Authenticator#addUser(Person, String)
	 */
	public void addUser(Person usr, String pwd) throws SecurityException {
		setConnection(_src);
		_src.addUser(usr, pwd);
		clearConnection(_src);
		for (Iterator<Authenticator> i = _dst.iterator(); i.hasNext(); ) {
			Authenticator dst = i.next();
			setConnection(dst);
			dst.addUser(usr, pwd);
			clearConnection(dst);
		}
	}

	/**
	 * Renames the user in all authenticators. If this operation fails, no guarantee of transaction
	 * atomicity is given.
	 * @param usr the user bean
	 * @param newName the new directory name
	 * @see org.deltava.security.Authenticator#rename(Person, java.lang.String)
	 */
	public void rename(Person usr, String newName) throws SecurityException {
		setConnection(_src);
		_src.rename(usr, newName);
		clearConnection(_src);
		for (Iterator<Authenticator> i = _dst.iterator(); i.hasNext(); ) {
			Authenticator dst = i.next();
			setConnection(dst);
			dst.rename(usr, newName);
			clearConnection(dst);
		}
	}

	/**
	 * Removes the user from all  authenticators. If this operation fails, no guarantee of transaction
	 * atomicity is given.
	 * @param usr the user bean
	 * @see Authenticator#removeUser(Person)
	 */
	public void removeUser(Person usr) throws SecurityException {
		if (_src.contains(usr)) {
			setConnection(_src);
			_src.removeUser(usr);
			clearConnection(_src);
		}
		
		// Remove from destinations
		for (Iterator<Authenticator> i = _dst.iterator(); i.hasNext(); ) {
			Authenticator dst = i.next();
			setConnection(dst);
			if (dst.contains(usr))
				dst.removeUser(usr);
			
			clearConnection(dst);
		}
	}
}