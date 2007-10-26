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
	 * Authenticates the user against the source authenticator. This then synchronizes credentials information with the
	 * destination authenticator.
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
	 * Updates the user's password in all authenticators. If this operation fails, no guarantee of transaction atomicity
	 * is given.
	 * @param usr the user bean
	 * @param pwd the user's new password
	 * @throws SecurityException if either update operation fails
	 * @see Authenticator#updatePassword(Person, String)
	 */
	public void updatePassword(Person usr, String pwd) throws SecurityException {
		setConnection(_src);
		_src.updatePassword(usr, pwd);
		clearConnection(_src);
		for (Iterator<Authenticator> i = _dst.iterator(); i.hasNext();) {
			Authenticator dst = i.next();
			setConnection(dst);
			if (dst.accepts(usr)) {
				if (dst.contains(usr))
					dst.updatePassword(usr, pwd);
				else
					dst.add(usr, pwd);
			}
			
			clearConnection(dst);
		}
	}

	/**
	 * Adds the user to all authenticators. If this operation fails, no guarantee of transaction atomicity is given.
	 * @param usr the User bean
	 * @param pwd the user's password
	 * @throws SecurityException if either add operation fails
	 * @see Authenticator#add(Person, String)
	 */
	public void add(Person usr, String pwd) throws SecurityException {
		setConnection(_src);
		_src.add(usr, pwd);
		clearConnection(_src);
		for (Iterator<Authenticator> i = _dst.iterator(); i.hasNext();) {
			Authenticator dst = i.next();
			if (dst.accepts(usr)) {
				setConnection(dst);
				try {
					dst.add(usr, pwd);
				} catch (SecurityException se) {
					throw se;
				} finally {
					clearConnection(dst);
				}
			}
		}
	}

	/**
	 * Renames the user in all authenticators. If this operation fails, no guarantee of transaction atomicity is given.
	 * @param usr the user bean
	 * @param newName the new directory name
	 * @see org.deltava.security.Authenticator#rename(Person, java.lang.String)
	 */
	public void rename(Person usr, String newName) throws SecurityException {
		setConnection(_src);
		_src.rename(usr, newName);
		clearConnection(_src);
		for (Iterator<Authenticator> i = _dst.iterator(); i.hasNext();) {
			Authenticator dst = i.next();
			if (dst.accepts(usr)) {
				setConnection(dst);
				dst.rename(usr, newName);
				clearConnection(dst);
			}
		}
	}
	
	/**
	 * Disables a Userus account in all authenticators. If this operation fails, no guarantee of transaction
	 * atomicity is given.
	 * @param usr the user bean
	 * @see Authenticator#disable(Person)
	 */
	public void disable(Person usr) throws SecurityException {
		if (_src.contains(usr)) {
			setConnection(_src);
			try {
				_src.disable(usr);
			} catch (SecurityException se) {
				throw se;
			} finally {
				clearConnection(_src);
			}
		}
		
		// Remove from destinations
		for (Iterator<Authenticator> i = _dst.iterator(); i.hasNext();) {
			Authenticator dst = i.next();
			setConnection(dst);
			if (dst.contains(usr)) {
				try {
					dst.disable(usr);
				} catch (SecurityException se) {
					log.error(se.getMessage(), se);
				}
			}

			clearConnection(dst);
		}
	}

	/**
	 * Removes the user from all authenticators. If this operation fails, no guarantee of transaction atomicity is
	 * given.
	 * @param usr the user bean
	 * @see Authenticator#remove(Person)
	 */
	public void remove(Person usr) throws SecurityException {
		if (_src.contains(usr)) {
			setConnection(_src);
			try {
				_src.remove(usr);
			} catch (SecurityException se) {
				throw se;
			} finally {
				clearConnection(_src);
			}
		}

		// Remove from destinations
		for (Iterator<Authenticator> i = _dst.iterator(); i.hasNext();) {
			Authenticator dst = i.next();
			setConnection(dst);
			if (dst.contains(usr)) {
				try {
					dst.remove(usr);
				} catch (SecurityException se) {
					log.error(se.getMessage(), se);
				}
			}

			clearConnection(dst);
		}
	}
}