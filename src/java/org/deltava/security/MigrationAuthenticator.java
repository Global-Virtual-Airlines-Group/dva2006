// Copyright (c) 2005, 2006 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.security;

import org.deltava.beans.Person;

/**
 * An Authenticator used to migrate user data from one Directory to another. When a user is sucessfully authenticated by
 * the first (&quot;source&quot;) authenticator, the directory name and password are written into the second
 * (&quot;destination&quot;) authenticator and deleted from the source.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class MigrationAuthenticator extends MultiAuthenticator {

	/**
	 * Initializes the Authenticator.
	 */
	public MigrationAuthenticator() {
		super(MigrationAuthenticator.class);
	}

	/**
	 * Authenticates the user. This authenticator will check the destination Directory to see if it contains an entry
	 * for the user. If it does, then authentication proceeds normally. If not, then the credentials are authenticated
	 * against the source Directory. If authentication succeeds, they are used to add credentials to the destnation
	 * Directory.
	 * @param usr the User bean
	 * @param pwd the user's password
	 * @throws SecurityException if authentication fails for any reason, contained within the cause of the exception.
	 * @see Authenticator#authenticate(Person, String)
	 */
	public void authenticate(Person usr, String pwd) throws SecurityException {

		// Figure out which authenticator to use
		Authenticator auth = _dst.contains(usr.getDN()) ? _dst : _src;
		auth.authenticate(usr, pwd);

		// If we got this far, and we're not in the destination directory, then add us
		if (auth == _src)
			_dst.addUser(usr, pwd);
	}

	/**
	 * Checks if a particular name exists in either Directory.
	 * @param directoryName the fully-qualified directory name
	 * @return TRUE if the user exists in either Authenticator, otherwise FALSE
	 * @throws SecurityException if an error occurs
	 */
	public boolean contains(String directoryName) throws SecurityException {
		return _src.contains(directoryName) || _dst.contains(directoryName);
	}

	/**
	 * Updates a user's password. This user must be present in the destination directory.
	 * @param usr the User bean
	 * @param pwd the new password
	 * @throws SecurityException if an error occurs, or the user is not in the Destination directory
	 */
	public void updatePassword(Person usr, String pwd) throws SecurityException {
		if (_dst.contains(usr.getDN())) {
			_dst.updatePassword(usr, pwd);
		} else {
			_dst.addUser(usr, pwd);
		}
	}
	
	/**
	 * Adds a User to the Destination Directory.
	 * @param usr the User bean
	 * @param pwd the User's password
	 * @throws SecurityException if an error occurs
	 */
	public void addUser(Person usr, String pwd) throws SecurityException {
		_dst.addUser(usr, pwd);
	}

	/**
	 * Removes a User from the Directory. This will remove from the first directory containing the user, first the
	 * Destination, then the Source.
	 * @param directoryName the User's fully-qualified Directory name
	 * @throws SecurityException if an error occurs or the user does not exist
	 */
	public void removeUser(String directoryName) throws SecurityException {
		if (_dst.contains(directoryName)) {
			_dst.removeUser(directoryName);
		} else if (_src.contains(directoryName)) {
			_src.removeUser(directoryName);
		} else {
			throw new SecurityException("Unknown User - " + directoryName);
		}
	}
	
   /**
    * Renames a user in the <i>Destination</i> Directory.
    * @param oldName the old fully-qualified directory name
    * @param newName the new fully-qualified directory 
    * @throws SecurityException if an error occurs
    */
	public void rename(String oldName, String newName) throws SecurityException {
		_dst.rename(oldName, newName);
	}
}